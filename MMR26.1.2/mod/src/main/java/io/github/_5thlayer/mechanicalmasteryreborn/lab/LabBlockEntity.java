// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Accounts;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Generators;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.NullShards;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.life.Pelican;
import io.github._5thlayer.mechanicalmasteryreborn.record.Spell;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * A placed lab's work once its team's last stand starts, when it awakens as the Hermetic Lab (#263, ADR 0014) and stands
 * changed ({@link LabBlock#HERMETIC}). It takes Nullessence, and nothing else, by any automation or by hand, and makes
 * Quintessence of it, which automation takes out, while its team's last stand runs and at least one of its supply lines
 * counts. Before the last stand it does nothing.
 *
 * <p>Each line is fed only by what its handicap takes from the team, as it takes it, while the team bears that handicap's
 * {@link Spell spell}, and every {@link #INTERVAL} ticks it pays for the next: the team's generators' output on its island,
 * taken through their own faces ({@link Generators#output}), at {@link ServerConfig#hermeticLabEnergyDemand its demand}; the
 * quality meals it eats (#370), each of a kind not among the last it took and of the Pelican's quality, each keeping the
 * line fed for a time set by its saturation from when it is eaten, with nothing saved up past it; the Null Shards of the spell running, which its Assault's mobs dropped, and only while that Assault isn't paused; and money
 * drained from the team's Account, which holds only what it earned during the spell. A line not paid for counts for
 * nothing until it is again; what it holds once its spell ends is lost, so nothing saved up feeds a line. The more lines
 * count, the faster it works and the fewer Nullessence a Quintessence takes ({@link HermeticLabWork}).
 */
public final class LabBlockEntity extends BlockEntity implements MenuProvider {

    /** How often, in ticks, the supply lines are paid for, and so how long one paid for counts. */
    public static final int INTERVAL = 20;
    private static final Component TITLE = Component.translatable("container.mechanicalmasteryreborn.hermetic_lab");
    /** Slots of Nullessence it holds: enough for the dearest Quintessence. */
    static final int NULLESSENCE_SLOTS = 8;

    private static final String NULLESSENCE_KEY = "nullessence";
    private static final String SHARDS_KEY = "null_shards";
    private static final String OUTPUT_KEY = "output";
    private static final String MEALS_KEY = "meals";
    private static final String FED_KEY = "fed";
    private static final String SHARD_TICKS_KEY = "shard_ticks";
    private static final String PROGRESS_KEY = "progress";
    private static final String OWED_KEY = "owed";
    private static final int TICKS_PER_SECOND = 20;
    private static final int TICKS_PER_MINUTE = 1200;

    /** What a Hermetic Lab is doing, as its menu shows it. */
    public enum Phase {
        /** Its team's last stand hasn't started: it does no work. */
        LOCKED,
        /** Its team's last stand runs, but no line counts. */
        IDLE,
        /** At least one line counts. */
        WORKING,
        /** Its team has won the pack: C.O.D. is shut down. */
        DONE
    }

    private final Input nullessence = new Input(this);
    private final Shards shards = new Shards(this);
    private final Output output = new Output(this::setChanged);
    private final Mouth mouth = new Mouth(this);
    /** What automation reaches on every side: Quintessence to take, then Nullessence, Null Shards and meals to put in. */
    private final ResourceHandler<ItemResource> face = new CombinedResourceHandler<>(output, nullessence, shards, mouth);
    /** The kinds of meal the life line took last, oldest first, and the ticks its last keeps the line fed for. */
    private Meals meals = Meals.NONE;
    /** The ticks the Null Shards already taken still feed the protection line for. */
    private double shardTicks;
    /** How far it is toward the next Quintessence, from 0 to 1. */
    private double progress;
    /**
     * The Nullessence the next Quintessence owes so far: each tick's share of it at the cost of the lines counting then,
     * so a Quintessence begun on fewer lines and finished on more pays for each part at its own cost.
     */
    private double owed;
    /** The lines that count until the next payment, while their spells run; never saved, so a load pays at once. */
    private final EnumSet<Handicap> lines = EnumSet.noneOf(Handicap.class);
    /** Ticks since the lines were last paid for; never saved, so a load pays at once. */
    private int sinceEvaluated = INTERVAL;
    /** What it is doing, as of its last tick; never saved. */
    private Phase phase = Phase.LOCKED;
    /** The team whose island it stands on, found on its first tick: a lab never moves. */
    private @Nullable UUID team;

    public LabBlockEntity(BlockPos pos, BlockState state) {
        super(Lab.BLOCK_ENTITY.get(), pos, state);
    }

    /** What automation reaches on every side. */
    public ResourceHandler<ItemResource> face() {
        return face;
    }

    ItemStacksResourceHandler outputSlot() {
        return output;
    }

    /** What it is doing, as of its last tick. */
    public Phase phase() {
        return phase;
    }

    /** Whether {@code handicap}'s line counts until the next payment. */
    public boolean counts(Handicap handicap) {
        return lines.contains(handicap);
    }

    /** How many lines count until the next payment. */
    public int linesCounting() {
        return lines.size();
    }

    /** The Nullessence it holds. */
    public int nullessence() {
        int count = 0;
        for (int slot = 0; slot < nullessence.size(); slot++) {
            count += (int) nullessence.getAmountAsLong(slot);
        }
        return count;
    }

    /** The Quintessence it holds, waiting to be taken. */
    public int quintessence() {
        return (int) output.getAmountAsLong(0);
    }

    /** The Null Shards it holds, waiting to feed the protection line. */
    public int nullShards() {
        return (int) shards.getAmountAsLong(0);
    }

    /** The ticks the life line's last meal still keeps it fed for. */
    public int fedTicks() {
        return meals.fed();
    }

    /** The kinds of meal the life line took last, oldest first, by item id. */
    public List<String> recentMeals() {
        return meals.kinds();
    }

    /** How far it is toward the next Quintessence, from 0 to 1. */
    public double progress() {
        return progress;
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, LabBlockEntity lab) {
        lab.tick((ServerLevel) level);
    }

    private void tick(ServerLevel level) {
        MinecraftServer server = level.getServer();
        UUID owner = owner(level);
        TeamState teams = TeamState.get(server);
        showHermetic(LastStands.started(server, owner));
        if (!teams.inLastStand(owner)) {
            phase = teams.wonThePack(owner) ? Phase.DONE : Phase.LOCKED;
            lines.clear();
            return;
        }
        if (++sinceEvaluated >= INTERVAL) {
            sinceEvaluated = 0;
            lines.clear();
            lines.addAll(pay(level, owner, teams));
        }
        if (teams.underSpell(owner, Handicap.LIFE)) {
            if (meals.fed() > 0) {
                setMeals(meals.withFed(meals.fed() - 1));
            }
        } else {
            setMeals(Meals.NONE);
        }
        // A line paid for counts only while its spell runs, and the life line only while a meal keeps it fed.
        lines.removeIf(handicap -> !teams.underSpell(owner, handicap) || handicap == Handicap.LIFE && meals.fed() == 0);
        phase = lines.isEmpty() ? Phase.IDLE : Phase.WORKING;
        work();
    }

    /** Stands as the Hermetic Lab once its team's last stand has started, and as the lab before, if that changed. */
    private void showHermetic(boolean hermetic) {
        BlockState state = getBlockState();
        if (level != null && state.hasProperty(LabBlock.HERMETIC) && state.getValue(LabBlock.HERMETIC) != hermetic) {
            level.setBlock(worldPosition, state.setValue(LabBlock.HERMETIC, hermetic), Block.UPDATE_ALL);
        }
    }

    /** Pays for each line whose spell runs, for the next {@link #INTERVAL}, and says which lines count. */
    private EnumSet<Handicap> pay(ServerLevel level, UUID owner, TeamState teams) {
        MinecraftServer server = level.getServer();
        EnumSet<Handicap> counting = EnumSet.noneOf(Handicap.class);
        if (teams.underSpell(owner, Handicap.ENERGY) && takeOutput(level, owner, (long) ServerConfig.hermeticLabEnergyDemand() * INTERVAL)) {
            counting.add(Handicap.ENERGY);
        }
        if (teams.underSpell(owner, Handicap.LIFE) && meals.fed() > 0) {
            counting.add(Handicap.LIFE);
        }
        Optional<Spell> protection = teams.spell(owner, Handicap.PROTECTION);
        shards.voidAllBut(protection.map(Spell::id));
        if (protection.isEmpty()) {
            setShardTicks(0);
        } else if (assaultRuns(server, owner)) {
            double perShard = (double) TICKS_PER_MINUTE / ServerConfig.hermeticLabProtectionDemand();
            while (shardTicks < INTERVAL && shards.takeOne()) {
                setShardTicks(shardTicks + perShard);
            }
            if (shardTicks >= INTERVAL) {
                setShardTicks(shardTicks - INTERVAL);
                counting.add(Handicap.PROTECTION);
            }
        }
        if (teams.underSpell(owner, Handicap.MONEY)
                && Accounts.get().take(server, owner, (long) ServerConfig.hermeticLabMoneyDemand() * INTERVAL / TICKS_PER_SECOND)) {
            counting.add(Handicap.MONEY);
        }
        return counting;
    }

    /**
     * Takes {@code need} FE from the team's generators within {@link ServerConfig#hermeticLabEnergyReach reach}, through their
     * own faces, and says whether they gave that much; if not, it takes none.
     */
    private boolean takeOutput(ServerLevel level, UUID owner, long need) {
        int reach = ServerConfig.hermeticLabEnergyReach();
        ChunkPos centre = ChunkPos.containing(worldPosition);
        try (Transaction transaction = Transaction.openRoot()) {
            long taken = 0;
            for (int x = centre.x() - reach; x <= centre.x() + reach; x++) {
                for (int z = centre.z() - reach; z <= centre.z() + reach; z++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                    if (chunk == null) {
                        continue;
                    }
                    for (BlockEntity entity : chunk.getBlockEntities().values()) {
                        BlockPos at = entity.getBlockPos();
                        if (!entity.getBlockState().is(Generators.TAG) || !Teams.at(level, at).equals(owner)) {
                            continue;
                        }
                        EnergyHandler generator = Generators.output(level, at);
                        if (generator != null) {
                            taken += generator.extract((int) Math.min(Integer.MAX_VALUE, need - taken), transaction);
                        }
                        if (taken >= need) {
                            transaction.commit();
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /** Whether the team's protection spell's Assault runs: sent, and not paused for no one being home. */
    private static boolean assaultRuns(MinecraftServer server, UUID owner) {
        AssaultState state = AssaultState.get(server);
        return state.assault(owner).isPresent() && state.paused(owner).isEmpty();
    }

    /**
     * Moves the next Quintessence on by a tick while a line counts, each tick owing its share of the Quintessence's cost
     * at the lines counting then, so long as it holds the Nullessence owed; once done, uses up what is owed and makes it.
     */
    private void work() {
        int counting = linesCounting();
        if (counting == 0) {
            return;
        }
        double share = HermeticLabWork.progressPerTick(counting, ServerConfig.hermeticLabTicks());
        double owing = owed + share * HermeticLabWork.cost(counting, ServerConfig.hermeticLabCosts());
        if (progress < 1.0) {
            if (nullessence() < Math.ceil(owing - 1e-9)) {
                return;
            }
            progress = Math.min(1.0, progress + share);
            owed = owing;
            setChanged();
        }
        if (progress < 1.0 || !output.hasRoom()) {
            return;
        }
        nullessence.use((int) Math.min(nullessence(), Math.round(owed)));
        output.makeOne();
        progress = 0;
        owed = 0;
        setChanged();
    }

    /**
     * Puts as much of {@code stack} in as it takes, Nullessence, the running spell's Null Shards or a meal, as a player's click
     * does, and says how many it took; the stack is left with the rest.
     */
    public int feed(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        int taken;
        try (var transaction = Transaction.openRoot()) {
            ItemResource resource = ItemResource.of(stack);
            taken = 0;
            for (int slot = 0; slot < nullessence.size() && taken < stack.getCount(); slot++) {
                taken += nullessence.insert(slot, resource, stack.getCount() - taken, transaction);
            }
            if (taken < stack.getCount()) {
                taken += shards.insert(0, resource, stack.getCount() - taken, transaction);
            }
            if (taken < stack.getCount()) {
                taken += mouth.insert(0, resource, stack.getCount() - taken, transaction);
            }
            transaction.commit();
        }
        stack.shrink(taken);
        return taken;
    }

    /** Whether its team's last stand runs; never on a client. */
    boolean lastStandRuns() {
        return level instanceof ServerLevel server && TeamState.get(server.getServer()).inLastStand(owner(server));
    }

    /** Whether its team's last stand runs and it bears the life spell, so the life line eats; never on a client. */
    boolean lifeSpellRuns() {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        TeamState teams = TeamState.get(server.getServer());
        UUID owner = owner(server);
        return teams.inLastStand(owner) && teams.underSpell(owner, Handicap.LIFE);
    }

    /** The id of the protection spell its team bears, whose Null Shards it takes; none on a client. */
    Optional<UUID> shardSpell() {
        if (!(level instanceof ServerLevel server)) {
            return Optional.empty();
        }
        return TeamState.get(server.getServer()).spell(owner(server), Handicap.PROTECTION).map(Spell::id);
    }

    /** The team whose island it stands on. */
    UUID owner(ServerLevel server) {
        if (team == null) {
            team = Teams.at(server, worldPosition);
        }
        return team;
    }

    private void setMeals(Meals next) {
        if (!meals.equals(next)) {
            meals = next;
            setChanged();
        }
    }

    private void setShardTicks(double ticks) {
        if (shardTicks != ticks) {
            shardTicks = ticks;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        nullessence.serialize(out.child(NULLESSENCE_KEY));
        shards.serialize(out.child(SHARDS_KEY));
        output.serialize(out.child(OUTPUT_KEY));
        out.store(MEALS_KEY, Codec.STRING.listOf(), meals.kinds());
        out.putInt(FED_KEY, meals.fed());
        out.putDouble(SHARD_TICKS_KEY, shardTicks);
        out.putDouble(PROGRESS_KEY, progress);
        out.putDouble(OWED_KEY, owed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        nullessence.deserialize(input.childOrEmpty(NULLESSENCE_KEY));
        shards.deserialize(input.childOrEmpty(SHARDS_KEY));
        output.deserialize(input.childOrEmpty(OUTPUT_KEY));
        meals = new Meals(List.copyOf(input.read(MEALS_KEY, Codec.STRING.listOf()).orElse(List.of())), input.getIntOr(FED_KEY, 0));
        shardTicks = input.getDoubleOr(SHARD_TICKS_KEY, 0);
        progress = input.getDoubleOr(PROGRESS_KEY, 0);
        owed = input.getDoubleOr(OWED_KEY, 0);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new HermeticLabMenu(containerId, inventory, this);
    }

    /** Its Nullessence: put in by anything, taken out by nothing but its work. */
    private static final class Input extends ItemStacksResourceHandler {

        private final LabBlockEntity lab;

        Input(LabBlockEntity lab) {
            super(NULLESSENCE_SLOTS);
            this.lab = lab;
        }

        /** Nullessence, and only while the lab's team's last stand runs, so none is put in where it would never be used. */
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(Essences.item(Essence.NULLESSENCE)) && lab.lastStandRuns();
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        /** Uses up {@code count} of what it holds, which must hold that many. */
        void use(int count) {
            int left = count;
            for (int slot = 0; slot < size() && left > 0; slot++) {
                ItemStack stack = stacks.get(slot);
                int used = Math.min(left, stack.getCount());
                left -= used;
                set(slot, used == stack.getCount() ? ItemResource.EMPTY : ItemResource.of(stack), stack.getCount() - used);
            }
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            lab.setChanged();
        }
    }

    /** Its Null Shards: put in by anything, only those of the protection spell running, and used up by its work. */
    private static final class Shards extends ItemStacksResourceHandler {

        private final LabBlockEntity lab;

        Shards(LabBlockEntity lab) {
            super(1);
            this.lab = lab;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (!resource.is(NullShards.ITEM.get())) {
                return false;
            }
            UUID stamped = resource.get(NullShards.SPELL.get());
            return stamped != null && lab.shardSpell().filter(stamped::equals).isPresent();
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        /** Uses up one shard, and says whether it held one. */
        boolean takeOne() {
            ItemStack stack = stacks.get(0);
            if (stack.isEmpty()) {
                return false;
            }
            set(0, stack.getCount() == 1 ? ItemResource.EMPTY : ItemResource.of(stack), stack.getCount() - 1);
            return true;
        }

        /** Voids what it holds unless it fell in {@code spell}, the protection spell running: an earlier spell's feed nothing. */
        void voidAllBut(Optional<UUID> spell) {
            ItemStack stack = stacks.get(0);
            if (!stack.isEmpty() && NullShards.spellOf(stack).filter(id -> spell.filter(id::equals).isPresent()).isEmpty()) {
                set(0, ItemResource.EMPTY, 0);
            }
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            lab.setChanged();
        }
    }

    /** Its Quintessence: taken out by anything, put in only by its work. */
    static final class Output extends ItemStacksResourceHandler {

        private final Runnable changed;

        Output(Runnable changed) {
            super(1);
            this.changed = changed;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(Essences.item(Essence.QUINTESSENCE));
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        boolean hasRoom() {
            ItemStack stack = stacks.get(0);
            return stack.isEmpty() || stack.getCount() < stack.getMaxStackSize();
        }

        void makeOne() {
            ItemStack stack = stacks.get(0);
            set(0, ItemResource.of(Essences.item(Essence.QUINTESSENCE)), stack.isEmpty() ? 1 : stack.getCount() + 1);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            changed.run();
        }
    }

    /**
     * The life line's meals: the kinds it took last, oldest first, at most as many as a Pelican's batch's kinds, and the
     * ticks its last meal keeps it fed for.
     */
    record Meals(List<String> kinds, int fed) {

        static final Meals NONE = new Meals(List.of(), 0);

        Meals withFed(int ticks) {
            return new Meals(kinds, ticks);
        }

        /** These meals with {@code kind} eaten last, keeping the line fed {@code ticks} from now, whatever was left. */
        Meals eat(String kind, int ticks) {
            List<String> next = new ArrayList<>(kinds);
            next.add(kind);
            int window = ServerConfig.pelicanKinds();
            while (next.size() > window) {
                next.removeFirst();
            }
            return new Meals(List.copyOf(next), ticks);
        }
    }

    /** Whether the life line would eat {@code meal} now: its spell runs, a meal of the Pelican's quality, of a kind not taken last. */
    private boolean edible(ItemResource meal) {
        return Pelican.meal(meal) && FoodQuality.level(meal.toStack(1)) >= ServerConfig.pelicanQuality()
                && !meals.kinds().contains(kind(meal)) && lifeSpellRuns();
    }

    private static String kind(ItemResource meal) {
        return BuiltInRegistries.ITEM.getKey(meal.getItem()).toString();
    }

    /** The ticks {@code meal} keeps the life line fed for, by its own saturation. */
    private static int fedFor(ItemResource meal) {
        FoodProperties food = meal.getComponents().get(DataComponents.FOOD);
        return food == null ? 0 : (int) Math.round(food.saturation() * ServerConfig.hermeticLabLifeTicksPerSaturation());
    }

    /**
     * The life line's mouth: takes one meal at a time, the one it would eat ({@link #edible}), and holds nothing, so a meal
     * refused stays where it was; journalled, so an aborted or simulated insert eats nothing.
     */
    private static final class Mouth extends SnapshotJournal<Meals> implements ResourceHandler<ItemResource> {

        private final LabBlockEntity lab;

        Mouth(LabBlockEntity lab) {
            this.lab = lab;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            Objects.checkIndex(index, size());
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            Objects.checkIndex(index, size());
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            if (resource.isEmpty()) {
                return lab.lifeSpellRuns() ? 1 : 0;
            }
            return lab.edible(resource) ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return Pelican.meal(resource);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(index, size());
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (amount == 0 || !lab.edible(resource)) {
                return 0;
            }
            updateSnapshots(transaction);
            lab.meals = lab.meals.eat(kind(resource), fedFor(resource));
            return 1;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected Meals createSnapshot() {
            return lab.meals;
        }

        @Override
        protected void revertToSnapshot(Meals snapshot) {
            lab.meals = snapshot;
        }

        @Override
        protected void onRootCommit(Meals original) {
            lab.setChanged();
        }
    }
}
