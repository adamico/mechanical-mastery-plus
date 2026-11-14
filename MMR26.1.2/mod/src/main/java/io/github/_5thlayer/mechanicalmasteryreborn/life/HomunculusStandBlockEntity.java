// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * A Homunculus Stand's base, and the belly of the Homunculus bound to it (#367). Its food takes any food but rotten flesh
 * from automation on any side and gives none back. Its store takes FE only while its Homunculus sleeps, up to one
 * jumpstart's worth, and a full store wakes it. A Homunculus kindled from a new Vital Spark is full; one from a spent spark
 * has an empty belly. Awake, it works off what it last ate, then eats the next food, working faster the better that food's
 * quality; it eats, and falls asleep, only standing by the stand, so it walks back for both. With nothing left once it has
 * eaten since it woke, it falls asleep; one jumpstarted again waits awake for its first food. While its team hasn't
 * unlocked life's chain, as after a stop, it stands still, eating nothing, and works on once life is started again; a
 * reset, which keeps the chain unlocked, changes nothing. While its Homunculus isn't loaded, the stand waits.
 */
public final class HomunculusStandBlockEntity extends BlockEntity {

    /** What the Homunculus bound to a stand is doing, as its body shows it. */
    public enum Doing {
        /** Standing a statue until a jumpstart. */
        ASLEEP,
        /** Awake, but standing a statue while its team's life chain is locked. */
        LOCKED,
        /** Awake and about. */
        AWAKE
    }

    private static final String HOMUNCULUS_KEY = "homunculus";
    private static final String AWAKE_KEY = "awake";
    private static final String FED_KEY = "fed";
    private static final String QUALITY_KEY = "quality";
    private static final String COOLDOWN_KEY = "cooldown";
    private static final String WORKS_KEY = "works";
    private static final String EATEN_KEY = "eaten";
    private static final String FOOD_KEY = "food";
    private static final String STORE_KEY = "store";

    private final Food food = new Food();
    private final Store store = new Store();
    /** The Homunculus bound to it, if one is. */
    private @Nullable UUID homunculus;
    private boolean awake;
    /** Whether its team's life chain was locked at its last tick. */
    private boolean locked;
    /** Whether it has eaten since it last woke: one woken with nothing yet to eat waits for its first food. */
    private boolean eaten;
    /** Ticks of work the food it last ate has left. */
    private int fed;
    /** The Quality Food level of the food it last ate. */
    private int quality;
    /** Ticks since its last work. */
    private int cooldown;
    /** The works it has done. */
    private int works;

    public HomunculusStandBlockEntity(BlockPos pos, BlockState state) {
        super(Homunculus.STAND_ENTITY.get(), pos, state);
    }

    /** The stand's food, as automation reaches it: insert only. A player's hand fills it too ({@link #feed}). */
    public ResourceHandler<ItemResource> food() {
        return food;
    }

    /**
     * The stand's store, as a cable reaches it: there only while its Homunculus sleeps, insert only, to jumpstart it.
     * Absent otherwise, so nothing offers FE to, or shows the store of, a stand that would take none. A client, which
     * doesn't know whether it sleeps, always has it, so Jade asks the server for it and shows what the server says.
     */
    public @Nullable EnergyHandler store() {
        return sleeping() || level != null && level.isClientSide() ? store : null;
    }

    /** Whether the stand's food takes {@code held} at all: a food, but not rotten flesh. */
    public boolean takes(ItemStack held) {
        return !held.isEmpty() && food.isValid(0, ItemResource.of(held));
    }

    /** Puts as much of {@code held} as the stand's food takes into it, from a player's hand, and says how many it took. */
    public int feed(ItemStack held) {
        if (held.isEmpty()) {
            return 0;
        }
        try (var transaction = Transaction.openRoot()) {
            int taken = food.insert(ItemResource.of(held), held.getCount(), transaction);
            transaction.commit();
            return taken;
        }
    }

    /** Whether a Homunculus bound to this stand sleeps, so its store takes FE. */
    private boolean sleeping() {
        return homunculus != null && !awake;
    }

    /** How many foods the stand holds. */
    public int foods() {
        int foods = 0;
        for (int slot = 0; slot < food.size(); slot++) {
            foods += food.getAmountAsInt(slot);
        }
        return foods;
    }

    /** Whether a Homunculus is bound to this stand. */
    public boolean seated() {
        return homunculus != null;
    }

    /** Whether the Homunculus {@code id} is the one bound to this stand. */
    public boolean isBoundTo(UUID id) {
        return id.equals(homunculus);
    }

    /** What its Homunculus is doing. */
    public Doing doing() {
        return !awake ? Doing.ASLEEP : locked ? Doing.LOCKED : Doing.AWAKE;
    }

    /** Whether its Homunculus is awake: jumpstarted, and not starved since. */
    public boolean awake() {
        return awake;
    }

    /** Whether its Homunculus, awake, has nothing left in its belly, so it comes to the stand to eat. */
    public boolean hungry() {
        return awake && fed == 0;
    }

    /** The ticks of work left in what it last ate. */
    public int workLeft() {
        return fed;
    }

    /** The works its Homunculus has done. */
    public int works() {
        return works;
    }

