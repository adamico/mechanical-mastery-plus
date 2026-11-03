// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.server.level.ServerLevel;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Rejection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * An input: what a player right-clicks into it, a hopper above drops in or anything against it pushes goes
 * into its Recycler's core, all into the core's one buffer (its item face is the core's, see {@link Recycler}).
 * Nothing comes back out of it. It looks open on top, but takes in from every side all the same.
 *
 * <p>A player's right-click with anything in hand is always the input's: it goes in, or the input refuses it and
 * says why on the action bar, and never falls through to placing a block against it. A sneaking right-click
 * places one, since vanilla then skips the block's use.
 */
public final class RecyclerInputBlock extends Block implements RecyclerPart {

    public RecyclerInputBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PLACE, RecyclerPlace.NONE).setValue(FLOOR, RecyclerFloor.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLACE, FLOOR);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this)) {
            RecyclerPart.reframeAround(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        RecyclerPart.reframeAround(level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return RecyclerPart.placed(defaultBlockState(), context);
    }

    @Override
    public Role role() {
        return Role.INPUT;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        var core = RecyclerPart.coreOf(level, pos);
        if (core.isEmpty()) {
            return refuse(player, Rejection.NO_RECYCLER);
        }
        int inserted;
        try (var transaction = Transaction.openRoot()) {
            inserted = core.get().inputFace().insert(ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
        }
        if (inserted == 0) {
            return refuse(player, core.get().rejectionOf(stack));
        }
        stack.shrink(inserted);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Takes the click, so nothing is placed, and tells {@code player} why on the action bar. */
    private static InteractionResult refuse(Player player, Rejection why) {
        player.sendOverlayMessage(why.message());
        return InteractionResult.CONSUME;
    }
}
