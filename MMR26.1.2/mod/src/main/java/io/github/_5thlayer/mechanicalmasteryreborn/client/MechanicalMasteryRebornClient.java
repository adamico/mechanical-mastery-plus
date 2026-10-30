// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingTable;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/**
 * The client's side: C.O.D.'s and the lab's screens, the items on a Recycler output's shelf, the recycling table EMI
 * shows, and how a team's Inflation bar is drawn.
 */
@Mod(value = MechanicalMasteryReborn.MOD_ID, dist = Dist.CLIENT)
public final class MechanicalMasteryRebornClient {

    public MechanicalMasteryRebornClient(IEventBus modBus) {
        modBus.addListener(MechanicalMasteryRebornClient::registerScreens);
        modBus.addListener(MechanicalMasteryRebornClient::registerRenderers);
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(RecyclingTable.TYPE,
                (table, context) -> RecyclingTable.receive(table)));
        NeoForge.EVENT_BUS.addListener(InflationBarOverlay::draw);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Cod.MENU.get(), CodScreen::new);
        event.register(Lab.MENU.get(), LabScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Recycler.OUTPUT_ENTITY.get(), RecyclerOutputRenderer::new);
    }
}
