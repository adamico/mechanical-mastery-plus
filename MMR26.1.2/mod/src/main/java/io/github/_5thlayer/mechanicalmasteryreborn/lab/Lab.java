// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The lab's block, item and menu. */
public final class Lab {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MechanicalMasteryReborn.MOD_ID);

    /** Unbreakable, as C.O.D. is: every island starts with one, and no recipe makes another. */
    public static final DeferredBlock<LabBlock> BLOCK = BLOCKS.registerBlock("lab", LabBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noLootTable());
    public static final DeferredItem<BlockItem> ITEM = ITEMS.registerSimpleBlockItem("lab", BLOCK);
    public static final DeferredHolder<MenuType<?>, MenuType<LabMenu>> MENU =
            MENUS.register("lab", () -> new MenuType<>(LabMenu::new, FeatureFlags.VANILLA_SET));

    private Lab() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        MENUS.register(modBus);
    }
}
