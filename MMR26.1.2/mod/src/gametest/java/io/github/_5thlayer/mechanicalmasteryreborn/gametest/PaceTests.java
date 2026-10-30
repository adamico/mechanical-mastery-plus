// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * C.O.D.'s pace (#205): awake, it makes essence at its waking pace while its team plays no challenge, and ever faster
 * with the time since a challenge began while one is played, back at the waking pace once it ends. The tests play at
 * the default growth, a tenth of the waking pace more each minute, and the default waking interval of 40 ticks. A
 * challenge chosen as of an earlier game time stands for one that has gone on that long. Each test is an island of
 * its own.
 */
final class PaceTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    /** The default {@code cod.waking_interval} the tests play at, which their expected intervals are worked out from. */
    private static final int WAKING = 40;
    /** Twice the waking pace. */
    private static final int TWICE = WAKING / 2;
    private static final int MINUTE = 1200;
    /** Ticks past an interval's end, so a check never lands on the tick the essence is made. */
    private static final int MARGIN = 2;
    /** A third of a minute: long enough for the pace to grow by a tick. */
    private static final int A_WHILE = 400;

    private PaceTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("pace_does_not_grow_with_no_challenge", A_WHILE + 10, PaceTests::noGrowthWithNoChallenge);
        tests.test("pace_keeps_growing_while_a_challenge_is_played", A_WHILE + 10, PaceTests::growsWhilePlayed);
        tests.test("pace_makes_essence_twice_as_fast_ten_minutes_into_a_challenge", 50, PaceTests::twiceAsFastTenMinutesIn);
        tests.test("pace_falls_back_to_waking_once_the_challenge_ends", 10, PaceTests::fallsBackOnceEnded);
    }

    private static void noGrowthWithNoChallenge(GameTestHelper helper) {
        helper.assertValueEqual(ServerConfig.codWakingInterval(), WAKING, "the waking interval the tests play at");
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        state(helper).wake(team);
        helper.runAtTickTime(1, () -> helper.assertValueEqual(cod(helper).interval(), WAKING, "the waking interval"));
        helper.runAtTickTime(A_WHILE + 1, () -> {
            helper.assertValueEqual(cod(helper).interval(), WAKING, "the interval a third of a minute on, with no challenge");
            helper.succeed();
        });
    }

    private static void growsWhilePlayed(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        state(helper).wake(team);
        choose(helper, team, 0);
        helper.runAtTickTime(1, () -> helper.assertValueEqual(cod(helper).interval(), WAKING, "the interval as the challenge begins"));
        helper.runAtTickTime(A_WHILE + 1, () -> {
            helper.assertValueEqual(cod(helper).interval(), 39, "the interval a third of a minute into the challenge");
            helper.succeed();
        });
    }

    /** Ten minutes in, at twice the waking pace, it makes one essence each 20 ticks, all counted as made. */
    private static void twiceAsFastTenMinutesIn(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        state(helper).wake(team);
        choose(helper, team, 10 * MINUTE);
        helper.runAtTickTime(2 * TWICE + MARGIN, () -> {
            helper.assertValueEqual(cod(helper).interval(), TWICE, "the interval ten minutes into the challenge");
            helper.assertValueEqual(state(helper).inflation(team).orElseThrow().made(), 2L, "the essence made in 42 ticks");
            helper.succeed();
        });
    }

    private static void fallsBackOnceEnded(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        state(helper).wake(team);
        choose(helper, team, 10 * MINUTE);
        helper.runAtTickTime(1, () -> {
            helper.assertValueEqual(cod(helper).interval(), TWICE, "the interval ten minutes into the challenge");
            helper.assertTrue(state(helper).endChallenge(team), "the challenge didn't end");
        });
        helper.runAtTickTime(2, () -> {
            helper.assertValueEqual(cod(helper).interval(), WAKING, "the interval once the challenge ended");
            helper.succeed();
        });
    }

    /** Chooses the energy challenge for {@code team}, as though it began {@code ago} ticks before now. */
    private static void choose(GameTestHelper helper, UUID team, long ago) {
        long now = helper.getLevel().getServer().overworld().getGameTime();
        helper.assertTrue(state(helper).choose(team, Handicap.ENERGY, now - ago), "the challenge didn't start");
    }

    private static CodBlockEntity cod(GameTestHelper helper) {
        return helper.getBlockEntity(COD, CodBlockEntity.class);
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(helper.getLevel().getServer());
    }
}
