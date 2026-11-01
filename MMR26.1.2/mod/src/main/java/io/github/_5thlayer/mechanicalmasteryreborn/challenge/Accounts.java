// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.compat.adminshop.AdminShopAccounts;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;

/**
 * Each team's AdminShop Account (AdminShop's glossary: Account, Balance, Permit), by the team's id ({@link Teams}), as
 * the money handicap acts on it. Amounts are in AdminShop's cents. With AdminShop installed, as the pack plays, it is
 * the Account of the FTB party the team's island owns (#225); without it there is no money, and nothing to act on.
 */
public interface Accounts {

    /** Whether {@code team} has an Account yet, so the money handicap has something to act on. */
    boolean opened(MinecraftServer server, UUID team);

    /** {@code team}'s Balance. */
    long balance(MinecraftServer server, UUID team);

    /** Takes {@code amount} from {@code team}'s Balance, and says whether it held that much. */
    boolean take(MinecraftServer server, UUID team, long amount);

    /** Adds {@code amount} to {@code team}'s Balance. */
    void give(MinecraftServer server, UUID team, long amount);

    /** Gives {@code team} {@code permit}, for good. */
    void grant(MinecraftServer server, UUID team, String permit);

    /** The team whose Account {@code account} is, by AdminShop's id for it, if any. */
    Optional<UUID> teamOf(MinecraftServer server, UUID account);

    /** No money at all, as a world without AdminShop has. */
    Accounts NONE = new Accounts() {
        @Override
        public boolean opened(MinecraftServer server, UUID team) {
            return true;
        }

        @Override
        public long balance(MinecraftServer server, UUID team) {
            return 0;
        }

        @Override
        public boolean take(MinecraftServer server, UUID team, long amount) {
            return amount == 0;
        }

        @Override
        public void give(MinecraftServer server, UUID team, long amount) {
        }

        @Override
        public void grant(MinecraftServer server, UUID team, String permit) {
        }

        @Override
        public Optional<UUID> teamOf(MinecraftServer server, UUID account) {
            return Optional.empty();
        }
    };

    /** The Accounts a world without anything else uses: AdminShop's when it is installed. */
    static Accounts installed() {
        return ModList.get().isLoaded("adminshop") ? new AdminShopAccounts() : NONE;
    }

    /** The Accounts in use. */
    static Accounts get() {
        return Holder.accounts != null ? Holder.accounts : (Holder.accounts = installed());
    }

    /** Replaces the Accounts in use, as the game tests do to give each test an Account of its own. */
    static void use(Accounts replacement) {
        Holder.accounts = replacement;
    }

    /** Where the Accounts in use are kept, since an interface holds no mutable field. */
    final class Holder {
        private static volatile Accounts accounts;

        private Holder() {
        }
    }
}
