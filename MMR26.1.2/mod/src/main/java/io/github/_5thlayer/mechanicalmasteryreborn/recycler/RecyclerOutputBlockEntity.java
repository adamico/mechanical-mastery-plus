// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An output's shelf: three positions, which only its core fills and anything may empty, a hopper below or a
 * player's right-click. Every client is sent what it holds, so the shelf shows it.
 */
public final class RecyclerOutputBlockEntity extends BlockEntity {

    private static final String SHELF_KEY = "shelf";

    private final Shelf shelf = new Shelf();

    public RecyclerOutputBlockEntity(BlockPos pos, BlockState state) {
        super(Recycler.OUTPUT_ENTITY.get(), pos, state);
    }

    /** The shelf as automation reaches it: extract only. */
    public ResourceHandler<ItemResource> face() {
        return shelf;
    }

    /** What position {@code position} holds. */
    public ItemStack item(int position) {
        return shelf.getResource(position).toStack(shelf.getAmountAsInt(position));
    }

    /** Whether all of {@code stack} fits on position {@code position}. */
    boolean fits(int position, ItemStack stack) {
        ItemStack held = item(position);
        return held.isEmpty() || ItemStack.isSameItemSameComponents(held, stack) && held.getCount() + stack.getCount() <= held.getMaxStackSize();
    }

    /** Adds {@code stack} to position {@code position}, which {@link #fits} said it fits. */
    void put(int position, ItemStack stack) {
        shelf.set(position, ItemResource.of(stack), item(position).getCount() + stack.getCount());
    }

    /** Takes all of position {@code position}. */
    ItemStack take(int position) {
        ItemStack held = item(position);
        shelf.set(position, ItemResource.EMPTY, 0);
        return held;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        shelf.serialize(output.child(SHELF_KEY));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        shelf.deserialize(input.childOrEmpty(SHELF_KEY));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            for (int position = 0; position < RecyclerLayout.POSITIONS_PER_OUTPUT; position++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), item(position));
            }
        }
    }

    /** Nothing goes in from outside; its core sets it. */
    private final class Shelf extends ItemStacksResourceHandler {

        Shelf() {
            super(RecyclerLayout.POSITIONS_PER_OUTPUT);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
