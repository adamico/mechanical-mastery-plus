// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The game tests' block a Homunculus works (#368), in the game-test pack's {@code homunculus_works} tag: it keeps every
 * right-click it is given and uses up nothing. Clicked with paper it plays a block that hands back another item: the paper
 * becomes a map, and a feather goes into the clicker's inventory.
 */
final class ClickedBlock extends Block {

    /** One right-click, as the block was given it. */
    record Click(UUID by, boolean fake, boolean sneaking, InteractionHand hand, ItemStack held, Direction face, Vec3 at) {
    }

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);
    static final DeferredBlock<ClickedBlock> CLICKED = BLOCKS.registerBlock("gametest/clicked", ClickedBlock::new, props -> props.strength(1F));

    /** The clicks each clicked block was given, by its position. */
    private static final Map<BlockPos, List<Click>> CLICKS = new HashMap<>();

    private ClickedBlock(Properties properties) {
        super(properties);
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    /** The clicks the block at {@code pos} was given, oldest first. */
    static synchronized List<Click> clicks(BlockPos pos) {
        return List.copyOf(CLICKS.getOrDefault(pos, List.of()));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        synchronized (ClickedBlock.class) {
            CLICKS.computeIfAbsent(pos.immutable(), at -> new ArrayList<>()).add(new Click(player.getUUID(), player.isFakePlayer(),
                    player.isShiftKeyDown(), hand, held.copy(), hit.getDirection(), hit.getLocation()));
        }
        if (held.is(Items.PAPER)) {
            player.setItemInHand(hand, new ItemStack(Items.MAP));
            player.getInventory().add(new ItemStack(Items.FEATHER));
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
