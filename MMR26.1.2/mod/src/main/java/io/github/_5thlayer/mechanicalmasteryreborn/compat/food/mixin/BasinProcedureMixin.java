// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Butchery's Basin does no work while life's chain is locked on its island (#362). Its block's scheduled tick runs this
 * procedure and then schedules the next, so cancelling the procedure, not the tick, keeps the loop alive for a start.
 * {@link Pseudo}, so without Butchery it never applies.
 */
@Pseudo
@Mixin(targets = "net.mcreator.butchery.procedures.BasinupdatetickProcedure")
public abstract class BasinProcedureMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void mechanicalmasteryreborn$locked(LevelAccessor world, double x, double y, double z, BlockState blockState,
                                                       CallbackInfo callback) {
        if (world instanceof Level level) {
            BlockPos pos = BlockPos.containing(x, y, z);
            if (ChainMachines.lockedAt(level, pos, level.getBlockState(pos)).isPresent()) {
                callback.cancel();
            }
        }
    }
}
