// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.port;

import java.util.Objects;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A Port's item face: it takes its own essence, up to what's left of the quota while open, and nothing else, ever.
 * What a transaction inserts is held as pending, and counted toward the quota only once the outermost transaction
 * commits, so a simulated insert counts nothing. It holds nothing: what it takes is used up.
 */
final class PortInput extends SnapshotJournal<Integer> implements ResourceHandler<ItemResource> {

    private final PortBlockEntity port;
    /** What the open transactions have inserted, not counted yet. */
    private int pending;

    PortInput(PortBlockEntity port) {
        this.port = port;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        return ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return 0;
    }

    /** What's left of the quota, for its own essence or an empty one, so a hopper sees room while there is some. */
    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || isValid(index, resource) ? Math.max(0, port.needed() - pending) : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return resource.is(Essences.item(((PortBlock) port.getBlockState().getBlock()).essence()));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!isValid(index, resource)) {
            return 0;
        }
        int taken = Math.min(amount, Math.max(0, port.needed() - pending));
        if (taken > 0) {
            updateSnapshots(transaction);
            pending += taken;
        }
        return taken;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    protected Integer createSnapshot() {
        return pending;
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        pending = snapshot;
    }

    @Override
    protected void onRootCommit(Integer originalState) {
        int count = pending;
        pending = 0;
        port.take(count);
    }
}
