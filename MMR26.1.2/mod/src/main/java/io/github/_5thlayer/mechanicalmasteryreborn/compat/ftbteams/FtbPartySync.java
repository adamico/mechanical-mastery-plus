// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.ftbteams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.property.TeamProperties;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.HomeGates;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.MoneyHandicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.IslandParties;
import io.github._5thlayer.mechanicalmasteryreborn.record.PartySync;
import io.github._5thlayer.mechanicalmasteryreborn.record.PartySync.Island;
import io.github._5thlayer.mechanicalmasteryreborn.record.PartySync.Move;
import io.github._5thlayer.mechanicalmasteryreborn.record.PartySync.Party;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/**
 * Keeps each Skyblock Builder island's players in one FTB party of its own (#225), by {@link PartySync}'s moves, once
 * a second. Skyblock Builder's invitations, join requests and admin commands don't all fire events, so it checks
 * rather than listens, and catches a player's new team of one within a second of their first login. Players can't
 * make parties of their own, which it would only undo. A new party of a team that has started the money challenge is
 * given its Permit again, since a new Account starts without it. Loaded only when both mods are installed; FTB Teams'
 * types are named only here and in {@link FtbTeams}.
 */
public final class FtbPartySync {

    private static final Logger LOGGER = LogUtils.getLogger();
    /** Ticks between two checks. */
    private static final int PERIOD = 20;
    /** Moves applied in one check at most, each run picking up from the last: enough for a party made and joined. */
    private static final int PASSES = 4;

    private FtbPartySync() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> FTBTeamsAPI.api().setPartyCreationFromAPIOnly(true));
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> {
            MinecraftServer server = event.getServer();
            if (server.getTickCount() % PERIOD == 0 && FTBTeamsAPI.api().isManagerLoaded()) {
                sync(server);
            }
        });
    }

    private static void sync(MinecraftServer server) {
        TeamManager manager = FTBTeamsAPI.api().getManager();
        IslandParties links = IslandParties.get(server);
        for (int pass = 0; pass < PASSES; pass++) {
            List<Move> moves = PartySync.moves(islands(server), links.all(), parties(manager), online(server));
            if (moves.isEmpty()) {
                return;
            }
            for (Move move : moves) {
                try {
                    apply(server, manager, links, move);
                } catch (CommandSyntaxException | RuntimeException e) {
                    LOGGER.warn("Couldn't {}: {}", move, e.getMessage());
                }
            }
        }
    }

    private static void apply(MinecraftServer server, TeamManager manager, IslandParties links, Move move) throws CommandSyntaxException {
        switch (move) {
            case Move.Create create -> {
                ServerPlayer owner = server.getPlayerList().getPlayer(create.owner());
                Team party = manager.createPartyTeam(owner, create.name(), "", Color4I.WHITE);
                links.link(create.island(), party.getId());
                MoneyHandicap.regrant(server, create.island());
                HomeGates.grant(server, create.island());
                LOGGER.info("Island {} has FTB party {}, owned by {}", create.island(), party.getId(), owner.getGameProfile().name());
            }
            case Move.Join join -> {
                ServerPlayer player = server.getPlayerList().getPlayer(join.player());
                party(manager, join.party()).join(player);
                Chains.unlock(server, player);
            }
            case Move.Leave leave -> {
                ServerPlayer player = server.getPlayerList().getPlayer(leave.player());
                party(manager, leave.party()).kickPlayerForcibly(player);
                Chains.unlock(server, player);
            }
            case Move.Rename rename -> {
                Team party = party(manager, rename.party());
                party.setProperty(TeamProperties.DISPLAY_NAME, rename.name());
                party.markDirty();
                party.syncOnePropertyToAll(server, TeamProperties.DISPLAY_NAME, rename.name());
            }
            case Move.Forget forget -> links.forget(forget.island());
        }
    }

    private static PartyTeam party(TeamManager manager, UUID id) {
        return (PartyTeam) manager.getTeamByID(id).orElseThrow();
    }

    private static Map<UUID, Island> islands(MinecraftServer server) {
        Map<UUID, Island> islands = new HashMap<>();
        for (var team : SkyblockSavedData.get(server.overworld()).getTeams()) {
            if (!team.isSpawn()) {
                islands.put(team.id(), new Island(team.getName(), Set.copyOf(team.getPlayers())));
            }
        }
        return islands;
    }

    private static Map<UUID, Party> parties(TeamManager manager) {
        Map<UUID, Party> parties = new HashMap<>();
        for (Team team : manager.getTeams()) {
            if (team.isPartyTeam()) {
                parties.put(team.getId(), new Party(team.getProperty(TeamProperties.DISPLAY_NAME), Set.copyOf(team.getMembers())));
            }
        }
        return parties;
    }

    private static Set<UUID> online(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream().map(ServerPlayer::getUUID).collect(Collectors.toSet());
    }
}
