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
 * Pantry's Artisan Press does no work while life's chain is locked on its island (#362): its server tick asks
 * {@link ChainMachines#lockedAt} first, and while locked clears its processing state, so it stops animating, and
 * returns. {@link Pseudo}, so without Pantry it never applies.
 */
@Pseudo
@Mixin(targets = "net.blay09.mods.pantryforblockheads.block.entity.ArtisanPressBlockEntity")
public abstract class ArtisanPressBlockEntityMixin {

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mechanicalmasteryreborn$locked(Level level, BlockPos pos, BlockState state,
                                                       @Coerce BlockEntity entity, CallbackInfo callback) {
        if (ChainMachines.lockedAt(level, pos, state).isPresent()) {
            ((ArtisanPressProcessing) entity).mechanicalmasteryreborn$setProcessing(false);
            callback.cancel();
        }
    }
}
