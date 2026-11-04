// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;

/**
 * A challenge's chain, locked until the team starts it (ADR 0007): a player is given the stage of every chain its team
 * has unlocked, and only those, keeps it past a reset and a win, and loses it to a stop, an op's end among them (ADR 0013). Chapters locks the chain's recipes behind the
 * stage, named after the handicap ({@link TestStages}). A chain is started as an op's command does.
 */
final class ChainTests {

    private ChainTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("chain_locked_until_its_challenge_starts", 1, helper -> lockedUntilStarted(helper, Handicap.ENERGY, Handicap.PROTECTION));
        tests.test("chain_stays_unlocked_after_a_win", 1, helper -> staysUnlockedAfterAWin(helper, Handicap.ENERGY));
        tests.test("chain_locked_again_after_an_op_ends_its_challenge", 1, helper -> lockedAgainAfterAnEnd(helper, Handicap.ENERGY));
        tests.test("chain_stays_unlocked_after_a_reset", 1, helper -> staysUnlockedAfterAReset(helper, Handicap.ENERGY));
        // The protection chain, the Warding Essence's recipe (#235).
        tests.test("chain_protection_locked_until_its_challenge_starts", 1,
                helper -> lockedUntilStarted(helper, Handicap.PROTECTION, Handicap.ENERGY));
        tests.test("chain_protection_locked_again_after_an_op_ends_its_challenge", 1, helper -> lockedAgainAfterAnEnd(helper, Handicap.PROTECTION));
        tests.test("chain_protection_stays_unlocked_after_a_reset", 1, helper -> staysUnlockedAfterAReset(helper, Handicap.PROTECTION));
        // The life chain, Neo Vitae's machines and the Vital Essence's infusion (#243).
        tests.test("chain_life_locked_until_its_challenge_starts", 1, helper -> lockedUntilStarted(helper, Handicap.LIFE, Handicap.ENERGY));
        tests.test("chain_life_locked_again_after_an_op_ends_its_challenge", 1, helper -> lockedAgainAfterAnEnd(helper, Handicap.LIFE));
        tests.test("chain_life_stays_unlocked_after_a_reset", 1, helper -> staysUnlockedAfterAReset(helper, Handicap.LIFE));
    }

    private static void lockedUntilStarted(GameTestHelper helper, Handicap handicap, Handicap other) {
        UUID team = TestTeams.claim(helper);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        var server = helper.getLevel().getServer();
        Chains.unlock(server, player);
        helper.assertFalse(TestStages.holds(player, handicap.id()), "the " + handicap.id() + " chain was unlocked before its challenge");
        Challenges.begin(server, team, handicap);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, handicap.id()), "the " + handicap.id() + " chain stayed locked once its challenge started");
        helper.assertFalse(TestStages.holds(player, other.id()), "the " + handicap.id() + " challenge unlocked the " + other.id() + " chain");
        helper.succeed();
    }

    private static void staysUnlockedAfterAWin(GameTestHelper helper, Handicap handicap) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        Challenges.begin(server, team, handicap);
        Challenges.win(server, team);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, handicap.id()), "a player who joined after the " + handicap.id() + " challenge's win found its chain locked");
        helper.succeed();
    }

    /** An op's {@code /cod challenge end} is a stop with no vote: the chain is locked again, from a player online and one who logs in later. */
    private static void lockedAgainAfterAnEnd(GameTestHelper helper, Handicap handicap) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        var online = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(online, team);
        var offline = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(offline, team);
        TestTeams.logIn(online);
        try {
            Challenges.begin(server, team, handicap);
            Chains.unlock(server, online);
            Chains.unlock(server, offline);
            helper.assertTrue(Challenges.end(server, team), "the " + handicap.id() + " challenge didn't end");
            helper.assertFalse(TestStages.holds(online, handicap.id()), "a player online kept the " + handicap.id() + " chain past the end");
            helper.assertTrue(TestStages.holds(offline, handicap.id()), "a player offline lost the " + handicap.id() + " chain before logging in");
            Chains.unlock(server, offline);
            helper.assertFalse(TestStages.holds(offline, handicap.id()), "a player who logged in after the end kept the " + handicap.id() + " chain");
        } finally {
            TestTeams.logOut(online);
        }
        helper.succeed();
    }

    private static void staysUnlockedAfterAReset(GameTestHelper helper, Handicap handicap) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        Challenges.begin(server, team, handicap);
        Challenges.reset(server, team);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, handicap.id()), "the " + handicap.id() + " chain was locked again after a reset");
        helper.succeed();
    }
}
