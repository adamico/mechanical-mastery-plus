// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github._5thlayer.libworks.ghost.Ghosts;
import io.github._5thlayer.libworks.machine.client.MachineDraw;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.athanor.AthanorBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.athanor.AthanorMenu;
import io.github._5thlayer.mechanicalmasteryreborn.athanor.AthanorWork;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

/**
 * The Athanor (#263): what it is doing, the Nullessence it holds and how far the next Quintessence is, with its one
 * Quintessence slot beside; then each handicap as a spell, with what it costs or how long it has left and whether its line
 * counts, and a button to take it on; then a row to give a heart, with the Essentia Vitae it holds; in the suite's
 * stand-in machine look.
 */
final class AthanorScreen extends AbstractContainerScreen<AthanorMenu> {

    private static final int TOP = 20;
    private static final int LINE = 10;
    private static final int COST_Y = 10;
    private static final int BUTTON_WIDTH = 72;
    private static final int BUTTON_HEIGHT = 20;
    private static final Component TAKE_ON = Component.translatable("mechanicalmasteryreborn.athanor.take_on");
    private static final Component BEARING = Component.translatable("mechanicalmasteryreborn.athanor.bearing");
    private static final Component TAKE_ON_TOOLTIP = Component.translatable("mechanicalmasteryreborn.athanor.take_on.tooltip");
    private static final Component HEART = Component.translatable("mechanicalmasteryreborn.athanor.heart");
    private static final Component GIVE_HEART = Component.translatable("mechanicalmasteryreborn.athanor.heart.give");
    private static final Component GIVE_HEART_TOOLTIP = Component.translatable("mechanicalmasteryreborn.athanor.heart.tooltip");

    private final Ghosts ghosts = new Ghosts(() -> leftPos, () -> topPos);
    private final List<Button> buttons = new ArrayList<>();
    private @Nullable Button heart;

    AthanorScreen(AthanorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, AthanorMenu.WIDTH, AthanorMenu.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = MachineDraw.MARGIN;
        titleLabelY = 6;
        inventoryLabelX = AthanorMenu.INVENTORY_X;
        inventoryLabelY = AthanorMenu.INVENTORY_Y - 11;
        buttons.clear();
        int x = leftPos + imageWidth - MachineDraw.MARGIN - BUTTON_WIDTH;
        for (Handicap handicap : Handicap.values()) {
            int id = handicap.ordinal();
            buttons.add(addRenderableWidget(Button.builder(TAKE_ON, button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                    .bounds(x, topPos + rowTop(id), BUTTON_WIDTH, BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(TAKE_ON_TOOLTIP))
                    .build()));
        }
        heart = addRenderableWidget(Button.builder(GIVE_HEART, button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        AthanorMenu.GIVE_HEART))
                .bounds(x, topPos + rowTop(AthanorMenu.GIVE_HEART), BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(GIVE_HEART_TOOLTIP))
                .build());
        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        for (Handicap handicap : Handicap.values()) {
            Button button = buttons.get(handicap.ordinal());
            button.active = menu.canBegin(handicap);
            button.setMessage(menu.remaining(handicap) > 0 ? BEARING : TAKE_ON);
        }
        if (heart != null) {
            heart.active = menu.canGiveHeart();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        MachineDraw.panel(graphics, font, leftPos, topPos, imageWidth, imageHeight, menu.slots, ghosts, Util.getMillis());
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, MachineDraw.TEXT, false);
        int statusWidth = AthanorMenu.OUTPUT_X - 2 * MachineDraw.MARGIN;
        graphics.text(font, MachineDraw.fitted(font, status(), statusWidth), MachineDraw.MARGIN, TOP, MachineDraw.TEXT, false);
        Component held = Component.translatable("mechanicalmasteryreborn.athanor.held", menu.nullessence(), menu.progress() / 10);
        graphics.text(font, MachineDraw.fitted(font, held, statusWidth), MachineDraw.MARGIN, TOP + LINE, MachineDraw.TEXT, false);
        int nameWidth = imageWidth - 3 * MachineDraw.MARGIN - BUTTON_WIDTH;
        for (Handicap handicap : Handicap.values()) {
            int y = rowTop(handicap.ordinal());
            graphics.text(font, MachineDraw.fitted(font, Spells.title(handicap).withStyle(ChatFormatting.BOLD), nameWidth),
                    MachineDraw.MARGIN, y, MachineDraw.TEXT, false);
            graphics.text(font, MachineDraw.fitted(font, line(handicap), nameWidth), MachineDraw.MARGIN, y + COST_Y, MachineDraw.TEXT, false);
        }
        int y = rowTop(AthanorMenu.GIVE_HEART);
        graphics.text(font, MachineDraw.fitted(font, HEART.copy().withStyle(ChatFormatting.BOLD), nameWidth), MachineDraw.MARGIN, y,
                MachineDraw.TEXT, false);
        graphics.text(font, MachineDraw.fitted(font, Component.translatable("mechanicalmasteryreborn.athanor.heart.held", menu.essentia()), nameWidth),
                MachineDraw.MARGIN, y + COST_Y, MachineDraw.TEXT, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MachineDraw.TEXT, false);
    }

    /** What the Athanor is doing, and while it works, how many lines count and what a Quintessence costs. */
    private Component status() {
        AthanorBlockEntity.Phase phase = menu.phase();
        if (phase != AthanorBlockEntity.Phase.WORKING) {
            return Component.translatable("mechanicalmasteryreborn.athanor.phase." + phase.name().toLowerCase(Locale.ROOT));
        }
        int lines = menu.linesCounting();
        return Component.translatable("mechanicalmasteryreborn.athanor.phase.working", lines, AthanorWork.LINES,
                AthanorWork.cost(lines, ServerConfig.athanorCosts()));
    }

    /** A spell's line: what it costs while it runs none, or how long it has left and whether its line counts. */
    private Component line(Handicap handicap) {
        int seconds = menu.remaining(handicap);
        if (seconds <= 0) {
            return Spells.cost(handicap);
        }
        String left = String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
        return Component.translatable(menu.counts(handicap) ? "mechanicalmasteryreborn.athanor.line.fed" : "mechanicalmasteryreborn.athanor.line.unfed",
                left, Challenges.name(handicap));
    }

    /** Where row {@code index} starts: a handicap's by its ordinal, then the heart's. */
    private static int rowTop(int index) {
        return AthanorMenu.ROWS_Y + index * AthanorMenu.ROW;
    }
}
