// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Pantry's Artisan Press gives each of its results its input's quality ({@link FoodQuality#carry}, #365), its one input's
 * being the average; the remainder takes none. Both when it checks the results fit and when it makes them.
 * {@link Pseudo}, so without Pantry it never applies.
 */
@Pseudo
@Mixin(targets = "net.blay09.mods.pantryforblockheads.block.entity.ArtisanPressBlockEntity")
public abstract class ArtisanPressQualityMixin {

    @ModifyExpressionValue(method = "outputResults", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStackTemplate;create()Lnet/minecraft/world/item/ItemStack;", ordinal = 0))
    private ItemStack mechanicalmasteryreborn$quality(ItemStack result) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level != null) {
            ItemStack input = ((ArtisanPressProcessing) this).mechanicalmasteryreborn$inputItem();
            FoodQuality.carry(result, List.of(input.copyWithCount(1)), level.registryAccess());
        }
        return result;
    }
}
