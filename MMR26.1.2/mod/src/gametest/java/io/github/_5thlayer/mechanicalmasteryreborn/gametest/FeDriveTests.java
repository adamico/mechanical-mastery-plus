// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The FE drive (#186): it turns its Recycler on FE from its own store, once every {@code recycler.fe_drive.interval}
 * ticks, spending {@code recycler.fe_drive.fe_per_turn} on each turn that counts; it stops when its store is empty,
 * and keeps turning on what it stores while its team plays the energy challenge, which cuts only generators (MMR26.1.2
 * ADR 0009). FE drives add up with each other and with hand drives, and a drive whose Recycler has nothing to do spends
 * nothing, so once its store is full it takes no more. Each test builds its Recycler with {@link RecyclerBuild}.
 */
final class FeDriveTests {

    /** The standard build with its two hand drives (places 3 and 5) given {@code third} and {@code fifth}. */
    private static Block[] drives(Block third, Block fifth) {
        Block[] parts = RecyclerBuild.STANDARD.clone();
        parts[3] = third;
        parts[5] = fifth;
        return parts;
    }

    private FeDriveTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("fe_drive_turns_while_it_has_fe", 80, helper -> decomposes(helper, 1, false));
        tests.test("fe_drive_two_decompose_twice_as_fast", 80, helper -> decomposes(helper, 2, false));
        tests.test("fe_drive_keeps_turning_while_its_team_plays_energy", 80, helper -> decomposes(helper, 1, true));
        tests.test("fe_drive_stops_when_its_store_is_empty", 80, FeDriveTests::stopsWithoutFe);
        tests.test("fe_drive_adds_up_with_a_hand_drive", 80, FeDriveTests::withHandDrive);
        tests.test("fe_drive_takes_no_fe_while_its_recycler_has_nothing_to_do", 40, FeDriveTests::idleTakesNothing);
    }

    /** {@code drives} FE drives, each given enough for the whole decompose, take the ticks their interval sets. */
    private static void decomposes(GameTestHelper helper, int drives, boolean playingEnergy) {
        if (playingEnergy) {
            UUID team = TestTeams.claim(helper);
            TeamState.get(helper.getLevel().getServer()).choose(team, Handicap.ENERGY, 0);
        }
        var build = RecyclerBuild.build(helper, Direction.SOUTH,
                drives(Recycler.FE_DRIVE.get(), drives == 2 ? Recycler.FE_DRIVE.get() : Recycler.CASING.get()));
        build.feed(1);
        for (BlockPos place : build.places) {
            if (helper.getBlockState(place).is(Recycler.FE_DRIVE.get())) {
                helper.assertValueEqual(give(helper, place, turns(ServerConfig.turnsOf(1))), turns(ServerConfig.turnsOf(1)),
                        "the FE a drive took");
            }
        }
        int expected = (ServerConfig.turnsOf(1) / drives - 1) * ServerConfig.feDriveInterval();
        timed(helper, build, expected, drives + " FE drive(s)", () -> {
        });
    }

    private static void stopsWithoutFe(GameTestHelper helper) {
        var build = RecyclerBuild.build(helper, Direction.SOUTH, drives(Recycler.FE_DRIVE.get(), Recycler.CASING.get()));
        build.feed(1);
        BlockPos drive = build.firstOf(Recycler.FE_DRIVE.get());
        give(helper, drive, turns(3));
        helper.runAfterDelay(6 * ServerConfig.feDriveInterval(), () -> {
            helper.assertValueEqual(build.core().turns(), 3, "the turns three turns' worth of FE gave");
            helper.assertValueEqual(face(helper, drive).getAmountAsLong(), 0L, "the FE left in the drive");
            helper.assertTrue(build.outputsEmpty(), "the Recycler decomposed on three turns' worth of FE");
            give(helper, drive, turns(ServerConfig.turnsOf(1) - 3));
        });
        helper.runAfterDelay(12 * ServerConfig.feDriveInterval(), () -> {
            build.expectAdvancedMix();
            helper.succeed();
        });
    }

    /** One FE drive and one hand drive clicked every tick decompose as fast as two hand drives. */
    private static void withHandDrive(GameTestHelper helper) {
        var build = RecyclerBuild.build(helper, Direction.SOUTH, drives(Recycler.FE_DRIVE.get(), Recycler.HAND_DRIVE.get()));
        build.feed(1);
        give(helper, build.firstOf(Recycler.FE_DRIVE.get()), turns(ServerConfig.turnsOf(1)));
        BlockPos hand = build.firstOf(Recycler.HAND_DRIVE.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        int expected = (ServerConfig.turnsOf(1) / 2 - 1) * ServerConfig.feDriveInterval();
        timed(helper, build, expected, "an FE drive and a hand drive", () -> helper.useBlock(hand, player));
    }

    /**
     * Succeeds once {@code build} has decomposed exactly {@code expected} ticks after its first counted turn, running
     * {@code each} on every tick from that turn on. A new drive first ticks the tick after it is placed, and the test
     * sees each turn a tick after it, so the clock starts at the first turn seen, not at the build.
     */
    private static void timed(GameTestHelper helper, RecyclerBuild build, int expected, String drives, Runnable each) {
        long[] first = {-1};
        helper.onEachTick(() -> {
            if (first[0] < 0) {
                if (build.core().turns() == 0) {
                    helper.assertTrue(build.outputsEmpty(), drives + " decomposed before a turn was seen");
                    return;
                }
                first[0] = helper.getTick();
            }
            long elapsed = helper.getTick() - first[0];
            if (elapsed < expected) {
                helper.assertTrue(build.outputsEmpty(), drives + " decomposed after only " + elapsed + " ticks");
            }
            each.run();
            if (!build.outputsEmpty()) {
                helper.assertValueEqual(elapsed, (long) expected, "the ticks " + drives + " took");
                helper.succeed();
            }
        });
    }

    private static void idleTakesNothing(GameTestHelper helper) {
        var build = RecyclerBuild.build(helper, Direction.SOUTH, drives(Recycler.FE_DRIVE.get(), Recycler.CASING.get()));
        BlockPos drive = build.firstOf(Recycler.FE_DRIVE.get());
        int capacity = turns(ServerConfig.feDriveStoreTurns());
        helper.assertValueEqual(give(helper, drive, 2 * capacity), capacity, "the FE an empty drive took");
        helper.assertValueEqual(face(helper, drive).getCapacityAsLong(), (long) capacity, "the FE a drive can hold");
        helper.runAfterDelay(4 * ServerConfig.feDriveInterval(), () -> {
            helper.assertValueEqual(face(helper, drive).getAmountAsLong(), (long) capacity, "the FE an idle drive holds");
            helper.assertValueEqual(give(helper, drive, 1), 0, "the FE a full idle drive took");
            helper.assertValueEqual(build.core().turns(), 0, "the turns an empty Recycler counted");
            helper.succeed();
        });
    }

    /** The FE {@code turns} turns cost. */
    private static int turns(int turns) {
        return turns * ServerConfig.feDriveFePerTurn();
    }

    /** Offers {@code fe} to the drive at {@code pos}, as a cable would, and says how much it took. */
    private static int give(GameTestHelper helper, BlockPos pos, int fe) {
        try (Transaction transaction = Transaction.openRoot()) {
            int taken = face(helper, pos).insert(fe, transaction);
            transaction.commit();
            return taken;
        }
    }

    private static EnergyHandler face(GameTestHelper helper, BlockPos pos) {
        EnergyHandler face = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(pos), Direction.UP);
        helper.assertTrue(face != null, "an FE drive has no FE face");
        return face;
    }
}
