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
 * and a player who logs in or joins the team mid-challenge sees it too. The client draws the challenge's name above
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

    /** The bar {@code team}'s players see, while it plays a challenge. */
    public static Optional<ServerBossEvent> of(UUID team) {
        return Optional.ofNullable(BARS.get(team));
    }

    private static void update(MinecraftServer server) {
        TeamState state = TeamState.get(server);
        BARS.entrySet().removeIf(entry -> {
            Optional<Inflation> inflation = state.inflation(entry.getKey());
            if (inflation.isEmpty()) {
                entry.getValue().removeAllPlayers();
                return true;
            }
            show(entry.getValue(), state.handicap(entry.getKey()).orElseThrow(), inflation.get());
            for (ServerPlayer player : new ArrayList<>(entry.getValue().getPlayers())) {
                if (player.hasDisconnected() || !Teams.of(player).equals(entry.getKey())) {
                    entry.getValue().removePlayer(player);
                }
            }
            return false;
        });
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID team = Teams.of(player);
            state.inflation(team).ifPresent(inflation -> BARS.computeIfAbsent(team, ignored -> {
                var bar = new ServerBossEvent(UUID.randomUUID(), Component.empty(), BossEvent.BossBarColor.RED,
                        BossEvent.BossBarOverlay.NOTCHED_10);
                show(bar, state.handicap(team).orElseThrow(), inflation);
                return bar;
            }).addPlayer(player));
        }
    }

    /** Words the bar, such as "Energy Challenge - Inflation: 12 of 256", and fills it. */
    private static void show(ServerBossEvent bar, Handicap handicap, Inflation inflation) {
        int cap = ServerConfig.inflationCap();
        bar.setName(Component.translatable(TITLE, Challenges.title(handicap),
                Component.translatable(MechanicalMasteryReborn.MOD_ID + ".bar.inflation", inflation.excess(), cap)));
        bar.setProgress(Math.min(1.0F, (float) inflation.excess() / cap));
    }
}
