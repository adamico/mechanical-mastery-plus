// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Status;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerFloor;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerOutputBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerPart;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * A Recycler's floors (#217): as many in all as its core's tier, each above the core's a full 3x3 of any parts,
 * counted upward to the first that isn't full; its outputs fill floor by floor, and a place two cores would claim
 * belongs to neither.
 */
final class FloorTests {

    private FloorTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("floor_tier_1_claims_nothing_above", 1, FloorTests::tier1ClaimsNothingAbove);
        tests.test("floor_tier_2_counts_a_full_floor", 1, FloorTests::tier2CountsAFullFloor);
        tests.test("floor_parts_work_from_any_floor", 1, FloorTests::partsWorkFromAnyFloor);
        tests.test("floor_missing_a_block_counts_no_more", 1, FloorTests::missingABlock);
        tests.test("floor_above_the_tier_counts_once_raised", 1, FloorTests::raised);
        tests.test("floor_outputs_fill_upward", 1, FloorTests::fillUpward);
        tests.test("floor_shared_place_belongs_to_neither", 1, FloorTests::shared);
    }

    private static void tier1ClaimsNothingAbove(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.buildFloor(1, RecyclerBuild.casings());
        helper.assertValueEqual(build.core().floors(), 1, "a tier-1 Recycler's floors");
        for (BlockPos pos : build.floor(1)) {
            helper.assertValueEqual(helper.getBlockState(pos).getValue(RecyclerPart.PLACE), RecyclerPlace.NONE, "the place of " + pos);
        }
        helper.assertValueEqual(helper.getBlockState(build.places.get(0)).getValue(RecyclerPart.FLOOR), RecyclerFloor.SINGLE,
                "the floor of a part of a one-floor Recycler");
        helper.succeed();
    }

    private static void tier2CountsAFullFloor(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.swapCore(2);
        build.buildFloor(1, RecyclerBuild.casings());
        helper.assertValueEqual(build.core().floors(), 2, "a tier-2 Recycler's floors");
        for (BlockPos pos : build.floor(1)) {
            helper.assertTrue(helper.getBlockState(pos).getValue(RecyclerPart.PLACE) != RecyclerPlace.NONE, "no place at " + pos);
            helper.assertValueEqual(helper.getBlockState(pos).getValue(RecyclerPart.FLOOR), RecyclerFloor.TOP, "the floor at " + pos);
        }
        helper.assertValueEqual(helper.getBlockState(build.places.get(0)).getValue(RecyclerPart.FLOOR), RecyclerFloor.BOTTOM,
                "the floor of a part on the core's");
        helper.assertTrue(helper.getBlockState(build.core).getValue(RecyclerCoreBlock.TALL), "the core of a two-floor Recycler isn't tall");
        helper.succeed();
    }

    private static void partsWorkFromAnyFloor(GameTestHelper helper) {
        Block casing = Recycler.CASING.get();
        var build = RecyclerBuild.build(helper, Direction.SOUTH,
                new Block[] {Recycler.HAND_DRIVE.get(), casing, casing, casing, casing, casing, casing, casing});
        build.swapCore(2);
        helper.assertValueEqual(build.core().status(), Status.INCOMPLETE, "a Recycler with no input or output");
        build.buildFloor(1, Recycler.OUTPUT.get(), Recycler.OUTPUT.get(), casing, casing, casing, casing, casing, casing, Recycler.INPUT.get());
        build.feed(1);
        helper.assertValueEqual(build.core().status(), Status.READY, "a Recycler with its input and outputs a floor up");
        build.turn(ServerConfig.turnsOf(2));
        expectMixFrom(helper, build.floor(1).get(0), 0);
        expectMixFrom(helper, build.floor(1).get(1), 3);
        helper.succeed();
    }

    private static void missingABlock(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.swapCore(3);
        build.buildFloor(1, RecyclerBuild.casings());
        build.buildFloor(2, RecyclerBuild.casings());
        helper.assertValueEqual(build.core().floors(), 3, "a full tier-3 Recycler's floors");
        helper.destroyBlock(build.floor(2).get(4));
        helper.assertValueEqual(build.core().floors(), 2, "floors with the third one short a block");
        helper.assertValueEqual(helper.getBlockState(build.floor(2).get(0)).getValue(RecyclerPart.PLACE), RecyclerPlace.NONE,
                "the place on a floor short a block");
        helper.assertValueEqual(helper.getBlockState(build.floor(1).get(0)).getValue(RecyclerPart.FLOOR), RecyclerFloor.TOP,
                "the floor below one short a block");
        helper.destroyBlock(build.floor(1).get(8));
        helper.assertValueEqual(build.core().floors(), 1, "floors with the second one short a block");
        build.feed(1);
        build.turn(ServerConfig.turnsOf(3));
        build.expectAdvancedMix();
        helper.succeed();
    }

    private static void raised(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.buildFloor(1, RecyclerBuild.casings());
        helper.assertValueEqual(build.core().floors(), 1, "a tier-1 Recycler's floors");
        build.swapCore(2);
        helper.assertValueEqual(build.core().floors(), 2, "the floors once its core is raised");
        helper.assertValueEqual(helper.getBlockState(build.floor(1).get(0)).getValue(RecyclerPart.FLOOR), RecyclerFloor.TOP,
                "the floor of a part above, once claimed");
        helper.succeed();
    }

    private static void fillUpward(GameTestHelper helper) {
        Block casing = Recycler.CASING.get();
        var build = RecyclerBuild.build(helper, Direction.SOUTH, new Block[] {Recycler.OUTPUT.get(), casing, casing, Recycler.HAND_DRIVE.get(),
                casing, casing, casing, Recycler.INPUT.get()});
        build.swapCore(2);
        build.buildFloor(1, casing, casing, casing, casing, casing, casing, casing, casing, Recycler.OUTPUT.get());
        build.feed(1);
        build.turn(ServerConfig.turnsOf(2));
        expectMixFrom(helper, build.places.get(0), 0);
        expectMixFrom(helper, build.floor(1).get(8), 3);
        helper.succeed();
    }

    private static void shared(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.swapCore(2);
        build.buildFloor(1, RecyclerBuild.casings());
        helper.assertValueEqual(build.core().floors(), 2, "the lower Recycler's floors alone");
        // A second core on the lower one's second floor's layer, just outside its 3x3, its own 3x3 reaching into it.
        var upper = RecyclerBuild.build(helper, RecyclerBuild.CORE.offset(2, 1, -1), Direction.EAST, RecyclerBuild.STANDARD);
        helper.assertValueEqual(build.core().floors(), 1, "the lower Recycler's floors, a place of its second shared");
        BlockPos shared = RecyclerBuild.CORE.offset(1, 1, -1);
        helper.assertValueEqual(helper.getBlockState(shared).getValue(RecyclerPart.PLACE), RecyclerPlace.NONE, "the shared place");
        helper.assertValueEqual(upper.core().status(), Status.INCOMPLETE, "the upper Recycler, a place of it shared");
        helper.succeed();
    }

    /** Fails unless the output at {@code pos} holds the Advanced mix from result {@code first} on. */
    private static void expectMixFrom(GameTestHelper helper, BlockPos pos, int first) {
        var output = helper.getBlockEntity(pos, RecyclerOutputBlockEntity.class);
        for (int i = 0; i < 3; i++) {
            int result = first + i;
            ItemStack expected = result < RecyclerBuild.ADVANCED_MIX.size() ? RecyclerBuild.ADVANCED_MIX.get(result) : ItemStack.EMPTY;
            ItemStack held = output.item(i);
            helper.assertTrue(ItemStack.isSameItemSameComponents(held, expected) && held.getCount() == expected.getCount(),
                    "position " + i + " of the output at " + pos + " holds " + held + ", not " + expected);
        }
    }
}
