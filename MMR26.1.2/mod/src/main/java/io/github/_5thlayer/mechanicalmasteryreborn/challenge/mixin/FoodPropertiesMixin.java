// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.LifeHandicap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Names the food a player eats by hand, which its feeding counts as in their Diet (#244). */
@Mixin(FoodProperties.class)
public abstract class FoodPropertiesMixin {

    @Inject(method = "onConsume", at = @At("HEAD"))
    private void mechanicalmasteryreborn$eating(Level level, LivingEntity entity, ItemStack stack, Consumable consumable, CallbackInfo callback) {
        LifeHandicap.eating(stack);
    }

    @Inject(method = "onConsume", at = @At("RETURN"))
    private void mechanicalmasteryreborn$ate(Level level, LivingEntity entity, ItemStack stack, Consumable consumable, CallbackInfo callback) {
        LifeHandicap.ate();
    }
}
