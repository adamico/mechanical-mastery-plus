// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.compat.skyblockbuilder.SkyblockBuilderTeams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

/**
 * Which team something belongs to, by its id: the team whose island a block stands on, and a player's team.
 * With Skyblock Builder, as the pack plays, that is its team; without it the whole world is one team,
 * {@link #WORLD}.
 */
public final class Teams {

    /** The one team of a world without Skyblock Builder. */
    public static final UUID WORLD = new UUID(0L, 0L);

    /** How a team is found. */
    public interface Lookup {

        /** The team whose island {@code pos} is on. */
        UUID at(ServerLevel level, BlockPos pos);

        /** The team {@code player} plays in. */
        UUID of(Player player);
    }

    private static final Lookup ONE_TEAM = new Lookup() {
        @Override
        public UUID at(ServerLevel level, BlockPos pos) {
            return WORLD;
        }

        @Override
        public UUID of(Player player) {
            return WORLD;
        }
    };

    private static volatile Lookup lookup;

    private Teams() {
    }

    public static UUID at(ServerLevel level, BlockPos pos) {
        return lookup().at(level, pos);
    }

    public static UUID of(Player player) {
        return lookup().of(player);
    }

    /** The lookup a world without anything else uses: Skyblock Builder's when it is installed. */
    public static Lookup installed() {
        return ModList.get().isLoaded("skyblockbuilder") ? new SkyblockBuilderTeams() : ONE_TEAM;
    }

    /** Replaces the lookup, as the game tests do to give each test a team of its own. */
    public static void use(Lookup replacement) {
        lookup = replacement;
    }

    private static Lookup lookup() {
        Lookup found = lookup;
        if (found == null) {
            found = installed();
            lookup = found;
        }
        return found;
    }
}
