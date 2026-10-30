// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;

/**
 * Starting a team's challenge, by the handicap it chose at the lab. Each of the team's players online hears vanilla's
 * challenge fanfare, sees the challenge's name as a title with its cost below, and reads both in chat.
 */
public final class Challenges {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Challenges() {
    }

    /** Starts {@code handicap}'s challenge for {@code team}, as {@link TeamState#choose} does from now, and tells the team. */
    public static boolean start(MinecraftServer server, UUID team, Handicap handicap) {
        if (!TeamState.get(server).choose(team, handicap, server.overworld().getGameTime())) {
            return false;
        }
        Component message = Component.translatable("mechanicalmasteryreborn.challenge.started",
                name(handicap).withStyle(ChatFormatting.GOLD), cost(handicap)).withStyle(ChatFormatting.GRAY);
        Component title = Component.translatable("mechanicalmasteryreborn.challenge.title", name(handicap))
                .withStyle(ChatFormatting.GOLD);
        Component subtitle = cost(handicap);
        var sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
        int told = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                told++;
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                player.connection.send(new ClientboundSoundEntityPacket(sound, SoundSource.UI, player, 1.0F, 1.0F,
                        player.getRandom().nextLong()));
            }
        }
        LOGGER.info("Team {} started the {} challenge; told {} of its players online", team, handicap.id(), told);
        return true;
    }

    /** The handicap's name, as the lab shows it. */
    public static MutableComponent name(Handicap handicap) {
        return Component.translatable("mechanicalmasteryreborn.handicap." + handicap.id());
    }

    /** What the handicap costs the team while it plays its challenge, as the lab shows it before choosing. */
    public static MutableComponent cost(Handicap handicap) {
        return Component.translatable("mechanicalmasteryreborn.handicap." + handicap.id() + ".cost");
    }
}
