// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Making;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * An item resource made from a recipe's result, as Craftworks' Assembler makes its output with no item stack, passes
 * through {@link Making#made(ItemResource)}, so a Transmuted Essence a playing team assembles is stamped with its attempt
 * (#322). Only this way of making one: a resource made from a bare item is shared, and never stamped.
 */
@Mixin(ItemResource.class)
public abstract class ItemResourceMixin {

    @Inject(method = "of(Lnet/minecraft/world/item/ItemStackTemplate;)Lnet/neoforged/neoforge/transfer/item/ItemResource;",
            at = @At("RETURN"), cancellable = true)
    private static void mechanicalmasteryreborn$made(ItemStackTemplate template, CallbackInfoReturnable<ItemResource> callback) {
        ItemResource made = callback.getReturnValue();
        ItemResource stamped = Making.made(made);
        if (stamped != made) {
            callback.setReturnValue(stamped);
        }
    }
}
