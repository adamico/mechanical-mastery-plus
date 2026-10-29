// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

/** C.O.D.'s one slot over the player's inventory. The slot only gives. */
public final class CodMenu extends AbstractContainerMenu {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 133;
    public static final int RESERVE_X = 80;
    public static final int RESERVE_Y = 20;
    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_Y = 51;
    static final int RESERVE_SLOT = 0;
    private static final int FIRST_PLAYER_SLOT = 1;

    private final BlockPos pos;
    private final @Nullable CodBlockEntity cod;

    CodMenu(int containerId, Inventory inventory, CodBlockEntity cod) {
        this(containerId, inventory, cod.getBlockPos(), cod, cod.reserveSlot());
    }

    /** The client's menu, opened at the position the server wrote; its slot stands over a stub the sync fills. */
    public CodMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readBlockPos(), null, new ItemStacksResourceHandler(1));
    }

    private CodMenu(int containerId, Inventory inventory, BlockPos pos, @Nullable CodBlockEntity cod, ItemStacksResourceHandler reserve) {
        super(Cod.MENU.get(), containerId);
        this.pos = pos;
        this.cod = cod;
        addSlot(new ReserveSlot(reserve));
        addStandardInventorySlots(inventory, INVENTORY_X, INVENTORY_Y);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (index != RESERVE_SLOT || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (!moveItemStackTo(stack, FIRST_PLAYER_SLOT, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        // The slot gives what was moved; emptied, the reserve refills itself. One stack a click: vanilla
        // repeats a quick move while it returns the slot's item, which a reserve that refills always is.
        slot.set(stack);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (cod != null && cod.isRemoved()) {
            return false;
        }
        return player.isWithinBlockInteractionRange(pos, 4.0);
    }

    /** The reserve takes nothing from the player. */
    private static final class ReserveSlot extends ResourceHandlerSlot {

        ReserveSlot(ItemStacksResourceHandler handler) {
            super(handler, handler::set, 0, RESERVE_X, RESERVE_Y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
