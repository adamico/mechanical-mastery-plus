// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The money challenge's mint (ADR 0017, #320), what the pack's assembling recipes melt, cast and knock out a Coin
 * with: for each of copper, iron and gold a {@link Metal}, its molten metal, as Ward Tallow is a fluid, with its block
 * and bucket so pipes, tanks and an Assembler's drain and fill slots take it, its cast, a Mold filled with that metal,
 * which knows its coin, and its Coin; and, for every metal, the Mold (the line's glossary) and Burnt Sand, what the
 * shakeout leaves. None is in a mix, and the Shop buys none.
 */
public final class Mint {

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);

    public static final Metal COPPER = new Metal("copper", MapColor.COLOR_ORANGE);
    public static final Metal IRON = new Metal("iron", MapColor.METAL);
    public static final Metal GOLD = new Metal("gold", MapColor.GOLD);
    /** The mint's metals, in the order the Works tab lists them. */
    public static final List<Metal> METALS = List.of(COPPER, IRON, GOLD);

    public static final DeferredItem<Item> MOLD = ITEMS.registerSimpleItem("mold");
    public static final DeferredItem<Item> BURNT_SAND = ITEMS.registerSimpleItem("burnt_sand");

    private Mint() {
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    /**
     * One metal of the mint, named as its ids are: {@code molten_<name>}, its flowing fluid, block and bucket,
     * {@code <name>_cast} and {@code <name>_coin}.
     */
    public static final class Metal {

        private final String name;
        public final DeferredHolder<FluidType, FluidType> type;
        public final DeferredHolder<Fluid, BaseFlowingFluid.Source> molten;
        public final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
        public final DeferredHolder<Block, LiquidBlock> block;
        /** A plain {@link BucketItem}, not a subclass: NeoForge gives the fluid item capability to that exact class only. */
        public final DeferredItem<BucketItem> bucket;
        public final DeferredItem<Item> cast;
        public final DeferredItem<Item> coin;

        private Metal(String name, MapColor mapColor) {
            this.name = name;
            String molten = "molten_" + name;
            type = FLUID_TYPES.register(molten, () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type." + MechanicalMasteryReborn.MOD_ID + "." + molten)
                    .lightLevel(15)
                    .density(3000)
                    .temperature(1300)
                    .viscosity(6000)
                    .rarity(Rarity.COMMON)));
            this.molten = FLUIDS.register(molten, () -> new BaseFlowingFluid.Source(properties()));
            flowing = FLUIDS.register("flowing_" + molten, () -> new BaseFlowingFluid.Flowing(properties()));
            block = BLOCKS.registerBlock(molten, props -> new LiquidBlock(this.molten.get(), liquidProperties(props, mapColor)));
            bucket = ITEMS.registerItem(molten + "_bucket", props -> new BucketItem(this.molten.get(), props),
                    props -> props.craftRemainder(Items.BUCKET).stacksTo(1));
            cast = ITEMS.registerSimpleItem(name + "_cast");
            coin = ITEMS.registerSimpleItem(name + "_coin");
        }

        /** The metal's name, as in its ids. */
        public String name() {
            return name;
        }

        private BaseFlowingFluid.Properties properties() {
            return new BaseFlowingFluid.Properties(type, molten, flowing).block(block).bucket(bucket);
        }

        /** Takes the properties rather than making them: a block's registry id rides on them. */
        private static BlockBehaviour.Properties liquidProperties(BlockBehaviour.Properties props, MapColor mapColor) {
            return props
                    .mapColor(mapColor)
                    .replaceable()
                    .noCollision()
                    .strength(100.0F)
                    .lightLevel(state -> 15)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .liquid()
                    .sound(SoundType.EMPTY);
        }
    }
}
