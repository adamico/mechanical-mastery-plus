// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
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
 * Ward Tallow, the fluid the protection challenge's Chain renders from the Assault's flesh and bone meal with steam,
 * and charges the Rift Shards with (#252): the fluid type, still and flowing fluids, a liquid block and a bucket, as
 * Fieldworks' fluids have, so an Assembler's drain and fill slots take it. It is in no mix and the Shop never buys it.
 */
public final class WardTallow {

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> TYPE = FLUID_TYPES.register(
            "ward_tallow",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type." + MechanicalMasteryReborn.MOD_ID + ".ward_tallow")
                    .lightLevel(0)
                    .density(900)
                    .temperature(320)
                    .viscosity(3000)
                    .rarity(Rarity.COMMON)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> SOURCE =
            FLUIDS.register("ward_tallow", () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING =
            FLUIDS.register("flowing_ward_tallow", () -> new BaseFlowingFluid.Flowing(properties()));

    public static final DeferredHolder<Block, LiquidBlock> BLOCK = BLOCKS.registerBlock("ward_tallow",
            props -> new LiquidBlock(SOURCE.get(), liquidProperties(props)));

    /** A plain {@link BucketItem}, not a subclass: NeoForge gives the fluid item capability to that exact class only. */
    public static final DeferredItem<BucketItem> BUCKET = ITEMS.registerItem("ward_tallow_bucket",
            props -> new BucketItem(SOURCE.get(), props),
            props -> props.craftRemainder(Items.BUCKET).stacksTo(1));

    private WardTallow() {
    }

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(TYPE, SOURCE, FLOWING).block(BLOCK).bucket(BUCKET);
    }

    /** Takes the properties rather than making them: a block's registry id rides on them. */
    private static BlockBehaviour.Properties liquidProperties(BlockBehaviour.Properties props) {
        return props
                .mapColor(MapColor.SAND)
                .replaceable()
                .noCollision()
                .strength(100.0F)
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
