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
 * C.O.D.'s one slot of Advanced essence, at most a stack. Taking from it, by a player's click or by
 * automation's extraction, leaves the rest, or nothing; only C.O.D. puts essence back, one at a time.
 * Nothing goes in from outside.
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

    boolean isEmpty() {
        return stacks.get(0).isEmpty();
    }

    boolean isFull() {
        ItemStack stack = stacks.get(0);
        return !stack.isEmpty() && stack.getCount() >= stack.getMaxStackSize();
    }

    /** Adds one essence to what the slot holds. */
    void makeOne() {
        ItemStack stack = stacks.get(0);
        set(0, ItemResource.of(essence.get()), stack.isEmpty() ? 1 : stack.getCount() + 1);
    }

    /** Nothing goes in; the slot still reports what it holds as valid, so its capacity reads as a stack's. */
    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        changed.run();
    }
}
