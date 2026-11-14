// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A Homunculus stand and the Homunculus built at it (#367). Its food takes any food but rotten flesh from automation on
 * any side and gives none back; its store takes FE only while a seated Homunculus sleeps, up to one jumpstart's worth
 * ({@code life.homunculus.jumpstart}), and a full store wakes it, spent. A new one is built full, {@code life.homunculus.full} hunger points of work. Awake, it eats one food when the last is used up,
 * which keeps it working {@code life.homunculus.ticks_per_hunger} ticks for each point of the food's hunger, and works
 * every {@link Homunculus#interval} ticks for that food's quality. With nothing left to eat once it has eaten since it woke, it falls asleep; one jumpstarted again waits awake for its first food. While its
 * team hasn't unlocked life's chain, as after a stop, it stands still awake, eating nothing, and works on once life is
 * started again; a reset, which keeps the chain unlocked, changes nothing.
 */
public final class HomunculusStandBlockEntity extends BlockEntity {

    private static final String SEATED_KEY = "seated";
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
    private boolean seated;
    private boolean awake;
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

    /** The stand's food, as automation reaches it: insert only. */
    public ResourceHandler<ItemResource> food() {
        return food;
    }

    /** The stand's store, as a cable reaches it: insert only, and only to jumpstart a sleeping Homunculus. */
    public EnergyHandler store() {
        return store;
    }

    /** Whether a Homunculus has been built at this stand. */
    public boolean seated() {
        return seated;
    }

    /** Whether its Homunculus is awake: jumpstarted, and not starved since. */
    public boolean awake() {
        return awake;
    }

    /** The works its Homunculus has done. */
    public int works() {
        return works;
    }

    /** Builds a Homunculus at this empty stand, full ({@code life.homunculus.full}): it sleeps until jumpstarted. */
    public void seat() {
        seated = true;
        awake = false;
        // Built full: its belly counts as a meal of no quality, so it starves once that and the stand's food are gone.
        eaten = true;
        fed = ServerConfig.homunculusFull() * ServerConfig.homunculusTicksPerHunger();
        quality = 0;
        cooldown = 0;
        setChanged();
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, HomunculusStandBlockEntity stand) {
        stand.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        store.fit();
        if (!seated) {
            return;
        }
        if (!awake) {
            if (store.getAmountAsInt() < store.getCapacityAsInt()) {
                return;
            }
            store.set(0);
            awake = true;
            // A new one's full belly is a meal; one jumpstarted after starving has none yet.
            eaten = fed > 0;
            setChanged();
        }
        if (ChainMachines.lockedAt(level, pos, state).isPresent()) {
            return;
        }
        if (fed == 0 && !eat()) {
            if (!eaten) {
                return;
            }
            awake = false;
            cooldown = 0;
            setChanged();
            return;
        }
        fed--;
        cooldown++;
        if (cooldown >= Homunculus.interval(quality)) {
            cooldown = 0;
            works++;
        }
        setChanged();
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
        out.putBoolean(SEATED_KEY, seated);
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
        seated = in.getBooleanOr(SEATED_KEY, false);
        awake = in.getBooleanOr(AWAKE_KEY, false);
        fed = in.getIntOr(FED_KEY, 0);
        quality = in.getIntOr(QUALITY_KEY, 0);
        cooldown = in.getIntOr(COOLDOWN_KEY, 0);
        works = in.getIntOr(WORKS_KEY, 0);
        eaten = in.getBooleanOr(EATEN_KEY, false);
        food.deserialize(in.childOrEmpty(FOOD_KEY));
        store.deserialize(in.childOrEmpty(STORE_KEY));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        super.preRemoveSideEffects(pos, blockState);
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < food.size(); slot++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), food.getResource(slot).toStack(food.getAmountAsInt(slot)));
        }
        if (seated) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Homunculus.ITEM.get()));
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

    /** The store: one jumpstart's worth, filled only while a seated Homunculus sleeps, and nothing out. */
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
            return seated && !awake ? super.insert(amount, transaction) : 0;
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    }
}
