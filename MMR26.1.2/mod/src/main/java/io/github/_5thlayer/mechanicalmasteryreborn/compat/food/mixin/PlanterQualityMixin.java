// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import java.util.ArrayList;
import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Agritech's planters roll each crop's quality at harvest ({@link FoodQuality#roll}, #365), at odds its soil's growth times
 * its fertilizer's yield, so the better both are the better the crop: the drops it hands its post-harvest event, while the
 * fertilizer is still in its slot. A drop its roll leaves no room for takes a held stack's quality
 * ({@link FoodQuality#fitInto}), since a planter checks its room by item alone and throws away what doesn't fit.
 * {@link Pseudo}, so without Agritech it never applies.
 */
@Pseudo
@Mixin(targets = {"com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity",
        "com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity"})
public abstract class PlanterQualityMixin {

    @ModifyArg(method = "harvestPlant", index = 2, at = @At(value = "INVOKE",
            target = "Lcom/misterd/agritechevolved/integration/PlanterPostHarvestEvent;<init>(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V"))
    private List<ItemStack> mechanicalmasteryreborn$quality(List<ItemStack> drops) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level != null) {
            PlanterOdds planter = (PlanterOdds) this;
            double odds = planter.mechanicalmasteryreborn$soilGrowth(planter.mechanicalmasteryreborn$stack(PlanterOdds.SOIL))
                    * planter.mechanicalmasteryreborn$fertilizerYield();
            for (ItemStack drop : drops) {
                FoodQuality.roll(drop, odds, 0, level.getRandom(), level.registryAccess());
            }
            int size = planter.mechanicalmasteryreborn$inventory().size();
            List<ItemStack> outputs = new ArrayList<>();
            for (int slot = size - PlanterOdds.OUTPUTS; slot < size; slot++) {
                outputs.add(planter.mechanicalmasteryreborn$stack(slot));
            }
            FoodQuality.fitInto(drops, outputs);
        }
        return drops;
    }
}
