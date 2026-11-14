// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Craftworks' Assembler, of every tier, gives its product its ingredients' average quality ({@link FoodQuality#carry}, #365):
 * the stacks one craft took, which its {@code takeItems} fills, are kept for the product's placing, which follows in the
 * same {@code finish}. Its further results and the remainders take none. Both when it checks the product fits and when it
 * makes it. A product with no room at its own quality goes onto the held stack of its item nearest it, so its quality
 * never stalls the Assembler while it has room (#372). The Personal Assembler plans by each item's exact components, so it
 * carries none. {@link Pseudo}, so without Craftworks it never applies.
 */
@Pseudo
@Mixin(targets = "io.github._5thlayer.craftworks.machine.AssemblerBlockEntity")
public abstract class AssemblerQualityMixin {

    /** The stacks the craft under way took, one per item. */
    @Unique
    private List<ItemStack> mechanicalmasteryreborn$consumed = List.of();

    @Shadow
    public abstract ItemStacksResourceHandler inventory();

    @ModifyArg(method = "finish", index = 3, at = @At(value = "INVOKE",
            target = "Lio/github/_5thlayer/craftworks/machine/AssemblerBlockEntity;takeItems(Lio/github/_5thlayer/craftworks/recipe/AssemblingRecipe;Lnet/neoforged/neoforge/transfer/transaction/TransactionContext;Ljava/util/List;Ljava/util/List;)Lio/github/_5thlayer/craftworks/machine/MachineState;"))
    private List<ItemStack> mechanicalmasteryreborn$consumed(List<ItemStack> consumed) {
        mechanicalmasteryreborn$consumed = consumed;
        return consumed;
    }

    @WrapOperation(method = "finish", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lio/github/_5thlayer/craftworks/machine/AssemblerBlockEntity;place(Lnet/neoforged/neoforge/transfer/item/ItemResource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)Z"))
    private boolean mechanicalmasteryreborn$quality(@Coerce Object self, ItemResource product, int count, TransactionContext tx,
            Operation<Boolean> place) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        List<ItemStack> consumed = mechanicalmasteryreborn$consumed;
        mechanicalmasteryreborn$consumed = List.of();
        if (level == null || product.isEmpty()) {
            return place.call(self, product, count, tx);
        }
        ItemStack made = product.toStack(1);
        if (!FoodQuality.takesQuality(made)) {
            return place.call(self, product, count, tx);
        }
        FoodQuality.carry(made, consumed, level.registryAccess());
        if (mechanicalmasteryreborn$tryPlace(self, ItemResource.of(made), count, tx, place)) {
            return true;
        }
        for (ItemResource held : mechanicalmasteryreborn$heldKinds(made, level)) {
            if (mechanicalmasteryreborn$tryPlace(self, held, count, tx, place)) {
                return true;
            }
        }
        return false;
    }

    /** Places {@code count} of {@code product} if it all goes, in a transaction of its own so a part placed is undone. */
    @Unique
    private static boolean mechanicalmasteryreborn$tryPlace(Object self, ItemResource product, int count, TransactionContext tx,
            Operation<Boolean> place) {
        try (Transaction trial = Transaction.open(tx)) {
            if (place.call(self, product, count, trial)) {
                trial.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * The other qualities of {@code made} the Assembler holds, nearest its own first and the lower of two as near: where a
     * product with no room at its own quality goes, as a planter's crop does ({@link FoodQuality#fitInto}).
     */
    @Unique
    private List<ItemResource> mechanicalmasteryreborn$heldKinds(ItemStack made, Level level) {
        ItemStacksResourceHandler inventory = inventory();
        int own = FoodQuality.level(made);
        List<ItemResource> kinds = new ArrayList<>();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemResource held = inventory.getResource(slot);
            if (!held.isEmpty() && !kinds.contains(held)
                    && FoodQuality.blend(held.toStack(1), made, level.registryAccess()).isPresent()) {
                kinds.add(held);
            }
        }
        kinds.sort(Comparator.comparingInt((ItemResource kind) -> Math.abs(FoodQuality.level(kind.toStack(1)) - own))
                .thenComparingInt(kind -> FoodQuality.level(kind.toStack(1))));
        return kinds;
    }
}
