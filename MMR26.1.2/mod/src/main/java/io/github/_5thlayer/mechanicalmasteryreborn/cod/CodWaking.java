// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;

/**
 * A team's C.O.D. waking. The first time, each of the team's players online hears a beacon switch on and reads, in
 * chat, what C.O.D. now does. It is news, not a warning: an awake C.O.D. is only more essence until a challenge is
 * played, and the line tells them their automation can start.
 */
public final class CodWaking {

    private static final Logger LOGGER = LogUtils.getLogger();

    private CodWaking() {
    }

    /** Wakes {@code team}'s C.O.D., and tells the team's players if it was dormant until now. */
    public static void wake(MinecraftServer server, UUID team) {
        if (!TeamState.get(server).wake(team)) {
            return;
        }
        Component message = Component.translatable("mechanicalmasteryreborn.cod.woke",
                Cod.BLOCK.get().getName().withStyle(ChatFormatting.GOLD), seconds(ServerConfig.codWakingInterval()))
                .withStyle(ChatFormatting.GRAY);
        var sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BEACON_ACTIVATE);
        int told = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                told++;
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSoundPacket(sound, SoundSource.BLOCKS, player.getX(), player.getY(), player.getZ(),
                        1.0F, 1.0F, player.getRandom().nextLong()));
            }
        }
        LOGGER.info("C.O.D. woke for team {}; told {} of its players online", team, told);
    }

    /** {@code ticks} as seconds, whole when they are. */
    private static String seconds(int ticks) {
        return ticks % 20 == 0 ? Integer.toString(ticks / 20) : Double.toString(ticks / 20.0);
    }
}
