// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.compat.gateways.GatewaysGates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jspecify.annotations.Nullable;

/**
 * How the {@link Assault} opens its gate: with Gateways to Eternity installed, as the pack plays, the endless gate the
 * pack writes at {@code data/mechanicalmasteryreborn/gateways/gateways/assault.json}; without it there is no gate, only
 * the platform and its bridge. Closing one needs nothing of Gateways, so the Assault does it itself.
 */
public interface Gates {

    /**
     * Opens the Assault's gate in {@code level} at {@code at}, with {@code summoner} as its summoner, whose team its
     * mobs hunt, or none, and says the gate entity's id, or nothing if no gate opened.
     */
    Optional<UUID> open(ServerLevel level, Vec3 at, @Nullable ServerPlayer summoner);

    /** No gate at all, as a world without Gateways has. */
    Gates NONE = (level, at, summoner) -> Optional.empty();

    /** The gates a world without anything else uses: Gateways' when it is installed. */
    static Gates installed() {
        return ModList.get().isLoaded("gateways") ? new GatewaysGates() : NONE;
    }

    /** The gates in use. */
    static Gates get() {
        return Holder.gates != null ? Holder.gates : (Holder.gates = installed());
    }

    /** Replaces the gates in use, as the game tests do, since the dev runs have no Gateways. */
    static void use(Gates replacement) {
        Holder.gates = replacement;
    }

    /** Where the gates in use are kept, since an interface holds no mutable field. */
    final class Holder {
        private static volatile Gates gates;

        private Holder() {
        }
    }
}
