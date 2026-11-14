// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * A chain machine does no work while its island's team hasn't started its challenge, and its crafting is never locked
 * (ADR 0007, #246). Each chain's machines are its tag, {@code mechanicalmasteryreborn:chain_machines/<handicap>}, not
 * what its stage file's recipes make: no Theurgy or food mod is on the game tests' runs, so the game-test pack's tags
 * hold the blast furnace and smoker for energy's and the brewing stand, a test double, for life's, standing for the pack's own lists,
 * which {@code ChainMachineListTest} reads. Its stage files name the chain's own recipes, a smelting recipe for energy
 * and the shield's for protection, and the glass bottle's for life, which make no machine. The chain mods' own hooks only ask
 * {@link ChainMachines#lockedAt}, which these test.
 */
final class ChainMachineTests {

    /** In the game-test pack's energy tag. */
    private static final Block MACHINE = Blocks.BLAST_FURNACE;

    private static final BlockPos AT = new BlockPos(2, 2, 2);
    /** Another team's island, in a corner of the test's. */
    private static final BlockPos OTHER_ISLAND = new BlockPos(7, 2, 7);

    private ChainMachineTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("chain_machines_are_read_from_their_tags_not_the_stage_files", 1, ChainMachineTests::readFromTheTags);
        tests.test("chain_recipes_are_read_from_the_stage_files", 1, ChainMachineTests::recipesFromTheStageFiles);
        tests.test("chain_machines_of_every_chain_idle_until_its_challenge_starts", 1, ChainMachineTests::everyChainIdleUntilStarted);
        tests.test("chain_machine_locked_until_its_challenge_starts", 1, ChainMachineTests::lockedUntilStarted);
        tests.test("chain_machine_stays_unlocked_after_a_reset", 1, ChainMachineTests::staysUnlocked);
        tests.test("chain_machine_stays_unlocked_after_a_win", 1, ChainMachineTests::staysUnlockedAfterAWin);
        tests.test("chain_machine_of_a_team_that_started_works_beside_one_that_hasnt", 1, ChainMachineTests::perTeam);
        tests.test("chain_machine_lock_leaves_other_blocks_as_they_were", 1, ChainMachineTests::leavesOtherBlocks);
        tests.test("chain_machine_life_idle_until_life_starts", 1, ChainMachineTests::lifeIdleUntilStarted);
    }

    private static void readFromTheTags(GameTestHelper helper) {
        helper.assertValueEqual(ChainMachines.chainOf(Items.BLAST_FURNACE), Optional.of(Handicap.ENERGY), "the blast furnace's chain");
        helper.assertValueEqual(ChainMachines.chainOf(Items.SMOKER), Optional.of(Handicap.ENERGY), "the smoker's chain");
        helper.assertValueEqual(ChainMachines.chainOf(Items.BREWING_STAND), Optional.of(Handicap.LIFE), "the brewing stand's chain");
        helper.assertValueEqual(ChainMachines.chainOf(Items.SHIELD), Optional.empty(), "the chain of what a stage file's recipe makes");
        helper.assertValueEqual(ChainMachines.chainOf(Items.IRON_INGOT), Optional.empty(), "the chain of what a stage file's smelting makes");
        helper.succeed();
    }

    /** What EMI marks besides the locked machines' recipes: every recipe a stage file names, but one no pack has. */
    private static void recipesFromTheStageFiles(GameTestHelper helper) {
        var recipes = ChainMachines.recipes(helper.getLevel().getServer());
        helper.assertValueEqual(recipes.get(Identifier.withDefaultNamespace("iron_ingot_from_smelting_iron_ore")), Handicap.ENERGY,
                "the energy stage file's recipe's chain");
        helper.assertValueEqual(recipes.get(Identifier.withDefaultNamespace("shield")), Handicap.PROTECTION, "the protection stage file's recipe's chain");
        helper.assertValueEqual(recipes.get(Identifier.withDefaultNamespace("glass_bottle")), Handicap.LIFE, "the life stage file's recipe's chain");
        helper.assertValueEqual(recipes.get(Identifier.fromNamespaceAndPath("mechanicalmasteryreborn", "gametest/vital_spark")), Handicap.LIFE,
                "the Vital Spark's recipe's chain");
        helper.assertValueEqual(recipes.size(), 4, "the stage files' recipes, but the one no pack has");
        helper.succeed();
    }

    /** Every machine each chain's tag holds waits for that chain's challenge, and no other's. */
    private static void everyChainIdleUntilStarted(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        int checked = 0;
        for (Handicap handicap : Handicap.values()) {
            for (var item : BuiltInRegistries.ITEM.getTagOrEmpty(ChainMachines.tag(handicap))) {
                if (!(item.value() instanceof BlockItem block)) {
                    helper.fail(item.getRegisteredName() + " in the " + handicap.id() + " chain's tag is no block");
                    return;
                }
                helper.setBlock(AT, block.getBlock());
                helper.assertValueEqual(lockedAt(helper, AT), Optional.of(handicap), item.getRegisteredName() + "'s lock before its challenge");
                checked++;
            }
        }
        helper.assertValueEqual(checked, 5, "the machines the game-test pack's tags and the mod's own Pelican and Homunculus Stand hold");
        for (Handicap handicap : Handicap.values()) {
            Challenges.begin(server, team, handicap);
            for (var item : BuiltInRegistries.ITEM.getTagOrEmpty(ChainMachines.tag(handicap))) {
                helper.setBlock(AT, ((BlockItem) item.value()).getBlock());
                helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), item.getRegisteredName() + "'s lock once its challenge started");
            }
            Challenges.win(server, team);
        }
        helper.succeed();
    }

    private static void lockedUntilStarted(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(AT, MACHINE);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.of(Handicap.ENERGY), "the machine's lock before its challenge");
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.ENERGY);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the machine's lock once its challenge started");
        helper.succeed();
    }

    /** A life machine from a mix waits for life, not for another challenge (#243). */
    private static void lifeIdleUntilStarted(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        helper.setBlock(AT, Blocks.BREWING_STAND);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.of(Handicap.LIFE), "the life machine's lock before life");
        Challenges.begin(server, team, Handicap.ENERGY);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.of(Handicap.LIFE), "the life machine's lock once energy started");
        Challenges.end(server, team);
        Challenges.begin(server, team, Handicap.LIFE);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the life machine's lock once life started");
        helper.succeed();
    }

    private static void staysUnlocked(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        helper.setBlock(AT, MACHINE);
        Challenges.begin(server, team, Handicap.ENERGY);
        Challenges.reset(server, team);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the machine's lock after a reset");
        helper.succeed();
    }

    private static void staysUnlockedAfterAWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        helper.setBlock(AT, MACHINE);
        Challenges.begin(server, team, Handicap.ENERGY);
        Challenges.win(server, team);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the machine's lock after its challenge was won");
        helper.succeed();
    }

    private static void perTeam(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        helper.setBlock(AT, MACHINE);
        helper.setBlock(OTHER_ISLAND, MACHINE);
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.ENERGY);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the lock of the team that started energy");
        helper.assertValueEqual(lockedAt(helper, OTHER_ISLAND), Optional.of(Handicap.ENERGY), "the lock of a team that hasn't");
        helper.succeed();
    }

    private static void leavesOtherBlocks(GameTestHelper helper) {
        TestTeams.claim(helper);
        helper.setBlock(AT, Blocks.FURNACE);
        helper.assertValueEqual(lockedAt(helper, AT), Optional.empty(), "the lock of a block no chain's tag holds");
        helper.succeed();
    }

    private static Optional<Handicap> lockedAt(GameTestHelper helper, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        return ChainMachines.lockedAt(helper.getLevel(), absolute, helper.getLevel().getBlockState(absolute));
    }
}
