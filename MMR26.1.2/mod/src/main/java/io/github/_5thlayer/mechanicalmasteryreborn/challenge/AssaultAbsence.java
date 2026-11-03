// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/**
 * The Assault waits for someone at home (#240): its mobs hunt the team's players, and FTB Chunks keeps the island
 * loaded while any member is online anywhere, so while no member of the team is on its island (in the Nether, the End,
 * at spawn, or offline) its gate closes, and when one comes back it opens again at the wave it had reached. Mobs already
 * out stay, and inflation goes on. A gate closed in a chunk no one was near is kept out when it loads, its wave noted.
 * Closing for absence is not an Assault's removal: a shut gate's mobs aren't kept out.
 */
public final class AssaultAbsence {

    /** How often, in ticks, the Assault looks for someone at home. */
    public static final int INTERVAL = 20;

    private static final Logger LOGGER = LogUtils.getLogger();

    private AssaultAbsence() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> {
            if (event.getServer().getTickCount() % INTERVAL == 0) {
                update(event.getServer());
            }
        });
    }

    /** Closes or reopens each team's Assault gate by whether one of its players is on its island. */
    public static void update(MinecraftServer server) {
        AssaultState state = AssaultState.get(server);
        for (Map.Entry<UUID, AssaultState.Built> entry : Map.copyOf(state.assaults()).entrySet()) {
            UUID team = entry.getKey();
            ServerLevel level = server.getLevel(entry.getValue().dimension());
            if (level == null) {
                continue;
            }
            boolean home = home(server, level, team);
            boolean paused = state.paused(team).isPresent();
            if (!home && !paused) {
                close(state, level, team, entry.getValue());
            } else if (home && paused) {
                reopen(server, state, level, team);
            }
        }
    }

    /** Whether one of {@code team}'s players is on its island in {@code level}. */
    static boolean home(MinecraftServer server, ServerLevel level, UUID team) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() == level && Teams.of(player).equals(team) && Teams.onIsland(level, player.blockPosition())
                    && Teams.at(level, player.blockPosition()).equals(team)) {
                return true;
            }
        }
        return false;
    }

    private static void close(AssaultState state, ServerLevel level, UUID team, AssaultState.Built built) {
        Optional<UUID> gate = built.gate();
        int wave = 0;
        if (gate.isPresent()) {
            Entity entity = level.getEntity(gate.get());
            if (entity != null) {
                wave = Gates.get().wave(entity);
                entity.discard();
            }
        }
        state.pause(team, gate, wave);
        LOGGER.info("No one of team {} is home: its Assault's gate closes at wave {}", team, wave);
    }

    /** Opens {@code team}'s paused Assault's gate again, at the wave it had reached. */
    private static void reopen(MinecraftServer server, AssaultState state, ServerLevel level, UUID team) {
        Optional<GlobalPos> cod = state.cod(team);
        if (cod.isEmpty()) {
            return;
        }
        int wave = state.paused(team).orElse(0);
        Vec3 at = Vec3.atBottomCenterOf(cod.get().pos().offset(Assault.OFFSET, 0, 0));
        Optional<UUID> gate = Gates.get().open(level, at, Assault.summoner(server, level, team), wave);
        state.unpause(team, gate);
        LOGGER.info("Team {} is home: its Assault's gate opens again at wave {}", team, wave);
    }

    /**
     * Whether {@code entity} is a gate shut for no one being home, loading with its chunk: then it is kept out, and its
     * wave noted if its team is still away.
     */
    static boolean keptOut(AssaultState state, Entity entity) {
        Optional<UUID> team = state.shut(entity.getUUID());
        team.filter(t -> state.paused(t).isPresent())
                .ifPresent(t -> state.pause(t, Optional.of(entity.getUUID()), Gates.get().wave(entity)));
        return team.isPresent();
    }
}
