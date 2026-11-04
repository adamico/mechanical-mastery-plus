// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LifeHandicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.defence.Defences;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingTable;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * The client's side: C.O.D.'s and the lab's screens, the items on a Recycler output's shelf, the Electric Defence's sparks, the recycling table EMI
 * shows, the challenge a chain machine's tooltip names, what a food would feed under the life handicap, and how a team's Inflation bar is drawn.
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
        NeoForge.EVENT_BUS.addListener(MechanicalMasteryRebornClient::feedsNow);
        NeoForge.EVENT_BUS.addListener(InflationBarOverlay::draw);
    }

    /**
     * A chain machine's tooltip, in EMI and anywhere else, names the challenge that unlocks its work, until the player's
     * team has started it (#214, #246).
     */
    private static void nameChain(ItemTooltipEvent event) {
        ChainMachines.chainOf(event.getItemStack().getItem()).filter(ChainMachines.Table.received()::locked)
                .ifPresent(handicap -> event.getToolTip().add(ChainMachines.line(handicap).copy().withStyle(ChatFormatting.GOLD)));
    }

    /** A food's tooltip, while its player's Diet is kept, shows the share it would feed them now (#244). */
    private static void feedsNow(ItemTooltipEvent event) {
        var player = event.getEntity();
        if (player == null || !event.getItemStack().has(DataComponents.FOOD) || !LifeHandicap.kept(player)) {
            return;
        }
        long percent = Math.round(LifeHandicap.shareNow(player, event.getItemStack()) * 100);
        event.getToolTip().add(Component.translatable("mechanicalmasteryreborn.diet.feeds", percent)
                .withStyle(percent < 100 ? ChatFormatting.RED : ChatFormatting.GREEN));
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Cod.MENU.get(), CodScreen::new);
        event.register(Lab.MENU.get(), LabScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Recycler.OUTPUT_ENTITY.get(), RecyclerOutputRenderer::new);
        event.registerBlockEntityRenderer(Defences.FE_DEFENCE_ENTITY.get(), FeDefenceRenderer::new);
    }
}
