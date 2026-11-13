// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * The Artisan Press's private {@code setProcessing}, which {@link ArtisanPressBlockEntityMixin} calls on a locked tick, and its
 * input, whose quality {@link ArtisanPressQualityMixin} gives its results.
 */
@Pseudo
@Mixin(targets = "net.blay09.mods.pantryforblockheads.block.entity.ArtisanPressBlockEntity")
public interface ArtisanPressProcessing {

    @Invoker("setProcessing")
    void mechanicalmasteryreborn$setProcessing(boolean processing);

    @Invoker("getInputItem")
    ItemStack mechanicalmasteryreborn$inputItem();
}
