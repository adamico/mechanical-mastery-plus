// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Making;
import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every item stack made, whatever makes it, passes through {@link Making#made}, which stamps a Transmuted Essence a
 * playing team makes with its attempt (#322). The one constructor every other one and every copy ends in.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    private void mechanicalmasteryreborn$made(Holder<Item> item, int count, PatchedDataComponentMap components, CallbackInfo callback) {
        Making.made((ItemStack) (Object) this);
    }
}
