// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

/**
 * A team's vote, at the lab, to stop the challenge it plays (ADR 0013). A stop is offered once the current attempt has
 * reset; any member may ask for it and take their ask back, and the challenge stops ({@link Challenges#stop}) the moment
 * every member online has asked. The asks live in memory only: a member's is dropped as they log out, and a reset, a win
 * or the challenge's end clears them all. A member who logs in while asks are open counts from then on.
 */
public final class Stops {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Who asked, by team. */
    private static final Map<UUID, Set<UUID>> ASKS = new ConcurrentHashMap<>();

    private Stops() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class,
                event -> loggedOut(event.getEntity().level().getServer(), event.getEntity()));
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> ASKS.clear());
    }

    /** Whether {@code team} may stop the challenge it plays: only once this attempt has reset. */
    public static boolean offered(MinecraftServer server, UUID team) {
        TeamState state = TeamState.get(server);
        return state.handicap(team).isPresent() && state.resets(team) > 0;
    }

    /**
     * Notes that {@code player} asks to stop {@code team}'s challenge, tells the team, and stops it once every member online
     * has asked. Says whether the ask was taken: only from a member, while a stop is offered, and not twice.
     */
    public static boolean ask(MinecraftServer server, UUID team, Player player) {
        if (!Teams.of(player).equals(team) || !offered(server, team)
                || !ASKS.computeIfAbsent(team, ignored -> ConcurrentHashMap.newKeySet()).add(player.getUUID())) {
            return false;
        }
        List<Player> online = Teams.online(server, team);
        Handicap handicap = TeamState.get(server).handicap(team).orElseThrow();
        long asked = online.stream().filter(member -> asked(team, member)).count();
        tell(online, Component.translatable("mechanicalmasteryreborn.challenge.stop.asked", player.getDisplayName(),
                Challenges.name(handicap).withStyle(ChatFormatting.GOLD), asked, online.size()));
        LOGGER.info("{} asks to stop team {}'s {} challenge", player.getName().getString(), team, handicap.id());
        stopIfAllAsked(server, team, null);
        return true;
    }

    /** Takes back {@code player}'s ask to stop {@code team}'s challenge, tells the team, and says whether they had asked. */
    public static boolean takeBack(MinecraftServer server, UUID team, Player player) {
        Set<UUID> asks = ASKS.get(team);
        if (!Teams.of(player).equals(team) || asks == null || !asks.remove(player.getUUID())) {
            return false;
        }
        TeamState.get(server).handicap(team).ifPresent(handicap -> tell(Teams.online(server, team),
                Component.translatable("mechanicalmasteryreborn.challenge.stop.taken_back", player.getDisplayName(),
                        Challenges.name(handicap).withStyle(ChatFormatting.GOLD))));
        return true;
    }

    /** Whether {@code player} asks to stop {@code team}'s challenge. */
    public static boolean asked(UUID team, Player player) {
        return ASKS.getOrDefault(team, Set.of()).contains(player.getUUID());
    }

    /** Drops every ask to stop {@code team}'s challenge, as a reset, a win or its end does. */
    public static void clear(UUID team) {
        ASKS.remove(team);
    }

    /**
     * Drops the ask of {@code player}, who logs out, and stops their team's challenge if every member still online has
     * asked. They are still on the server's list as they log out, so they are left out by hand.
     */
    public static void loggedOut(MinecraftServer server, Player player) {
        if (server == null) {
            return;
        }
        UUID team = Teams.of(player);
        Set<UUID> asks = ASKS.get(team);
        if (asks != null && asks.remove(player.getUUID())) {
            stopIfAllAsked(server, team, player);
        }
    }

    /** Stops {@code team}'s challenge if every member online but {@code leaving} has asked, and at least one has. */
    private static void stopIfAllAsked(MinecraftServer server, UUID team, Player leaving) {
        List<Player> online = Teams.online(server, team).stream().filter(member -> member != leaving).toList();
        if (!online.isEmpty() && online.stream().allMatch(member -> asked(team, member))) {
            Challenges.stop(server, team);
        }
    }

    private static void tell(List<Player> players, Component message) {
        Component styled = message.copy().withStyle(ChatFormatting.GRAY);
        players.forEach(player -> player.sendSystemMessage(styled));
    }
}
