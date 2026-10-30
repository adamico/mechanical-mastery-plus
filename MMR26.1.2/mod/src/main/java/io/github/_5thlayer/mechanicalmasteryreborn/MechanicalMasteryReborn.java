// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Decomposing;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Recycling;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * The mod's entry point: the essences, C.O.D., the decomposing and recycling recipes and the Recycler with its config, and its part of the suite's Works tab. The game tests
 * hook themselves in from the {@code gametest} source set, which never ships (libworks ADR 0011).
 */
@Mod(MechanicalMasteryReborn.MOD_ID)
public final class MechanicalMasteryReborn {

    /** The mod id, which gradle.properties' {@code mod_id} must match. */
    public static final String MOD_ID = "mechanicalmasteryreborn";

    public MechanicalMasteryReborn(IEventBus modBus, ModContainer container) {
        Essences.register(modBus);
        Cod.register(modBus);
        Decomposing.register(modBus);
        Recycling.register(modBus);
        Recycler.register(modBus);
        RecyclerConfig.register(container);
        modBus.addListener(WorksTabContents::add);
    }
}
