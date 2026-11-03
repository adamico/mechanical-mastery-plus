// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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
 * The Recycler's parts (the line's glossary: Recycler): its cores, one for each of the {@link #TIERS} tiers, the hand and FE drives, the input, the
 * output and the casing, their items and block entities, and their faces: an input's is its core's
 * buffer, insert only, an output's its shelf, extract only, and an FE drive's its store, FE in only, on every side.
 */
public final class Recycler {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);

    /** How many core tiers there are (the line's glossary: Core). */
    public static final int TIERS = 4;

    /** The cores, the tier-1 one first; each is {@code recycler_core_<tier>}. */
    private static final List<DeferredBlock<RecyclerCoreBlock>> CORES = IntStream.rangeClosed(1, TIERS)
            .mapToObj(tier -> BLOCKS.registerBlock("recycler_core_" + tier, props -> new RecyclerCoreBlock(tier, props),
                    props -> props.mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()))
            .toList();
    private static final List<DeferredItem<RecyclerCoreItem>> CORE_ITEMS = CORES.stream()
            .map(core -> ITEMS.registerItem(core.getId().getPath(), props -> new RecyclerCoreItem(core.get(), props.useBlockDescriptionPrefix())))
            .toList();

    /** The tier-1 core. */
    public static final DeferredBlock<RecyclerCoreBlock> CORE = CORES.getFirst();
    public static final DeferredBlock<HandDriveBlock> HAND_DRIVE = BLOCKS.registerBlock("hand_drive", HandDriveBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<FeDriveBlock> FE_DRIVE = BLOCKS.registerBlock("fe_drive", FeDriveBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL));
    public static final DeferredBlock<RecyclerInputBlock> INPUT = BLOCKS.registerBlock("recycler_input", RecyclerInputBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<RecyclerOutputBlock> OUTPUT = BLOCKS.registerBlock("recycler_output", RecyclerOutputBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<RecyclerCasingBlock> CASING = BLOCKS.registerBlock("recycler_casing", RecyclerCasingBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));

    /** The tier-1 core's item. */
    public static final DeferredItem<RecyclerCoreItem> CORE_ITEM = CORE_ITEMS.getFirst();
    public static final DeferredItem<BlockItem> HAND_DRIVE_ITEM = ITEMS.registerSimpleBlockItem("hand_drive", HAND_DRIVE);
    public static final DeferredItem<BlockItem> FE_DRIVE_ITEM = ITEMS.registerSimpleBlockItem("fe_drive", FE_DRIVE);
    public static final DeferredItem<BlockItem> INPUT_ITEM = ITEMS.registerSimpleBlockItem("recycler_input", INPUT);
    public static final DeferredItem<BlockItem> OUTPUT_ITEM = ITEMS.registerSimpleBlockItem("recycler_output", OUTPUT);
    public static final DeferredItem<BlockItem> CASING_ITEM = ITEMS.registerSimpleBlockItem("recycler_casing", CASING);

    /** Every tier's core entity, under the tier-1 core's id it was released with, kept so saved worlds load. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RecyclerCoreBlockEntity>> CORE_ENTITY =
            BLOCK_ENTITIES.register("recycler_core_1", () -> new BlockEntityType<>(RecyclerCoreBlockEntity::new,
                    Set.copyOf(CORES.stream().map(DeferredBlock::get).toList())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FeDriveBlockEntity>> FE_DRIVE_ENTITY =
            BLOCK_ENTITIES.register("fe_drive", () -> new BlockEntityType<>(FeDriveBlockEntity::new, Set.of(FE_DRIVE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RecyclerOutputBlockEntity>> OUTPUT_ENTITY =
            BLOCK_ENTITIES.register("recycler_output", () -> new BlockEntityType<>(RecyclerOutputBlockEntity::new, Set.of(OUTPUT.get())));

    private Recycler() {
    }

    /** The core of {@code tier}, from 1 to {@link #TIERS}. */
    public static RecyclerCoreBlock core(int tier) {
        return CORES.get(tier - 1).get();
    }

    /** The item of the core of {@code tier}, from 1 to {@link #TIERS}. */
    public static RecyclerCoreItem coreItem(int tier) {
        return CORE_ITEMS.get(tier - 1).get();
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
        event.registerBlockEntity(Capabilities.Energy.BLOCK, FE_DRIVE_ENTITY.get(), (drive, side) -> drive.face());
    }
}
