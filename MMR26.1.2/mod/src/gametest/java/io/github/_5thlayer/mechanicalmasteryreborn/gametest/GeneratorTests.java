// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The energy handicap (#202): a generator, a block in the pack's generator tag, gives no FE to anything drawing from
 * it while the team whose island it stands on plays the energy challenge, and gives it again once the challenge ends.
 * Other teams' generators, and blocks not in the tag, answer as before. No suite mod is on the game tests' runs, so
 * the generator is a lodestone, which the game-test pack puts in the tag, and a target block stands for a machine
 * that isn't a generator. This class gives each an FE buffer, as a generator's own mod would, at the priority a
 * suite mod registers its FE at.
 */
final class GeneratorTests {

    /** In the game-test pack's generator tag. */
    private static final Block GENERATOR = Blocks.LODESTONE;
    /** Not in the tag. */
    private static final Block MACHINE = Blocks.TARGET;
    private static final int BUFFER = 1000;
    private static final int DRAW = 100;

    private static final BlockPos AT = new BlockPos(2, 2, 2);
    /** Another team's island, in a corner of the test's. */
    private static final BlockPos OTHER_ISLAND = new BlockPos(7, 2, 7);

    private GeneratorTests() {
    }

    static void registerCapabilities(IEventBus modBus) {
        modBus.addListener(GeneratorTests::fullBuffers);
    }

    private static void fullBuffers(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.Energy.BLOCK,
                (level, pos, state, entity, side) -> new SimpleEnergyHandler(BUFFER, BUFFER, BUFFER, BUFFER), GENERATOR, MACHINE);
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("generator_gives_no_fe_while_its_team_plays_energy", 1, GeneratorTests::givesNoFe);
        tests.test("generator_of_another_team_gives_fe", 1, GeneratorTests::anotherTeamsGivesFe);
        tests.test("generator_gives_fe_again_once_the_challenge_ends", 1, GeneratorTests::givesFeAgain);
        tests.test("generator_tag_leaves_other_blocks_as_they_were", 1, GeneratorTests::leavesOtherBlocks);
    }

    private static void givesNoFe(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(AT, GENERATOR);
        helper.assertValueEqual(draw(helper, AT), DRAW, "FE drawn before the challenge");
        state(helper).choose(team, Handicap.ENERGY);
        helper.assertValueEqual(draw(helper, AT), 0, "FE drawn while the team plays energy");
        helper.succeed();
    }

    private static void anotherTeamsGivesFe(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        helper.setBlock(AT, GENERATOR);
        helper.setBlock(OTHER_ISLAND, GENERATOR);
        state(helper).choose(team, Handicap.ENERGY);
        helper.assertValueEqual(draw(helper, AT), 0, "FE drawn from the team's generator");
        helper.assertValueEqual(draw(helper, OTHER_ISLAND), DRAW, "FE drawn from another team's generator");
        helper.succeed();
    }

    private static void givesFeAgain(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(AT, GENERATOR);
        state(helper).choose(team, Handicap.ENERGY);
        helper.assertValueEqual(draw(helper, AT), 0, "FE drawn while the team plays energy");
        state(helper).endChallenge(team);
        helper.assertValueEqual(draw(helper, AT), DRAW, "FE drawn once the challenge ended");
        helper.succeed();
    }

    private static void leavesOtherBlocks(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(AT, MACHINE);
        state(helper).choose(team, Handicap.ENERGY);
        helper.assertValueEqual(draw(helper, AT), DRAW, "FE drawn from a block not in the tag");
        helper.succeed();
    }

    /** What a network drawing {@link #DRAW} FE from {@code pos} would get, as Wireworks asks: on no side. */
    private static int draw(GameTestHelper helper, BlockPos pos) {
        EnergyHandler handler = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(pos), null);
        if (handler == null) {
            helper.fail("nothing answers for FE", pos);
            return -1;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.extract(DRAW, transaction);
        }
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(helper.getLevel().getServer());
    }
}
