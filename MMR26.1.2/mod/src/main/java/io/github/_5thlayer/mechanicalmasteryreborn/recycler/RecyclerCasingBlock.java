// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** A casing: fills a Recycler's place that needs no more drives, inputs or outputs, and does nothing else. */
public final class RecyclerCasingBlock extends Block implements RecyclerPart {

    public RecyclerCasingBlock(Properties properties) {
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
        return Role.CASING;
    }
}
