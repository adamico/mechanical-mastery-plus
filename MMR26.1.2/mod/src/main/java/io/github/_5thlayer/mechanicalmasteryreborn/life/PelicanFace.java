// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.Objects;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A Pelican's item face, the same on every side: index {@value #MOUTH}, its mouth, takes meals and holds nothing, and
 * index {@value #OUTPUT}, its output, gives the Vital Essence and takes nothing. A meal inserted is eaten into the batch
 * at once, its state journalled so an aborted or simulated insert eats nothing; a batch made ready starts digesting once
 * the outermost transaction commits.
 */
final class PelicanFace extends SnapshotJournal<PelicanBlockEntity.State> implements ResourceHandler<ItemResource> {

    static final int MOUTH = 0;
    static final int OUTPUT = 1;

    private final PelicanBlockEntity pelican;

    PelicanFace(PelicanBlockEntity pelican) {
        this.pelican = pelican;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        return index == OUTPUT ? pelican.output().getResource(0) : ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return index == OUTPUT ? pelican.output().getAmountAsLong(0) : 0;
    }

    /** The mouth's: how many of a meal it would eat now in a row, or for no resource, whether it eats at all. */
    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        if (index == OUTPUT) {
            return 1;
        }
        if (resource.isEmpty()) {
            return pelican.hungry() ? 1 : 0;
        }
        return Pelican.meal(resource) ? pelican.edible(resource, resource.getMaxStackSize()) : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return index == OUTPUT ? resource.is(Essences.item(Essence.VITAL)) : Pelican.meal(resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (index != MOUTH || !Pelican.meal(resource)) {
            return 0;
        }
        int eaten = pelican.edible(resource, amount);
        if (eaten > 0) {
            updateSnapshots(transaction);
            pelican.eat(resource, eaten);
        }
        return eaten;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return index == OUTPUT ? pelican.output().extract(0, resource, amount, transaction) : 0;
    }

    @Override
    protected PelicanBlockEntity.State createSnapshot() {
        return pelican.state();
    }

    @Override
    protected void revertToSnapshot(PelicanBlockEntity.State snapshot) {
        pelican.restore(snapshot);
    }

    @Override
    protected void onRootCommit(PelicanBlockEntity.State original) {
        pelican.startDigestingIfReady();
    }
}
