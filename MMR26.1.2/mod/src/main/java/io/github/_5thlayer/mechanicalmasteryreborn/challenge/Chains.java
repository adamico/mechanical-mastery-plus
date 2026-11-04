// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.compat.chapters.ChaptersStages;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * A challenge's chain, locked for a team until it starts the challenge and unlocked from then on, past a win or a
 * reset (ADR 0007). Each chain is a Chapters stage named after its handicap, such as {@code mechanicalmasteryreborn:energy},
 * and the pack's stage files lock the chain's own recipes behind it; its machines' work is locked by {@link ChainMachines}. A player is given the stage of every chain its team has
 * unlocked as the challenge starts and each time it logs in, so a player who joins the team later has them too.
 */
public final class Chains {

    /** How a player is given a stage. */
    public interface Stages {

        /** Gives {@code player} the stage {@code stage}, a handicap's id, if it hasn't it yet. */
        void grant(Player player, String stage);
    }

    private static final Stages NONE = (player, stage) -> {
    };

    private static volatile Stages stages;

    private Chains() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class,
                event -> unlock(event.getEntity().level().getServer(), event.getEntity()));
    }

    /** Gives {@code player} the stage of every chain its team has unlocked, and tells its client which they are. */
    public static void unlock(MinecraftServer server, Player player) {
        if (server == null || player == null) {
            return;
        }
        TeamState state = TeamState.get(server);
        var team = Teams.of(player);
        for (Handicap handicap : Handicap.values()) {
            if (state.unlocked(team, handicap)) {
                stages().grant(player, handicap.id());
            }
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ChainMachines.send(server, serverPlayer);
        }
    }

    /** The stages a world without anything else uses: Chapters' when it is installed. */
    public static Stages installed() {
        return ModList.get().isLoaded("chapters") ? new ChaptersStages() : NONE;
    }

    /** Replaces the stages, as the game tests do, since a mock player is no ServerPlayer. */
    public static void use(Stages replacement) {
        stages = replacement;
    }

    private static Stages stages() {
        Stages found = stages;
        return found != null ? found : (stages = installed());
    }
}
