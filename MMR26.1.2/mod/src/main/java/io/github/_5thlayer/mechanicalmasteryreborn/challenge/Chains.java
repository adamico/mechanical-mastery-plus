// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.compat.chapters.ChaptersStages;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Attempts;
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
 * reset (ADR 0007), until a stop locks it again (ADR 0013). Each chain is a Chapters stage named after its handicap, such as {@code mechanicalmasteryreborn:energy},
 * and the pack's stage files lock the chain's own recipes behind it; its machines' work is locked by {@link ChainMachines}.
 * A player is given the stage of every chain its team has unlocked as the challenge starts, each time it logs in and
 * as it joins or leaves an island, so a player who joins the team later has them too, and has the stage of every other
 * chain taken, so a stop reaches a member who was offline at the time.
 */
public final class Chains {

    /** How a player is given a stage, and has it taken. */
    public interface Stages {

        /**
         * Gives {@code player} the stage {@code stage}, a handicap's id, {@link LastChapter#STAGE} or {@link LastStands#STAGE},
         * if it hasn't it yet.
         */
        void grant(Player player, String stage);

        /** Takes the stage {@code stage}, a handicap's id, from {@code player}, if it has it. */
        void revoke(Player player, String stage);
    }

    private static final Stages NONE = new Stages() {
        @Override
        public void grant(Player player, String stage) {
        }

        @Override
        public void revoke(Player player, String stage) {
        }
    };

    private static volatile Stages stages;

    private Chains() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class,
                event -> unlock(event.getEntity().level().getServer(), event.getEntity()));
    }

    /**
     * Gives {@code player} the stage of every chain its team has unlocked and takes the stage of every other, gives it the
     * last chapter's while its team has won all four (ADR 0012) and the last stand's once it has started it (ADR 0014), and tells its client which chains are unlocked and its team's attempt (#322).
     */
    public static void unlock(MinecraftServer server, Player player) {
        if (server == null || player == null) {
            return;
        }
        TeamState state = TeamState.get(server);
        var team = Teams.of(player);
        for (Handicap handicap : Handicap.values()) {
            if (state.unlocked(team, handicap)) {
                stages().grant(player, handicap.id());
            } else {
                stages().revoke(player, handicap.id());
            }
        }
        if (LastChapter.reached(server, team)) {
            stages().grant(player, LastChapter.STAGE);
        } else {
            // Only an op's revert takes a win back.
            stages().revoke(player, LastChapter.STAGE);
        }
        if (LastStands.started(server, team)) {
            stages().grant(player, LastStands.STAGE);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ChainMachines.send(server, serverPlayer);
            Attempts.send(server, serverPlayer);
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
