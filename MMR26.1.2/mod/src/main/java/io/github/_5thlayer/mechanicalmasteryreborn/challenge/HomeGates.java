// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * The home gate (the line's glossary, #238): the pack's smaller Gateways gate, whose pearl no recipe makes and the
 * Shop sells once the team has won protection (#246), behind {@link #PERMIT}, so a won protection chain goes on making
 * Warding Essence once the Assault stops. It opens only on the
 * island of the player's own team, once that team has started protection (ADR 0007); anywhere else Gateways refuses
 * it with {@link #refusal}'s message and keeps the pearl.
 */
public final class HomeGates {

    /** The home gate's id, its file {@code data/mechanicalmasteryreborn/gateways/gateways/home.json}. */
    public static final Identifier GATE = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "home");

    /** The Permit a team's Account is given when it wins protection, which the home gate pearl's Offer needs. */
    public static final String PERMIT = "reborn_protection_won";

    private HomeGates() {
    }

    /** Gives {@code team}'s Account {@link #PERMIT} if it has won protection, as a win does and a new Account needs again. */
    public static void grant(MinecraftServer server, UUID team) {
        if (TeamState.get(server).won(team, Handicap.PROTECTION)) {
            Accounts.get().grant(server, team, PERMIT);
        }
    }

    /** Takes {@link #PERMIT} from {@code team}'s Account, as a reverted protection win does (#335). */
    static void revoke(MinecraftServer server, UUID team) {
        Accounts.get().revoke(server, team, PERMIT);
    }

    /**
     * Why {@code player} can't open {@code gate} where they stand, or null if they can, or it is no home gate. Only the
     * server knows the teams, so the client is never refused: the server's refusal is the one that counts.
     */
    public static @Nullable Component refusal(Player player, Identifier gate) {
        if (!GATE.equals(gate) || !(player.level() instanceof ServerLevel level)) {
            return null;
        }
        var team = Teams.at(level, player.blockPosition());
        if (!Teams.onIsland(level, player.blockPosition()) || !team.equals(Teams.of(player))) {
            return Component.translatable("mechanicalmasteryreborn.home_gate.not_home");
        }
        if (!TeamState.get(level.getServer()).unlocked(team, Handicap.PROTECTION)) {
            return Component.translatable("mechanicalmasteryreborn.home_gate.locked");
        }
        return null;
    }
}
