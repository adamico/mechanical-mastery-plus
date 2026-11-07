// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlock;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;

/**
 * A challenge's start plays its ritual on the team's C.O.D. (#318), and only on that team's: the sound and particles
 * are checked in game, but where it plays and the look C.O.D. keeps while the challenge is played are checked here.
 * Each test is an island of its own, with another team's island in a corner.
 */
final class CodRitualTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    /** Another team's C.O.D., on an island of its own in a corner of the test's. */
    private static final BlockPos OTHER_COD = new BlockPos(7, 2, 7);
    /** Ticks for both C.O.D.s to tick once, so each is found and shows its team's state. */
    private static final int SETTLE = 2;

    private CodRitualTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("cod_ritual_plays_on_the_starting_team_s_cod", SETTLE + 1, CodRitualTests::playsOnTheTeamsCod);
        tests.test("cod_ritual_plays_nowhere_without_a_cod_found", 1, CodRitualTests::playsNowhereWithoutACod);
        tests.test("cod_stands_challenged_while_its_team_plays", 3 * SETTLE + 1, CodRitualTests::standsChallengedWhilePlayed);
    }

    private static void playsOnTheTeamsCod(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        TestTeams.claim(helper, OTHER_COD, OTHER_COD);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(OTHER_COD, Cod.BLOCK.get());
        helper.runAtTickTime(SETTLE, () -> {
            Optional<GlobalPos> played = CodRitual.play(server(helper), team);
            helper.assertValueEqual(played, Optional.of(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(COD))),
                    "where the ritual played");
            helper.succeed();
        });
    }

    private static void playsNowhereWithoutACod(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.assertValueEqual(CodRitual.play(server(helper), team), Optional.empty(), "where the ritual played with no C.O.D.");
        helper.succeed();
    }

    private static void standsChallengedWhilePlayed(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        TestTeams.claim(helper, OTHER_COD, OTHER_COD);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(OTHER_COD, Cod.BLOCK.get());
        helper.runAtTickTime(SETTLE, () -> {
            helper.assertFalse(challenged(helper, COD), "C.O.D. stands challenged before a challenge starts");
            helper.assertTrue(Challenges.begin(server(helper), team, Handicap.ENERGY), "the energy challenge didn't start");
        });
        helper.runAtTickTime(2 * SETTLE, () -> {
            helper.assertTrue(challenged(helper, COD), "C.O.D. doesn't stand challenged while its team plays");
            helper.assertFalse(challenged(helper, OTHER_COD), "the other team's C.O.D. stands challenged");
            helper.assertTrue(Challenges.stop(server(helper), team), "the energy challenge didn't stop");
        });
        helper.runAtTickTime(3 * SETTLE, () -> {
            helper.assertFalse(challenged(helper, COD), "C.O.D. still stands challenged once its team stopped");
            helper.succeed();
        });
    }

    private static boolean challenged(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).getValue(CodBlock.CHALLENGED);
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
