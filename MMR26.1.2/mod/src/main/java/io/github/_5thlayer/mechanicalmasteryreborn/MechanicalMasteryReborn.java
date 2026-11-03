// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Generators;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.InflationBar;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.InflationCap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.compat.ftbteams.FtbPartySync;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.TheurgyHud;
import io.github._5thlayer.mechanicalmasteryreborn.defence.Defences;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.port.Port;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Decomposing;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Recycling;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

/**
 * The mod's entry point: the essences, C.O.D., the lab, the challenges' Ports and the energy handicap's stopped generators, the decomposing and recycling recipes and the Recycler with its config, and its part of the suite's Works tab. The game tests
 * hook themselves in from the {@code gametest} source set, which never ships (libworks ADR 0011).
 */
@Mod(MechanicalMasteryReborn.MOD_ID)
public final class MechanicalMasteryReborn {

    /** The mod id, which gradle.properties' {@code mod_id} must match. */
    public static final String MOD_ID = "mechanicalmasteryreborn";

    public MechanicalMasteryReborn(IEventBus modBus, ModContainer container) {
        Essences.register(modBus);
        Cod.register(modBus);
        Lab.register(modBus);
        Port.register(modBus);
        Generators.register(modBus);
        RiftShards.register(modBus);
        Chains.register();
        ChainMachines.register(modBus);
        InflationCap.register();
        InflationBar.register();
        Decomposing.register(modBus);
        Recycling.register(modBus);
        Recycler.register(modBus);
        Defences.register(modBus);
        ServerConfig.register(container);
        modBus.addListener(WorksTabContents::add);
        // With both installed, as the pack plays, an island's players share one FTB party (#225).
        if (ModList.get().isLoaded("skyblockbuilder") && ModList.get().isLoaded("ftbteams")) {
            FtbPartySync.register();
        }
        // With Theurgy installed, as the pack plays, its HUD says why a locked apparatus does no work (#214).
        if (ModList.get().isLoaded("theurgy")) {
            TheurgyHud.register();
        }
    }
}
