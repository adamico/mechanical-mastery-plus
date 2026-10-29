// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * The mod's entry point: the essences and C.O.D., and its part of the suite's Works tab. The game tests
 * hook themselves in from the {@code gametest} source set, which never ships (libworks ADR 0011).
 */
@Mod(MechanicalMasteryReborn.MOD_ID)
public final class MechanicalMasteryReborn {

    /** The mod id, which gradle.properties' {@code mod_id} must match. */
    public static final String MOD_ID = "mechanicalmasteryreborn";

    public MechanicalMasteryReborn(IEventBus modBus) {
        Essences.register(modBus);
        Cod.register(modBus);
        modBus.addListener(WorksTabContents::add);
    }
}
