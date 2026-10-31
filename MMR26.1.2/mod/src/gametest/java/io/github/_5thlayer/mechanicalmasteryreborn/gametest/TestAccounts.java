// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Accounts;
import net.minecraft.server.MinecraftServer;

/**
 * AdminShop's part, for the game tests: an Account per team, in memory, since AdminShop keeps Accounts only for real
 * FTB Teams, which a mock player has none of. A team's Account has the team's own id, so a sale names the team.
 */
final class TestAccounts implements Accounts {

    private static final Map<UUID, Long> BALANCES = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<String>> PERMITS = new ConcurrentHashMap<>();

    /** Gives {@code team} an Account holding {@code balance}. */
    static void open(UUID team, long balance) {
        BALANCES.put(team, balance);
        PERMITS.put(team, ConcurrentHashMap.newKeySet());
    }

    static long balanceOf(UUID team) {
        return BALANCES.getOrDefault(team, 0L);
    }

    static boolean holds(UUID team, String permit) {
        return PERMITS.getOrDefault(team, Set.of()).contains(permit);
    }

    @Override
    public long balance(MinecraftServer server, UUID team) {
        return balanceOf(team);
    }

    @Override
    public boolean take(MinecraftServer server, UUID team, long amount) {
        long balance = balanceOf(team);
        if (balance < amount) {
            return false;
        }
        BALANCES.put(team, balance - amount);
        return true;
    }

    @Override
    public void give(MinecraftServer server, UUID team, long amount) {
        BALANCES.merge(team, amount, Long::sum);
    }

    @Override
    public void grant(MinecraftServer server, UUID team, String permit) {
        PERMITS.computeIfAbsent(team, id -> ConcurrentHashMap.newKeySet()).add(permit);
    }

    @Override
    public Optional<UUID> teamOf(MinecraftServer server, UUID account) {
        return Optional.of(account);
    }
}
