// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Agritech's Advanced Planter does no work while life's chain is locked on its island (#362): its tick asks {@link ChainMachines#lockedAt}
 * first and returns. {@link Pseudo}, so without its mod it never applies.
 */
@Pseudo
@Mixin(targets = "com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity")
public abstract class AdvancedPlanterBlockEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mechanicalmasteryreborn$locked(Level level, BlockPos pos, BlockState state,
                                                       @Coerce BlockEntity entity, CallbackInfo callback) {
        if (ChainMachines.lockedAt(level, pos, state).isPresent()) {
            callback.cancel();
        }
    }
}
