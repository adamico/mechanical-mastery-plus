// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;

/**
 * Starting a team's challenge, by the handicap it chose at the lab, and stopping, winning or resetting it; a stop locks
 * its chain again (ADR 0013), where a win or a reset keeps it. Each of the team's
 * players online hears vanilla's raid horn when it starts, and its challenge fanfare when it is won, sees the challenge's name as a title with its cost
 * below, and reads both in chat, and is given the challenge's chain ({@link Chains}). The money handicap acts on the
 * team's Account as it starts, resets and ends, and the protection handicap's {@link Assault} is sent as it starts and
 * removed as it resets or ends; the life handicap's Diets are forgotten as it resets or ends.
 */
public final class Challenges {

    private static final Logger LOGGER = LogUtils.getLogger();
    /** The raid horn's files are mixed at a hundredth of full volume; vanilla's raids play it at 64, as this does. */
    static final float RAID_HORN_VOLUME = 64.0F;

    private Challenges() {
    }

    /**
     * Starts {@code handicap}'s challenge for {@code team}, as {@link TeamState#begin} does at the current game time, and
     * tells the team: the lab's button and an op's {@code /cod challenge start} (#235) alike.
     */
    public static boolean begin(MinecraftServer server, UUID team, Handicap handicap) {
        if (handicap == Handicap.MONEY && !MoneyHandicap.canStart(server, team)) {
            return false;
        }
        if (!TeamState.get(server).begin(team, handicap, server.overworld().getGameTime())) {
            return false;
        }
        if (handicap == Handicap.MONEY) {
            MoneyHandicap.start(server, team);
        }
        if (handicap == Handicap.PROTECTION) {
            Assault.start(server, team);
        }
        if (handicap == Handicap.LIFE) {
            LifeHandicap.refresh(server, team);
        }
        Component message = Component.translatable("mechanicalmasteryreborn.challenge.started",
                name(handicap).withStyle(ChatFormatting.GOLD), cost(handicap)).withStyle(ChatFormatting.GRAY);
        Component title = title(handicap).withStyle(ChatFormatting.GOLD);
        Component subtitle = cost(handicap);
        Holder<SoundEvent> sound = SoundEvents.RAID_HORN;
        int told = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                told++;
                Chains.unlock(server, player);
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                player.connection.send(new ClientboundSoundEntityPacket(sound, SoundSource.UI, player, RAID_HORN_VOLUME, 1.0F,
                        player.getRandom().nextLong()));
            }
        }
        LOGGER.info("Team {} started the {} challenge; told {} of its players online", team, handicap.id(), told);
        return true;
    }

    /**
     * Stops the challenge {@code team} plays at the lab, as {@link #end} does, and tells the team's players online with a
     * title, as a start does. Says whether it played one.
     */
    public static boolean stop(MinecraftServer server, UUID team) {
        var played = TeamState.get(server).handicap(team);
        if (played.isEmpty() || !end(server, team)) {
            return false;
        }
        Handicap handicap = played.get();
        Component message = Component.translatable("mechanicalmasteryreborn.challenge.stopped", name(handicap).withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.GRAY);
        Component title = Component.translatable("mechanicalmasteryreborn.challenge.stopped.title", title(handicap)).withStyle(ChatFormatting.GOLD);
        int told = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                told++;
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
            }
        }
        LOGGER.info("Team {} stopped the {} challenge; told {} of its players online", team, handicap.id(), told);
        return true;
    }

    /**
     * Ends the challenge {@code team} plays, as a stop does but with no vote and no reset needed, as an op's
     * {@code /cod challenge end} does: {@link #finish} it, and lock its chain again (ADR 0013), its stage taken from the
     * team's players online now and from the rest as they log in ({@link Chains#unlock}). Says whether it played one.
     */
    public static boolean end(MinecraftServer server, UUID team) {
        var played = TeamState.get(server).handicap(team);
        if (played.isEmpty() || !finish(server, team)) {
            return false;
        }
        TeamState.get(server).lockChain(team, played.get());
        for (var player : Teams.online(server, team)) {
            Chains.unlock(server, player);
        }
        return true;
    }

    /**
     * Ends the challenge {@code team} plays, as {@link TeamState#endChallenge} does, giving back what its handicap set aside
     * and dropping every ask to stop it, and says whether it played one. Its chain stays as it is: a win keeps it.
     */
    private static boolean finish(MinecraftServer server, UUID team) {
        Stops.clear(team);
        TeamState state = TeamState.get(server);
        boolean money = state.plays(team, Handicap.MONEY);
        boolean protection = state.plays(team, Handicap.PROTECTION);
        boolean life = state.plays(team, Handicap.LIFE);
        if (!state.endChallenge(team)) {
            return false;
        }
        if (money) {
            MoneyHandicap.end(server, team);
        }
        if (protection) {
            Assault.end(server, team);
        }
        if (life) {
            LifeHandicap.refresh(server, team);
        }
        return true;
    }

    /**
     * Wins the challenge {@code team} plays: blocks the tier its C.O.D. makes and notes the win ({@link TeamState#blockTier}),
     * ends the challenge as {@link #finish} does, its chain kept, and tells the team's players online with a title and a sound, giving them the
     * last chapter's stage on the fourth ({@link Chains#unlock}). Says whether it played one.
     */
    public static boolean win(MinecraftServer server, UUID team) {
        TeamState state = TeamState.get(server);
        var played = state.handicap(team);
        if (played.isEmpty()) {
            return false;
        }
        Handicap handicap = played.get();
        state.blockTier(team, handicap);
        finish(server, team);
        if (handicap == Handicap.PROTECTION) {
            HomeGates.grant(server, team);
        }
        tellWon(server, team, handicap);
        return true;
    }

    /**
     * Wins {@code handicap}'s challenge for {@code team}, as an op's {@code /cod challenge win} does: as {@link #win(MinecraftServer, UUID)}
     * does while the team plays it, and otherwise notes it started and won ({@link TeamState#winUnplayed}) with none of
     * a start's effects, and tells the team as a win does. Says whether it did: not for a challenge won already, nor
     * while the team plays another.
     */
    public static boolean win(MinecraftServer server, UUID team, Handicap handicap) {
        TeamState state = TeamState.get(server);
        if (state.plays(team, handicap)) {
            return win(server, team);
        }
        if (!state.winUnplayed(team, handicap)) {
            return false;
        }
        if (handicap == Handicap.PROTECTION) {
            HomeGates.grant(server, team);
        }
        tellWon(server, team, handicap);
        return true;
    }

    /** Tells {@code team}'s players online it won {@code handicap}'s challenge, with a title and a sound, and gives them its stages. */
    private static void tellWon(MinecraftServer server, UUID team, Handicap handicap) {
        Component message = Component.translatable("mechanicalmasteryreborn.challenge.won", name(handicap).withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.GRAY);
        Component title = Component.translatable("mechanicalmasteryreborn.challenge.won.title", title(handicap)).withStyle(ChatFormatting.GOLD);
        var sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
        int told = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                told++;
                // Gives the last chapter's stage once this is the fourth win (ADR 0012).
                Chains.unlock(server, player);
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                player.connection.send(new ClientboundSoundEntityPacket(sound, SoundSource.UI, player, 1.0F, 1.0F,
                        player.getRandom().nextLong()));
            }
        }
        LOGGER.info("Team {} won the {} challenge; told {} of its players online", team, handicap.id(), told);
    }

    /**
     * Resets the challenge {@code team} plays, as {@link TeamState#resetChallenge} does at the current game time, and its
     * handicap with it, drops every ask to stop it, and says whether it played one. Protection's Assault is removed and a
     * new one sent from wave 1, as though the challenge had just begun (#294).
     */
    public static boolean reset(MinecraftServer server, UUID team) {
        TeamState state = TeamState.get(server);
        if (!state.resetChallenge(team, server.overworld().getGameTime())) {
            return false;
        }
        Stops.clear(team);
        if (state.plays(team, Handicap.MONEY)) {
            MoneyHandicap.reset(server, team);
        }
        if (state.plays(team, Handicap.PROTECTION)) {
            Assault.end(server, team);
            Assault.start(server, team);
        }
        if (state.plays(team, Handicap.LIFE)) {
            LifeHandicap.forget(server, team);
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
