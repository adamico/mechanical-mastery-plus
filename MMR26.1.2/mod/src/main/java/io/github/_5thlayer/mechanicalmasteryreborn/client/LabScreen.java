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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;

/**
 * The lab: each handicap with what it costs, and a button to choose it, in the suite's stand-in machine look. Only
 * the ones the lab offers can be chosen; the others are shown as coming later, and the one the team plays as played.
 */
final class LabScreen extends AbstractContainerScreen<LabMenu> {

    private static final int WIDTH = 256;
    private static final int TOP = 20;
    /** The intro's two lines, above the rows. */
    private static final int INTRO_HEIGHT = 22;
    private static final int ROW = 26;
    private static final int BUTTON_WIDTH = 72;
    private static final int BUTTON_HEIGHT = 20;
    private static final int HEIGHT = TOP + INTRO_HEIGHT + Handicap.values().length * ROW + 24;
    /** A handicap the lab doesn't offer yet. */
    private static final int GREYED = 0xFF707070;
    private static final int WARNING = 0xFF8B0000;
    private static final Component INTRO = Component.translatable("mechanicalmasteryreborn.lab.intro");
    private static final Component CHOOSE = Component.translatable("mechanicalmasteryreborn.lab.choose");
    private static final Component PLAYED = Component.translatable("mechanicalmasteryreborn.lab.played");
    private static final Component LATER = Component.translatable("mechanicalmasteryreborn.lab.later");
    private static final Component NOT_MEMBER = Component.translatable("mechanicalmasteryreborn.lab.not_member");

    private final Ghosts ghosts = new Ghosts(() -> leftPos, () -> topPos);
    private final List<Button> buttons = new ArrayList<>();

    LabScreen(LabMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
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
                    .bounds(leftPos + imageWidth - MachineDraw.MARGIN - BUTTON_WIDTH, topPos + rowTop(handicap) + 1, BUTTON_WIDTH, BUTTON_HEIGHT)
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
            button.active = menu.canChoose(handicap);
            button.setMessage(chosen.equals(Optional.of(handicap)) ? PLAYED : handicap.choosable() ? CHOOSE : LATER);
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
                    MachineDraw.MARGIN, y + 1, color, false);
            graphics.text(font, MachineDraw.fitted(font, Challenges.cost(handicap), nameWidth), MachineDraw.MARGIN, y + 12, color, false);
        }
        if (!menu.isMember()) {
            graphics.textWithWordWrap(font, NOT_MEMBER, MachineDraw.MARGIN, rowTop(Handicap.values()[0]) + Handicap.values().length * ROW + 2,
                    textWidth, WARNING, false);
        }
    }

    /** Where {@code handicap}'s row starts, below the intro. */
    private static int rowTop(Handicap handicap) {
        return TOP + INTRO_HEIGHT + handicap.ordinal() * ROW;
    }
}
