// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.Direction;
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
 * at spawn, or offline) its gates close, and when one comes back each opens again at the wave it had reached, where it stood. Mobs already
 * out stay, and overproduction goes on. A gate closed in a chunk no one was near is kept out when it loads, its wave noted.
 * Closing for absence is not an Assault's removal: a shut gate's mobs aren't kept out, and a shut gate is no longer one of
 * its Assault's gates, so it is never reopened as a failed one ({@link Assault#reopen}).
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
        List<AssaultState.Waiting> waiting = new ArrayList<>();
        for (UUID gate : built.gates()) {
            Entity entity = level.getEntity(gate);
            if (entity != null) {
                waiting.add(new AssaultState.Waiting(gate, Optional.of(entity.position()), Gates.get().wave(entity)));
                entity.discard();
            } else {
                waiting.add(new AssaultState.Waiting(gate, Optional.empty(), 0));
            }
        }
        state.pause(team, waiting);
        LOGGER.info("No one of team {} is home: its Assault's gates close: {}", team, waiting);
    }

    /**
     * Opens {@code team}'s paused Assault's gates again, each where it stood and at the wave it had reached; a gate never
     * seen where it stood opens on a side no other one stands on.
     */
    private static void reopen(MinecraftServer server, AssaultState state, ServerLevel level, UUID team) {
        Optional<GlobalPos> cod = state.cod(team);
        List<AssaultState.Waiting> waiting = state.paused(team).orElse(List.of());
        if (cod.isEmpty()) {
            return;
        }
        List<Vec3> free = new ArrayList<>();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            Vec3 at = Assault.gateAt(cod.get().pos(), side);
            if (waiting.stream().noneMatch(gate -> gate.at().filter(seen -> seen.distanceToSqr(at) < 1.0).isPresent())) {
                free.add(at);
            }
        }
        ServerPlayer summoner = Assault.summoner(server, level, team);
        List<UUID> gates = new ArrayList<>();
        for (AssaultState.Waiting gate : waiting) {
            Vec3 at = Assault.gateAt(state, team)
                    .orElseGet(() -> gate.at().orElseGet(() -> free.isEmpty() ? Assault.gateAt(cod.get().pos(), Direction.EAST) : free.remove(0)));
            Gates.get().open(level, at, summoner, gate.wave()).ifPresent(gates::add);
        }
        state.unpause(team, gates);
        LOGGER.info("Team {} is home: its Assault's gates open again: {}", team, gates);
    }

    /**
     * Whether {@code entity} is a gate shut for no one being home, loading with its chunk: then it is kept out, and its
     * wave noted if its team is still away.
     */
    static boolean keptOut(AssaultState state, Entity entity) {
        if (state.shut(entity.getUUID()).isEmpty()) {
            return false;
        }
        state.noteWaiting(entity.getUUID(), entity.position(), Gates.get().wave(entity));
        return true;
    }
}
