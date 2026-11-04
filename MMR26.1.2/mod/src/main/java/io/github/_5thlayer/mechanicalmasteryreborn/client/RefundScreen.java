// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.libworks.ghost.Ghosts;
import io.github._5thlayer.libworks.machine.client.MachineDraw;
import io.github._5thlayer.mechanicalmasteryreborn.lab.RefundMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;

/**
 * The team's refund at its lab (#259), a page of it over the player's inventory, in the suite's stand-in machine look:
 * the page's number on the title's line, and below the page, buttons turn it, take everything that fits, and go back to
 * the lab.
 */
final class RefundScreen extends AbstractContainerScreen<RefundMenu> {

    private static final int SMALL = 16;
    private static final int WIDE = 44;
    private static final Component TAKE_ALL = Component.translatable("mechanicalmasteryreborn.refund.take_all");
    private static final Component TAKE_ALL_TOOLTIP = Component.translatable("mechanicalmasteryreborn.refund.take_all.tooltip");
    private static final Component NOT_MEMBER = Component.translatable("mechanicalmasteryreborn.refund.not_member");
    private static final Component BACK = Component.translatable("mechanicalmasteryreborn.refund.back");

    private final Ghosts ghosts = new Ghosts(() -> leftPos, () -> topPos);
    private Button previous;
    private Button next;
    private Button takeAll;

    RefundScreen(RefundMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, RefundMenu.WIDTH, RefundMenu.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = MachineDraw.MARGIN;
        titleLabelY = 6;
        inventoryLabelX = RefundMenu.INVENTORY_X;
        inventoryLabelY = RefundMenu.INVENTORY_Y - 11;
        int y = topPos + RefundMenu.BUTTONS_Y;
        int left = leftPos + RefundMenu.GRID_X;
        int right = leftPos + RefundMenu.GRID_X + RefundMenu.COLUMNS * 18;
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> click(RefundMenu.PREVIOUS))
                .bounds(left, y, SMALL, SMALL).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> click(RefundMenu.NEXT))
                .bounds(left + SMALL + 2, y, SMALL, SMALL).build());
        addRenderableWidget(Button.builder(BACK, button -> click(RefundMenu.BACK))
                .bounds(right - WIDE, y, WIDE, SMALL).build());
        takeAll = addRenderableWidget(Button.builder(TAKE_ALL, button -> click(RefundMenu.TAKE_ALL))
                .bounds(right - 2 * WIDE - 2, y, WIDE, SMALL).build());
        updateButtons();
    }

    private void click(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        previous.active = menu.page() > 0;
        next.active = menu.page() < menu.pages() - 1;
        takeAll.active = menu.isMember();
        takeAll.setTooltip(Tooltip.create(menu.isMember() ? TAKE_ALL_TOOLTIP : NOT_MEMBER));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        MachineDraw.panel(graphics, font, leftPos, topPos, imageWidth, imageHeight, menu.slots, ghosts, Util.getMillis());
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        // On the title's line, at the right, since the row of buttons below the page has no room for it.
        Component page = Component.translatable("mechanicalmasteryreborn.refund.page", menu.page() + 1, menu.pages());
        graphics.text(font, page, imageWidth - MachineDraw.MARGIN - font.width(page), titleLabelY, MachineDraw.TEXT, false);
    }
}
