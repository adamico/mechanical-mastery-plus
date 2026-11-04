// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.MoneyHandicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;

/**
 * The money handicap (#213): starting the money challenge sets the team's Balance aside and gives it the
 * {@code reborn_money} Permit for good; its end gives the Balance back, with what the team earned meanwhile; a reset
 * sets what it earned aside too, as though the challenge had just begun; and while it plays, a sale pays half. Each
 * test is a team of its own, with an Account of its own ({@link TestAccounts}).
 */
final class MoneyTests {

    private MoneyTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("money_start_sets_the_balance_aside_and_grants_its_permit", 1, MoneyTests::startSetsAside);
        tests.test("money_start_is_refused_without_an_account", 1, MoneyTests::refusedWithoutAnAccount);
        tests.test("money_challenge_stays_started_after_its_end", 1, MoneyTests::staysStarted);
        tests.test("money_end_gives_the_balance_back_with_what_was_earned", 1, MoneyTests::endGivesBack);
        tests.test("money_reset_sets_what_was_earned_aside_too", 1, MoneyTests::resetSetsAside);
        tests.test("money_sale_pays_half_only_while_the_challenge_is_played", 1, MoneyTests::salePaysHalf);
        tests.test("money_another_challenge_leaves_the_balance_alone", 1, MoneyTests::anotherChallenge);
        tests.test("money_set_aside_survives_a_save_and_load", 1, MoneyTests::survivesASaveAndLoad);
    }

    private static void startSetsAside(GameTestHelper helper) {
        UUID team = account(helper, 500);
        helper.assertTrue(Challenges.begin(server(helper), team, Handicap.MONEY), "the money challenge didn't start");
        helper.assertValueEqual(TestAccounts.balanceOf(team), 0L, "the Balance while the challenge is played");
        helper.assertValueEqual(state(helper).setAside(team), 500L, "what was set aside");
        helper.assertTrue(TestAccounts.holds(team, MoneyHandicap.PERMIT), "the team wasn't given " + MoneyHandicap.PERMIT);
        helper.succeed();
    }

    /** A team whose island has no FTB party yet has no Account, so nothing could be set aside or granted. */
    private static void refusedWithoutAnAccount(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.assertFalse(Challenges.begin(server(helper), team, Handicap.MONEY), "the money challenge started with no Account");
        helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the team's handicap");
        helper.succeed();
    }

    /** What lets a recreated party be given {@link MoneyHandicap#PERMIT} again (ADR 0007). */
    private static void staysStarted(GameTestHelper helper) {
        UUID team = account(helper, 0);
        helper.assertFalse(state(helper).started(team, Handicap.MONEY), "the money challenge was started before it started");
        Challenges.begin(server(helper), team, Handicap.MONEY);
        Challenges.end(server(helper), team);
        // A stop costs money only the attempt (ADR 0013).
        helper.assertTrue(state(helper).started(team, Handicap.MONEY), "the money challenge's start was forgotten after its end");
        helper.succeed();
    }

    private static void endGivesBack(GameTestHelper helper) {
        UUID team = account(helper, 500);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        TestAccounts.open(team, 120);
        helper.assertTrue(Challenges.end(server(helper), team), "the money challenge didn't end");
        helper.assertValueEqual(TestAccounts.balanceOf(team), 620L, "the Balance after the challenge");
        helper.assertValueEqual(state(helper).setAside(team), 0L, "what is still set aside");
        helper.succeed();
    }

    private static void resetSetsAside(GameTestHelper helper) {
        UUID team = account(helper, 500);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        TestAccounts.open(team, 120);
        helper.assertTrue(Challenges.reset(server(helper), team), "the money challenge didn't reset");
        helper.assertValueEqual(TestAccounts.balanceOf(team), 0L, "the Balance after a reset");
        helper.assertValueEqual(state(helper).setAside(team), 620L, "what was set aside after a reset");
        Challenges.end(server(helper), team);
        helper.assertValueEqual(TestAccounts.balanceOf(team), 620L, "the Balance after the challenge");
        helper.succeed();
    }

    private static void salePaysHalf(GameTestHelper helper) {
        UUID team = account(helper, 0);
        helper.assertValueEqual(MoneyHandicap.sale(server(helper), team, 101), 101L, "a sale before the challenge");
        Challenges.begin(server(helper), team, Handicap.MONEY);
        helper.assertValueEqual(MoneyHandicap.sale(server(helper), team, 101), 50L, "a sale while it is played");
        Challenges.end(server(helper), team);
        helper.assertValueEqual(MoneyHandicap.sale(server(helper), team, 101), 101L, "a sale after it");
        helper.succeed();
    }

    private static void anotherChallenge(GameTestHelper helper) {
        UUID team = account(helper, 500);
        Challenges.begin(server(helper), team, Handicap.ENERGY);
        helper.assertValueEqual(TestAccounts.balanceOf(team), 500L, "the Balance during the energy challenge");
        helper.assertValueEqual(MoneyHandicap.sale(server(helper), team, 101), 101L, "a sale during the energy challenge");
        helper.assertFalse(TestAccounts.holds(team, MoneyHandicap.PERMIT), "the energy challenge gave " + MoneyHandicap.PERMIT);
        Challenges.end(server(helper), team);
        helper.assertValueEqual(TestAccounts.balanceOf(team), 500L, "the Balance after the energy challenge");
        helper.succeed();
    }

    /** Through the codec the world saves and loads the team state by, as {@code lab_handicap_survives_a_save_and_load} does. */
    private static void survivesASaveAndLoad(GameTestHelper helper) {
        UUID team = account(helper, 500);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var codec = TeamState.TYPE.codec();
        TeamState loaded = codec.parse(ops, codec.encodeStart(ops, state(helper)).getOrThrow()).getOrThrow();
        helper.assertValueEqual(loaded.setAside(team), 500L, "what was set aside after a load");
        helper.succeed();
    }

    private static UUID account(GameTestHelper helper, long balance) {
        UUID team = TestTeams.claim(helper);
        TestAccounts.open(team, balance);
        return team;
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(server(helper));
    }
}
