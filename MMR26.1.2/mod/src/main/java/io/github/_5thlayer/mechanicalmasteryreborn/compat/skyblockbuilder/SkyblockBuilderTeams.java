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

    /** How far across from its centre a team's island reaches, for {@link #onIsland}: #207's first guess. */
    static final int ISLAND_RADIUS = 128;

    @Override
    public UUID at(ServerLevel level, BlockPos pos) {
        var data = SkyblockSavedData.get(level);
        Team nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (Team team : data.getTeams()) {
            // The level the team's island is in, as Skyblock Builder resolves it: the one shared level in single-world
            // mode, as the pack plays, where getTeamLevelKey() names a per-team dimension that doesn't exist.
            ServerLevel island = team.getLevel();
            if (team.isSpawn() || island == null || !island.dimension().equals(level.dimension())) {
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

    /**
     * Within {@link #ISLAND_RADIUS} blocks of a team's island's centre, across, and nearer it than the spawn island's: the
     * home gate's place (#238). The islands are thousands of blocks apart, so the void between is far from any.
     */
    @Override
    public boolean onIsland(ServerLevel level, BlockPos pos) {
        Team nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (Team team : SkyblockSavedData.get(level).getTeams()) {
            ServerLevel island = team.getLevel();
            if (island == null || !island.dimension().equals(level.dimension())) {
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
        return nearest != null && !nearest.isSpawn() && nearestDistance <= (long) ISLAND_RADIUS * ISLAND_RADIUS;
    }

    @Override
    public UUID of(Player player) {
        Team team = SkyblockSavedData.get(player.level()).getTeamFromPlayer(player);
        return team == null || team.isSpawn() ? Teams.WORLD : team.id();
    }
}
