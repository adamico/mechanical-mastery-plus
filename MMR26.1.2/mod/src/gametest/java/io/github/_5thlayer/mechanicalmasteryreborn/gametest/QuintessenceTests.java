// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Set;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.QuintessenceRung;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/**
 * Quintessence's Voidworks way (#262), what of it plays without Voidworks: the last stand's recipes, the ones its stage
 * file names, marked locked until the team starts it, and the lock a Mutation Chamber asks before it climbs a
 * Quartessence. The Chamber and the Crucible themselves are {@code SuiteQuintessenceTests}', on the suite run.
 */
final class QuintessenceTests {

    /** Where a Mutation Chamber would stand, on the test's island. */
    private static final BlockPos CHAMBER = new BlockPos(2, 2, 2);

    private QuintessenceTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("quintessence_last_stand_recipes_are_read_from_its_stage_file", 1, QuintessenceTests::recipesFromTheStageFile);
        tests.test("quintessence_last_stand_recipes_marked_locked_until_it_starts", 1, QuintessenceTests::markedUntilItStarts);
        tests.test("quintessence_rung_locked_until_the_chambers_team_starts_its_last_stand", 1, QuintessenceTests::rungLockedUntilItStarts);
    }

    /** What EMI marks as the last stand's: every recipe its stage file names, the game-test pack's the compass's. */
    private static void recipesFromTheStageFile(GameTestHelper helper) {
        helper.assertValueEqual(ChainMachines.lastStandRecipes(helper.getLevel().getServer()),
                Set.of(Identifier.withDefaultNamespace("compass")), "the last stand's stage file's recipes");
        helper.succeed();
    }

    /** The table a team's player is sent marks the last stand's recipes locked past the fourth win, until it starts. */
    private static void markedUntilItStarts(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        win(helper, team);
        var table = ChainMachines.table(server, team);
        helper.assertTrue(table.lastStandLocked(), "the last stand's recipes unmarked before it started");
        helper.assertTrue(table.lastStand().contains(Identifier.withDefaultNamespace("compass")), "the table sent lacks the last stand's recipe");
        helper.assertTrue(LastStands.begin(server, team), "the last stand didn't start");
        helper.assertFalse(ChainMachines.table(server, team).lastStandLocked(), "the last stand's recipes marked once it started");
        helper.succeed();
    }

    /** Only a Quartessence waits, and only until the island's team has started its last stand: winning all four isn't it. */
    private static void rungLockedUntilItStarts(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var level = helper.getLevel();
        BlockPos chamber = helper.absolutePos(CHAMBER);
        var quartessence = Essences.item(Essence.QUARTESSENCE);
        helper.assertFalse(QuintessenceRung.lockedAt(level, chamber, Items.COPPER_INGOT), "another Family's Rung locked");
        win(helper, team);
        helper.assertTrue(QuintessenceRung.lockedAt(level, chamber, quartessence), "a Quartessence climbs before the last stand");
        helper.assertTrue(LastStands.begin(level.getServer(), team), "the last stand didn't start");
        helper.assertFalse(QuintessenceRung.lockedAt(level, chamber, quartessence), "a Quartessence waits once the last stand started");
        helper.succeed();
    }

    /** Notes all four challenges won for {@code team}, as winning each does, with none of a win's other effects. */
    private static void win(GameTestHelper helper, UUID team) {
        for (Handicap handicap : Handicap.values()) {
            helper.assertTrue(TeamState.get(helper.getLevel().getServer()).blockTier(team, handicap),
                    "the " + handicap.id() + " challenge wasn't noted won");
        }
    }
}
