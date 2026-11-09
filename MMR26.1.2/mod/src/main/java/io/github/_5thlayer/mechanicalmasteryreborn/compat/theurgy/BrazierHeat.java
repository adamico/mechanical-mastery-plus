// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * A Pyromantic Brazier gives the chain machines no heat while the team whose island it stands on bears the energy
 * handicap (ADR 0018, #329), so all their heat comes from Caloric Flux Emitters and so from FE. It follows the handicap's
 * life as a stopped generator does ({@code Generators#isStopped}): a win or a stop heats it again. The heat mixin asks
 * {@link #cold} for the Brazier under a machine; an Emitter heats through the machine's heat receiver, which this never
 * touches. Knows nothing of Theurgy, so the game tests reach it.
 */
public final class BrazierHeat {

    private BrazierHeat() {
    }

    /** Whether a Brazier at {@code pos} gives no heat. Server-side only. */
    public static boolean cold(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        TeamState teams = TeamState.get(server.getServer());
        return (teams.anyPlays(Handicap.ENERGY) || teams.anyUnderSpell(Handicap.ENERGY)) && teams.bears(Teams.at(server, pos), Handicap.ENERGY);
    }
}
