// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.Set;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
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

/**
 * The Recycler's parts (the line's glossary: Recycler): its tier-1 core, the hand drive, the input, the
 * output and the casing, their items and block entities, and their item faces: an input's is its core's
 * buffer, insert only, and an output's its shelf, extract only, on every side.
 */
public final class Recycler {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);

    public static final DeferredBlock<RecyclerCoreBlock> CORE = BLOCKS.registerBlock("recycler_core_1", RecyclerCoreBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredBlock<HandDriveBlock> HAND_DRIVE = BLOCKS.registerBlock("hand_drive", HandDriveBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<RecyclerInputBlock> INPUT = BLOCKS.registerBlock("recycler_input", RecyclerInputBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<RecyclerOutputBlock> OUTPUT = BLOCKS.registerBlock("recycler_output", RecyclerOutputBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<RecyclerCasingBlock> CASING = BLOCKS.registerBlock("recycler_casing", RecyclerCasingBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));

    public static final DeferredItem<BlockItem> CORE_ITEM = ITEMS.registerSimpleBlockItem("recycler_core_1", CORE);
    public static final DeferredItem<BlockItem> HAND_DRIVE_ITEM = ITEMS.registerSimpleBlockItem("hand_drive", HAND_DRIVE);
    public static final DeferredItem<BlockItem> INPUT_ITEM = ITEMS.registerSimpleBlockItem("recycler_input", INPUT);
    public static final DeferredItem<BlockItem> OUTPUT_ITEM = ITEMS.registerSimpleBlockItem("recycler_output", OUTPUT);
    public static final DeferredItem<BlockItem> CASING_ITEM = ITEMS.registerSimpleBlockItem("recycler_casing", CASING);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RecyclerCoreBlockEntity>> CORE_ENTITY =
            BLOCK_ENTITIES.register("recycler_core_1", () -> new BlockEntityType<>(RecyclerCoreBlockEntity::new, Set.of(CORE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RecyclerOutputBlockEntity>> OUTPUT_ENTITY =
            BLOCK_ENTITIES.register("recycler_output", () -> new BlockEntityType<>(RecyclerOutputBlockEntity::new, Set.of(OUTPUT.get())));

    private Recycler() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(Recycler::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.Item.BLOCK,
                (level, pos, state, entity, side) -> RecyclerPart.coreOf(level, pos).map(RecyclerCoreBlockEntity::inputFace).orElse(null),
                INPUT.get());
        event.registerBlockEntity(Capabilities.Item.BLOCK, OUTPUT_ENTITY.get(), (output, side) -> output.face());
    }
}
