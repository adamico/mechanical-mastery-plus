// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.InflationBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

/**
 * Draws a team's {@link InflationBar} its own way: the challenge's name on top, the bar below it, and the Inflation
 * line under the bar, each a little apart. Vanilla draws a boss bar's name only above it, so this takes over the
 * drawing of the one bar whose title is the Inflation bar's, and leaves every other boss bar to vanilla.
 */
final class InflationBarOverlay {

    /** Vanilla's bar size, as its boss-bar overlay draws it. */
    private static final int WIDTH = 182;
    private static final int HEIGHT = 5;
    /** The gap between the challenge's name and the bar, and between the bar and the Inflation line. */
    private static final int GAP = 2;
    /** The gap below the Inflation line before the next boss bar's name, as vanilla leaves below a bar. */
    private static final int BELOW = 5;

    private InflationBarOverlay() {
    }

    /** Draws the Inflation bar at the event's place, with its name where vanilla puts it; any other is left to vanilla. */
    static void draw(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (!(event.getBossEvent().getName().getContents() instanceof TranslatableContents title)
                || !title.getKey().equals(InflationBar.TITLE) || title.getArgs().length != 2
                || !(title.getArgs()[0] instanceof Component challenge) || !(title.getArgs()[1] instanceof Component inflation)) {
            return;
        }
        event.setCanceled(true);
        var graphics = event.getGuiGraphics();
        var font = Minecraft.getInstance().font;
        int centre = event.getX() + WIDTH / 2;
        // Vanilla's name sits a line above the bar's place, so the name stays there and the bar moves down by the gap.
        int nameY = event.getY() - font.lineHeight;
        int barY = event.getY() + GAP;
        int inflationY = barY + HEIGHT + GAP;
        graphics.centeredText(font, challenge, centre, nameY, -1);
        BossEvent bar = event.getBossEvent();
        drawBar(graphics, bar, event.getX(), barY, WIDTH, "background");
        int filled = Mth.lerpDiscrete(bar.getProgress(), 0, WIDTH);
        if (filled > 0) {
            drawBar(graphics, bar, event.getX(), barY, filled, "progress");
        }
        graphics.centeredText(font, inflation, centre, inflationY, -1);
        // The next boss bar's place, whose name vanilla draws a line above it.
        event.setIncrement(inflationY + font.lineHeight + BELOW + font.lineHeight - event.getY());
    }

    /** Draws {@code width} of the bar's {@code part}, background or progress, in its colour and notches, as vanilla does. */
    private static void drawBar(GuiGraphicsExtractor graphics, BossEvent bar, int x, int y, int width, String part) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite(bar.getColor().getName(), part), WIDTH, HEIGHT, 0, 0, x, y,
                width, HEIGHT);
        if (bar.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite(bar.getOverlay().getName(), part), WIDTH, HEIGHT, 0, 0, x, y,
                    width, HEIGHT);
        }
    }

    private static Identifier sprite(String look, String part) {
        return Identifier.withDefaultNamespace("boss_bar/" + look + "_" + part);
    }
}
