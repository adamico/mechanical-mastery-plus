// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.FedPlayer;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LifeHandicap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The life handicap on a player's food data itself (#244), so whatever feeds or tires them counts: its exhaustion grows
 * and its feeding shrinks by {@link LifeHandicap}. Vanilla's food data doesn't know its player, so it is told
 * ({@link FedPlayer}).
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin implements FedPlayer {

    @Unique
    private Player mechanicalmasteryreborn$player;
    @Unique
    private boolean mechanicalmasteryreborn$feeding;

    @Shadow
    private void add(int food, float saturation) {
    }

    @Override
    public void mechanicalmasteryreborn$feeds(Player player) {
        mechanicalmasteryreborn$player = player;
    }

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"))
    private void mechanicalmasteryreborn$owner(ServerPlayer player, CallbackInfo callback) {
        mechanicalmasteryreborn$player = player;
    }

    @ModifyVariable(method = "addExhaustion(F)V", at = @At("HEAD"), argsOnly = true)
    private float mechanicalmasteryreborn$hungrier(float amount) {
        Player player = mechanicalmasteryreborn$player;
        return player == null ? amount : LifeHandicap.exhaustion(player, amount);
    }

    /** Both of vanilla's feedings come here, so it feeds again with the Diet's share, once. */
    @Inject(method = "add(IF)V", at = @At("HEAD"), cancellable = true)
    private void mechanicalmasteryreborn$diet(int food, float saturation, CallbackInfo callback) {
        Player player = mechanicalmasteryreborn$player;
        if (player == null || mechanicalmasteryreborn$feeding) {
            return;
        }
        double share = LifeHandicap.feed(player);
        if (share >= 1.0) {
            return;
        }
        callback.cancel();
        mechanicalmasteryreborn$feeding = true;
        try {
            add((int) Math.round(food * share), (float) (saturation * share));
        } finally {
            mechanicalmasteryreborn$feeding = false;
        }
    }
}
