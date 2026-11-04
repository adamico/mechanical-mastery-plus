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
 * has unlocked, and only those, and keeps it past the challenge's end. Chapters locks the chain's recipes behind the
 * stage, named after the handicap ({@link TestStages}). A chain is started as an op's command does.
 */
final class ChainTests {

    private ChainTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("chain_locked_until_its_challenge_starts", 1, helper -> lockedUntilStarted(helper, Handicap.ENERGY, Handicap.PROTECTION));
        tests.test("chain_stays_unlocked_after_its_challenge_ends", 1, helper -> staysUnlocked(helper, Handicap.ENERGY));
        tests.test("chain_stays_unlocked_after_a_reset", 1, helper -> staysUnlockedAfterAReset(helper, Handicap.ENERGY));
        // The protection chain, the Warding Essence's recipe (#235).
        tests.test("chain_protection_locked_until_its_challenge_starts", 1,
                helper -> lockedUntilStarted(helper, Handicap.PROTECTION, Handicap.ENERGY));
        tests.test("chain_protection_stays_unlocked_after_its_challenge_ends", 1, helper -> staysUnlocked(helper, Handicap.PROTECTION));
        tests.test("chain_protection_stays_unlocked_after_a_reset", 1, helper -> staysUnlockedAfterAReset(helper, Handicap.PROTECTION));
        // The life chain, Neo Vitae's machines and the Vital Essence's infusion (#243).
        tests.test("chain_life_locked_until_its_challenge_starts", 1, helper -> lockedUntilStarted(helper, Handicap.LIFE, Handicap.ENERGY));
        tests.test("chain_life_stays_unlocked_after_its_challenge_ends", 1, helper -> staysUnlocked(helper, Handicap.LIFE));
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

    private static void staysUnlocked(GameTestHelper helper, Handicap handicap) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        Challenges.begin(server, team, handicap);
        Challenges.end(server, team);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, handicap.id()), "a player who joined after the " + handicap.id() + " challenge's end found its chain locked");
        helper.succeed();
    }

    private static void staysUnlockedAfterAReset(GameTestHelper helper, Handicap handicap) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        Challenges.begin(server, team, handicap);
        Challenges.reset(server, team);
        Challenges.end(server, team);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, handicap.id()), "the " + handicap.id() + " chain was locked again after a reset");
        helper.succeed();
    }
}
