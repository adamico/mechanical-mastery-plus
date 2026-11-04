// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * The last chapter, which a team reaches with its fourth win, and with it the Nether and the End (ADR 0012). Its players
 * are given {@link #STAGE}, which the End Portal Frame's recipe is locked behind ({@link Chains#unlock}), and a Nether
 * portal forms only on the island of a team that has reached it, or in the Nether, so the way home is there. Only the
 * forming is locked: anyone may go through a portal that formed.
 */
public final class LastChapter {

    /** The Chapters stage a team's players are given once it has won all four challenges. */
    public static final String STAGE = "last_chapter";

    /** How far from a refused frame players are told why: as far as anyone could have lit it from, and more. */
    private static final double TOLD_WITHIN = 16.0;

    private LastChapter() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BlockEvent.PortalSpawnEvent.class, event -> {
            if (event.getLevel() instanceof ServerLevel level && !mayFormNetherPortal(level, event.getPos())) {
                event.setCanceled(true);
                refused(level, event.getPos());
            }
        });
    }

    /** Whether {@code team} has won all four challenges, and so reached the last chapter, for good. */
    public static boolean reached(MinecraftServer server, UUID team) {
        TeamState state = TeamState.get(server);
        for (Handicap handicap : Handicap.values()) {
            if (!state.won(team, handicap)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether a frame lit at {@code pos} forms a Nether portal: always in the Nether, and elsewhere only on the island of
     * a team that has reached the last chapter, read from its wins, never from {@link #STAGE}, since Chapters is optional.
     */
    public static boolean mayFormNetherPortal(ServerLevel level, BlockPos pos) {
        if (level.dimension() == Level.NETHER) {
            return true;
        }
        return Teams.onIsland(level, pos) && reached(level.getServer(), Teams.at(level, pos));
    }

    private static void refused(ServerLevel level, BlockPos pos) {
        Component message = Component.translatable("mechanicalmasteryreborn.nether_portal.locked").withStyle(ChatFormatting.GRAY);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getCenter()) <= TOLD_WITHIN * TOLD_WITHIN) {
                player.sendSystemMessage(message);
            }
        }
    }
}
