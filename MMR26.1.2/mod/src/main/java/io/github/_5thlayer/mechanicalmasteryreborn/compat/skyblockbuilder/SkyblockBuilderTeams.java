// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.skyblockbuilder;

import java.util.UUID;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Skyblock Builder's teams. A block is on the island whose centre is nearest to it in its dimension; a player
 * with no team is in none, the world's. Loaded only when Skyblock Builder is installed, and nothing else in
 * the mod names a Skyblock Builder type.
 */
public final class SkyblockBuilderTeams implements Teams.Lookup {

    @Override
    public UUID at(ServerLevel level, BlockPos pos) {
        var data = SkyblockSavedData.get(level);
        Team nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (Team team : data.getTeams()) {
            if (team.isSpawn() || !team.getTeamLevelKey().equals(level.dimension())) {
                continue;
            }
            BlockPos center = team.getIsland().getCenter();
            long dx = center.getX() - pos.getX();
            long dz = center.getZ() - pos.getZ();
            long distance = dx * dx + dz * dz;
            if (distance < nearestDistance) {
                nearest = team;
                nearestDistance = distance;
            }
        }
        return nearest == null ? Teams.WORLD : nearest.id();
    }

    @Override
    public UUID of(Player player) {
        Team team = SkyblockSavedData.get(player.level()).getTeamFromPlayer(player);
        return team == null || team.isSpawn() ? Teams.WORLD : team.id();
    }
}
