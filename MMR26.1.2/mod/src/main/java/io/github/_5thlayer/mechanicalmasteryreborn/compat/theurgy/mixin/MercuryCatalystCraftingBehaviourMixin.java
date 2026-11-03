// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.ChainMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Mercury Catalyst ticks its own way, past {@link CraftingBehaviourMixin}'s check, so it asks the same at its own
 * tick's head (#214). It goes on converting the flux it has taken while active, with no input, so it asks then too.
 * {@link Pseudo}, as that one is.
 */
@Pseudo
@Mixin(targets = "com.klikli_dev.theurgy.content.apparatus.mercurycatalyst.MercuryCatalystCraftingBehaviour")
public abstract class MercuryCatalystCraftingBehaviourMixin {

    @Inject(method = "tickServer(ZZ)V", at = @At("HEAD"), cancellable = true)
    private void mechanicalmasteryreborn$locked(boolean isActive, boolean hasInput, CallbackInfo callback) {
        if ((isActive || hasInput) && ((ChainMachine) (Object) this).mechanicalmasteryreborn$stopIfLocked()) {
            callback.cancel();
        }
    }
}
