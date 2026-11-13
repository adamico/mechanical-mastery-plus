// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Accounts;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * The Pelican (the line's glossary, #366): the vessel at the end of life's chain, which makes the Vital Essence by eating
 * meals, the items of the spoil class meal. Nothing crafts it: the Shop sells it behind {@link #PERMIT}, which a team's
 * Account holds while its life chain is unlocked, as the Wave Caller's. It is one of life's chain machines, by the mod's
 * own {@code chain_machines/life} tag, so it idles until its team starts life.
 */
public final class Pelican {

    /**
     * The Permit a team's Account is given when it starts the life challenge, kept by a win and taken back by a stop,
     * which the Pelican's Offer needs.
     */
    public static final String PERMIT = "reborn_life_started";

    /** The meals, the only food it eats: the pack's spoil class meal. */
    public static final TagKey<Item> MEALS = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "spoil_classes/meal"));

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);

    public static final DeferredBlock<PelicanBlock> BLOCK = BLOCKS.registerBlock("pelican", PelicanBlock::new,
            props -> props.mapColor(MapColor.TERRACOTTA_WHITE).strength(2.0F, 6.0F).sound(SoundType.DECORATED_POT));
    public static final DeferredItem<BlockItem> ITEM = ITEMS.registerSimpleBlockItem("pelican", BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PelicanBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pelican", () -> new BlockEntityType<>(PelicanBlockEntity::new, BLOCK.get()));

    private Pelican() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(Pelican::registerCapabilities);
    }

    /** What a batch must reach to be digested, from the server config. */
    public static Batch.Thresholds thresholds() {
        return new Batch.Thresholds(ServerConfig.pelicanSaturation(), ServerConfig.pelicanKinds(), ServerConfig.pelicanQuality());
    }

    /** The ticks a ready batch digests for, from the server config. */
    public static int digestingTicks() {
        return ServerConfig.pelicanDigesting();
    }

    /** Whether {@code resource} is a meal; rotten flesh never is. */
    static boolean meal(ItemResource resource) {
        return !resource.isEmpty() && !resource.is(Items.ROTTEN_FLESH) && resource.typeHolder().is(MEALS);
    }

    /** Gives {@code team}'s Account {@link #PERMIT} if its life chain is unlocked, by a start not stopped since or a win. */
    public static void grant(MinecraftServer server, UUID team) {
        if (TeamState.get(server).unlocked(team, Handicap.LIFE)) {
            Accounts.get().grant(server, team, PERMIT);
        }
    }

    /** Takes {@link #PERMIT} from {@code team}'s Account, as a stop locks its life chain again (ADR 0013). */
    public static void lock(MinecraftServer server, UUID team) {
        Accounts.get().revoke(server, team, PERMIT);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BLOCK_ENTITY.get(), (pelican, side) -> pelican.face());
    }
}
