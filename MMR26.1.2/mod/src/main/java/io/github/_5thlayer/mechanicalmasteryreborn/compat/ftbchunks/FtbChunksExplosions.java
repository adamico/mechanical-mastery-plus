// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.ftbchunks;

import java.util.Optional;
import java.util.UUID;

import dev.ftb.mods.ftbchunks.api.FTBChunksProperties;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.event.TeamPropertiesChangedEvent;
import dev.ftb.mods.ftbteams.api.neoforge.FTBTeamsEvent;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultExplosions;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Explosions;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * FTB Chunks' part: a team's {@link FTBChunksProperties#ALLOW_EXPLOSIONS}, set as FTB's own command sets a property
 * (the previous properties kept, the change posted and synced), and a player's change of it handed to
 * {@link AssaultExplosions}, which keeps it on while the team's Assault runs.
 */
public final class FtbChunksExplosions implements Explosions {

    /** Hands a player's change of the property to {@link AssaultExplosions}. */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(FTBTeamsEvent.TeamPropertiesChanged.class, event -> {
            var data = event.getEventData();
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (!data.isClient() && server != null && data.hasPropertyChanged(FTBChunksProperties.ALLOW_EXPLOSIONS)) {
                AssaultExplosions.changed(server, data.team().getId());
            }
        });
    }

    @Override
    public Optional<Boolean> allowed(MinecraftServer server, UUID team) {
        return FTBTeamsAPI.api().getManager().getTeamByID(team).map(found -> found.getProperty(FTBChunksProperties.ALLOW_EXPLOSIONS));
    }

    @Override
    public void set(MinecraftServer server, UUID team, boolean allowed) {
        FTBTeamsAPI.api().getManager().getTeamByID(team).ifPresent(found -> {
            var previous = found.getProperties().copy();
            found.setProperty(FTBChunksProperties.ALLOW_EXPLOSIONS, allowed);
            NativeEventPosting.get().postEvent(new TeamPropertiesChangedEvent.Data(found, previous, false));
            found.syncOnePropertyToAll(server, FTBChunksProperties.ALLOW_EXPLOSIONS, allowed);
        });
    }
}
