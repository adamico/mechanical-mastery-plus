// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.ArrayList;
import java.util.List;

import io.github._5thlayer.libworks.transfer.GuardedResourceHandler;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Decomposing;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.DecomposingRecipe;
import io.github._5thlayer.mechanicalmasteryreborn.record.DecomposeLog;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * A Recycler's core: its one input buffer, the turns its drives have delivered, and the work. Each turn
 * counts only while the Recycler can decompose what it holds; the {@link #TURNS}th decomposes one recipe's
 * worth into its mix, on the outputs, result <i>i</i> on the <i>i</i>th position ({@link RecyclerLayout}).
 * It needs no power and no ticking: drives turn it.
 *
 * <p>Each decompose is recorded, on a Recycler, for the team whose island it stands on ({@link DecomposeLog}).
 */
public final class RecyclerCoreBlockEntity extends BlockEntity {

    /** The first core's tier. */
    public static final int TIER = 1;
    /** Turns a decompose takes on a tier-1 core. */
    public static final int TURNS = 8;

    private static final String BUFFER_KEY = "buffer";
    private static final String TURNS_KEY = "turns";

    /** Whether the Recycler can decompose now, and why not. */
    public enum Status {
        /** Whole, with enough of a decomposable item and room for its mix. */
        READY,
        /** Its 3x3 lacks a drive, an input or an output, or holds something else. */
        INCOMPLETE,
        /** It holds nothing. */
        EMPTY,
        /** It holds what no recipe decomposes, since the recipes changed. */
        NO_RECIPE,
        /** It holds fewer than one decompose takes. */
        TOO_FEW,
        /** The mix has more results than its outputs have positions. */
        TOO_FEW_OUTPUTS,
        /** The whole mix doesn't fit on its outputs. */
        NO_ROOM
    }

    private final Buffer buffer = new Buffer();
    private final ResourceHandler<ItemResource> inputFace = new InputFace(buffer);
    private int turns;

    public RecyclerCoreBlockEntity(BlockPos pos, BlockState state) {
        super(Recycler.CORE_ENTITY.get(), pos, state);
    }

    /** The buffer as its inputs reach it: insert only, and only what a recipe decomposes. */
    public ResourceHandler<ItemResource> inputFace() {
        return inputFace;
    }

    /** What the buffer holds. */
    public ItemStack held() {
        return buffer.getResource(0).toStack(buffer.getAmountAsInt(0));
    }

    /** Turns delivered toward the decompose under way. */
    public int turns() {
        return turns;
    }

    public Status status() {
        return plan().status();
    }

    /** What a turn did. */
    public enum Turn {
        /** Nothing: the Recycler can't decompose now. */
        STOPPED,
        /** It counted toward the decompose under way. */
        COUNTED,
        /** It was the last, and decomposed. */
        DECOMPOSED
    }

    /** One turn from a drive. It counts only while the Recycler can decompose; the last one decomposes. */
    public Turn turn() {
        if (!(level instanceof ServerLevel server)) {
            return Turn.STOPPED;
        }
        Plan plan = plan();
        if (plan.status() != Status.READY) {
            return Turn.STOPPED;
        }
        turns++;
        setChanged();
        if (turns < TURNS) {
            return Turn.COUNTED;
        }
        turns = 0;
        decompose(server, plan);
        return Turn.DECOMPOSED;
    }

    private void decompose(ServerLevel server, Plan plan) {
        int count = plan.recipe().ingredient().count();
        ItemStack held = held();
        buffer.set(0, ItemResource.of(held), held.getCount() - count);
        List<ItemStack> mix = plan.recipe().mix();
        for (int i = 0; i < mix.size(); i++) {
            plan.outputs().get(i / RecyclerLayout.POSITIONS_PER_OUTPUT).put(i % RecyclerLayout.POSITIONS_PER_OUTPUT, mix.get(i));
        }
        Essences.of(held.getItem()).ifPresent(essence -> DecomposeLog.get(server.getServer())
                .record(Teams.at(server, worldPosition), essence, count, DecomposeLog.Way.RECYCLER));
    }

    /** What the Recycler would decompose now, onto which outputs, or why it can't. */
    private Plan plan() {
        if (!(level instanceof ServerLevel server)) {
            return Plan.stopped(Status.INCOMPLETE);
        }
        List<RecyclerOutputBlockEntity> outputs = outputs();
        if (outputs == null) {
            return Plan.stopped(Status.INCOMPLETE);
        }
        ItemStack held = held();
        if (held.isEmpty()) {
            return Plan.stopped(Status.EMPTY);
        }
        var found = Decomposing.recipeFor(server, held);
        if (found.isEmpty()) {
            return Plan.stopped(Status.NO_RECIPE);
        }
        DecomposingRecipe recipe = found.get().value();
        if (held.getCount() < recipe.ingredient().count()) {
            return Plan.stopped(Status.TOO_FEW);
        }
        List<ItemStack> mix = recipe.mix();
        if (mix.size() > outputs.size() * RecyclerLayout.POSITIONS_PER_OUTPUT) {
            return Plan.stopped(Status.TOO_FEW_OUTPUTS);
        }
        for (int i = 0; i < mix.size(); i++) {
            if (!outputs.get(i / RecyclerLayout.POSITIONS_PER_OUTPUT).fits(i % RecyclerLayout.POSITIONS_PER_OUTPUT, mix.get(i))) {
                return Plan.stopped(Status.NO_ROOM);
            }
        }
        return new Plan(Status.READY, recipe, outputs);
    }

    /**
     * The outputs in the order a mix fills them, or null when the 3x3 isn't a whole Recycler: every one of the
     * eight places a part of this core's alone, with at least one drive, input and output among them.
     */
    private @Nullable List<RecyclerOutputBlockEntity> outputs() {
        Direction front = getBlockState().getValue(RecyclerCoreBlock.FACING);
        var outputs = new ArrayList<RecyclerOutputBlockEntity>();
        boolean drive = false;
        boolean input = false;
        for (int[] place : RecyclerLayout.clockwiseFromFrontLeft(front.getStepX(), front.getStepZ())) {
            BlockPos pos = worldPosition.offset(place[0], 0, place[1]);
            if (!(level.getBlockState(pos).getBlock() instanceof RecyclerPart part)
                    || RecyclerPart.coreOf(level, pos).filter(core -> core == this).isEmpty()) {
                return null;
            }
            switch (part.role()) {
                case DRIVE -> drive = true;
                case INPUT -> input = true;
                case OUTPUT -> {
                    if (!(level.getBlockEntity(pos) instanceof RecyclerOutputBlockEntity output)) {
                        return null;
                    }
                    outputs.add(output);
                }
                case CASING -> {
                }
            }
        }
        return drive && input && !outputs.isEmpty() ? outputs : null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        buffer.serialize(output.child(BUFFER_KEY));
        output.putInt(TURNS_KEY, turns);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        buffer.deserialize(input.childOrEmpty(BUFFER_KEY));
        turns = input.getIntOr(TURNS_KEY, 0);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), held());
        }
    }

    private record Plan(Status status, @Nullable DecomposingRecipe recipe, List<RecyclerOutputBlockEntity> outputs) {

        static Plan stopped(Status status) {
            return new Plan(status, null, List.of());
        }
    }

    /** The one slot, which takes only what a recipe decomposes. */
    private final class Buffer extends ItemStacksResourceHandler {

        Buffer() {
            super(1);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return level instanceof ServerLevel server && Decomposing.recipeFor(server, resource.toStack()).isPresent();
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }

    /** The buffer, from which nothing is taken. */
    private static final class InputFace extends GuardedResourceHandler<ItemResource> {

        InputFace(ResourceHandler<ItemResource> buffer) {
            super(buffer);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
