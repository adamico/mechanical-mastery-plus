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
import org.jspecify.annotations.Nullable;

/**
 * The lab: each handicap with what it costs, and a button to choose it, in the suite's stand-in machine look. Only
 * the lab's team can choose, one at a time; the one it plays is shown as played, and the ones it has won as won. Below
 * them, while a stop is offered, once the challenge played has reset, a row stops it: a member asks for the stop or takes
 * their ask back, and the row names who online has asked and who is left (ADR 0013, #271). Then, while something of the
 * team's refund waits, a row says how much and opens it (#259, #269). Last, once the team has won all four challenges, a
 * row starts its last stand (ADR 0014). A row that doesn't show takes no space, the rows below it move up, and a row
 * showing or going while the lab is open grows or shrinks it at once.
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
    private static final Component INTRO = Component.translatable("mechanicalmasteryreborn.lab.intro");
    private static final Component CHOOSE = Component.translatable("mechanicalmasteryreborn.lab.choose");
    private static final Component PLAYED = Component.translatable("mechanicalmasteryreborn.lab.played");
    private static final Component WON = Component.translatable("mechanicalmasteryreborn.lab.won");
    private static final Component NOT_MEMBER = Component.translatable("mechanicalmasteryreborn.lab.not_member");
    private static final Component NOT_MEMBER_TOOLTIP = Component.translatable("mechanicalmasteryreborn.lab.not_member.tooltip");
    private static final Component ASK = Component.translatable("mechanicalmasteryreborn.lab.stop.ask");
    private static final Component TAKE_BACK = Component.translatable("mechanicalmasteryreborn.lab.stop.take_back");
    private static final Component STOP_TOOLTIP = Component.translatable("mechanicalmasteryreborn.lab.stop.tooltip");
    private static final Component NOBODY = Component.translatable("mechanicalmasteryreborn.lab.stop.nobody");
    private static final Component REFUND = Component.translatable("mechanicalmasteryreborn.lab.refund");
    private static final Component REFUND_OPEN = Component.translatable("mechanicalmasteryreborn.lab.refund.open");
    private static final Component REFUND_TOOLTIP = Component.translatable("mechanicalmasteryreborn.lab.refund.tooltip");
    private static final Component LAST_STAND = Component.translatable("mechanicalmasteryreborn.last_stand");
    private static final Component LAST_STAND_COST = Component.translatable("mechanicalmasteryreborn.lab.last_stand.cost");
    private static final Component LAST_STAND_START = Component.translatable("mechanicalmasteryreborn.lab.last_stand.start");
    private static final Component LAST_STAND_RUNNING = Component.translatable("mechanicalmasteryreborn.lab.last_stand.running");
    private static final Component LAST_STAND_TOOLTIP = Component.translatable("mechanicalmasteryreborn.lab.last_stand.tooltip");

    private final Ghosts ghosts = new Ghosts(() -> leftPos, () -> topPos);
    private final List<Button> buttons = new ArrayList<>();
    /** None while no stop is offered. */
    private @Nullable Button stop;
    /** None while the team's refund holds nothing. */
    private @Nullable Button refund;
    /** None until the team has won all four challenges. */
    private @Nullable Button lastStand;
    /** Where the rows start, below the intro however many lines it wraps to. */
    private final int rowsTop;
    /** Whether this screen has the stop's, the refund's and the last stand's rows, which its height is set by. */
    private final boolean showsStop;
    private final boolean showsRefund;
    private final boolean showsLastStand;

    LabScreen(LabMenu menu, Inventory inventory, Component title) {
        this(menu, inventory, title, width(Minecraft.getInstance().font, menu.showsLastStand()));
    }

    private LabScreen(LabMenu menu, Inventory inventory, Component title, int width) {
        this(menu, inventory, title, width, TOP + introLines(Minecraft.getInstance().font, width) * LINE + GAP);
    }

    private LabScreen(LabMenu menu, Inventory inventory, Component title, int width, int rowsTop) {
        // A row per handicap, and below them the stop's, the refund's and the last stand's, each only while it shows.
        super(menu, inventory, title, width, rowsTop + (rows(menu) - 1) * ROW + BUTTON_HEIGHT + MachineDraw.MARGIN);
        this.rowsTop = rowsTop;
        this.showsStop = menu.showsStop();
        this.showsRefund = menu.showsRefund();
        this.showsLastStand = menu.showsLastStand();
    }

    /** How many rows the lab shows: one per handicap and one per optional row that shows. */
    private static int rows(LabMenu menu) {
        return Handicap.values().length + (menu.showsStop() ? 1 : 0) + (menu.showsRefund() ? 1 : 0) + (menu.showsLastStand() ? 1 : 0);
    }

    /** Wide enough for every handicap's name and cost beside its button, and the last stand's line when {@code withLastStand}. */
    private static int width(Font font, boolean withLastStand) {
        int text = 0;
        for (Handicap handicap : Handicap.values()) {
            text = Math.max(text, font.width(Challenges.name(handicap).withStyle(ChatFormatting.BOLD)));
            text = Math.max(text, font.width(Challenges.cost(handicap)));
        }
        if (withLastStand) {
            text = Math.max(text, font.width(LAST_STAND_COST));
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
        stop = !showsStop ? null : addRenderableWidget(Button.builder(ASK, button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        menu.asked() ? LabMenu.TAKE_BACK : LabMenu.ASK))
                .bounds(leftPos + imageWidth - MachineDraw.MARGIN - BUTTON_WIDTH, topPos + stopTop(), BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(STOP_TOOLTIP))
                .build());
        refund = !showsRefund ? null : addRenderableWidget(Button.builder(REFUND_OPEN, button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        LabMenu.OPEN_REFUND))
                .bounds(leftPos + imageWidth - MachineDraw.MARGIN - BUTTON_WIDTH, topPos + refundTop(), BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(REFUND_TOOLTIP))
                .build());
        lastStand = !showsLastStand ? null : addRenderableWidget(Button.builder(LAST_STAND_START,
                        button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, LabMenu.START_LAST_STAND))
                .bounds(leftPos + imageWidth - MachineDraw.MARGIN - BUTTON_WIDTH, topPos + lastStandTop(), BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(LAST_STAND_TOOLTIP))
                .build());
        updateButtons();
    }

    /** A screen's size is fixed, so once a row is to show or go, a screen with the rows that show takes this one's place over the same menu. */
    @Override
    protected void containerTick() {
        super.containerTick();
        if (menu.showsStop() != showsStop || menu.showsRefund() != showsRefund || menu.showsLastStand() != showsLastStand) {
            minecraft.setScreen(new LabScreen(menu, minecraft.player.getInventory(), title));
            return;
        }
        updateButtons();
    }

    private void updateButtons() {
        Optional<Handicap> chosen = menu.chosen();
        for (Handicap handicap : Handicap.values()) {
            Button button = buttons.get(handicap.ordinal());
            boolean won = menu.won(handicap);
            boolean notMember = chosen.isEmpty() && !won && !menu.isMember();
            button.active = menu.canChoose(handicap);
            button.setMessage(chosen.equals(Optional.of(handicap)) ? PLAYED : won ? WON : notMember ? NOT_MEMBER : CHOOSE);
            button.setTooltip(notMember ? Tooltip.create(NOT_MEMBER_TOOLTIP) : null);
        }
        if (stop != null) {
            stop.active = menu.isMember();
            stop.setMessage(menu.asked() ? TAKE_BACK : ASK);
        }
        if (lastStand == null) {
            return;
        }
        LabMenu.LastStandPhase phase = menu.lastStand();
        lastStand.active = menu.canStartLastStand();
        lastStand.setMessage(switch (phase) {
            case RUNNING -> LAST_STAND_RUNNING;
            case WON -> WON;
            default -> menu.isMember() ? LAST_STAND_START : NOT_MEMBER;
        });
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
            graphics.text(font, MachineDraw.fitted(font, Challenges.name(handicap).withStyle(ChatFormatting.BOLD), nameWidth),
                    MachineDraw.MARGIN, y, MachineDraw.TEXT, false);
            graphics.text(font, MachineDraw.fitted(font, Challenges.cost(handicap), nameWidth), MachineDraw.MARGIN, y + COST_Y, MachineDraw.TEXT, false);
        }
        Optional<Handicap> chosen = menu.chosen();
        if (showsStop && chosen.isPresent()) {
            int y = stopTop();
            Component name = Component.translatable("mechanicalmasteryreborn.lab.stop", Challenges.title(chosen.get())).withStyle(ChatFormatting.BOLD);
            graphics.text(font, MachineDraw.fitted(font, name, nameWidth), MachineDraw.MARGIN, y, MachineDraw.TEXT, false);
            Component line = Component.translatable("mechanicalmasteryreborn.lab.stop.asks", names(menu.asks().asked()), names(menu.asks().left()));
            graphics.text(font, MachineDraw.fitted(font, line, nameWidth), MachineDraw.MARGIN, y + COST_Y, MachineDraw.TEXT, false);
        }
        if (showsRefund) {
            int refundY = refundTop();
            graphics.text(font, MachineDraw.fitted(font, REFUND.copy().withStyle(ChatFormatting.BOLD), nameWidth), MachineDraw.MARGIN, refundY,
                    MachineDraw.TEXT, false);
            Component waiting = Component.translatable("mechanicalmasteryreborn.lab.refund.count", menu.refund());
            graphics.text(font, MachineDraw.fitted(font, waiting, nameWidth), MachineDraw.MARGIN, refundY + COST_Y, MachineDraw.TEXT, false);
        }
        if (showsLastStand) {
            int lastStandY = lastStandTop();
            graphics.text(font, MachineDraw.fitted(font, LAST_STAND.copy().withStyle(ChatFormatting.BOLD), nameWidth), MachineDraw.MARGIN,
                    lastStandY, MachineDraw.TEXT, false);
            graphics.text(font, MachineDraw.fitted(font, LAST_STAND_COST, nameWidth), MachineDraw.MARGIN, lastStandY + COST_Y,
                    MachineDraw.TEXT, false);
        }
    }

    private static Component names(List<String> names) {
        return names.isEmpty() ? NOBODY : Component.literal(String.join(", ", names));
    }

    /** Where the stop's row starts, below the handicaps'. */
    private int stopTop() {
        return rowsTop + Handicap.values().length * ROW;
    }

    /** Where the refund's row starts, below the stop's if it shows. */
    private int refundTop() {
        return stopTop() + (showsStop ? ROW : 0);
    }

    /** Where the last stand's row starts, below the refund's if it shows. */
    private int lastStandTop() {
        return refundTop() + (showsRefund ? ROW : 0);
    }

    /** Where {@code handicap}'s row starts, below the intro. */
    private int rowTop(Handicap handicap) {
        return rowsTop + handicap.ordinal() * ROW;
    }
}