    /** Binds the Homunculus {@code id}, newly kindled, to this stand: full unless from a {@code spent} spark, and asleep. */
    void bind(UUID id, boolean spent) {
        homunculus = id;
        awake = false;
        locked = false;
        // A full belly counts as a meal of no quality, so it starves once that and the stand's food are gone.
        eaten = !spent;
        fed = spent ? 0 : ServerConfig.homunculusFull() * ServerConfig.homunculusTicksPerHunger();
        quality = 0;
        cooldown = 0;
        store.set(0);
        changed();
    }

    /** The Homunculus {@code id}, gone for good, leaves the stand free for a new one. */
    void release(UUID id) {
        if (isBoundTo(id)) {
            homunculus = null;
            awake = false;
            fed = 0;
            store.set(0);
            changed();
        }
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, HomunculusStandBlockEntity stand) {
        stand.tick((ServerLevel) level, pos, state);
    }

    private void tick(ServerLevel level, BlockPos pos, BlockState state) {
        store.fit();
        if (homunculus == null || !(level.getEntity(homunculus) instanceof HomunculusEntity body)) {
            return;
        }
        if (!awake) {
            if (store.getAmountAsInt() < store.getCapacityAsInt()) {
                return;
            }
            store.set(0);
            awake = true;
            // A full belly is a meal; one jumpstarted after starving, or kindled spent, has none yet.
            eaten = fed > 0;
            changed();
        }
        locked = ChainMachines.lockedAt(level, pos, state).isPresent();
        if (locked) {
            return;
        }
        if (fed == 0) {
            if (!body.atStand() || !eat()) {
                if (eaten && body.atStand()) {
                    awake = false;
                    cooldown = 0;
                    changed();
                }
                return;
            }
        }
        fed--;
        cooldown++;
        if (cooldown >= Homunculus.interval(quality)) {
            cooldown = 0;
            works++;
            body.worked();
        }
        setChanged();
    }

    /** Saved, and its FE face looked up again on both halves, as the store comes and goes with its Homunculus's sleep. */
    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
            level.invalidateCapabilities(worldPosition.above());
        }
    }

    /** Eats one food from the stand, if it holds any. */
    private boolean eat() {
        int slot = 0;
        while (slot < food.size() && food.getResource(slot).isEmpty()) {
            slot++;
        }
        if (slot == food.size()) {
            return false;
        }
        ItemResource meal = food.getResource(slot);
        int amount = food.getAmountAsInt(slot);
        FoodProperties properties = meal.getComponents().get(DataComponents.FOOD);
        fed = Math.max(1, (properties == null ? 0 : properties.nutrition()) * ServerConfig.homunculusTicksPerHunger());
        quality = FoodQuality.level(meal.toStack(1));
        food.set(slot, amount == 1 ? ItemResource.EMPTY : meal, amount - 1);
        eaten = true;
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.storeNullable(HOMUNCULUS_KEY, UUIDUtil.CODEC, homunculus);
        out.putBoolean(AWAKE_KEY, awake);
        out.putInt(FED_KEY, fed);
        out.putInt(QUALITY_KEY, quality);
        out.putInt(COOLDOWN_KEY, cooldown);
        out.putInt(WORKS_KEY, works);
        out.putBoolean(EATEN_KEY, eaten);
        food.serialize(out.child(FOOD_KEY));
        store.serialize(out.child(STORE_KEY));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        homunculus = in.read(HOMUNCULUS_KEY, UUIDUtil.CODEC).orElse(null);
        awake = in.getBooleanOr(AWAKE_KEY, false);
        fed = in.getIntOr(FED_KEY, 0);
        quality = in.getIntOr(QUALITY_KEY, 0);
        cooldown = in.getIntOr(COOLDOWN_KEY, 0);
        works = in.getIntOr(WORKS_KEY, 0);
        eaten = in.getBooleanOr(EATEN_KEY, false);
        food.deserialize(in.childOrEmpty(FOOD_KEY));
        store.deserialize(in.childOrEmpty(STORE_KEY));
    }

    /** Broken, the stand spills its food; its Homunculus finds it gone and stands a statue for good. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        super.preRemoveSideEffects(pos, blockState);
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < food.size(); slot++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), food.getResource(slot).toStack(food.getAmountAsInt(slot)));
        }
    }

    /** The stand's food: nine stacks of any food but rotten flesh, which only the Homunculus takes out, first slot first. */
    private final class Food extends ItemStacksResourceHandler {

        static final int SLOTS = 9;

        Food() {
            super(SLOTS);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return !resource.isEmpty() && !resource.is(Items.ROTTEN_FLESH) && resource.getComponents().has(DataComponents.FOOD);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }

    /** The store: one jumpstart's worth, filled only while its Homunculus sleeps, and nothing out. */
    private final class Store extends SimpleEnergyHandler {

        Store() {
            super(0, 0, 0);
            fit();
        }

        /** Sizes the store to the config's jumpstart, which may have changed since it was made. */
        void fit() {
            capacity = Math.max(1, ServerConfig.homunculusJumpstart());
            maxInsert = capacity;
            if (energy > capacity) {
                set(capacity);
            }
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return sleeping() ? super.insert(amount, transaction) : 0;
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    }
}
