// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * A Craftworks machine's input slot takes the food it holds at another quality (#372). A slot holds one item resource,
 * components and all, so it would refuse it. The two are {@link FoodQuality#blend blended} instead: the slot then holds
 * both, at their average quality, which is what the craft would have given its product. The swap happens in the same
 * transaction, so a probe that aborts changes nothing. {@link Pseudo}, so without Craftworks it never applies.
 */
@Pseudo
@Mixin(targets = "io.github._5thlayer.craftworks.machine.MachineInventory")
public abstract class MachineInventoryBlendMixin extends ItemStacksResourceHandler {

    private MachineInventoryBlendMixin() {
        super(0);
    }

    @Shadow
    abstract boolean isInput(int slot);

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ItemResource held = getResource(index);
        int holding = getAmountAsInt(index);
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || holding == 0 || held.equals(resource) || !isInput(index)) {
            return super.insert(index, resource, amount, transaction);
        }
        int room = Math.min(amount, getCapacity(index, held) - holding);
        if (room <= 0) {
            return 0;
        }
        var blended = FoodQuality.blend(held.toStack(holding), resource.toStack(room), server.registryAccess());
        if (blended.isEmpty()) {
            return super.insert(index, resource, amount, transaction);
        }
        ItemStack both = blended.get();
        try (Transaction swap = Transaction.open(transaction)) {
            if (super.extract(index, held, holding, swap) == holding
                    && super.insert(index, ItemResource.of(both), both.getCount(), swap) == both.getCount()) {
                swap.commit();
                return room;
            }
        }
        return 0;
    }
}
