// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.defence;

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
 * The Defences (the line's glossary: Defence, MMR26.1.2 ADR 0010): blocks that fight the Assault's mobs on their own,
 * fed by the factory and never for nothing, and free from the start. The first is the FE defence (#236), whose face is
 * its store, FE in only, on every side.
 */
public final class Defences {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);

    public static final DeferredBlock<FeDefenceBlock> FE_DEFENCE = BLOCKS.registerBlock("fe_defence", FeDefenceBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> FE_DEFENCE_ITEM = ITEMS.registerSimpleBlockItem("fe_defence", FE_DEFENCE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FeDefenceBlockEntity>> FE_DEFENCE_ENTITY =
            BLOCK_ENTITIES.register("fe_defence", () -> new BlockEntityType<>(FeDefenceBlockEntity::new, Set.of(FE_DEFENCE.get())));

    private Defences() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(Defences::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, FE_DEFENCE_ENTITY.get(), (defence, side) -> defence.face());
    }
}
