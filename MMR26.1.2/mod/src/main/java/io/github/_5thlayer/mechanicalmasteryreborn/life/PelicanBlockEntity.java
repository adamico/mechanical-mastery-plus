// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * A placed Pelican (#366). It eats each meal automation or a hand gives it as it comes in, into its batch, holding none; once the
 * batch is {@link Batch#ready ready} it digests for {@link Pelican#digestingTicks} ticks, eating nothing, then puts one Vital
 * Essence in its output, which holds one, stamped with its team's attempt as it is made ({@code Making}), and begins a
 * new batch. It eats nothing while its team hasn't unlocked life's chain ({@link ChainMachines#lockedAt}), while it
 * digests, or while its output holds an essence. Its batch belongs to the attempt it was begun in: a stop, which locks
 * the chain, or a reset, which moves the attempt on while life is played, loses it, digesting or not. A win keeps it.
 */
public final class PelicanBlockEntity extends BlockEntity {

    private static final Codec<Batch> BATCH_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("saturation").forGetter(Batch::saturation),
            Codec.STRING.listOf().<Set<String>>xmap(Set::copyOf, List::copyOf).fieldOf("kinds").forGetter(Batch::kinds),
            Codec.INT.fieldOf("levels").forGetter(Batch::levels),
            Codec.INT.fieldOf("meals").forGetter(Batch::meals))
            .apply(instance, Batch::new));

    private static final String BATCH_KEY = "batch";
    private static final String ATTEMPT_KEY = "attempt";
    private static final String DIGEST_KEY = "digest";
    private static final String OUTPUT_KEY = "output";

    /**
     * Its batch, the team's attempt number it was begun in, and the ticks left to digest it once ready. Kept whole, so a
     * transaction's snapshot ({@link PelicanFace}) is the record itself.
     */
    record State(Batch batch, int attempt, int digest) {
        static final State EMPTY = new State(Batch.EMPTY, 0, 0);

        /** This state with {@code digest} ticks left to digest. */
        State withDigest(int digest) {
            return new State(batch, attempt, digest);
        }
    }

    private final PelicanFace face = new PelicanFace(this);
    private final Output output = new Output();
    private State state = State.EMPTY;
    /** The team whose island it stands on, found on first need: a Pelican is placed anew when moved. */
    private @Nullable UUID team;

    public PelicanBlockEntity(BlockPos pos, BlockState state) {
        super(Pelican.BLOCK_ENTITY.get(), pos, state);
    }

    /** What automation reaches on every side: a mouth that eats meals, and its output, which gives the Vital Essence. */
    public ResourceHandler<ItemResource> face() {
        return face;
    }

    /** The meals it has eaten since its last Vital Essence; none once a stop or a reset has lost them. */
    public Batch batch() {
        return current().batch();
    }

    /** Whether it digests a ready batch, or holds one digested until its output has room for the Vital Essence. */
    public boolean digesting() {
        return batch().ready(Pelican.thresholds());
    }

    /** The ticks left to digest its ready batch; 0 while it eats, or holds a digested batch for want of room. */
    public int digestingLeft() {
        return current().digest();
    }

    /** Takes out the Vital Essence its output holds, as a hand does; empty if it holds none. */
    ItemStack takeOutput() {
        ItemResource held = output.getResource(0);
        if (held.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var transaction = Transaction.openRoot()) {
            int taken = output.extract(0, held, output.getAmountAsInt(0), transaction);
            transaction.commit();
            return held.toStack(taken);
        }
    }

    /** Its output, which holds one Vital Essence at most. */
    ResourceHandler<ItemResource> output() {
        return output;
    }

    /** Its state as it is held, lost or not, for {@link PelicanFace}'s snapshot. */
    State state() {
        return state;
    }

    /** Puts back a snapshot of its state, as {@link PelicanFace} reverts an aborted transaction. */
    void restore(State state) {
        this.state = state;
    }

    /** How many of {@code amount} {@code meal} it would eat now in a row: as many as keep its batch short of ready. */
    int edible(ItemResource meal, int amount) {
        if (!hungry()) {
            return 0;
        }
        Batch batch = batch();
        Batch.Thresholds thresholds = Pelican.thresholds();
        int eaten = 0;
        while (eaten < amount && !batch.ready(thresholds)) {
            batch = withMeal(batch, meal);
            eaten++;
        }
        return eaten;
    }

    /** Eats {@code count} {@code meal}, which {@link #edible} allowed; a first meal begins the batch in the current attempt. */
    void eat(ItemResource meal, int count) {
        State current = current();
        Batch batch = current.batch();
        int attempt = batch.meals() == 0 ? attempt() : current.attempt();
        for (int eaten = 0; eaten < count; eaten++) {
            batch = withMeal(batch, meal);
        }
        state = new State(batch, attempt, 0);
    }

    /** Once what a transaction fed it is eaten for good: a batch made ready starts digesting. */
    void startDigestingIfReady() {
        if (state.batch().ready(Pelican.thresholds()) && state.digest() == 0) {
            state = state.withDigest(Pelican.digestingTicks());
        }
        setChanged();
    }

    static void serverTick(Level level, BlockPos pos, BlockState blockState, PelicanBlockEntity pelican) {
        pelican.tick();
    }

    private void tick() {
        State current = current();
        if (current != state) {
            state = current;
            setChanged();
        }
        if (!state.batch().ready(Pelican.thresholds())) {
            light();
            return;
        }
        if (state.digest() > 0) {
            state = state.withDigest(state.digest() - 1);
            setChanged();
        }
        if (state.digest() == 0 && output.getResource(0).isEmpty()) {
            // Made while this block entity ticks, so Making stamps it with the team's attempt.
            output.set(0, ItemResource.of(new ItemStack(Essences.item(Essence.VITAL))), 1);
            state = State.EMPTY;
            setChanged();
        }
        light();
    }

    /** Lights its contents while it counts its digesting down, and dims them otherwise. */
    private void light() {
        boolean lit = state.digest() > 0;
        BlockState blockState = getBlockState();
        if (level != null && blockState.getValue(PelicanBlock.LIT) != lit) {
            level.setBlock(worldPosition, blockState.setValue(PelicanBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    /** Whether it would eat a meal now: life's chain unlocked, not digesting, and its output empty. */
    boolean hungry() {
        return level instanceof ServerLevel server && ChainMachines.lockedAt(server, worldPosition, getBlockState()).isEmpty()
                && !digesting() && output.getResource(0).isEmpty();
    }

    /** Its state, or an empty one if a stop or a reset has lost its batch since it was begun. */
    private State current() {
        if (state.batch().meals() == 0 || !(level instanceof ServerLevel server)) {
            return state;
        }
        TeamState teams = TeamState.get(server.getServer());
        UUID owner = team(server);
        boolean lost = !teams.unlocked(owner, Handicap.LIFE)
                || teams.plays(owner, Handicap.LIFE) && state.attempt() != teams.attempt(owner);
        return lost ? State.EMPTY : state;
    }

    private int attempt() {
        return level instanceof ServerLevel server ? TeamState.get(server.getServer()).attempt(team(server)) : 0;
    }

    /** {@code batch} with one {@code meal} more, by its item, its own saturation and its quality. */
    private static Batch withMeal(Batch batch, ItemResource meal) {
        FoodProperties food = meal.getComponents().get(DataComponents.FOOD);
        return batch.eat(BuiltInRegistries.ITEM.getKey(meal.getItem()).toString(), food == null ? 0 : food.saturation(),
                FoodQuality.level(meal.toStack(1)));
    }

    private UUID team(ServerLevel server) {
        if (team == null) {
            team = Teams.at(server, worldPosition);
        }
        return team;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store(BATCH_KEY, BATCH_CODEC, state.batch());
        out.putInt(ATTEMPT_KEY, state.attempt());
        out.putInt(DIGEST_KEY, state.digest());
        output.serialize(out.child(OUTPUT_KEY));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        state = new State(in.read(BATCH_KEY, BATCH_CODEC).orElse(Batch.EMPTY), in.getIntOr(ATTEMPT_KEY, 0), in.getIntOr(DIGEST_KEY, 0));
        output.deserialize(in.childOrEmpty(OUTPUT_KEY));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        super.preRemoveSideEffects(pos, blockState);
        if (level != null) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), output.getResource(0).toStack(output.getAmountAsInt(0)));
        }
    }

    /** Its output: one Vital Essence at most, which only it puts in and anything may take. */
    private final class Output extends ItemStacksResourceHandler {

        Output() {
            super(1);
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return 1;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }
}
