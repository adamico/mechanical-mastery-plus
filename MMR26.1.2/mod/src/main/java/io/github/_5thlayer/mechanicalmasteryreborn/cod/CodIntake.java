// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.Objects;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * C.O.D.'s Quintessence intake in its last stand, as a Port's item face is for its Transmuted Essence: it takes
 * Quintessence, up to what its team still needs to reach the margin while the last stand runs, and nothing else, ever.
 * What a transaction inserts is held as pending, and counted only once the outermost transaction commits, so a simulated
 * insert cancels nothing. It holds nothing: what it takes is used up.
 */
final class CodIntake extends SnapshotJournal<Integer> implements ResourceHandler<ItemResource> {

    private final CodBlockEntity cod;
    /** What the open transactions have inserted, not counted yet. */
    private int pending;

    CodIntake(CodBlockEntity cod) {
        this.cod = cod;
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

    /** What's left to the margin, for Quintessence or an empty resource, so a hopper sees room while there is some. */
    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || isValid(index, resource) ? Math.max(0, cod.quintessenceNeeded() - pending) : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return resource.is(Essences.item(Essence.QUINTESSENCE));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!isValid(index, resource)) {
            return 0;
        }
        int taken = Math.min(amount, Math.max(0, cod.quintessenceNeeded() - pending));
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
        cod.takeQuintessence(count);
    }
}
