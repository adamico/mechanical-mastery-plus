// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import net.minecraft.world.entity.player.Player;

/** Chapters' part, for the game tests: each player's stages, in memory, since a mock player is no ServerPlayer. */
final class TestStages implements Chains.Stages {

    private static final Map<UUID, Set<String>> STAGES = new ConcurrentHashMap<>();

    static boolean holds(Player player, String stage) {
        return STAGES.getOrDefault(player.getUUID(), Set.of()).contains(stage);
    }

    @Override
    public void grant(Player player, String stage) {
        STAGES.computeIfAbsent(player.getUUID(), id -> ConcurrentHashMap.newKeySet()).add(stage);
    }

    @Override
    public void revoke(Player player, String stage) {
        Set<String> held = STAGES.get(player.getUUID());
        if (held != null) {
            held.remove(stage);
        }
    }
}
