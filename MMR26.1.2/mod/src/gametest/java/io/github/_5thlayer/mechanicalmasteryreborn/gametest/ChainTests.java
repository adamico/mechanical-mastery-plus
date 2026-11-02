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
 * stage, named after the handicap ({@link TestStages}).
 */
final class ChainTests {

    private ChainTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("chain_locked_until_its_challenge_starts", 1, ChainTests::lockedUntilStarted);
        tests.test("chain_stays_unlocked_after_its_challenge_ends", 1, ChainTests::staysUnlocked);
        tests.test("chain_stays_unlocked_after_a_reset", 1, ChainTests::staysUnlockedAfterAReset);
    }

    private static void lockedUntilStarted(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        var server = helper.getLevel().getServer();
        Chains.unlock(server, player);
        helper.assertFalse(TestStages.holds(player, "energy"), "the energy chain was unlocked before its challenge");
        Challenges.start(server, team, Handicap.ENERGY);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, "energy"), "the energy chain stayed locked once its challenge started");
        helper.assertFalse(TestStages.holds(player, "protection"), "the energy challenge unlocked the protection chain");
        helper.succeed();
    }

    private static void staysUnlocked(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        Challenges.start(server, team, Handicap.ENERGY);
        Challenges.end(server, team);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, "energy"), "a player who joined after the challenge's end found its chain locked");
        helper.succeed();
    }

    private static void staysUnlockedAfterAReset(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        Challenges.start(server, team, Handicap.ENERGY);
        Challenges.reset(server, team);
        Challenges.end(server, team);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, "energy"), "the energy chain was locked again after a reset");
        helper.succeed();
    }
}
