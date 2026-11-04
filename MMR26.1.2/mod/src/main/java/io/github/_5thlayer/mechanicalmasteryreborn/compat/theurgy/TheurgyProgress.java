// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy;

import java.util.Optional;

import com.klikli_dev.theurgy.content.behaviour.crafting.HasCraftingBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * How far a Theurgy apparatus is through what it is processing, as Theurgy's own in-world HUD reads it, for Jade to
 * show now that the pack turns that HUD off (#255). Loaded only when Theurgy is installed.
 */
public final class TheurgyProgress {

    private TheurgyProgress() {
    }

    /**
     * The percent done while the apparatus is processing, and nothing otherwise. A two-block apparatus keeps its
     * crafting in the lower half, so the upper half answers for it, as in Theurgy's HUD.
     */
    public static Optional<Integer> percent(BlockEntity entity) {
        BlockEntity crafting = entity;
        BlockState state = entity.getBlockState();
        if (!(crafting instanceof HasCraftingBehaviour<?, ?, ?>) && entity.getLevel() != null
                && state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            crafting = entity.getLevel().getBlockEntity(entity.getBlockPos().below());
        }
        return crafting instanceof HasCraftingBehaviour<?, ?, ?> machine && machine.craftingBehaviour().isProcessing()
                ? Optional.of(machine.craftingBehaviour().progressPercent())
                : Optional.empty();
    }
}
