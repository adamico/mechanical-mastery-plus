// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

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

/** C.O.D.'s block, item, block entity and menu, and its reserve as an item face on every side. */
public final class Cod {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MechanicalMasteryReborn.MOD_ID);

    /** Unbreakable, as the adversary of the line that cannot be shut down. */
    public static final DeferredBlock<CodBlock> BLOCK = BLOCKS.registerBlock("cod", CodBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noLootTable());
    public static final DeferredItem<BlockItem> ITEM = ITEMS.registerSimpleBlockItem("cod", BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CodBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register("cod", () -> new BlockEntityType<>(CodBlockEntity::new, Set.of(BLOCK.get())));
    /** The extra data the server sends on opening is C.O.D.'s position. */
    public static final DeferredHolder<MenuType<?>, MenuType<CodMenu>> MENU =
            MENUS.register("cod", () -> IMenuTypeExtension.create(CodMenu::new));

    private Cod() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(Cod::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BLOCK_ENTITY.get(), (cod, side) -> cod.reserve());
    }
}
