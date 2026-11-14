// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

/**
 * The Homunculus (the line's glossary, #367, ADR 0021): the companion mod's made figure, a {@link HomunculusEntity} built
 * like vanilla's golems, and its two-block-tall {@link HomunculusStandBlock}. Neither is crafted: a {@link #VITAL_SPARK}, whose
 * recipe life's Chapters stage file names, used on a lightning rod set on a clay block kindles them ({@link Kindling}), so a
 * team makes one only while its life chain is unlocked. The stand is one of life's chain machines, by the mod's own
 * {@code chain_machines/life} tag, and its {@link HomunculusStandBlockEntity} holds the food, the FE and the Homunculus's
 * belly; the Homunculus is its body, which walks near the stand and stands as a statue while it sleeps.
 */
public final class Homunculus {

    private static final String SPENT = "spent";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MechanicalMasteryReborn.MOD_ID);

    public static final DeferredItem<VitalSparkItem> VITAL_SPARK = ITEMS.registerItem("vital_spark", VitalSparkItem::new,
            props -> props.stacksTo(16));
    /** A clay block under a lightning rod, which it was. */
    public static final DeferredBlock<HomunculusStandBlock> STAND = BLOCKS.registerBlock("homunculus_stand", HomunculusStandBlock::new,
            props -> props.mapColor(MapColor.CLAY).strength(2.0F, 3.0F).sound(SoundType.GRAVEL).noOcclusion());
    public static final DeferredItem<BlockItem> STAND_ITEM = ITEMS.registerSimpleBlockItem("homunculus_stand", STAND);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HomunculusStandBlockEntity>> STAND_ENTITY =
            BLOCK_ENTITIES.register("homunculus_stand", () -> new BlockEntityType<>(HomunculusStandBlockEntity::new, STAND.get()));
    /** 18 pixels tall, its head half of it. */
    public static final DeferredHolder<EntityType<?>, EntityType<HomunculusEntity>> HOMUNCULUS = ENTITIES.registerEntityType("homunculus",
            HomunculusEntity::new, MobCategory.MISC, builder -> builder.sized(0.5F, 1.15F).eyeHeight(0.85F).clientTrackingRange(10));

    private Homunculus() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(Homunculus::registerCapabilities);
        modBus.addListener((EntityAttributeCreationEvent event) -> event.put(HOMUNCULUS.get(), HomunculusEntity.createAttributes().build()));
        NeoForge.EVENT_BUS.addListener(Kindling::kindle);
    }

    /** {@link HomunculusPace#interval} at the server config's interval and speed-up. */
    public static int interval(int level) {
        return HomunculusPace.interval(ServerConfig.homunculusInterval(), ServerConfig.homunculusSpeedUp(), level);
    }

    /** A Vital Spark: a new one, or a {@code spent} one, which a dead Homunculus leaves. */
    public static ItemStack vitalSpark(boolean spent) {
        ItemStack stack = new ItemStack(VITAL_SPARK.get());
        if (spent) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(SPENT, true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return stack;
    }

    /** Whether {@code stack} is a spent Vital Spark, which kindles a Homunculus with an empty belly. */
    public static boolean spent(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(SPENT, false);
    }

    /** The stand whose half is at {@code pos}, either half, or null. */
    public static @Nullable HomunculusStandBlockEntity standAt(Level level, BlockPos pos, BlockState state) {
        if (!state.is(STAND.get())) {
            return null;
        }
        BlockPos base = state.getValue(HomunculusStandBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        return level.getBlockEntity(base) instanceof HomunculusStandBlockEntity stand ? stand : null;
    }

    /** Both halves give the stand's food and its FE, the FE only while its Homunculus sleeps. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.Item.BLOCK, (level, pos, state, entity, side) -> {
            var stand = standAt(level, pos, state);
            return stand == null ? null : stand.food();
        }, STAND.get());
        event.registerBlock(Capabilities.Energy.BLOCK, (level, pos, state, entity, side) -> {
            var stand = standAt(level, pos, state);
            return stand == null ? null : stand.store();
        }, STAND.get());
    }
}
