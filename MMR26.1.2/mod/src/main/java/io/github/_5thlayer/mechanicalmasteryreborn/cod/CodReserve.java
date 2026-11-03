// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * C.O.D.'s one slot of essence, at most a stack. Taking from it, by a player's click or by automation's
 * extraction, leaves the rest, or nothing; only C.O.D. puts essence back, one at a time, of the tier it makes.
 * Once its tier drops, what the slot still holds stays, and takes no more until it is emptied. Nothing goes in
 * from outside.
 */
final class CodReserve extends ItemStacksResourceHandler {

    private final Runnable changed;

    CodReserve(Runnable changed) {
        super(1);
        this.changed = changed;
    }

    /** Fills the slot to a full stack of {@code essence}, as a newly placed C.O.D. holds. */
    void fill(Item essence) {
        ItemStack full = new ItemStack(essence);
        set(0, ItemResource.of(full), full.getMaxStackSize());
    }

    boolean isEmpty() {
        return stacks.get(0).isEmpty();
    }

    /** Whether the slot takes no more of {@code essence}: it holds a full stack of it, or anything else. */
    boolean isFullFor(Item essence) {
        ItemStack stack = stacks.get(0);
        return !stack.isEmpty() && (!stack.is(essence) || stack.getCount() >= stack.getMaxStackSize());
    }

    /** Whether the slot holds a full stack of {@code essence}. */
    boolean isFullOf(Item essence) {
        ItemStack stack = stacks.get(0);
        return stack.is(essence) && stack.getCount() >= stack.getMaxStackSize();
    }

    /** Adds one {@code essence} to what the slot holds, which must not be {@link #isFullFor full} for it. */
    void makeOne(Item essence) {
        ItemStack stack = stacks.get(0);
        set(0, ItemResource.of(essence), stack.isEmpty() ? 1 : stack.getCount() + 1);
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
