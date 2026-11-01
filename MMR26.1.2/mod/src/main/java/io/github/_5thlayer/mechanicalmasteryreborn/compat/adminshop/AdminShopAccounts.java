// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.adminshop;

import java.util.Optional;
import java.util.UUID;

import com.ammonium.adminshop.money.MoneyHelper;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Accounts;
import io.github._5thlayer.mechanicalmasteryreborn.compat.ftbteams.FtbTeams;
import io.github._5thlayer.mechanicalmasteryreborn.record.IslandParties;
import net.minecraft.server.MinecraftServer;

/**
 * AdminShop's Accounts. A team's Account is the one of the FTB party its island owns (#225); a team with no party yet
 * has none. AdminShop opens an Account only when one of its team's players is first seen, so one that doesn't exist
 * yet is opened through a member online. Loaded only when AdminShop is installed, and nothing else in the mod names
 * an AdminShop type.
 */
public final class AdminShopAccounts implements Accounts {

    @Override
    public boolean opened(MinecraftServer server, UUID team) {
        return account(server, team).map(id -> MoneyHelper.get(server).getAccountById(id) != null).orElse(false);
    }

    @Override
    public long balance(MinecraftServer server, UUID team) {
        return account(server, team).map(id -> {
            var account = MoneyHelper.get(server).getAccountById(id);
            return account == null ? 0L : account.balance();
        }).orElse(0L);
    }

    @Override
    public boolean take(MinecraftServer server, UUID team, long amount) {
        return account(server, team).map(id -> MoneyHelper.get(server).removeMoney(id, amount)).orElse(false);
    }

    @Override
    public void give(MinecraftServer server, UUID team, long amount) {
        account(server, team).ifPresent(id -> MoneyHelper.get(server).addMoney(id, amount));
    }

    @Override
    public void grant(MinecraftServer server, UUID team, String permit) {
        account(server, team).ifPresent(id -> MoneyHelper.get(server).addPermit(id, permit));
    }

    @Override
    public Optional<UUID> teamOf(MinecraftServer server, UUID account) {
        return IslandParties.get(server).islandOf(account);
    }

    /** AdminShop's id for {@code team}'s Account, opened through a member online if it has none yet. */
    private static Optional<UUID> account(MinecraftServer server, UUID team) {
        Optional<UUID> party = IslandParties.get(server).party(team);
        party.ifPresent(id -> {
            MoneyHelper money = MoneyHelper.get(server);
            if (money.getAccountById(id) == null) {
                FtbTeams.onlineMember(id).ifPresent(money::getPlayerAccount);
            }
        });
        return party;
    }
}
