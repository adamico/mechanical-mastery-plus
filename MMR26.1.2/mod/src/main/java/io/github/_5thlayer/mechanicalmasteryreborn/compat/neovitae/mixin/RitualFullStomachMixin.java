// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.neovitae.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.LifeHandicap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Names the food Neo Vitae's Ritual of Full Stomach feeds a player from its chest, which the feeding counts as in their
 * Diet (#244): the last stack it reads the food of is the one it feeds, since it feeds it at once. {@link Pseudo}, so
 * without Neo Vitae, whose class it targets by name, it never applies.
 */
@Pseudo
@Mixin(targets = "com.breakinblocks.neovitae.ritual.types.RitualFullStomach")
public abstract class RitualFullStomachMixin {

    @Redirect(method = "feedFromInventory", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object mechanicalmasteryreborn$eating(ItemStack stack, DataComponentType<?> type) {
        LifeHandicap.eating(stack);
        return stack.get(type);
    }

    @Inject(method = "feedFromInventory", at = @At("RETURN"))
    private void mechanicalmasteryreborn$ate(CallbackInfoReturnable<Boolean> callback) {
        LifeHandicap.ate();
    }
}
