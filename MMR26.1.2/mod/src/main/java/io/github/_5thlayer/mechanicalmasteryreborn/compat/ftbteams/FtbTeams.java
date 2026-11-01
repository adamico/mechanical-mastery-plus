// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.ftbteams;

import java.util.Optional;
import java.util.UUID;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;

/**
 * What the rest of the mod asks of FTB Teams, so its types are named only under this package. Called only where FTB
 * Teams is installed, as it is wherever AdminShop is, which needs it.
 */
public final class FtbTeams {

    private FtbTeams() {
    }

    /** A player online in the FTB team {@code team}, if any. */
    public static Optional<ServerPlayer> onlineMember(UUID team) {
        return FTBTeamsAPI.api().getManager().getTeamByID(team).flatMap(found -> found.getOnlineMembers().stream().findFirst());
    }
}
