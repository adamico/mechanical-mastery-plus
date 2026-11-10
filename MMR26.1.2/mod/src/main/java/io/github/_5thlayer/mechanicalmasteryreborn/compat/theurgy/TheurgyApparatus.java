// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Finds the block entity that answers for a Theurgy apparatus. Loaded only when Theurgy is installed. */
final class TheurgyApparatus {

    private TheurgyApparatus() {
    }

    /**
     * The lower half's block entity for the upper half of a two-block apparatus, which keeps its state there, as
     * Theurgy's HUD reads it; {@code entity} itself when that is already {@code type} or no upper half. May be null.
     */
    static BlockEntity answering(BlockEntity entity, Class<?> type) {
        BlockState state = entity.getBlockState();
        if (!type.isInstance(entity) && entity.getLevel() != null
                && state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            return entity.getLevel().getBlockEntity(entity.getBlockPos().below());
        }
        return entity;
    }
}
