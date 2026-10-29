// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.DecomposeLog;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.DriveCorner;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.HandDriveBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Status;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerInputBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerOutputBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * The Recycler (#166): a whole one decomposes, by the game-test pack's recipe, with no power, its mix on its
 * outputs clockwise from the front-left place; an incomplete one doesn't run; hoppers feed and empty it; its
 * drives add up and are capped; and each decompose is recorded for the team whose island it stands on. Each
 * test builds its Recycler with {@link RecyclerBuild}.
 */
final class RecyclerTests {

    private RecyclerTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("recycler_whole_decomposes_with_no_power", 1, RecyclerTests::decomposes);
        tests.test("recycler_counts_no_turn_short_of_a_decompose", 1, RecyclerTests::partTurned);
        tests.test("recycler_without_a_core_takes_nothing_in", 1, RecyclerTests::withoutCore);
        tests.test("recycler_without_a_drive_does_not_run", 1, helper -> incomplete(helper, 5, Recycler.CASING.get(), 7, Recycler.CASING.get()));
        tests.test("recycler_without_an_input_does_not_run", 1, helper -> incomplete(helper, 3, Recycler.CASING.get()));
        tests.test("recycler_without_an_output_does_not_run", 1,
                helper -> incomplete(helper, 0, Recycler.CASING.get(), 1, Recycler.CASING.get()));
        tests.test("recycler_with_a_foreign_block_does_not_run", 1, helper -> incomplete(helper, 2, Blocks.STONE));
        tests.test("recycler_with_an_empty_place_does_not_run", 1, helper -> incomplete(helper, 4, Blocks.AIR));
        tests.test("recycler_with_a_part_shared_by_two_cores_does_not_run", 1, RecyclerTests::sharedPart);
        tests.test("recycler_hopper_feeds_an_input_and_hoppers_empty_the_outputs", 100, RecyclerTests::hoppers);
        tests.test("recycler_results_land_clockwise_from_the_front_left_place", 1, RecyclerTests::clockwiseFacingSouth);
        tests.test("recycler_results_follow_the_core_facing_east", 1, RecyclerTests::clockwiseFacingEast);
        tests.test("recycler_will_not_start_when_the_mix_does_not_fit", 1, RecyclerTests::noRoom);
        tests.test("recycler_will_not_run_a_mix_longer_than_its_outputs", 1, RecyclerTests::tooFewOutputs);
        tests.test("recycler_right_click_takes_a_position_stack", 1, RecyclerTests::rightClickTakes);
        tests.test("recycler_right_click_feeds_an_input", 1, RecyclerTests::rightClickFeeds);
        tests.test("recycler_one_drive_decomposes_in_eight_turns_of_its_cap", 60, helper -> drivesTurned(helper, 1));
        tests.test("recycler_two_drives_decompose_twice_as_fast", 60, helper -> drivesTurned(helper, 2));
        tests.test("recycler_drive_turns_no_faster_than_its_cap", 20, RecyclerTests::driveCap);
        tests.test("recycler_decompose_is_recorded_for_its_island_team", 1, RecyclerTests::recorded);
        tests.test("recycler_core_placed_turns_its_corner_drives", 1, RecyclerTests::coreTurnsDrives);
        tests.test("recycler_drive_placed_takes_its_corner", 1, RecyclerTests::drivePlacedTakesCorner);
        tests.test("recycler_input_placed_faces_its_player", 1, RecyclerTests::inputFacesPlayer);
    }

    private static void decomposes(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectAdvancedMix();
        helper.assertTrue(build.core().held().isEmpty(), "the core still holds " + build.core().held());
        helper.succeed();
    }

    private static void partTurned(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        build.turn(RecyclerCoreBlockEntity.TURNS - 1);
        helper.assertTrue(build.outputsEmpty(), "a Recycler decomposed one turn short");
        helper.assertValueEqual(build.core().turns(), RecyclerCoreBlockEntity.TURNS - 1, "the turns done");
        helper.succeed();
    }

    private static void withoutCore(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        helper.setBlock(RecyclerBuild.CORE, Blocks.AIR);
        BlockPos input = build.firstOf(Recycler.INPUT.get());
        var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(input), Direction.UP);
        helper.assertTrue(face == null, "an input with no core has an item face");
        helper.succeed();
    }

    /** The standard build with place {@code at} given {@code block}, and maybe a second, doesn't run. */
    private static void incomplete(GameTestHelper helper, Object... changes) {
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        for (int i = 0; i < changes.length; i += 2) {
            build.place((Integer) changes[i], Blocks.AIR);
            build.place((Integer) changes[i], (Block) changes[i + 1]);
        }
        helper.assertValueEqual(build.core().status(), Status.INCOMPLETE, "the Recycler's status");
        build.turn(RecyclerCoreBlockEntity.TURNS);
        helper.assertValueEqual(build.core().turns(), 0, "the turns an incomplete Recycler counted");
        helper.assertTrue(build.outputsEmpty(), "an incomplete Recycler decomposed");
        helper.succeed();
    }

    private static void sharedPart(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        // A second core two places east: the east drive is in both 3x3s.
        helper.setBlock(RecyclerBuild.CORE.east(2), Recycler.CORE.get());
        helper.assertValueEqual(build.core().status(), Status.INCOMPLETE, "the status of a Recycler sharing a drive");
        helper.succeed();
    }

    private static void hoppers(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        BlockPos input = build.firstOf(Recycler.INPUT.get());
        helper.setBlock(input.above(), Blocks.HOPPER.defaultBlockState());
        helper.getBlockEntity(input.above(), HopperBlockEntity.class).setItem(0, new ItemStack(Essences.item(Essence.ADVANCED)));
        BlockPos first = build.output(0);
        BlockPos second = build.output(1);
        helper.setBlock(first.below(), Blocks.HOPPER);
        helper.setBlock(second.below(), Blocks.HOPPER);
        helper.succeedWhen(() -> {
            if (!build.core().held().isEmpty() && build.outputsEmpty() && build.core().turns() == 0) {
                build.turn(RecyclerCoreBlockEntity.TURNS);
            }
            for (int i = 0; i < RecyclerBuild.ADVANCED_MIX.size(); i++) {
                BlockPos hopper = (i < 3 ? first : second).below();
                var item = RecyclerBuild.ADVANCED_MIX.get(i).getItem();
                helper.assertTrue(helper.getBlockEntity(hopper, HopperBlockEntity.class).countItem(item) == 1, "the hopper has no " + item);
            }
            helper.assertTrue(build.outputsEmpty(), "the hoppers left items on the outputs");
        });
    }

    private static void clockwiseFacingSouth(GameTestHelper helper) {
        // Outputs on the left (place 1) and front-right (place 6) of a core facing south.
        var build = RecyclerBuild.build(helper, Direction.SOUTH, new Block[] {Recycler.CASING.get(), Recycler.OUTPUT.get(), Recycler.CASING.get(),
                Recycler.INPUT.get(), Recycler.CASING.get(), Recycler.HAND_DRIVE.get(), Recycler.OUTPUT.get(), Recycler.CASING.get()});
        helper.assertValueEqual(build.output(0), RecyclerBuild.CORE.west(), "the first output of a core facing south");
        helper.assertValueEqual(build.output(1), RecyclerBuild.CORE.south().east(), "the second output of a core facing south");
        build.feed(1);
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectAdvancedMix();
        helper.succeed();
    }

    private static void clockwiseFacingEast(GameTestHelper helper) {
        // Facing east, the front-left place is south-east, and the place after it south.
        var build = RecyclerBuild.build(helper, Direction.EAST, new Block[] {Recycler.OUTPUT.get(), Recycler.OUTPUT.get(), Recycler.CASING.get(),
                Recycler.INPUT.get(), Recycler.CASING.get(), Recycler.HAND_DRIVE.get(), Recycler.CASING.get(), Recycler.CASING.get()});
        helper.assertValueEqual(build.output(0), RecyclerBuild.CORE.south().east(), "the first output of a core facing east");
        helper.assertValueEqual(build.output(1), RecyclerBuild.CORE.south(), "the second output of a core facing east");
        build.feed(1);
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectAdvancedMix();
        helper.succeed();
    }

    private static void noRoom(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(2);
        build.turn(RecyclerCoreBlockEntity.TURNS);
        // The buckets don't stack, so the second mix doesn't fit beside the first.
        helper.assertValueEqual(build.core().status(), Status.NO_ROOM, "the status with the first mix still on the outputs");
        build.turn(RecyclerCoreBlockEntity.TURNS);
        helper.assertValueEqual(build.core().turns(), 0, "the turns counted with no room for the mix");
        helper.assertValueEqual(build.core().held().getCount(), 1, "the essence left in the core");
        build.expectAdvancedMix();
        helper.succeed();
    }

    private static void tooFewOutputs(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.place(1, Blocks.AIR);
        build.place(1, Recycler.CASING.get());
        build.feed(1);
        helper.assertValueEqual(build.core().status(), Status.TOO_FEW_OUTPUTS, "the status with three positions for a mix of five");
        build.turn(RecyclerCoreBlockEntity.TURNS);
        helper.assertTrue(build.outputsEmpty(), "a Recycler with too few outputs decomposed");
        helper.succeed();
    }

    private static void rightClickTakes(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        build.turn(RecyclerCoreBlockEntity.TURNS);
        BlockPos output = build.output(0);
        Direction facing = helper.getBlockState(output).getValue(RecyclerOutputBlock.FACING);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        // Position 1 is the middle third of the shelf's face.
        BlockPos absolute = helper.absolutePos(output);
        Vec3 hit = Vec3.atCenterOf(absolute).relative(facing, 0.5);
        helper.useBlock(output, player, new BlockHitResult(hit, facing, absolute, false));
        helper.assertValueEqual(player.getInventory().countItem(RecyclerBuild.ADVANCED_MIX.get(1).getItem()), 1, "the hoppers taken");
        helper.assertTrue(build.outputEntity(0).item(1).isEmpty(), "the middle position still holds " + build.outputEntity(0).item(1));
        build.expectPosition(0, RecyclerBuild.ADVANCED_MIX.get(0));
        helper.succeed();
    }

    private static void rightClickFeeds(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(Essence.ADVANCED), 3));
        helper.useBlock(build.firstOf(Recycler.INPUT.get()), player);
        helper.assertValueEqual(build.core().held().getCount(), 3, "the essence the core took from the player's click");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the player still holds " + player.getMainHandItem());
        helper.succeed();
    }

    /**
     * Every tick a player clicks each of {@code drives} drives. One drive turns every {@link HandDriveBlock#CAP} ticks,
     * so its eighth turn comes 35 ticks after its first; two take turns side by side, so theirs comes after 15.
     */
    private static void drivesTurned(GameTestHelper helper, int drives) {
        var build = RecyclerBuild.standard(helper);
        if (drives == 1) {
            build.place(7, Blocks.AIR);
            build.place(7, Recycler.CASING.get());
        }
        build.feed(1);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        long start = helper.getTick();
        int expected = (RecyclerCoreBlockEntity.TURNS / drives - 1) * HandDriveBlock.CAP;
        helper.onEachTick(() -> {
            long elapsed = helper.getTick() - start;
            if (elapsed < expected) {
                helper.assertTrue(build.outputsEmpty(), drives + " drive(s) decomposed after only " + elapsed + " ticks");
            }
            for (BlockPos place : build.places) {
                if (helper.getBlockState(place).is(Recycler.HAND_DRIVE.get())) {
                    helper.useBlock(place, player);
                }
            }
            if (!build.outputsEmpty()) {
                helper.assertValueEqual(elapsed, (long) expected, "the ticks " + drives + " drive(s) took");
                helper.succeed();
            }
        });
    }

    private static void driveCap(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        BlockPos drive = build.firstOf(Recycler.HAND_DRIVE.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < 10; i++) {
            helper.useBlock(drive, player);
        }
        helper.assertValueEqual(build.core().turns(), 1, "the turns ten clicks in one tick gave");
        helper.runAfterDelay(HandDriveBlock.CAP, () -> helper.assertValueEqual(build.core().turns(), 2,
                "the turns once the cap allowed the kept click"));
        helper.runAfterDelay(3 * HandDriveBlock.CAP, () -> {
            helper.assertValueEqual(build.core().turns(), 2, "the turns long after ten clicks in one tick");
            helper.succeed();
        });
    }

    private static void recorded(GameTestHelper helper) {
        var team = TestTeams.claim(helper);
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        build.turn(RecyclerCoreBlockEntity.TURNS);
        var log = DecomposeLog.get(helper.getLevel().getServer());
        helper.assertValueEqual(log.count(team, Essence.ADVANCED, DecomposeLog.Way.RECYCLER), 1L,
                "the Advanced essence the island's team decomposed on a Recycler");
        helper.assertValueEqual(log.count(team, Essence.ADVANCED, DecomposeLog.Way.HAND), 0L,
                "the Advanced essence the island's team decomposed by hand");
        helper.succeed();
    }

    private static void coreTurnsDrives(GameTestHelper helper) {
        for (DriveCorner corner : DriveCorner.values()) {
            helper.setBlock(corner.of(RecyclerBuild.CORE), Recycler.HAND_DRIVE.get());
        }
        helper.setBlock(RecyclerBuild.CORE, Recycler.CORE.get());
        for (DriveCorner corner : DriveCorner.values()) {
            helper.assertValueEqual(helper.getBlockState(corner.of(RecyclerBuild.CORE)).getValue(HandDriveBlock.CORNER), corner,
                    "the corner of the drive placed before its core at " + corner.getSerializedName());
        }
        helper.succeed();
    }

    private static void drivePlacedTakesCorner(GameTestHelper helper) {
        BlockPos core = new BlockPos(4, 1, 4);
        helper.setBlock(core, Recycler.CORE.get());
        BlockPos drive = core.south().east();
        placeOnTheFloor(helper, Recycler.HAND_DRIVE_ITEM.get(), drive);
        helper.assertValueEqual(helper.getBlockState(drive).getValue(HandDriveBlock.CORNER), DriveCorner.SOUTH_EAST,
                "the corner of a drive placed south-east of a core");
        helper.succeed();
    }

    private static void inputFacesPlayer(GameTestHelper helper) {
        BlockPos input = new BlockPos(4, 1, 4);
        // A mock player looks south, so the input's front faces back north, toward them.
        placeOnTheFloor(helper, Recycler.INPUT_ITEM.get(), input);
        helper.assertValueEqual(helper.getBlockState(input).getValue(RecyclerInputBlock.FACING), Direction.NORTH,
                "the front of an input a player looking south placed");
        helper.succeed();
    }

    /** A player places {@code item} at {@code pos}, clicking the top of the block below it. */
    private static void placeOnTheFloor(GameTestHelper helper, Item item, BlockPos pos) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        BlockPos floor = helper.absolutePos(pos.below());
        var hit = new BlockHitResult(Vec3.atCenterOf(floor).relative(Direction.UP, 0.5), Direction.UP, floor, false);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }
}
