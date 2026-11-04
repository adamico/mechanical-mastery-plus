// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.HomeGates;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * Where a home gate opens (#238): on the island of the player's own team, once it has started protection, and past its
 * win; anywhere else, refused. No Gateways is on the game tests' runs, so these ask {@link HomeGates#refusal}, which is
 * all Gateways' own {@code canOpen} asks.
 */
final class HomeGateTests {

    /** On the test's own island, away from the corner another team claims. */
    private static final BlockPos HOME = new BlockPos(2, 2, 2);
    private static final BlockPos OTHER_ISLAND = new BlockPos(7, 2, 7);

    private HomeGateTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("home_gate_opens_on_its_team_s_island_once_protection_starts", 1, HomeGateTests::opensAtHome);
        tests.test("home_gate_opens_after_protection_is_won", 1, HomeGateTests::opensAfterAWin);
        tests.test("home_gate_refused_before_protection_starts", 1, HomeGateTests::refusedBeforeProtection);
        tests.test("home_gate_refused_on_another_team_s_island", 1, HomeGateTests::refusedOnAnotherIsland);
        tests.test("home_gate_refused_off_any_island", 1, HomeGateTests::refusedOffAnyIsland);
        tests.test("home_gate_refused_to_a_player_with_no_team", 1, HomeGateTests::refusedWithNoTeam);
        tests.test("home_gate_check_leaves_other_gates_as_they_were", 1, HomeGateTests::leavesOtherGates);
        tests.test("home_gate_pearl_s_permit_given_once_protection_is_won", 1, HomeGateTests::permitOnceWon);
    }

    private static void opensAtHome(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.PROTECTION);
        helper.assertTrue(HomeGates.refusal(player(helper, team, HOME), HomeGates.GATE) == null, "a home gate was refused at home");
        helper.succeed();
    }

    private static void opensAfterAWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        Challenges.begin(server, team, Handicap.PROTECTION);
        Challenges.end(server, team);
        helper.assertTrue(HomeGates.refusal(player(helper, team, HOME), HomeGates.GATE) == null, "a home gate was refused at home after the win");
        helper.succeed();
    }

    /** The pearl has no recipe: the Shop sells it behind {@link HomeGates#PERMIT}, which only a protection win gives (#246). */
    private static void permitOnceWon(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        TestAccounts.open(team, 0);
        Challenges.begin(server, team, Handicap.PROTECTION);
        helper.assertFalse(TestAccounts.holds(team, HomeGates.PERMIT), "the pearl's Permit while protection is played");
        Challenges.win(server, team);
        helper.assertTrue(TestAccounts.holds(team, HomeGates.PERMIT), "the pearl's Permit once protection is won");
        helper.succeed();
    }

    private static void refusedBeforeProtection(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.ENERGY);
        helper.assertTrue(HomeGates.refusal(player(helper, team, HOME), HomeGates.GATE) != null,
                "a team that started only energy opened a home gate");
        helper.succeed();
    }

    private static void refusedOnAnotherIsland(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        var server = helper.getLevel().getServer();
        Challenges.begin(server, team, Handicap.PROTECTION);
        Challenges.begin(server, other, Handicap.PROTECTION);
        helper.assertTrue(HomeGates.refusal(player(helper, team, OTHER_ISLAND), HomeGates.GATE) != null,
                "a home gate opened on another team's island");
        helper.succeed();
    }

    /** Outside every claimed island, as the void and the spawn island are. */
    private static void refusedOffAnyIsland(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper, HOME, HOME);
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.PROTECTION);
        helper.assertTrue(HomeGates.refusal(player(helper, team, OTHER_ISLAND), HomeGates.GATE) != null,
                "a home gate opened off any island");
        helper.succeed();
    }

    private static void refusedWithNoTeam(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.PROTECTION);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(HOME.getBottomCenter()));
        helper.assertTrue(HomeGates.refusal(player, HomeGates.GATE) != null, "a home gate opened for a player with no team");
        helper.succeed();
    }

    private static void leavesOtherGates(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper, HOME, HOME);
        var gate = Identifier.fromNamespaceAndPath("gateways", "basic/slime");
        helper.assertTrue(HomeGates.refusal(player(helper, team, OTHER_ISLAND), gate) == null, "another gate was refused");
        helper.succeed();
    }

    private static Player player(GameTestHelper helper, UUID team, BlockPos at) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        player.setPos(helper.absoluteVec(at.getBottomCenter()));
        return player;
    }
}
