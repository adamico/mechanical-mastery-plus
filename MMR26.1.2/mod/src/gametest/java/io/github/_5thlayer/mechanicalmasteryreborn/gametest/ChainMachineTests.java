// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * A chain machine from a mix does no work while its island's team hasn't unlocked its chain (#214). The chain machines
 * are what the recipes in the chain's stage file make, of any type, as Craftworks' assembling recipes are in the pack;
 * no Theurgy is on the game tests' runs, so the game-test pack's energy stage file names the blast furnace's crafting
 * recipe, a smelting recipe, and a recipe no pack has, and its protection stage file the shield's, as the pack's names the
 * Warding Essence's assembling recipe (#235).
 * Theurgy's own hook only asks {@link ChainMachines#lockedAt}, which these test.
 */
final class ChainMachineTests {

    /** Made by the crafting recipe in the game-test pack's energy stage file. */
    private static final Block MACHINE = Blocks.BLAST_FURNACE;

    private static final BlockPos AT = new BlockPos(2, 2, 2);
    /** Another team's island, in a corner of the test's. */
    private static final BlockPos OTHER_ISLAND = new BlockPos(7, 2, 7);

    private ChainMachineTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("chain_machines_are_read_from_the_stage_file_s_recipes_of_any_type", 1, ChainMachineTests::readFromTheStageFile);
        tests.test("chain_machine_locked_until_its_challenge_starts", 1, ChainMachineTests::lockedUntilStarted);
        tests.test("chain_machine_stays_unlocked_after_a_reset_and_an_end", 1, ChainMachineTests::staysUnlocked);
        tests.test("chain_machine_of_a_team_that_started_works_beside_one_that_hasnt", 1, ChainMachineTests::perTeam);
        tests.test("chain_machine_lock_leaves_other_blocks_as_they_were", 1, ChainMachineTests::leavesOtherBlocks);
    }

    private static void readFromTheStageFile(GameTestHelper helper) {
        var machines = ChainMachines.of(helper.getLevel().getServer());
        helper.assertValueEqual(machines.get(Items.BLAST_FURNACE), Handicap.ENERGY, "the blast furnace's chain");
        helper.assertValueEqual(machines.get(Items.IRON_INGOT), Handicap.ENERGY, "the smelting recipe's result's chain");
        helper.assertValueEqual(machines.get(Items.SHIELD), Handicap.PROTECTION, "the protection stage file's shield's chain");
        helper.assertValueEqual(machines.size(), 3, "what the stage files' recipes make, but the one no pack has");
        helper.succeed();
    }

    private static void lockedUntilStarted(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(AT, MACHINE);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.of(Handicap.ENERGY), "the machine's lock before its challenge");
        Challenges.start(helper.getLevel().getServer(), team, Handicap.ENERGY);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the machine's lock once its challenge started");
        helper.succeed();
    }

    private static void staysUnlocked(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        helper.setBlock(AT, MACHINE);
        Challenges.start(server, team, Handicap.ENERGY);
        Challenges.reset(server, team);
        Challenges.end(server, team);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the machine's lock after a reset and the challenge's end");
        helper.succeed();
    }

    private static void perTeam(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        helper.setBlock(AT, MACHINE);
        helper.setBlock(OTHER_ISLAND, MACHINE);
        Challenges.start(helper.getLevel().getServer(), team, Handicap.ENERGY);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the lock of the team that started energy");
        helper.assertValueEqual(lockedAt(helper, OTHER_ISLAND), Optional.of(Handicap.ENERGY), "the lock of a team that hasn't");
        helper.succeed();
    }

    private static void leavesOtherBlocks(GameTestHelper helper) {
        TestTeams.claim(helper);
        helper.setBlock(AT, Blocks.FURNACE);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the lock of a block no stage file names");
        helper.succeed();
    }

    private static Optional<Handicap> lockedAt(GameTestHelper helper, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        return ChainMachines.lockedAt(helper.getLevel(), absolute, helper.getLevel().getBlockState(absolute));
    }
}
