// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * C.O.D.'s one slot: a stack of Advanced essence that is full again the moment it is emptied. Taking part
 * of it leaves the rest; taking the last of it, by a player's click or by automation's extraction, refills
 * it to a full stack when that change lands, at the end of the extraction's transaction, so the next
 * extraction finds it full. Nothing goes in. Refilling makes nothing new as far as Inflation goes: a
 * dormant C.O.D. only keeps its stack.
 */
final class CodReserve extends ItemStacksResourceHandler {

    private final Supplier<Item> essence;
    private final Runnable changed;

    CodReserve(Supplier<Item> essence, Runnable changed) {
        super(1);
        this.essence = essence;
        this.changed = changed;
    }

    /** Fills the slot to a full stack, as a newly placed C.O.D. holds. */
    void fill() {
        ItemStack full = new ItemStack(essence.get());
        set(0, ItemResource.of(full), full.getMaxStackSize());
    }

    /** Nothing goes in; the slot still reports what it holds as valid, so its capacity reads as a stack's. */
    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        if (stacks.get(index).isEmpty()) {
            // set() calls back here once more, with the slot full, which ends it.
            fill();
            return;
        }
        changed.run();
    }
}
