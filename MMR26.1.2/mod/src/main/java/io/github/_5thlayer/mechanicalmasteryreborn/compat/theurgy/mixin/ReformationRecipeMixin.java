// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Keeps a reformation that makes one of the mod's items, Reformed Salt and Reformed Sulfur (#330), from stamping its
 * target's components on the result: Theurgy copies them all, so the result took the target's name and model.
 * {@link Pseudo}, as the crafting mixins are.
 */
@Pseudo
@Mixin(targets = "com.klikli_dev.theurgy.content.recipe.ReformationRecipe")
public abstract class ReformationRecipeMixin {

    @Redirect(method = "assemble", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V"))
    private void mechanicalmasteryreborn$ownResult(ItemStack result, DataComponentMap target) {
        if (!MechanicalMasteryReborn.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(result.getItem()).getNamespace())) {
            result.applyComponents(target);
        }
    }
}
