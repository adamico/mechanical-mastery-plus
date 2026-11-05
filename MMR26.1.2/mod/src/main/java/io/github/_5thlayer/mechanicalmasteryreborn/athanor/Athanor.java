// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.athanor;

import java.util.Set;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The Athanor's block, item, block entity and menu (#263, ADR 0014), and its item face on every side: Quintessence out,
 * Nullessence and Null Shards in. It answers for no fluid, so no Essentia Vitae can be piped in.
 */
public final class Athanor {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MechanicalMasteryReborn.MOD_ID);

    public static final DeferredBlock<AthanorBlock> BLOCK = BLOCKS.registerBlock("athanor", AthanorBlock::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(5.0F, 1200.0F).sound(SoundType.NETHER_BRICKS).lightLevel(state -> 7)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> ITEM = ITEMS.registerSimpleBlockItem("athanor", BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AthanorBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register("athanor", () -> new BlockEntityType<>(AthanorBlockEntity::new, Set.of(BLOCK.get())));
    /** The extra data the server sends on opening is the Athanor's position. */
    public static final DeferredHolder<MenuType<?>, MenuType<AthanorMenu>> MENU =
            MENUS.register("athanor", () -> IMenuTypeExtension.create(AthanorMenu::new));

    private Athanor() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(Athanor::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BLOCK_ENTITY.get(), (athanor, side) -> athanor.face());
    }
}
