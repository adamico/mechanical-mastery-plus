// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerLayout;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerOutputBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerOutputBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * A Recycler a game test builds, its core at {@link #CORE} on the platform, its eight places given in the order
 * {@link RecyclerLayout} reads them: clockwise around the centre from the front-left one, then the centre. Each
 * output faces outward, away from the centre, and one in the centre faces the core's front.
 */
final class RecyclerBuild {

    static final BlockPos CORE = new BlockPos(4, 2, 4);

    /** What the game-test pack's recipe decomposes one Advanced Mechanical Essence into, in order. */
    static final List<ItemStack> ADVANCED_MIX = List.of(new ItemStack(Items.PISTON), new ItemStack(Items.HOPPER),
            new ItemStack(Items.COMPARATOR), new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.LAVA_BUCKET));

    /** Two outputs first, then a casing, a drive, a casing, a second drive, a casing, and the input in the centre. */
    static final Block[] STANDARD = {Recycler.OUTPUT.get(), Recycler.OUTPUT.get(), Recycler.CASING.get(), Recycler.HAND_DRIVE.get(),
            Recycler.CASING.get(), Recycler.HAND_DRIVE.get(), Recycler.CASING.get(), Recycler.INPUT.get()};

    /** The place in the 3x3's centre. */
    static final int CENTRE = 7;

    final BlockPos core;

    final GameTestHelper helper;
    final Direction front;
    /** The places' positions, clockwise around the centre from the front-left one, then the centre. */
    final List<BlockPos> places = new ArrayList<>();

    private RecyclerBuild(GameTestHelper helper, BlockPos core, Direction front) {
        this.helper = helper;
        this.core = core;
        this.front = front;
        for (int[] place : RecyclerLayout.places(front.getStepX(), front.getStepZ())) {
            places.add(core.offset(place[0], 0, place[1]));
        }
    }

    /** The standard build, its core facing south. */
    static RecyclerBuild standard(GameTestHelper helper) {
        return build(helper, Direction.SOUTH, STANDARD);
    }

    /** A core at {@link #CORE} facing {@code front}, and {@code parts} in its eight places, null for none. */
    static RecyclerBuild build(GameTestHelper helper, Direction front, Block[] parts) {
        return build(helper, CORE, front, parts);
    }

    /** A core at {@code core} facing {@code front}, and {@code parts} in its eight places, null for none. */
    static RecyclerBuild build(GameTestHelper helper, BlockPos core, Direction front, Block[] parts) {
        var build = new RecyclerBuild(helper, core, front);
        helper.setBlock(core, Recycler.CORE.get().defaultBlockState().setValue(RecyclerCoreBlock.FACING, front));
        for (int i = 0; i < parts.length; i++) {
            build.place(i, parts[i]);
        }
        return build;
    }

    /** Puts {@code block} in place {@code i}, an output facing away from the centre. */
    void place(int i, Block block) {
        BlockPos pos = places.get(i);
        if (block == null) {
            return;
        }
        var state = block.defaultBlockState();
        if (block instanceof RecyclerOutputBlock) {
            BlockPos centre = places.get(CENTRE);
            Direction outward = pos.equals(centre) ? front
                    : Direction.getApproximateNearest(pos.getX() - centre.getX(), 0, pos.getZ() - centre.getZ());
            state = state.setValue(RecyclerOutputBlock.FACING, outward);
        }
        helper.setBlock(pos, state);
    }

    RecyclerCoreBlockEntity core() {
        return helper.getBlockEntity(core, RecyclerCoreBlockEntity.class);
    }

    /** The place of the {@code n}th output, counting from 0 in the order the mix fills them. */
    BlockPos output(int n) {
        for (BlockPos pos : places) {
            if (helper.getBlockState(pos).getBlock() instanceof RecyclerOutputBlock && n-- == 0) {
                return pos;
            }
        }
        throw new IllegalArgumentException("no output " + n);
    }

    RecyclerOutputBlockEntity outputEntity(int n) {
        return helper.getBlockEntity(output(n), RecyclerOutputBlockEntity.class);
    }

    BlockPos firstOf(Block block) {
        for (BlockPos pos : places) {
            if (helper.getBlockState(pos).is(block)) {
                return pos;
            }
        }
        throw new IllegalArgumentException("no " + block);
    }

    /** Feeds {@code count} Advanced essence through the core's input face, as an input passes it on. */
    void feed(int count) {
        feed(new ItemStack(Essences.item(Essence.ADVANCED), count));
    }

    /** Feeds {@code stack} through the core's input face, and says how many it took. */
    int feed(ItemStack stack) {
        try (var transaction = Transaction.openRoot()) {
            int taken = core().inputFace().insert(ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
            return taken;
        }
    }

    /** Turns the core {@code turns} times, as its drives would. */
    void turn(int turns) {
        for (int i = 0; i < turns; i++) {
            core().turn();
        }
    }

    /** Takes what output position {@code position}, counted across the outputs in order, holds, as a hopper would. */
    ItemStack take(int position) {
        var output = outputEntity(position / RecyclerLayout.POSITIONS_PER_OUTPUT);
        int index = position % RecyclerLayout.POSITIONS_PER_OUTPUT;
        ItemStack held = output.item(index);
        try (var transaction = Transaction.openRoot()) {
            int taken = output.face().extract(index, ItemResource.of(held), held.getCount(), transaction);
            transaction.commit();
            return held.copyWithCount(taken);
        }
    }

    /** Whether every output position is empty. */
    boolean outputsEmpty() {
        for (BlockPos pos : places) {
            if (helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof RecyclerOutputBlockEntity output) {
                for (int p = 0; p < RecyclerLayout.POSITIONS_PER_OUTPUT; p++) {
                    if (!output.item(p).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Fails unless output position {@code position}, counted across the outputs in order, holds {@code expected}. */
    void expectPosition(int position, ItemStack expected) {
        ItemStack held = outputEntity(position / RecyclerLayout.POSITIONS_PER_OUTPUT).item(position % RecyclerLayout.POSITIONS_PER_OUTPUT);
        helper.assertTrue(ItemStack.isSameItemSameComponents(held, expected) && held.getCount() == expected.getCount(),
                "output position " + position + " holds " + held + ", not " + expected);
    }

    /** Fails unless the outputs hold {@code expected}, result i on position i, and nothing else. */
    void expectOutputs(ItemStack... expected) {
        int positions = 0;
        for (BlockPos pos : places) {
            if (helper.getBlockState(pos).getBlock() instanceof RecyclerOutputBlock) {
                positions += RecyclerLayout.POSITIONS_PER_OUTPUT;
            }
        }
        for (int i = 0; i < positions; i++) {
            expectPosition(i, i < expected.length ? expected[i] : ItemStack.EMPTY);
        }
    }

    /** Fails unless the outputs hold the Advanced mix, result i on position i. */
    void expectAdvancedMix() {
        for (int i = 0; i < ADVANCED_MIX.size(); i++) {
            expectPosition(i, ADVANCED_MIX.get(i));
        }
    }
}
