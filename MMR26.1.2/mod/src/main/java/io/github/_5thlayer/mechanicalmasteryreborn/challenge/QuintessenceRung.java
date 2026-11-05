// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Quintessence's Rung (#262): the pack's Voidworks Family climbs a Quartessence into Quintessence, in a Mutation Chamber
 * alone, and only once the Chamber's island's team has started its last stand (ADR 0007's rules, as for a chain). Voidworks
 * has no lock of its own, so its mixins ask this: the Chamber holds its Quartessence until then, and the Void Crucible,
 * which never fails, never takes one. Every other Family, and the machines themselves, are never locked.
 */
public final class QuintessenceRung {

    private QuintessenceRung() {
    }

    /** Whether {@code item} is the Quartessence, the Rung's bottom, which only a Mutation Chamber climbs. */
    public static boolean climbs(Item item) {
        return item == Essences.item(Essence.QUARTESSENCE);
    }

    /**
     * Whether a Mutation Chamber at {@code pos} holding {@code held} waits: it holds a Quartessence, and its island's team
     * hasn't started its last stand. Server-side only: anywhere else, never.
     */
    public static boolean lockedAt(Level level, BlockPos pos, Item held) {
        return climbs(held) && level instanceof ServerLevel server
                && TeamState.get(server.getServer()).lastStand(Teams.at(server, pos)).isEmpty();
    }
}
