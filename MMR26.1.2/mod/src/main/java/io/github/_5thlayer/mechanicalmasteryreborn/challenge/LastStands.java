// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Locale;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.LastStand;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
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
 * C.O.D.'s last stand (ADR 0014), which a team that has won all four challenges starts at the lab: from then on C.O.D.
 * makes Nullessence, each one against the team's Lead, and each Quintessence put into C.O.D. cancels a fixed amount of it.
 * Starting it gives the team's players {@link #STAGE}, which Quintessence's chain is locked behind ({@link Chains#unlock}).
 * Once the Lead reaches the margin C.O.D. shuts down for good and the team has won the pack. It has no handicap, never
 * resets and can't be stopped. Each tick moves every running last stand's pace on ({@link LastStand#afterTick}).
 */
public final class LastStands {

    /** The Chapters stage a team's players are given once it has started its last stand. */
    public static final String STAGE = "last_stand";

    private static final Logger LOGGER = LogUtils.getLogger();

    private LastStands() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> TeamState.get(event.getServer())
                .tickLastStands(ServerConfig.lastStandFloor(), ServerConfig.lastStandCeiling(), ServerConfig.lastStandLag()));
    }

    /** Whether the lab offers {@code team} its last stand: it has won all four challenges and not started it yet. */
    public static boolean offered(MinecraftServer server, UUID team) {
        return LastChapter.reached(server, team) && TeamState.get(server).lastStand(team).isEmpty();
    }

    /**
     * Starts {@code team}'s last stand, as {@link TeamState#beginLastStand} does, and tells the team's players online with
     * a title and a sound, giving them {@link #STAGE}. Says whether it started: only once the team has won all four.
     */
    public static boolean begin(MinecraftServer server, UUID team) {
        if (!TeamState.get(server).beginLastStand(team, ServerConfig.lastStandFloor())) {
            return false;
        }
        Component name = Component.translatable(MechanicalMasteryReborn.MOD_ID + ".last_stand");
        Component cost = Component.translatable(MechanicalMasteryReborn.MOD_ID + ".last_stand.started.subtitle", ServerConfig.lastStandMargin());
        int told = tell(server, team, Component.translatable(MechanicalMasteryReborn.MOD_ID + ".last_stand.started",
                name.copy().withStyle(ChatFormatting.GOLD), cost).withStyle(ChatFormatting.GRAY),
                name.copy().withStyle(ChatFormatting.GOLD), cost, SoundEvents.RAID_HORN);
        LOGGER.info("Team {} started its last stand; told {} of its players online", team, told);
        return true;
    }

    /** How many more Quintessence {@code team}'s running last stand takes to reach the margin; none while none runs. */
    public static int needed(MinecraftServer server, UUID team) {
        var stand = TeamState.get(server).lastStand(team).filter(LastStand::running);
        if (stand.isEmpty()) {
            return 0;
        }
        long behind = ServerConfig.lastStandMargin() - stand.get().lead();
        int cancels = ServerConfig.lastStandQuintessenceCancels();
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, (behind + cancels - 1) / cancels));
    }

    /**
     * Counts {@code count} Quintessence put into {@code team}'s C.O.D., each cancelling
     * {@link ServerConfig#lastStandQuintessenceCancels its amount}, and wins the pack once the Lead reaches the margin.
     */
    public static void take(MinecraftServer server, UUID team, int count) {
        if (count <= 0) {
            return;
        }
        long amount = (long) count * ServerConfig.lastStandQuintessenceCancels();
        if (TeamState.get(server).cancel(team, amount, ServerConfig.lastStandLag(), ServerConfig.lastStandMargin())) {
            won(server, team);
        }
    }

    /** The last stand's line, as its bar and Jade word it: its Lead against the margin, and C.O.D.'s rate. */
    public static Component lead(LastStand stand, int margin) {
        return Component.translatable(MechanicalMasteryReborn.MOD_ID + ".bar.last_stand", stand.lead(), margin,
                String.format(Locale.ROOT, "%.1f", stand.rate()));
    }

    /** Tells the team's players online that C.O.D. shut down and the pack is won. */
    private static void won(MinecraftServer server, UUID team) {
        Component title = Component.translatable(MechanicalMasteryReborn.MOD_ID + ".last_stand.won.title").withStyle(ChatFormatting.GOLD);
        Component subtitle = Component.translatable(MechanicalMasteryReborn.MOD_ID + ".last_stand.won.subtitle");
        int told = tell(server, team, Component.translatable(MechanicalMasteryReborn.MOD_ID + ".last_stand.won").withStyle(ChatFormatting.GRAY),
                title, subtitle, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.END_PORTAL_SPAWN));
        LOGGER.info("Team {} shut C.O.D. down and won the pack; told {} of its players online", team, told);
    }

    /** Tells {@code team}'s players online, giving each its stages, and says how many it told. */
    private static int tell(MinecraftServer server, UUID team, Component message, Component title, Component subtitle, Holder<SoundEvent> holder) {
        int told = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                told++;
                Chains.unlock(server, player);
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                player.connection.send(new ClientboundSoundEntityPacket(holder, SoundSource.UI, player, 1.0F, 1.0F,
                        player.getRandom().nextLong()));
            }
        }
        return told;
    }
}
