// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.port;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
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
 * The four Ports, one per challenge, each taking its Transmuted Essence toward the challenge's quota (#224), and
 * their input as an item face on every side. Their items are in the Works tab, so EMI lists them, and nothing makes them.
 */
public final class Port {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);

    /** Each challenge's Transmuted Essence. */
    private static final Map<Handicap, Essence> ESSENCES = new EnumMap<>(Map.of(
            Handicap.ENERGY, Essence.GALVANIC, Handicap.PROTECTION, Essence.WARDING, Handicap.LIFE, Essence.VITAL, Handicap.MONEY, Essence.AUREATE));

    /** Each challenge's Port, unbreakable as C.O.D. is, lit while open and glowing once won. */
    public static final Map<Handicap, DeferredBlock<PortBlock>> BLOCKS_BY_HANDICAP = blocks();
    public static final Map<Handicap, DeferredItem<BlockItem>> ITEMS_BY_HANDICAP = items();
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register("port", () -> new BlockEntityType<>(PortBlockEntity::new,
                    Set.copyOf(BLOCKS_BY_HANDICAP.values().stream().map(DeferredBlock::get).toList())));

    private Port() {
    }

    /** {@code handicap}'s Transmuted Essence, which its Port takes. */
    public static Essence essence(Handicap handicap) {
        return ESSENCES.get(handicap);
    }

    /** The id of {@code handicap}'s Port, after its essence: {@code galvanic_port} for energy's. */
    public static String id(Handicap handicap) {
        String essence = essence(handicap).path();
        return essence.substring(0, essence.indexOf('_')) + "_port";
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(Port::registerCapabilities);
    }

    private static Map<Handicap, DeferredBlock<PortBlock>> blocks() {
        Map<Handicap, DeferredBlock<PortBlock>> blocks = new EnumMap<>(Handicap.class);
        for (Handicap handicap : Handicap.values()) {
            blocks.put(handicap, BLOCKS.registerBlock(id(handicap), props -> new PortBlock(handicap, props),
                    props -> props.mapColor(MapColor.METAL).strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noLootTable()
                            .lightLevel(state -> state.getValue(PortBlock.PHASE).light())));
        }
        return blocks;
    }

    private static Map<Handicap, DeferredItem<BlockItem>> items() {
        Map<Handicap, DeferredItem<BlockItem>> items = new EnumMap<>(Handicap.class);
        for (Handicap handicap : Handicap.values()) {
            items.put(handicap, ITEMS.registerSimpleBlockItem(id(handicap), BLOCKS_BY_HANDICAP.get(handicap)));
        }
        return items;
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BLOCK_ENTITY.get(), (port, side) -> port.input());
    }
}
