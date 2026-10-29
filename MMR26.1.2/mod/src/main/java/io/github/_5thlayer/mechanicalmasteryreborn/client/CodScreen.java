// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.libworks.ghost.Ghosts;
import io.github._5thlayer.libworks.machine.client.MachineDraw;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;

/** C.O.D.'s one slot over the player's inventory, in the suite's stand-in machine look. */
final class CodScreen extends AbstractContainerScreen<CodMenu> {

    private final Ghosts ghosts = new Ghosts(() -> leftPos, () -> topPos);

    CodScreen(CodMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CodMenu.WIDTH, CodMenu.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = MachineDraw.MARGIN;
        titleLabelY = 6;
        inventoryLabelX = CodMenu.INVENTORY_X;
        inventoryLabelY = CodMenu.INVENTORY_Y - 11;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        MachineDraw.panel(graphics, font, leftPos, topPos, imageWidth, imageHeight, menu.slots, ghosts, Util.getMillis());
    }
}
