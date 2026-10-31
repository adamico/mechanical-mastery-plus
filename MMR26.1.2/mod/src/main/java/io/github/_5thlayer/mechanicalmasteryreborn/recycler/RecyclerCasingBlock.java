// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** A casing: fills a Recycler's place that needs no more drives, inputs or outputs, and does nothing else. */
public final class RecyclerCasingBlock extends Block implements RecyclerPart {

    public RecyclerCasingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PLACE, RecyclerPlace.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLACE);
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
