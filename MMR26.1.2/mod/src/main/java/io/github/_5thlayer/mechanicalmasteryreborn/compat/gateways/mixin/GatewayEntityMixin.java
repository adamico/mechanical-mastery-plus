// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.gateways.mixin;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import io.github._5thlayer.mechanicalmasteryreborn.compat.gateways.GatewaysGates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A called wave (#341): a gate asks {@code canStartNextWave} each tick out of a wave, and says yes only once its
 * breather is over; a gate with a wave called says yes at once. {@link Pseudo}, so without Gateways, whose class it
 * targets by name, it never applies.
 */
@Pseudo
@Mixin(targets = "dev.shadowsoffire.gateways.entity.GatewayEntity")
public abstract class GatewayEntityMixin {

    @Inject(method = "canStartNextWave", at = @At("HEAD"), cancellable = true)
    private void mechanicalmasteryreborn$called(CallbackInfoReturnable<Boolean> cir) {
        if (GatewaysGates.called((GatewayEntity) (Object) this) > 0) {
            cir.setReturnValue(true);
        }
    }
}
