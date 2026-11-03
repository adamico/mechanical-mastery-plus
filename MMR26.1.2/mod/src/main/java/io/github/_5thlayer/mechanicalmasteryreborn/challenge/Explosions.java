// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.compat.ftbchunks.FtbChunksExplosions;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;

/**
 * Whether explosions damage a team's claimed blocks: with FTB Chunks installed, as the pack plays, its team property
 * {@code ftbchunks:allow_explosions}; without it no claim keeps explosions out, so there is nothing to lift. The
 * {@link Assault} lifts it while it runs ({@link AssaultExplosions}).
 */
public interface Explosions {

    /** Whether {@code team} lets explosions through its claims, or nothing if it has no such setting. */
    Optional<Boolean> allowed(MinecraftServer server, UUID team);

    /** Sets whether {@code team} lets explosions through its claims, as a player's change would, synced and told. */
    void set(MinecraftServer server, UUID team, boolean allowed);

    /** No claims at all, as a world without FTB Chunks has. */
    Explosions NONE = new Explosions() {
        @Override
        public Optional<Boolean> allowed(MinecraftServer server, UUID team) {
            return Optional.empty();
        }

        @Override
        public void set(MinecraftServer server, UUID team, boolean allowed) {
        }
    };

    /** The claims a world without anything else uses: FTB Chunks' when it is installed. */
    static Explosions installed() {
        return ModList.get().isLoaded("ftbchunks") ? new FtbChunksExplosions() : NONE;
    }

    /** The claims in use. */
    static Explosions get() {
        return Holder.explosions != null ? Holder.explosions : (Holder.explosions = installed());
    }

    /** Replaces the claims in use, as the game tests do, since the dev runs have no FTB Chunks. */
    static void use(Explosions replacement) {
        Holder.explosions = replacement;
    }

    /** Where the claims in use are kept, since an interface holds no mutable field. */
    final class Holder {
        private static volatile Explosions explosions;

        private Holder() {
        }
    }
}
