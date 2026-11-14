// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
 * The Homunculus (the line's glossary, #367): the companion mod's made servant, which lives at its stand. Its item is
 * crafted by {@code mechanicalmasteryreborn:homunculus}, which life's Chapters stage file names, so it can be built only
 * while the team's life chain is unlocked; used on a stand, it takes its place there. The stand is one of life's chain
 * machines, by the mod's own {@code chain_machines/life} tag. A new Homunculus sleeps until a one-time jumpstart of FE
 * from any source fills the stand's store; then it eats from the stand's food, which automation fills, and works, the
 * faster the better the meal's quality. Once it has nothing left to eat it falls asleep and needs a new jumpstart.
 */
public final class Homunculus {

    private static final String SPENT = "spent";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);

    public static final DeferredItem<Item> ITEM = ITEMS.registerSimpleItem("homunculus", props -> props.stacksTo(1));
    public static final DeferredBlock<HomunculusStandBlock> STAND = BLOCKS.registerBlock("homunculus_stand", HomunculusStandBlock::new,
            props -> props.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion());
    public static final DeferredItem<BlockItem> STAND_ITEM = ITEMS.registerSimpleBlockItem("homunculus_stand", STAND);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HomunculusStandBlockEntity>> STAND_ENTITY =
            BLOCK_ENTITIES.register("homunculus_stand", () -> new BlockEntityType<>(HomunculusStandBlockEntity::new, STAND.get()));

    private Homunculus() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(Homunculus::registerCapabilities);
    }

    /** {@link HomunculusPace#interval} at the server config's interval and speed-up. */
    public static int interval(int level) {
        return HomunculusPace.interval(ServerConfig.homunculusInterval(), ServerConfig.homunculusSpeedUp(), level);
    }

    /** A Homunculus item: a new one, or a {@code spent} one taken off a stand after it had woken. */
    public static ItemStack item(boolean spent) {
        ItemStack stack = new ItemStack(ITEM.get());
        if (spent) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(SPENT, true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return stack;
    }

    /** Whether {@code stack} is a spent Homunculus, built again with an empty belly. */
    public static boolean spent(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(SPENT, false);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, STAND_ENTITY.get(), (stand, side) -> stand.food());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, STAND_ENTITY.get(), (stand, side) -> stand.store());
    }
}
