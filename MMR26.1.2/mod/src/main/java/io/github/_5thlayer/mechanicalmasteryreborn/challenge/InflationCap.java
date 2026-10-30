// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.Inflation;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/**
 * Inflation's {@link ServerConfig#inflationCap cap}. A team whose Inflation reaches it has its challenge reset, as
 * though it had just begun: its Inflation from nothing, and so C.O.D. back to its waking pace, its handicap kept, and
 * its island untouched. Each of the team's players online reads that in chat, with a sound. Nearing it, at 80%, they
 * are warned the same way, once, and again only after Inflation has fallen back below; while none of them is online
 * the warning waits for the first who is. Checked at the end of each tick, before the bars are, so nothing that
 * changes Inflation has to tell it and a bar never shows a reached cap.
 */
public final class InflationCap {

    private static final Logger LOGGER = LogUtils.getLogger();

    private InflationCap() {
    }

    public static void register() {
        // Registered before InflationBar's listener, so it runs first in the same tick.
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> check(event.getServer()));
    }

    private static void check(MinecraftServer server) {
        TeamState state = TeamState.get(server);
        int cap = ServerConfig.inflationCap();
        for (UUID team : state.playing()) {
            Inflation inflation = state.inflation(team).orElseThrow();
            if (inflation.excess() >= cap) {
                state.resetChallenge(team, server.overworld().getGameTime());
                Handicap handicap = state.handicap(team).orElseThrow();
                int told = tell(server, team, Component.translatable("mechanicalmasteryreborn.challenge.reset",
                        cap, Challenges.title(handicap).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.RED),
                        SoundEvents.BEACON_DEACTIVATE);
                LOGGER.info("Team {} reached its Inflation cap of {}; its {} challenge was reset; told {} of its players online",
                        team, cap, handicap.id(), told);
            } else if (near(inflation.excess(), cap)) {
                if (!state.warned(team)) {
                    Handicap handicap = state.handicap(team).orElseThrow();
                    int told = tell(server, team, Component.translatable("mechanicalmasteryreborn.challenge.warning",
                            inflation.excess(), cap, Challenges.title(handicap)).withStyle(ChatFormatting.YELLOW),
                            SoundEvents.BELL_BLOCK);
                    if (told > 0) {
                        state.setWarned(team, true);
                        LOGGER.info("Team {} nears its Inflation cap, at {} of {}; told {} of its players online",
                                team, inflation.excess(), cap, told);
                    }
                }
            } else {
                state.setWarned(team, false);
            }
        }
    }

    /** Whether {@code excess} is at 80% of {@code cap} or more, where the team is warned. */
    private static boolean near(long excess, int cap) {
        return excess * 5 >= cap * 4L;
    }

    /** Tells {@code team}'s players online {@code message} in chat, with {@code sound}, and says how many it told. */
    private static int tell(MinecraftServer server, UUID team, Component message, SoundEvent sound) {
        var holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
        int told = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                told++;
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSoundEntityPacket(holder, SoundSource.UI, player, 1.0F, 1.0F,
                        player.getRandom().nextLong()));
            }
        }
        return told;
    }
}
