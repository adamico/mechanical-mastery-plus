// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.gateways.mixin;

import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.HomeGates;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The home gate's place (#238): a gate pearl asks {@link Gateway#canOpen} first, and on a message refuses in red and
 * keeps the pearl, so {@link HomeGates#refusal}'s message does both. The op-only {@code /open_gateway} never asks.
 * {@link Pseudo}, so without Gateways, whose class it targets by name, it never applies.
 */
@Pseudo
@Mixin(targets = "dev.shadowsoffire.gateways.gate.Gateway")
public interface GatewayMixin {

    @Inject(method = "canOpen", at = @At("HEAD"), cancellable = true)
    private void mechanicalmasteryreborn$home(Player player, CallbackInfoReturnable<Component> cir) {
        Component refusal = HomeGates.refusal(player, GatewayRegistry.INSTANCE.getKey((Gateway) this));
        if (refusal != null) {
            cir.setReturnValue(refusal);
        }
    }
}
