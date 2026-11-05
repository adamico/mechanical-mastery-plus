// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.athanor;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Accounts;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Generators;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.NullShards;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.Spell;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * A placed Athanor (#263, ADR 0014). It takes Nullessence, and nothing else, by any automation or by hand, and makes
 * Quintessence of it, which automation takes out, while its team's last stand runs and at least one of its supply lines
 * counts. One works to a team: the first to tick claims it in the team's state, and another of the same team does nothing
 * until that one is broken.
 *
 * <p>Each line is fed only by what its handicap takes from the team, as it takes it, while the team bears that handicap's
 * {@link Spell spell}, and every {@link #INTERVAL} ticks it pays for the next: the team's generators' output on its island,
 * taken through their own faces ({@link Generators#output}), at {@link ServerConfig#athanorEnergyDemand its demand}; the
 * Essentia Vitae the hearts given at it became, which only {@link #giveHeart} adds to, so none comes piped in; the
 * Null Shards of the spell running, which its Assault's mobs dropped, and only while that Assault isn't paused; and money
 * drained from the team's Account, which holds only what it earned during the spell. A line not paid for counts for
 * nothing until it is again; what it holds once its spell ends is lost, so nothing saved up feeds a line. The more lines
 * count, the faster it works and the fewer Nullessence a Quintessence takes ({@link AthanorWork}).
 */
public final class AthanorBlockEntity extends BlockEntity implements MenuProvider {

    /** How often, in ticks, the supply lines are paid for, and so how long one paid for counts. */
    public static final int INTERVAL = 20;
    /** The most Essentia Vitae it holds, in mB. */
    public static final int ESSENTIA_CAPACITY = 4000;
    /** How far, in blocks each way, it counts Neo Vitae's self-sacrifice runes around it. */
    public static final int RUNE_REACH = 2;
    /** The health a heart given at it costs. */
    public static final float HEART = 2.0F;
    /** Neo Vitae's Rune of Self Sacrifice, and its reinforced one, which counts twice. */
    static final Identifier RUNE = Identifier.fromNamespaceAndPath("neovitae", "rune_sacrifice_self");
    static final Identifier REINFORCED_RUNE = Identifier.fromNamespaceAndPath("neovitae", "rune_2_sacrifice_self");
    /** Slots of Nullessence it holds: enough for the dearest Quintessence. */
    static final int NULLESSENCE_SLOTS = 8;

    private static final String NULLESSENCE_KEY = "nullessence";
    private static final String SHARDS_KEY = "null_shards";
    private static final String OUTPUT_KEY = "output";
    private static final String ESSENTIA_KEY = "essentia";
    private static final String SHARD_TICKS_KEY = "shard_ticks";
    private static final String PROGRESS_KEY = "progress";
    private static final String OWED_KEY = "owed";
    private static final int TICKS_PER_SECOND = 20;
    private static final int TICKS_PER_MINUTE = 1200;

    /** What an Athanor is doing, as its menu shows it. */
    public enum Phase {
        /** Its team's last stand hasn't started: it does no work. */
        LOCKED,
        /** Another Athanor of its team is the one that works. */
        SECOND,
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
    /** What automation reaches on every side: Quintessence to take, then Nullessence and Null Shards to put in. */
    private final ResourceHandler<ItemResource> face = new CombinedResourceHandler<>(output, nullessence, shards);
    /** The Essentia Vitae the hearts given at it became, in mB. */
    private int essentia;
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
    /** The team whose island it stands on, found on its first tick: an Athanor never moves. */
    private @Nullable UUID team;

    public AthanorBlockEntity(BlockPos pos, BlockState state) {
        super(Athanor.BLOCK_ENTITY.get(), pos, state);
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

    /** The Essentia Vitae it holds, in mB. */
    public int essentia() {
        return essentia;
    }

    /** How far it is toward the next Quintessence, from 0 to 1. */
    public double progress() {
        return progress;
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, AthanorBlockEntity athanor) {
        athanor.tick((ServerLevel) level);
    }

    private void tick(ServerLevel level) {
        MinecraftServer server = level.getServer();
        UUID owner = owner(level);
        TeamState teams = TeamState.get(server);
        if (!claim(level, owner, teams)) {
            phase = Phase.SECOND;
            lines.clear();
            return;
        }
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
        // A line paid for counts only while its spell runs: one that just ended stops at once.
        lines.removeIf(handicap -> !teams.underSpell(owner, handicap));
        phase = lines.isEmpty() ? Phase.IDLE : Phase.WORKING;
        work();
    }

    /**
     * Makes this its team's working Athanor, unless another is and still stands, and says whether this is the one. One
     * that no longer stands where the team's state has it, in a loaded chunk, is forgotten.
     */
    private boolean claim(ServerLevel level, UUID owner, TeamState teams) {
        GlobalPos here = GlobalPos.of(level.dimension(), worldPosition);
        if (teams.claimAthanor(owner, here)) {
            return true;
        }
        Optional<GlobalPos> claimed = teams.athanor(owner);
        ServerLevel there = claimed.map(pos -> level.getServer().getLevel(pos.dimension())).orElse(null);
        if (claimed.isPresent() && there != null && there.isLoaded(claimed.get().pos())
                && !there.getBlockState(claimed.get().pos()).is(Athanor.BLOCK.get())) {
            teams.releaseAthanor(owner, claimed.get());
            return teams.claimAthanor(owner, here);
        }
        return false;
    }

    /** Pays for each line whose spell runs, for the next {@link #INTERVAL}, and says which lines count. */
    private EnumSet<Handicap> pay(ServerLevel level, UUID owner, TeamState teams) {
        MinecraftServer server = level.getServer();
        EnumSet<Handicap> counting = EnumSet.noneOf(Handicap.class);
        if (teams.underSpell(owner, Handicap.ENERGY) && takeOutput(level, owner, (long) ServerConfig.athanorEnergyDemand() * INTERVAL)) {
            counting.add(Handicap.ENERGY);
        }
        if (!teams.underSpell(owner, Handicap.LIFE)) {
            setEssentia(0);
        } else {
            int need = ServerConfig.athanorLifeDemand() * INTERVAL / TICKS_PER_SECOND;
            if (essentia >= need) {
                setEssentia(essentia - need);
                counting.add(Handicap.LIFE);
            }
        }
        Optional<Spell> protection = teams.spell(owner, Handicap.PROTECTION);
        shards.voidAllBut(protection.map(Spell::id));
        if (protection.isEmpty()) {
            setShardTicks(0);
        } else if (assaultRuns(server, owner)) {
            double perShard = (double) TICKS_PER_MINUTE / ServerConfig.athanorProtectionDemand();
            while (shardTicks < INTERVAL && shards.takeOne()) {
                setShardTicks(shardTicks + perShard);
            }
            if (shardTicks >= INTERVAL) {
                setShardTicks(shardTicks - INTERVAL);
                counting.add(Handicap.PROTECTION);
            }
        }
        if (teams.underSpell(owner, Handicap.MONEY)
                && Accounts.get().take(server, owner, (long) ServerConfig.athanorMoneyDemand() * INTERVAL / TICKS_PER_SECOND)) {
            counting.add(Handicap.MONEY);
        }
        return counting;
    }

    /**
     * Takes {@code need} FE from the team's generators within {@link ServerConfig#athanorEnergyReach reach}, through their
     * own faces, and says whether they gave that much; if not, it takes none.
     */
    private boolean takeOutput(ServerLevel level, UUID owner, long need) {
        int reach = ServerConfig.athanorEnergyReach();
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
        double share = AthanorWork.progressPerTick(counting, ServerConfig.athanorTicks());
        double owing = owed + share * AthanorWork.cost(counting, ServerConfig.athanorCosts());
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
     * Takes a heart of {@code player}'s, as Neo Vitae's self-sacrifice does, into Essentia Vitae, more for each
     * self-sacrifice rune around, and says why not if it doesn't: only from its team's player, while this is its team's
     * working Athanor, its last stand runs and it bears the life spell, and never a player's last heart.
     */
    public Optional<Component> giveHeart(Player player) {
        if (!(level instanceof ServerLevel server)) {
            return Optional.of(Component.empty());
        }
        UUID owner = owner(server);
        TeamState teams = TeamState.get(server.getServer());
        if (!Teams.of(player).equals(owner)) {
            return Optional.of(Component.translatable("mechanicalmasteryreborn.athanor.not_member"));
        }
        if (phase == Phase.SECOND || phase == Phase.LOCKED || phase == Phase.DONE || !teams.underSpell(owner, Handicap.LIFE)) {
            return Optional.of(Component.translatable("mechanicalmasteryreborn.athanor.heart.no_spell"));
        }
        if (player.getHealth() <= HEART) {
            return Optional.of(Component.translatable("mechanicalmasteryreborn.athanor.heart.last"));
        }
        if (essentia >= ESSENTIA_CAPACITY) {
            return Optional.of(Component.translatable("mechanicalmasteryreborn.athanor.heart.full"));
        }
        player.setHealth(player.getHealth() - HEART);
        int yield = (int) Math.round(ServerConfig.athanorLifePerHeart() * (1 + ServerConfig.athanorLifeRuneBonus() * runes()));
        setEssentia(Math.min(ESSENTIA_CAPACITY, essentia + yield));
        server.playSound(null, worldPosition, SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.8F);
        return Optional.empty();
    }

    /** The self-sacrifice runes within {@link #RUNE_REACH}, a reinforced one counting twice. */
    int runes() {
        if (level == null) {
            return 0;
        }
        int runes = 0;
        for (BlockPos at : BlockPos.betweenClosed(worldPosition.offset(-RUNE_REACH, -RUNE_REACH, -RUNE_REACH),
                worldPosition.offset(RUNE_REACH, RUNE_REACH, RUNE_REACH))) {
            Identifier block = BuiltInRegistries.BLOCK.getKey(level.getBlockState(at).getBlock());
            runes += block.equals(RUNE) ? 1 : block.equals(REINFORCED_RUNE) ? 2 : 0;
        }
        return runes;
    }

    /**
     * Puts as much of {@code stack} in as it takes, Nullessence or the running spell's Null Shards, as a player's click
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
            transaction.commit();
        }
        stack.shrink(taken);
        return taken;
    }

    /** The id of the protection spell its team bears, whose Null Shards it takes; none on a client. */
    Optional<UUID> shardSpell() {
        if (!(level instanceof ServerLevel server)) {
            return Optional.empty();
        }
        return TeamState.get(server.getServer()).spell(owner(server), Handicap.PROTECTION).map(Spell::id);
    }

    /** Forgets that this is its team's working Athanor, as it is broken. */
    void release() {
        if (level instanceof ServerLevel server) {
            TeamState.get(server.getServer()).releaseAthanor(owner(server), GlobalPos.of(server.dimension(), worldPosition));
        }
    }

    /** The team whose island it stands on. */
    UUID owner(ServerLevel server) {
        if (team == null) {
            team = Teams.at(server, worldPosition);
        }
        return team;
    }

    private void setEssentia(int amount) {
        if (essentia != amount) {
            essentia = amount;
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
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        release();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        nullessence.serialize(out.child(NULLESSENCE_KEY));
        shards.serialize(out.child(SHARDS_KEY));
        output.serialize(out.child(OUTPUT_KEY));
        out.putInt(ESSENTIA_KEY, essentia);
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
        essentia = input.getIntOr(ESSENTIA_KEY, 0);
        shardTicks = input.getDoubleOr(SHARD_TICKS_KEY, 0);
        progress = input.getDoubleOr(PROGRESS_KEY, 0);
        owed = input.getDoubleOr(OWED_KEY, 0);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AthanorMenu(containerId, inventory, this);
    }

    /** Its Nullessence: put in by anything, taken out by nothing but its work. */
    private static final class Input extends ItemStacksResourceHandler {

        private final AthanorBlockEntity athanor;

        Input(AthanorBlockEntity athanor) {
            super(NULLESSENCE_SLOTS);
            this.athanor = athanor;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(Essences.item(Essence.NULLESSENCE));
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
            athanor.setChanged();
        }
    }

    /** Its Null Shards: put in by anything, only those of the protection spell running, and used up by its work. */
    private static final class Shards extends ItemStacksResourceHandler {

        private final AthanorBlockEntity athanor;

        Shards(AthanorBlockEntity athanor) {
            super(1);
            this.athanor = athanor;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (!resource.is(NullShards.ITEM.get())) {
                return false;
            }
            UUID stamped = resource.get(NullShards.SPELL.get());
            return stamped != null && athanor.shardSpell().filter(stamped::equals).isPresent();
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
            athanor.setChanged();
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
}
