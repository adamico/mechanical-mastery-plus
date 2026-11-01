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
 * Starting a team's challenge, by the handicap it chose at the lab, and ending or resetting it. Each of the team's
 * players online hears vanilla's challenge fanfare when it starts, sees the challenge's name as a title with its cost
 * below, and reads both in chat. The money handicap acts on the team's Account as it starts, resets and ends.
 */
public final class Challenges {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Challenges() {
    }

    /** Starts {@code handicap}'s challenge for {@code team}, as {@link TeamState#choose} does at the current game time, and tells the team. */
    public static boolean start(MinecraftServer server, UUID team, Handicap handicap) {
        if (handicap == Handicap.MONEY && !Accounts.get().opened(server, team)) {
            // Nothing could be set aside or granted: the team's island has no FTB party yet (#225).
            Component noAccount = Component.translatable("mechanicalmasteryreborn.challenge.no_account").withStyle(ChatFormatting.RED);
            server.getPlayerList().getPlayers().stream().filter(player -> Teams.of(player).equals(team))
                    .forEach(player -> player.sendSystemMessage(noAccount));
            LOGGER.info("Team {} has no Account yet, so its money challenge didn't start", team);
            return false;
        }
        if (!TeamState.get(server).choose(team, handicap, server.overworld().getGameTime())) {
            return false;
        }
        if (handicap == Handicap.MONEY) {
            MoneyHandicap.start(server, team);
        }
        Component message = Component.translatable("mechanicalmasteryreborn.challenge.started",
                name(handicap).withStyle(ChatFormatting.GOLD), cost(handicap)).withStyle(ChatFormatting.GRAY);
        Component title = title(handicap).withStyle(ChatFormatting.GOLD);
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

    /** Ends the challenge {@code team} plays, as {@link TeamState#endChallenge} does, giving back what its handicap set aside, and says whether it played one. */
    public static boolean end(MinecraftServer server, UUID team) {
        TeamState state = TeamState.get(server);
        boolean money = state.plays(team, Handicap.MONEY);
        if (!state.endChallenge(team)) {
            return false;
        }
        if (money) {
            MoneyHandicap.end(server, team);
        }
        return true;
    }

    /** Resets the challenge {@code team} plays, as {@link TeamState#resetChallenge} does at the current game time, and its handicap with it, and says whether it played one. */
    public static boolean reset(MinecraftServer server, UUID team) {
        TeamState state = TeamState.get(server);
        if (!state.resetChallenge(team, server.overworld().getGameTime())) {
            return false;
        }
        if (state.plays(team, Handicap.MONEY)) {
            MoneyHandicap.reset(server, team);
        }
        return true;
    }

    /** The handicap's name, as the lab shows it. */
    public static MutableComponent name(Handicap handicap) {
        return Component.translatable("mechanicalmasteryreborn.handicap." + handicap.id());
    }

    /** The challenge's name, such as "Energy Challenge", as its start's title and its Inflation's bar show it. */
    public static MutableComponent title(Handicap handicap) {
        return Component.translatable("mechanicalmasteryreborn.challenge.title", name(handicap));
    }

    /** What the handicap costs the team while it plays its challenge, as the lab shows it before choosing. */
    public static MutableComponent cost(Handicap handicap) {
        return Component.translatable("mechanicalmasteryreborn.handicap." + handicap.id() + ".cost");
    }
}
