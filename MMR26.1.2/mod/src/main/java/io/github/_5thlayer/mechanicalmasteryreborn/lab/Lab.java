// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import java.util.Set;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The lab's block, item, block entity and menus, its item face on every side once it is the Hermetic Lab (#263):
 * Quintessence out, Nullessence and Null Shards in, and no fluid, so no Essentia Vitae can be piped in; and what the lab's
 * menu sends its client of the asks to stop a challenge.
 */
public final class Lab {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MechanicalMasteryReborn.MOD_ID);

    /** Unbreakable, as C.O.D. is: every island starts with one, and no recipe makes another. */
    public static final DeferredBlock<LabBlock> BLOCK = BLOCKS.registerBlock("lab", LabBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noLootTable());
    public static final DeferredItem<BlockItem> ITEM = ITEMS.registerSimpleBlockItem("lab", BLOCK);
    /** Its work as the Hermetic Lab, once its team's last stand has started. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LabBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register("lab", () -> new BlockEntityType<>(LabBlockEntity::new, Set.of(BLOCK.get())));
    /** The extra data the server sends on opening is whether the lab shows its last stand's row ({@link LabMenu#open}). */
    public static final DeferredHolder<MenuType<?>, MenuType<LabMenu>> MENU =
            MENUS.register("lab", () -> IMenuTypeExtension.create(LabMenu::new));
    /** The team's refund, a page at a time (#259). */
    public static final DeferredHolder<MenuType<?>, MenuType<RefundMenu>> REFUND_MENU =
            MENUS.register("lab_refund", () -> new MenuType<>(RefundMenu::new, FeatureFlags.VANILLA_SET));
    /** The Hermetic Lab's spells and lines; the extra data the server sends on opening is the lab's position. */
    public static final DeferredHolder<MenuType<?>, MenuType<HermeticLabMenu>> HERMETIC_MENU =
            MENUS.register("hermetic_lab", () -> IMenuTypeExtension.create(HermeticLabMenu::new));

    private Lab() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) ->
                event.registerBlockEntity(Capabilities.Item.BLOCK, BLOCK_ENTITY.get(), (lab, side) -> lab.face()));
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1")
                .playToClient(LabMenu.Asks.TYPE, LabMenu.Asks.STREAM_CODEC));
    }
}
