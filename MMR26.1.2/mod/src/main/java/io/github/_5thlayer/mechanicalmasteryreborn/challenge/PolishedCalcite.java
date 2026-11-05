// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Polished Calcite, which the pack has the Assembler make from four calcite, and the End Portal Frame from four of it
 * (ADR 0012, #265). It is calcite as a block, mined and sounding as calcite does, drawn as vanilla's calcite under a
 * bevelled border of the mod's own. Cut from one raw kind alone, it is refined, so it is never recycled.
 */
public final class PolishedCalcite {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);

    public static final DeferredBlock<Block> BLOCK = BLOCKS.registerSimpleBlock("polished_calcite",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE));
    public static final DeferredItem<BlockItem> ITEM = ITEMS.registerSimpleBlockItem("polished_calcite", BLOCK);

    private PolishedCalcite() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
