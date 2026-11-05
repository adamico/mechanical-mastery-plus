// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LifeHandicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.NullShards;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.WardTallow;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlock;
import io.github._5thlayer.mechanicalmasteryreborn.compat.guideme.PlacedBlockPages;
import io.github._5thlayer.mechanicalmasteryreborn.defence.Defences;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabMenu;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingTable;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * The client's side: C.O.D.'s and the lab's screens, the items on a Recycler output's shelf, the Electric Defence's sparks, the recycling table EMI
 * shows, the challenge a chain machine's tooltip names, the fish on C.O.D.'s lid, how Ward Tallow draws, what a food would feed under the life handicap, and how a team's Inflation bar is drawn.
 */
@Mod(value = MechanicalMasteryReborn.MOD_ID, dist = Dist.CLIENT)
public final class MechanicalMasteryRebornClient {

    public MechanicalMasteryRebornClient(IEventBus modBus) {
        modBus.addListener(MechanicalMasteryRebornClient::registerScreens);
        modBus.addListener(MechanicalMasteryRebornClient::registerRenderers);
        modBus.addListener(MechanicalMasteryRebornClient::registerFluidModels);
        modBus.addListener(MechanicalMasteryRebornClient::registerBlockTints);
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(RecyclingTable.TYPE,
                (table, context) -> RecyclingTable.receive(table)));
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(ChainMachines.Table.TYPE,
                (table, context) -> ChainMachines.Table.receive(table)));
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(LabMenu.Asks.TYPE,
                (asks, context) -> LabMenu.Asks.receive(asks, context.player())));
        NeoForge.EVENT_BUS.addListener(MechanicalMasteryRebornClient::nameChain);
        NeoForge.EVENT_BUS.addListener(MechanicalMasteryRebornClient::feedsNow);
        NeoForge.EVENT_BUS.addListener(MechanicalMasteryRebornClient::nullShardUse);
        NeoForge.EVENT_BUS.addListener(InflationBarOverlay::draw);
        // With GuideME, as the pack plays, its key opens the manual's page of a block that only ever stands placed.
        if (ModList.get().isLoaded("guideme")) {
            PlacedBlockPages.register();
        }
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

    /** A Null Shard's tooltip says what it is for, since no recipe uses it (#263). */
    private static void nullShardUse(ItemTooltipEvent event) {
        if (event.getItemStack().is(NullShards.ITEM.get())) {
            event.getToolTip().add(Component.translatable("item.mechanicalmasteryreborn.null_shard.tooltip").withStyle(ChatFormatting.GRAY));
        }
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Cod.MENU.get(), CodScreen::new);
        event.register(Lab.HERMETIC_MENU.get(), HermeticLabScreen::new);
        event.register(Lab.MENU.get(), LabScreen::new);
        event.register(Lab.REFUND_MENU.get(), RefundScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Recycler.OUTPUT_ENTITY.get(), RecyclerOutputRenderer::new);
        event.registerBlockEntityRenderer(Defences.FE_DEFENCE_ENTITY.get(), FeDefenceRenderer::new);
    }

    /** Ward Tallow in the world, a tank or a pipe: vanilla water's sprites, drawn to be tinted, under a tallow tint. */
    private static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(new FluidModel.Unbaked(new Material(Identifier.withDefaultNamespace("block/water_still")),
                new Material(Identifier.withDefaultNamespace("block/water_flow")), null, FluidTintSources.constant(0xFFE6D3A3)),
                WardTallow.SOURCE, WardTallow.FLOWING);
    }

    /**
     * The fish engraved on C.O.D.'s lid, vanilla's cod read from the player's own game (libworks ADR 0012), lit green
     * while C.O.D. runs and dark once it is shut down. The item draws it green too.
     */
    private static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return state.getValue(CodBlock.SHUT_DOWN) ? 0xFF505458 : 0xFF50DC70;
            }

            @Override
            public Set<Property<?>> relevantProperties() {
                return Set.of(CodBlock.SHUT_DOWN);
            }
        }), Cod.BLOCK.get());
    }
}
