// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Refunds;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The refund waiting at a lab for the team whose island it stands on (#259), a page at a time over the player's
 * inventory. A member takes a stack by a click or a shift-click, or everything that fits by {@link #TAKE_ALL}; nothing
 * goes in. {@link #PREVIOUS} and {@link #NEXT} turn the page, and {@link #BACK} goes back to the lab's choice.
 */
public final class RefundMenu extends AbstractContainerMenu {

    public static final int COLUMNS = 9;
    public static final int ROWS = 5;
    /** The refund's slots on a page. */
    public static final int PAGE = COLUMNS * ROWS;
    public static final int WIDTH = 176;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 18;
    /** Where the row of buttons below the grid starts. */
    public static final int BUTTONS_Y = GRID_Y + ROWS * 18 + 4;
    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_Y = BUTTONS_Y + 16 + 15;
    public static final int HEIGHT = INVENTORY_Y + 3 * 18 + 4 + 18 + 7;

    public static final int PREVIOUS = 0;
    public static final int NEXT = 1;
    public static final int TAKE_ALL = 2;
    public static final int BACK = 3;

    private static final Component TITLE = Component.translatable("container.mechanicalmasteryreborn.lab_refund");

    private final ContainerLevelAccess access;
    private final DataSlot page;
    private final DataSlot pages;
    private final DataSlot member;

    /** The client's menu, whose slots and page the sync fills. */
    public RefundMenu(int containerId, Inventory inventory) {
        super(Lab.REFUND_MENU.get(), containerId);
        this.access = ContainerLevelAccess.NULL;
        this.page = addDataSlot(DataSlot.standalone());
        this.pages = addDataSlot(DataSlot.standalone());
        this.member = addDataSlot(DataSlot.standalone());
        addSlots(new SimpleContainer(PAGE), inventory);
    }

    RefundMenu(int containerId, Inventory inventory, ServerLevel level, BlockPos pos) {
        super(Lab.REFUND_MENU.get(), containerId);
        this.access = ContainerLevelAccess.create(level, pos);
        UUID team = Teams.at(level, pos);
        Refunds refunds = Refunds.get(level.getServer());
        this.page = addDataSlot(DataSlot.standalone());
        this.pages = addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pageCount(refunds.waiting(team).size());
            }

            @Override
            public void set(int value) {
            }
        });
        this.member = addDataSlot(DataSlot.standalone());
        member.set(team.equals(Teams.of(inventory.player)) ? 1 : 0);
        addSlots(new Page(refunds, () -> refunds.waiting(team), page), inventory);
    }

    /** What opens the refund of the team whose island the lab at {@code pos} stands on. */
    public static MenuProvider provider(ServerLevel level, BlockPos pos) {
        return new SimpleMenuProvider((containerId, inventory, player) -> new RefundMenu(containerId, inventory, level, pos), TITLE);
    }

    private void addSlots(Container refund, Inventory inventory) {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlot(new RefundSlot(refund, row * COLUMNS + column, GRID_X + column * 18, GRID_Y + row * 18));
            }
        }
        addStandardInventorySlots(inventory, INVENTORY_X, INVENTORY_Y);
    }

    /** How many pages {@code stacks} stacks fill: one at least, though empty. */
    static int pageCount(int stacks) {
        return Math.max(1, (stacks + PAGE - 1) / PAGE);
    }

    /** The page shown, from 0. */
    public int page() {
        return Math.min(page.get(), pages() - 1);
    }

    public int pages() {
        return Math.max(1, pages.get());
    }

    /** Whether the player is in the team whose island the lab stands on, and so may take its refund. */
    public boolean isMember() {
        return member.get() == 1;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case PREVIOUS -> {
                page.set(Math.max(0, page() - 1));
                return true;
            }
            case NEXT -> {
                page.set(Math.min(pages() - 1, page() + 1));
                return true;
            }
            case TAKE_ALL -> {
                return isMember() && access.evaluate((level, pos) -> takeAll((ServerLevel) level, pos, player), false);
            }
            case BACK -> {
                access.execute((level, pos) -> player.openMenu(level.getBlockState(pos).getMenuProvider(level, pos)));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /** Moves into {@code player}'s inventory as much of the whole refund as fits, and says whether any did. */
    private static boolean takeAll(ServerLevel level, BlockPos pos, Player player) {
        UUID team = Teams.at(level, pos);
        Refunds refunds = Refunds.get(level.getServer());
        List<ItemStack> waiting = refunds.waiting(team);
        boolean took = false;
        for (ItemStack stack : waiting) {
            int before = stack.getCount();
            if (before > 0) {
                player.getInventory().add(stack);
                took |= stack.getCount() < before;
            }
        }
        if (took) {
            refunds.compact(team);
            refunds.setDirty();
        }
        return took;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= PAGE) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();
        if (!moveItemStackTo(stack, PAGE, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        slot.setChanged();
        return stack.isEmpty() ? moved : ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Lab.BLOCK.get());
    }

    /** A slot of the refund: it gives, to the lab's team alone, and takes nothing. */
    private final class RefundSlot extends Slot {

        RefundSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return isMember();
        }
    }

    /**
     * The page {@code page} of the refund, read and taken from in place: a stack taken whole leaves an empty one where
     * it stood, so the stacks after it keep their slots while the page is open.
     */
    private record Page(Refunds refunds, Supplier<List<ItemStack>> waiting, DataSlot page) implements Container {

        private int index(int slot) {
            return page.get() * PAGE + slot;
        }

        @Override
        public int getContainerSize() {
            return PAGE;
        }

        @Override
        public boolean isEmpty() {
            return waiting.get().stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            List<ItemStack> list = waiting.get();
            int index = index(slot);
            return index < list.size() ? list.get(index) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty() || count <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack taken = stack.split(count);
            setChanged();
            return taken;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return removeItem(slot, getItem(slot).getCount());
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            List<ItemStack> list = waiting.get();
            int index = index(slot);
            if (index < list.size()) {
                list.set(index, stack);
                setChanged();
            } else if (!stack.isEmpty()) {
                list.add(stack);
                setChanged();
            }
        }

        @Override
        public void setChanged() {
            refunds.setDirty();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }
}
