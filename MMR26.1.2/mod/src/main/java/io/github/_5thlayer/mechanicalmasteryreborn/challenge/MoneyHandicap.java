// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

/**
 * The money handicap, less money (#213), on the team's {@link Accounts Account}. When the team starts the money
 * challenge its Balance is set aside, so the chain is paid only with money earned under the handicap, and it is given
 * {@link #PERMIT}, which opens the chain's Offers: money's chain, kept past a win or a reset and taken back by a stop,
 * as every chain is (ADRs 0007, 0013). A reset sets aside what it earned too, as though
 * the challenge had just begun; its end gives back everything set aside, on top of what it earned since. While it plays,
 * each sale pays only {@link ServerConfig#moneySaleShare its share} of the price; buying is unaffected. The money spell
 * at the Hermetic Lab (#263) sets the Balance aside the same way and gives it back as it ends, pays a smaller share of a sale,
 * and raises the Shop's prices ({@link #purchase}).
 */
public final class MoneyHandicap {

    /**
     * The Permit the team is given when it starts the money challenge and loses when it stops it, which every Offer of
     * the chain needs. A Permit the team buys is its own, kept as a built machine is, so none of the chain's Offers hangs on one.
     */
    public static final String PERMIT = "reborn_money";

    private static final Logger LOGGER = LogUtils.getLogger();

    private MoneyHandicap() {
    }

    /**
     * Whether {@code team}'s money challenge can start: only once it has an Account to act on, which it has from its
     * island's first FTB party (#225). If not, its players online read why.
     */
    static boolean canStart(MinecraftServer server, UUID team) {
        if (Accounts.get().opened(server, team)) {
            return true;
        }
        Component noAccount = Component.translatable("mechanicalmasteryreborn.challenge.no_account").withStyle(ChatFormatting.RED);
        server.getPlayerList().getPlayers().stream().filter(player -> Teams.of(player).equals(team))
                .forEach(player -> player.sendSystemMessage(noAccount));
        LOGGER.info("Team {} has no Account yet, so its money challenge didn't start", team);
        return false;
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

    /**
     * Gives {@code team}'s Account {@link #PERMIT} again if its money chain is unlocked, by a start not stopped since or a
     * win (ADR 0013), since a new Account starts without it.
     */
    public static void regrant(MinecraftServer server, UUID team) {
        if (TeamState.get(server).unlocked(team, Handicap.MONEY)) {
            Accounts.get().grant(server, team, PERMIT);
        }
    }

    /** Takes {@link #PERMIT} from {@code team}'s Account, as a stop locks its money chain again (ADR 0013). */
    static void lock(MinecraftServer server, UUID team) {
        Accounts.get().revoke(server, team, PERMIT);
        LOGGER.info("Took {} from team {}, as its money challenge stopped", PERMIT, team);
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
        Optional<UUID> team = Accounts.get().teamOf(server, account);
        TeamState state = TeamState.get(server);
        if (team.filter(id -> state.plays(id, Handicap.MONEY)).isPresent()) {
            return share(price, ServerConfig.moneySaleShare());
        }
        if (team.filter(id -> state.underSpell(id, Handicap.MONEY)).isPresent()) {
            return share(price, ServerConfig.hermeticLabMoneySaleShare());
        }
        return price;
    }

    /**
     * What a purchase of {@code price} costs {@code account}, AdminShop's id for a team's Account: the price risen by
     * {@link ServerConfig#hermeticLabMoneyPriceRise its rise}, rounded up, while the team bears the money spell at its Hermetic Lab
     * (#263), and the price otherwise.
     */
    public static long purchase(MinecraftServer server, UUID account, long price) {
        boolean spell = Accounts.get().teamOf(server, account)
                .map(team -> TeamState.get(server).underSpell(team, Handicap.MONEY))
                .orElse(false);
        return spell ? rise(price, ServerConfig.hermeticLabMoneyPriceRise()) : price;
    }

    /** {@code price} times {@code rise}, rounded up. */
    static long rise(long price, double rise) {
        return (long) Math.ceil(price * rise);
    }

    /** {@code share} of {@code price}, rounded down. */
    static long share(long price, double share) {
        return (long) Math.floor(price * share);
    }

    /** Sets {@code team}'s whole Balance aside, so its Account holds only what it earns from now on. */
    static void setAside(MinecraftServer server, UUID team) {
        Accounts accounts = Accounts.get();
        long balance = accounts.balance(server, team);
        if (balance > 0 && accounts.take(server, team, balance)) {
            TeamState.get(server).putAside(team, balance);
            LOGGER.info("Set aside team {}'s {} cents", team, balance);
        }
    }
}
