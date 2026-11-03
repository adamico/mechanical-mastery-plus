// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.FedPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tells a new player's food data whose it is (#244), before its first tick would. */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow
    protected FoodData foodData;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mechanicalmasteryreborn$feeds(CallbackInfo callback) {
        ((FedPlayer) foodData).mechanicalmasteryreborn$feeds((Player) (Object) this);
    }
}
