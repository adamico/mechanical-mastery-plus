// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.Inflation;
import io.github._5thlayer.mechanicalmasteryreborn.record.LastStand;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Each team's Inflation as a boss bar, filling toward the {@link ServerConfig#inflationCap cap}, titled with the
 * challenge the team plays, as its start's title names it, then the excess and the cap. Every one of the team's
 * players online sees it, wherever they stand, from the moment the team chooses a handicap until its challenge ends,
 * and a player who logs in or joins the team mid-challenge sees it too. In the team's last stand the same bar shows its
 * Lead instead, from minus the margin, empty, to the margin, full, and C.O.D.'s rate, until the pack is won. The client draws the challenge's name above
 * the bar and the Inflation line below it; its title joins the two, for anything else that shows it.
 * Kept in step with the team's state each tick, so nothing that changes Inflation has to tell it; a bar sends its
 * players anything only when it changes. Never saved: the server makes them afresh from the teams' state.
 */
public final class InflationBar {

    /** The key of every Inflation bar's title, by which the client knows to draw it its own way. */
    public static final String TITLE = MechanicalMasteryReborn.MOD_ID + ".bar.challenge";

    private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();

    private InflationBar() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> update(event.getServer()));
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> BARS.clear());
    }

    /** The bar {@code team}'s players see, while it plays a challenge or its last stand runs. */
    public static Optional<ServerBossEvent> of(UUID team) {
        return Optional.ofNullable(BARS.get(team));
    }

    private static void update(MinecraftServer server) {
        TeamState state = TeamState.get(server);
        BARS.entrySet().removeIf(entry -> {
            if (!shown(state, entry.getKey())) {
                entry.getValue().removeAllPlayers();
                return true;
            }
            show(server, state, entry.getKey(), entry.getValue());
            for (ServerPlayer player : new ArrayList<>(entry.getValue().getPlayers())) {
                if (player.hasDisconnected() || !Teams.of(player).equals(entry.getKey())) {
                    entry.getValue().removePlayer(player);
                }
            }
            return false;
        });
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID team = Teams.of(player);
            if (shown(state, team)) {
                BARS.computeIfAbsent(team, ignored -> {
                    var bar = new ServerBossEvent(UUID.randomUUID(), Component.empty(), BossEvent.BossBarColor.RED,
                            BossEvent.BossBarOverlay.NOTCHED_10);
                    show(server, state, team, bar);
                    return bar;
                }).addPlayer(player);
            }
        }
    }

    /** Whether {@code team} has a bar: while it plays a challenge or its last stand runs. */
    private static boolean shown(TeamState state, UUID team) {
        return state.inflation(team).isPresent() || state.inLastStand(team);
    }

    /** Words and fills {@code team}'s bar, by its challenge's Inflation or its last stand's Lead. */
    private static void show(MinecraftServer server, TeamState state, UUID team, ServerBossEvent bar) {
        Optional<Inflation> inflation = state.inflation(team);
        if (inflation.isPresent()) {
            show(server, team, bar, state.handicap(team).orElseThrow(), inflation.get());
        } else {
            show(bar, state.lastStand(team).orElseThrow());
        }
    }

    /**
     * Words the bar, such as "Last Stand - Lead: -12 of 1024 · C.O.D.: 16.0 a minute", and fills it from minus the margin,
     * empty, to the margin, full, so a level Lead is half full.
     */
    private static void show(ServerBossEvent bar, LastStand stand) {
        int margin = ServerConfig.lastStandMargin();
        bar.setColor(BossEvent.BossBarColor.PURPLE);
        bar.setName(Component.translatable(TITLE, Component.translatable(MechanicalMasteryReborn.MOD_ID + ".last_stand"), LastStands.lead(stand, margin)));
        bar.setProgress(Math.clamp((float) (stand.lead() + margin) / (2 * margin), 0.0F, 1.0F));
    }


    /**
     * Words the bar, such as "Energy Challenge - Inflation: 12 of 256", and fills it. While the team's Assault runs, the
     * challenge's name carries its wave and side, such as "Protection Challenge · Wave 7, from the north" (#239).
     */
    private static void show(MinecraftServer server, UUID team, ServerBossEvent bar, Handicap handicap, Inflation inflation) {
        int cap = ServerConfig.inflationCap();
        Component challenge = Challenges.title(handicap);
        Optional<AssaultState.Built> assault = AssaultState.get(server).assault(team);
        if (assault.isPresent() && assault.get().side().isPresent()) {
            challenge = Component.translatable(MechanicalMasteryReborn.MOD_ID + ".bar.assault", challenge,
                    Math.max(1, assault.get().wave()), Assault.sideName(assault.get().side().get()));
        }
        bar.setName(Component.translatable(TITLE, challenge,
                Component.translatable(MechanicalMasteryReborn.MOD_ID + ".bar.inflation", inflation.excess(), cap)));
        bar.setProgress(Math.min(1.0F, (float) inflation.excess() / cap));
    }
}
