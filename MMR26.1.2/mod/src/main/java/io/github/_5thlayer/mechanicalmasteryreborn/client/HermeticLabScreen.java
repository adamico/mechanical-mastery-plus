// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github._5thlayer.libworks.ghost.Ghosts;
import io.github._5thlayer.libworks.machine.client.MachineDraw;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Spells;
import io.github._5thlayer.mechanicalmasteryreborn.lab.HermeticLabMenu;
import io.github._5thlayer.mechanicalmasteryreborn.lab.HermeticLabWork;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabBlockEntity;
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
 * The Hermetic Lab (#263): what it is doing, the Nullessence it holds and how far the next Quintessence is, with its one
 * Quintessence slot beside; then each handicap as a spell, with what it costs or how long it has left and whether its line
 * counts, and a button to take it on; then a row to give a heart, with the Essentia Vitae it holds; then the team's
 * refund, with how much of it waits and a button to open it, which an Assault of a protection spell fills; in the suite's
 * stand-in machine look.
 */
final class HermeticLabScreen extends AbstractContainerScreen<HermeticLabMenu> {

    private static final int TOP = 20;
    private static final int LINE = 10;
    private static final int COST_Y = 10;
    private static final int BUTTON_WIDTH = 72;
    private static final int BUTTON_HEIGHT = 20;
    private static final Component TAKE_ON = Component.translatable("mechanicalmasteryreborn.hermetic_lab.take_on");
    private static final Component BEARING = Component.translatable("mechanicalmasteryreborn.hermetic_lab.bearing");
    private static final Component TAKE_ON_TOOLTIP = Component.translatable("mechanicalmasteryreborn.hermetic_lab.take_on.tooltip");
    private static final Component HEART = Component.translatable("mechanicalmasteryreborn.hermetic_lab.heart");
    private static final Component GIVE_HEART = Component.translatable("mechanicalmasteryreborn.hermetic_lab.heart.give");
    private static final Component GIVE_HEART_TOOLTIP = Component.translatable("mechanicalmasteryreborn.hermetic_lab.heart.tooltip");
    private static final Component REFUND = Component.translatable("mechanicalmasteryreborn.lab.refund");
    private static final Component REFUND_OPEN = Component.translatable("mechanicalmasteryreborn.lab.refund.open");
    private static final Component REFUND_TOOLTIP = Component.translatable("mechanicalmasteryreborn.lab.refund.tooltip");

    private final Ghosts ghosts = new Ghosts(() -> leftPos, () -> topPos);
    private final List<Button> buttons = new ArrayList<>();
    private @Nullable Button heart;
    private @Nullable Button refund;

    HermeticLabScreen(HermeticLabMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, HermeticLabMenu.WIDTH, HermeticLabMenu.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = MachineDraw.MARGIN;
        titleLabelY = 6;
        inventoryLabelX = HermeticLabMenu.INVENTORY_X;
        inventoryLabelY = HermeticLabMenu.INVENTORY_Y - 11;
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
                        HermeticLabMenu.GIVE_HEART))
                .bounds(x, topPos + rowTop(HermeticLabMenu.GIVE_HEART), BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(GIVE_HEART_TOOLTIP))
                .build());
        refund = addRenderableWidget(Button.builder(REFUND_OPEN, button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        HermeticLabMenu.OPEN_REFUND))
                .bounds(x, topPos + rowTop(HermeticLabMenu.OPEN_REFUND), BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(REFUND_TOOLTIP))
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
        if (refund != null) {
            refund.active = menu.refund() > 0;
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
        int statusWidth = HermeticLabMenu.OUTPUT_X - 2 * MachineDraw.MARGIN;
        graphics.text(font, MachineDraw.fitted(font, status(), statusWidth), MachineDraw.MARGIN, TOP, MachineDraw.TEXT, false);
        Component held = Component.translatable("mechanicalmasteryreborn.hermetic_lab.held", menu.nullessence(), menu.progress() / 10);
        graphics.text(font, MachineDraw.fitted(font, held, statusWidth), MachineDraw.MARGIN, TOP + LINE, MachineDraw.TEXT, false);
        int nameWidth = imageWidth - 3 * MachineDraw.MARGIN - BUTTON_WIDTH;
        for (Handicap handicap : Handicap.values()) {
            int y = rowTop(handicap.ordinal());
            graphics.text(font, MachineDraw.fitted(font, Spells.title(handicap).withStyle(ChatFormatting.BOLD), nameWidth),
                    MachineDraw.MARGIN, y, MachineDraw.TEXT, false);
            graphics.text(font, MachineDraw.fitted(font, line(handicap), nameWidth), MachineDraw.MARGIN, y + COST_Y, MachineDraw.TEXT, false);
        }
        int y = rowTop(HermeticLabMenu.GIVE_HEART);
        graphics.text(font, MachineDraw.fitted(font, HEART.copy().withStyle(ChatFormatting.BOLD), nameWidth), MachineDraw.MARGIN, y,
                MachineDraw.TEXT, false);
        graphics.text(font, MachineDraw.fitted(font, Component.translatable("mechanicalmasteryreborn.hermetic_lab.heart.held", menu.essentia()), nameWidth),
                MachineDraw.MARGIN, y + COST_Y, MachineDraw.TEXT, false);
        int refundY = rowTop(HermeticLabMenu.OPEN_REFUND);
        graphics.text(font, MachineDraw.fitted(font, REFUND.copy().withStyle(ChatFormatting.BOLD), nameWidth), MachineDraw.MARGIN, refundY,
                MachineDraw.TEXT, false);
        graphics.text(font, MachineDraw.fitted(font, Component.translatable("mechanicalmasteryreborn.lab.refund.count", menu.refund()), nameWidth),
                MachineDraw.MARGIN, refundY + COST_Y, MachineDraw.TEXT, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MachineDraw.TEXT, false);
    }

    /** What the Hermetic Lab is doing, and while it works, how many lines count and what a Quintessence costs. */
    private Component status() {
        LabBlockEntity.Phase phase = menu.phase();
        if (phase != LabBlockEntity.Phase.WORKING) {
            return Component.translatable("mechanicalmasteryreborn.hermetic_lab.phase." + phase.name().toLowerCase(Locale.ROOT));
        }
        int lines = menu.linesCounting();
        return Component.translatable("mechanicalmasteryreborn.hermetic_lab.phase.working", lines, HermeticLabWork.LINES,
                HermeticLabWork.cost(lines, ServerConfig.hermeticLabCosts()));
    }

    /** A spell's line: what it costs while it runs none, or how long it has left and whether its line counts. */
    private Component line(Handicap handicap) {
        int seconds = menu.remaining(handicap);
        if (seconds <= 0) {
            return Spells.cost(handicap);
        }
        String left = String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
        return Component.translatable(menu.counts(handicap) ? "mechanicalmasteryreborn.hermetic_lab.line.fed" : "mechanicalmasteryreborn.hermetic_lab.line.unfed",
                left, Challenges.name(handicap));
    }

    /** Where row {@code index} starts: a handicap's by its ordinal, then the heart's, then the refund's. */
    private static int rowTop(int index) {
        return HermeticLabMenu.ROWS_Y + index * HermeticLabMenu.ROW;
    }
}
