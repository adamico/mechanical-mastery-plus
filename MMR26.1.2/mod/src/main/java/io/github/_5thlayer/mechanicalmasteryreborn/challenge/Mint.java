// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

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
 * with: molten gold, as Ward Tallow is a fluid, with its block and bucket so pipes, tanks and an Assembler's drain and
 * fill slots take it; the Mold (the line's glossary), one for every metal; the gold cast, a Mold filled with gold, which
 * knows its coin; Burnt Sand, what the shakeout leaves; and the Gold Coin. None is in a mix, and the Shop buys none.
 */
public final class Mint {

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_GOLD_TYPE = FLUID_TYPES.register(
            "molten_gold",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type." + MechanicalMasteryReborn.MOD_ID + ".molten_gold")
                    .lightLevel(15)
                    .density(3000)
                    .temperature(1300)
                    .viscosity(6000)
                    .rarity(Rarity.COMMON)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_GOLD =
            FLUIDS.register("molten_gold", () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MOLTEN_GOLD =
            FLUIDS.register("flowing_molten_gold", () -> new BaseFlowingFluid.Flowing(properties()));

    public static final DeferredHolder<Block, LiquidBlock> MOLTEN_GOLD_BLOCK = BLOCKS.registerBlock("molten_gold",
            props -> new LiquidBlock(MOLTEN_GOLD.get(), liquidProperties(props)));

    /** A plain {@link BucketItem}, not a subclass: NeoForge gives the fluid item capability to that exact class only. */
    public static final DeferredItem<BucketItem> MOLTEN_GOLD_BUCKET = ITEMS.registerItem("molten_gold_bucket",
            props -> new BucketItem(MOLTEN_GOLD.get(), props),
            props -> props.craftRemainder(Items.BUCKET).stacksTo(1));

    public static final DeferredItem<Item> MOLD = ITEMS.registerSimpleItem("mold");
    public static final DeferredItem<Item> GOLD_CAST = ITEMS.registerSimpleItem("gold_cast");
    public static final DeferredItem<Item> BURNT_SAND = ITEMS.registerSimpleItem("burnt_sand");
    public static final DeferredItem<Item> GOLD_COIN = ITEMS.registerSimpleItem("gold_coin");

    private Mint() {
    }

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(MOLTEN_GOLD_TYPE, MOLTEN_GOLD, FLOWING_MOLTEN_GOLD)
                .block(MOLTEN_GOLD_BLOCK).bucket(MOLTEN_GOLD_BUCKET);
    }

    /** Takes the properties rather than making them: a block's registry id rides on them. */
    private static BlockBehaviour.Properties liquidProperties(BlockBehaviour.Properties props) {
        return props
                .mapColor(MapColor.GOLD)
                .replaceable()
                .noCollision()
                .strength(100.0F)
                .lightLevel(state -> 15)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY);
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
