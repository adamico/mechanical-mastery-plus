// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * What raises an Agritech planter's odds of a quality harvest, its soil's growth and its fertilizer's yield, and its
 * inventory, whose last twelve slots are its outputs, which {@link PlanterQualityMixin} reads. Both planters name
 * them alike, their soil in slot 1. {@link Pseudo}, so without Agritech it never applies.
 */
@Pseudo
@Mixin(targets = {"com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity",
        "com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity"})
public interface PlanterOdds {

    @Accessor("inventory")
    ItemStacksResourceHandler mechanicalmasteryreborn$inventory();

    @Invoker("getStack")
    ItemStack mechanicalmasteryreborn$stack(int slot);

    @Invoker("getSoilGrowthModifier")
    float mechanicalmasteryreborn$soilGrowth(ItemStack soil);

    @Invoker("getFertilizerYieldModifier")
    float mechanicalmasteryreborn$fertilizerYield();
}
