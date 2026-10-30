// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github._5thlayer.libworks.ghost.Ghosts;
import io.github._5thlayer.libworks.machine.client.MachineDraw;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;

/**
 * The lab: each handicap with what it costs, and a button to choose it, in the suite's stand-in machine look. Only
 * the ones the lab offers can be chosen; the others are shown as coming later, and the one the team plays as played.
 */
final class LabScreen extends AbstractContainerScreen<LabMenu> {

    /** The narrowest the lab is; wider when a handicap's cost needs it. */
    private static final int MIN_WIDTH = 256;
    private static final int TOP = 20;
    private static final int LINE = 9;
    /** From a row's name down to its cost. */
    private static final int COST_Y = 11;
    /** Between the intro and the rows. */
    private static final int GAP = 4;
    private static final int ROW = 26;
    private static final int BUTTON_WIDTH = 72;
    private static final int BUTTON_HEIGHT = 20;
    /** A handicap the lab doesn't offer yet. */
    private static final int GREYED = 0xFF707070;
    private static final Component INTRO = Component.translatable("mechanicalmasteryreborn.lab.intro");
    private static final Component CHOOSE = Component.translatable("mechanicalmasteryreborn.lab.choose");
    private static final Component PLAYED = Component.translatable("mechanicalmasteryreborn.lab.played");
    private static final Component LATER = Component.translatable("mechanicalmasteryreborn.lab.later");
    private static final Component NOT_MEMBER = Component.translatable("mechanicalmasteryreborn.lab.not_member");
    private static final Component NOT_MEMBER_TOOLTIP = Component.translatable("mechanicalmasteryreborn.lab.not_member.tooltip");

    private final Ghosts ghosts = new Ghosts(() -> leftPos, () -> topPos);
    private final List<Button> buttons = new ArrayList<>();
    /** Where the rows start, below the intro however many lines it wraps to. */
    private final int rowsTop;

    LabScreen(LabMenu menu, Inventory inventory, Component title) {
        this(menu, inventory, title, width(Minecraft.getInstance().font));
    }

    private LabScreen(LabMenu menu, Inventory inventory, Component title, int width) {
        this(menu, inventory, title, width, TOP + introLines(Minecraft.getInstance().font, width) * LINE + GAP);
    }

    private LabScreen(LabMenu menu, Inventory inventory, Component title, int width, int rowsTop) {
        super(menu, inventory, title, width, rowsTop + (Handicap.values().length - 1) * ROW + BUTTON_HEIGHT + MachineDraw.MARGIN);
        this.rowsTop = rowsTop;
    }

    /** Wide enough for every handicap's name and cost beside its button. */
    private static int width(Font font) {
        int text = 0;
        for (Handicap handicap : Handicap.values()) {
            text = Math.max(text, font.width(Challenges.name(handicap).withStyle(ChatFormatting.BOLD)));
            text = Math.max(text, font.width(Challenges.cost(handicap)));
        }
        return Math.max(MIN_WIDTH, 3 * MachineDraw.MARGIN + BUTTON_WIDTH + text);
    }

    private static int introLines(Font font, int width) {
        return font.split(INTRO, width - 2 * MachineDraw.MARGIN).size();
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = MachineDraw.MARGIN;
        titleLabelY = 6;
        buttons.clear();
        for (Handicap handicap : Handicap.values()) {
            int id = handicap.ordinal();
            buttons.add(addRenderableWidget(Button.builder(CHOOSE, button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                    .bounds(leftPos + imageWidth - MachineDraw.MARGIN - BUTTON_WIDTH, topPos + rowTop(handicap), BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build()));
        }
        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        Optional<Handicap> chosen = menu.chosen();
        for (Handicap handicap : Handicap.values()) {
            Button button = buttons.get(handicap.ordinal());
            boolean notMember = handicap.choosable() && chosen.isEmpty() && !menu.isMember();
            button.active = menu.canChoose(handicap);
            button.setMessage(chosen.equals(Optional.of(handicap)) ? PLAYED : !handicap.choosable() ? LATER : notMember ? NOT_MEMBER : CHOOSE);
            button.setTooltip(notMember ? Tooltip.create(NOT_MEMBER_TOOLTIP) : null);
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
        int textWidth = imageWidth - 2 * MachineDraw.MARGIN;
        graphics.textWithWordWrap(font, INTRO, MachineDraw.MARGIN, TOP, textWidth, MachineDraw.TEXT, false);
        int nameWidth = imageWidth - 3 * MachineDraw.MARGIN - BUTTON_WIDTH;
        for (Handicap handicap : Handicap.values()) {
            int y = rowTop(handicap);
            int color = handicap.choosable() ? MachineDraw.TEXT : GREYED;
            graphics.text(font, MachineDraw.fitted(font, Challenges.name(handicap).withStyle(ChatFormatting.BOLD), nameWidth),
                    MachineDraw.MARGIN, y, color, false);
            graphics.text(font, MachineDraw.fitted(font, Challenges.cost(handicap), nameWidth), MachineDraw.MARGIN, y + COST_Y, color, false);
        }
    }

    /** Where {@code handicap}'s row starts, below the intro. */
    private int rowTop(Handicap handicap) {
        return rowsTop + handicap.ordinal() * ROW;
    }
}
