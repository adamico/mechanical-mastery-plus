// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * The yield the game tests play at: a full one, so every decompose and recycle returns exact amounts, unless a
 * test asks for another for the length of its body. A body runs in one go, within a tick, so no other test ever
 * sees its yield.
 */
final class TestYield {

    /** The yield the game tests play at. */
    static final double FULL = 1.0;

    private TestYield() {
    }

    /** From the server's start, a tier-1 core and decomposing by hand return everything. */
    static void useFull() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> ServerConfig.TIER_1_YIELD.set(FULL));
    }

    /** Runs {@code body} at a tier-1 yield of {@code yield}, then puts the full one back. */
    static void at(double yield, Runnable body) {
        ServerConfig.TIER_1_YIELD.set(yield);
        try {
            body.run();
        } finally {
            ServerConfig.TIER_1_YIELD.set(FULL);
        }
    }
}
