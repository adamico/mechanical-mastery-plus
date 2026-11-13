// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Farmer's Delight's Cooking Pot gives its meal its ingredients' average quality ({@link FoodQuality#carry}, #365): the meal
 * its recipe assembles, both when it checks the meal fits and when it cooks it, so a meal of another quality waits for the
 * one it holds to leave. {@link Pseudo}, so without its mod it never applies.
 */
@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity")
public abstract class CookingPotQualityMixin {

    /** The ingredient slots, before the meal, container and output slots. */
    private static final int INGREDIENTS = 6;

    @SuppressWarnings("removal")
    @Shadow
    @Final
    private ItemStackHandler inventory;

    @ModifyExpressionValue(method = {"canCook", "processCooking"}, at = @At(value = "INVOKE",
            target = "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;assemble(Lnet/neoforged/neoforge/items/wrapper/RecipeWrapper;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mechanicalmasteryreborn$quality(ItemStack meal) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level != null) {
            List<ItemStack> ingredients = new ArrayList<>();
            for (int slot = 0; slot < INGREDIENTS; slot++) {
                ingredients.add(inventory.getStackInSlot(slot).copyWithCount(1));
            }
            FoodQuality.carry(meal, ingredients, level.registryAccess());
        }
        return meal;
    }
}
