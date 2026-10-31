// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

/**
 * The money handicap, less money (#213), on the team's {@link Accounts Account}. When the team starts the money
 * challenge its Balance is set aside, so the chain is paid only with money earned under the handicap, and it is given
 * {@link #PERMIT}, which opens the chain's Offers, for good (ADR 0007). A reset sets aside what it earned too, as though
 * the challenge had just begun; its end gives back everything set aside, on top of what it earned since. While it plays,
 * each sale pays only {@link ServerConfig#moneySaleShare its share} of the price; buying is unaffected.
 */
public final class MoneyHandicap {

    /** The Permit the team is given when it starts the money challenge, which every Offer of the chain needs. */
    public static final String PERMIT = "reborn_money";

    private static final Logger LOGGER = LogUtils.getLogger();

    private MoneyHandicap() {
    }

    /** Sets {@code team}'s Balance aside and gives it {@link #PERMIT}, as its money challenge starts. */
    static void start(MinecraftServer server, UUID team) {
        setAside(server, team);
        Accounts.get().grant(server, team, PERMIT);
    }

    /** Sets aside what {@code team} earned, as its money challenge resets. */
    static void reset(MinecraftServer server, UUID team) {
        setAside(server, team);
    }

    /** Gives {@code team} back everything set aside, as its money challenge ends. */
    static void end(MinecraftServer server, UUID team) {
        long amount = TeamState.get(server).takeBackSetAside(team);
        Accounts.get().give(server, team, amount);
        LOGGER.info("Gave team {} back the {} cents set aside", team, amount);
    }

    /**
     * What a sale of {@code price} pays {@code account}, AdminShop's id for a team's Account: its share, rounded down,
     * while the team plays the money challenge, and the whole price otherwise.
     */
    public static long sale(MinecraftServer server, UUID account, long price) {
        boolean plays = Accounts.get().teamOf(server, account)
                .map(team -> TeamState.get(server).plays(team, Handicap.MONEY))
                .orElse(false);
        return plays ? share(price, ServerConfig.moneySaleShare()) : price;
    }

    /** {@code share} of {@code price}, rounded down. */
    static long share(long price, double share) {
        return (long) Math.floor(price * share);
    }

    private static void setAside(MinecraftServer server, UUID team) {
        Accounts accounts = Accounts.get();
        long balance = accounts.balance(server, team);
        if (balance > 0 && accounts.take(server, team, balance)) {
            TeamState.get(server).setAside(team, balance);
            LOGGER.info("Set aside team {}'s {} cents", team, balance);
        }
    }
}
