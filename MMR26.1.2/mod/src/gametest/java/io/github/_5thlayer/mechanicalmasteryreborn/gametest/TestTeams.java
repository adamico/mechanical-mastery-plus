// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Skyblock Builder's part, for the game tests: a test claims a team of its own, whose island is the test's
 * structure, or a part of it, and can put a player in it. A block on several islands is on the smallest.
 * Anything else is the installed lookup's, so the tests that run together never share a team.
 */
final class TestTeams implements Teams.Lookup {

    private static final Map<AABB, UUID> ISLANDS = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> PLAYERS = new ConcurrentHashMap<>();

    private final Teams.Lookup installed;

    TestTeams(Teams.Lookup installed) {
        this.installed = installed;
    }

    /** A new team, whose island is the test's structure. */
    static UUID claim(GameTestHelper helper) {
        UUID team = UUID.randomUUID();
        ISLANDS.put(helper.getBounds(), team);
        return team;
    }

    /** A new team, whose island is the part of the test's structure from {@code from} to {@code to}, inclusive. */
    static UUID claim(GameTestHelper helper, BlockPos from, BlockPos to) {
        UUID team = UUID.randomUUID();
        ISLANDS.put(AABB.encapsulatingFullBlocks(helper.absolutePos(from), helper.absolutePos(to)), team);
        return team;
    }

    static void join(Player player, UUID team) {
        PLAYERS.put(player.getUUID(), team);
    }

    @Override
    public UUID at(ServerLevel level, BlockPos pos) {
        UUID smallest = null;
        double smallestSize = Double.MAX_VALUE;
        for (var island : ISLANDS.entrySet()) {
            AABB box = island.getKey();
            double size = box.getXsize() * box.getYsize() * box.getZsize();
            if (box.contains(pos.getCenter()) && size < smallestSize) {
                smallest = island.getValue();
                smallestSize = size;
            }
        }
        return smallest != null ? smallest : installed.at(level, pos);
    }

    @Override
    public UUID of(Player player) {
        UUID team = PLAYERS.get(player.getUUID());
        return team != null ? team : installed.of(player);
    }
}
