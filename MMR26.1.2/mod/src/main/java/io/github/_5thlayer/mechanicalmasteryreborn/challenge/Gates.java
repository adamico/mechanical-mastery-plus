// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.compat.gateways.GatewaysGates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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
     * mobs hunt, or none, at {@code wave}, the waves already behind it (0 for a new gate, the wave a failed one had
     * reached as it reopens), and says the gate entity's id, or nothing if no gate opened.
     */
    Optional<UUID> open(ServerLevel level, Vec3 at, @Nullable ServerPlayer summoner, int wave);

    /** The wave the gate {@code gate} has reached. */
    default int wave(Entity gate) {
        return 0;
    }

    /**
     * Brings on the gate {@code gate}'s next wave at once, as a Wave Caller does (#341): in its breather, at once; in a
     * wave, as soon as that wave ends, with no breather. Says whether it is a gate whose wave could be called.
     */
    default boolean call(ServerLevel level, UUID gate) {
        return false;
    }

    /** How many of the gate {@code gate}'s next waves are called and not yet started, which a gate opened in its place keeps. */
    default int called(ServerLevel level, UUID gate) {
        return 0;
    }

    /**
     * Calls the next wave of every home gate open on {@code team}'s island in {@code level}, as a Wave Caller does with no
     * Assault running (#341), and says whether there was one.
     */
    default boolean callHome(ServerLevel level, UUID team) {
        return false;
    }

    /** No gate at all, as a world without Gateways has. */
    Gates NONE = (level, at, summoner, wave) -> Optional.empty();

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
