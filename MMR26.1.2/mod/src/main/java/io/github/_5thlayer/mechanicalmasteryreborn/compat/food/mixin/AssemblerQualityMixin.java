// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Craftworks' Assembler, of every tier, gives its product its ingredients' average quality ({@link FoodQuality#carry}, #365):
 * the stacks one craft took, which its {@code takeItems} fills, are kept for the product's placing, which follows in the
 * same {@code finish}. Its further results and the remainders take none. Both when it checks the product fits and when it
 * makes it. The Personal Assembler plans by each item's exact components, so it carries none. {@link Pseudo}, so without
 * Craftworks it never applies.
 */
@Pseudo
@Mixin(targets = "io.github._5thlayer.craftworks.machine.AssemblerBlockEntity")
public abstract class AssemblerQualityMixin {

    /** The stacks the craft under way took, one per item. */
    @Unique
    private List<ItemStack> mechanicalmasteryreborn$consumed = List.of();

    @ModifyArg(method = "finish", index = 3, at = @At(value = "INVOKE",
            target = "Lio/github/_5thlayer/craftworks/machine/AssemblerBlockEntity;takeItems(Lio/github/_5thlayer/craftworks/recipe/AssemblingRecipe;Lnet/neoforged/neoforge/transfer/transaction/TransactionContext;Ljava/util/List;Ljava/util/List;)Lio/github/_5thlayer/craftworks/machine/MachineState;"))
    private List<ItemStack> mechanicalmasteryreborn$consumed(List<ItemStack> consumed) {
        mechanicalmasteryreborn$consumed = consumed;
        return consumed;
    }

    @ModifyArg(method = "finish", index = 0, at = @At(value = "INVOKE", ordinal = 0,
            target = "Lio/github/_5thlayer/craftworks/machine/AssemblerBlockEntity;place(Lnet/neoforged/neoforge/transfer/item/ItemResource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)Z"))
    private ItemResource mechanicalmasteryreborn$quality(ItemResource product) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        List<ItemStack> consumed = mechanicalmasteryreborn$consumed;
        mechanicalmasteryreborn$consumed = List.of();
        if (level == null || product.isEmpty()) {
            return product;
        }
        ItemStack made = product.toStack(1);
        if (!FoodQuality.takesQuality(made)) {
            return product;
        }
        FoodQuality.carry(made, consumed, level.registryAccess());
        return ItemResource.of(made);
    }
}
