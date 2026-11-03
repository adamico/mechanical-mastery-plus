// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.neovitae.mixin;

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
 * A Neo Vitae Ara Vitae from Advanced's mix does no work while the life chain is locked for its island's team (#243):
 * its tick asks {@link ChainMachines#lockedAt} first and while locked skips the whole tick, its infusions and its orb filling. Neo Vitae has
 * no hook of its own. {@link Pseudo}, so without Neo Vitae, whose class it targets by name, it never applies.
 */
@Pseudo
@Mixin(targets = "com.breakinblocks.neovitae.common.blockentity.AraVitaeTile")
public abstract class AraVitaeTileMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void mechanicalmasteryreborn$locked(Level level, BlockPos pos, BlockState state, @Coerce BlockEntity entity,
            CallbackInfo callback) {
        if (ChainMachines.lockedAt(level, pos, state).isPresent()) {
            callback.cancel();
        }
    }
}
