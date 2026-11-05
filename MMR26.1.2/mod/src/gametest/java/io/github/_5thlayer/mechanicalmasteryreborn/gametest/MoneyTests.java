// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Accounts;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.MoneyHandicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;

/**
 * The money handicap (#213): starting the money challenge sets the team's Balance aside and gives it the
 * {@code reborn_money} Permit, which a win or a reset keeps and a stop takes back (ADR 0013, #296); its end gives the
 * Balance back, with what the team earned meanwhile; a reset sets what it earned aside too, as though the challenge had
 * just begun; and while it plays, a sale pays half. Each test is a team of its own, with an Account of its own
 * ({@link TestAccounts}).
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
        tests.test("money_reset_keeps_its_permit", 1, MoneyTests::resetKeepsPermit);
        tests.test("money_win_keeps_its_permit", 1, MoneyTests::winKeepsPermit);
        tests.test("money_stop_takes_its_permit_back", 1, MoneyTests::stopTakesPermitBack);
        tests.test("money_stop_keeps_the_permits_the_team_bought", 1, MoneyTests::stopKeepsBoughtPermits);
        tests.test("money_start_again_gives_its_permit_back", 1, MoneyTests::startAgainGivesPermitBack);
        tests.test("money_new_account_regranted_only_while_the_chain_is_unlocked", 1, MoneyTests::regrantOnlyWhileUnlocked);
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

    /** A stop forgets no start, as any challenge's; only the chain, and money's Permit with it, is locked again (ADR 0013). */
    private static void staysStarted(GameTestHelper helper) {
        UUID team = account(helper, 0);
        helper.assertFalse(state(helper).started(team, Handicap.MONEY), "the money challenge counted as started before it was");
        Challenges.begin(server(helper), team, Handicap.MONEY);
        Challenges.end(server(helper), team);
        helper.assertTrue(state(helper).started(team, Handicap.MONEY), "the money challenge's start was forgotten after its end");
        helper.assertFalse(state(helper).unlocked(team, Handicap.MONEY), "the money chain stayed unlocked past its stop");
        helper.succeed();
    }

    private static void resetKeepsPermit(GameTestHelper helper) {
        UUID team = account(helper, 0);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        helper.assertTrue(Challenges.reset(server(helper), team), "the money challenge didn't reset");
        helper.assertTrue(TestAccounts.holds(team, MoneyHandicap.PERMIT), "a reset took " + MoneyHandicap.PERMIT);
        helper.succeed();
    }

    private static void winKeepsPermit(GameTestHelper helper) {
        UUID team = account(helper, 0);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        helper.assertTrue(Challenges.win(server(helper), team), "the money challenge wasn't won");
        helper.assertTrue(TestAccounts.holds(team, MoneyHandicap.PERMIT), "a win took " + MoneyHandicap.PERMIT);
        helper.assertTrue(state(helper).unlocked(team, Handicap.MONEY), "a win locked the money chain");
        helper.succeed();
    }

    /** By the lab's vote ({@link Challenges#stop}) and by an operator's {@code /cod challenge end} ({@link Challenges#end}) alike. */
    private static void stopTakesPermitBack(GameTestHelper helper) {
        UUID team = account(helper, 0);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        Challenges.reset(server(helper), team);
        helper.assertTrue(Challenges.stop(server(helper), team), "the money challenge didn't stop");
        helper.assertFalse(TestAccounts.holds(team, MoneyHandicap.PERMIT), "the team kept " + MoneyHandicap.PERMIT + " past the stop");
        Challenges.begin(server(helper), team, Handicap.MONEY);
        helper.assertTrue(Challenges.end(server(helper), team), "the money challenge didn't end");
        helper.assertFalse(TestAccounts.holds(team, MoneyHandicap.PERMIT), "the team kept " + MoneyHandicap.PERMIT + " past /cod challenge end");
        helper.succeed();
    }

    /** The Permits the team bought in the Shop are its own, as a built machine is. */
    private static void stopKeepsBoughtPermits(GameTestHelper helper) {
        UUID team = account(helper, 0);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        Accounts.get().grant(server(helper), team, "reborn_essence_sell");
        Challenges.end(server(helper), team);
        helper.assertTrue(TestAccounts.holds(team, "reborn_essence_sell"), "the stop took the Essence Sell Permit the team bought");
        helper.assertFalse(TestAccounts.holds(team, MoneyHandicap.PERMIT), "the team kept " + MoneyHandicap.PERMIT + " past the stop");
        helper.succeed();
    }

    private static void startAgainGivesPermitBack(GameTestHelper helper) {
        UUID team = account(helper, 0);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        Challenges.end(server(helper), team);
        helper.assertTrue(Challenges.begin(server(helper), team, Handicap.MONEY), "the money challenge didn't start again");
        helper.assertTrue(TestAccounts.holds(team, MoneyHandicap.PERMIT), "starting again didn't give " + MoneyHandicap.PERMIT + " back");
        helper.succeed();
    }

    /** A new Account, as a recreated party's, starts with no Permit: it is given money's again only while the chain is unlocked. */
    private static void regrantOnlyWhileUnlocked(GameTestHelper helper) {
        UUID team = account(helper, 0);
        MoneyHandicap.regrant(server(helper), team);
        helper.assertFalse(TestAccounts.holds(team, MoneyHandicap.PERMIT), "a team that never started money was given " + MoneyHandicap.PERMIT);
        Challenges.begin(server(helper), team, Handicap.MONEY);
        TestAccounts.open(team, 0);
        MoneyHandicap.regrant(server(helper), team);
        helper.assertTrue(TestAccounts.holds(team, MoneyHandicap.PERMIT), "a new Account wasn't given " + MoneyHandicap.PERMIT + " while money is played");
        Challenges.end(server(helper), team);
        TestAccounts.open(team, 0);
        MoneyHandicap.regrant(server(helper), team);
        helper.assertFalse(TestAccounts.holds(team, MoneyHandicap.PERMIT), "a new Account was given " + MoneyHandicap.PERMIT + " after a stop");
        Challenges.begin(server(helper), team, Handicap.MONEY);
        Challenges.win(server(helper), team);
        TestAccounts.open(team, 0);
        MoneyHandicap.regrant(server(helper), team);
        helper.assertTrue(TestAccounts.holds(team, MoneyHandicap.PERMIT), "a new Account wasn't given " + MoneyHandicap.PERMIT + " once money is won");
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
