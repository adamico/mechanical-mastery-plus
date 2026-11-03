// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingTable;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * The client's side: C.O.D.'s and the lab's screens, the items on a Recycler output's shelf, the recycling table EMI
 * shows, the challenge a chain machine's tooltip names, and how a team's Inflation bar is drawn.
 */
@Mod(value = MechanicalMasteryReborn.MOD_ID, dist = Dist.CLIENT)
public final class MechanicalMasteryRebornClient {

    public MechanicalMasteryRebornClient(IEventBus modBus) {
        modBus.addListener(MechanicalMasteryRebornClient::registerScreens);
        modBus.addListener(MechanicalMasteryRebornClient::registerRenderers);
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(RecyclingTable.TYPE,
                (table, context) -> RecyclingTable.receive(table)));
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(ChainMachines.Table.TYPE,
                (table, context) -> ChainMachines.Table.receive(table)));
        NeoForge.EVENT_BUS.addListener(MechanicalMasteryRebornClient::nameChain);
        NeoForge.EVENT_BUS.addListener(InflationBarOverlay::draw);
    }

    /** A chain machine's tooltip, in EMI and anywhere else, names the challenge that unlocks it (#214). */
    private static void nameChain(ItemTooltipEvent event) {
        Handicap handicap = ChainMachines.Table.received().machines().get(event.getItemStack().getItem());
        if (handicap != null) {
            event.getToolTip().add(ChainMachines.line(handicap).copy().withStyle(ChatFormatting.GOLD));
        }
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Cod.MENU.get(), CodScreen::new);
        event.register(Lab.MENU.get(), LabScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Recycler.OUTPUT_ENTITY.get(), RecyclerOutputRenderer::new);
    }
}
