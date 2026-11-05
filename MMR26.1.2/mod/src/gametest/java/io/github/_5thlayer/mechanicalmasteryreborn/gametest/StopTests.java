// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Stops;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabMenu;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

/**
 * A stop at the lab (ADR 0013): offered once the challenge's attempt has reset, made the moment every member online has
 * asked, and locking the challenge's chain again, where a reset and a win keep it. Each test's mock players are counted
 * online ({@link TestTeams#logIn}) for its body only.
 */
final class StopTests {

    private static final BlockPos LAB = new BlockPos(4, 2, 4);
    /** An energy chain machine, by the game-test pack's tag, as {@code chain_machine_*} places it. */
    private static final BlockPos MACHINE = new BlockPos(2, 2, 2);

    private StopTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("stop_not_offered_before_a_reset", 1, StopTests::notOfferedBeforeAReset);
        tests.test("stop_lab_shows_its_row_only_while_offered", 1, StopTests::rowOnlyWhileOffered);
        tests.test("stop_by_a_team_of_one_after_a_reset", 1, StopTests::byATeamOfOne);
        tests.test("stop_locks_the_chain_again", 1, StopTests::locksTheChainAgain);
        tests.test("stop_then_start_again_unlocks_and_waits_for_a_reset", 1, StopTests::startAgain);
        tests.test("stop_keeps_another_chain_unlocked", 1, StopTests::keepsAnotherChain);
        tests.test("stop_waits_for_every_member_online", 1, StopTests::waitsForEveryMember);
        tests.test("stop_drops_the_ask_of_a_member_who_logs_out", 1, StopTests::dropsALoggedOutAsk);
        tests.test("stop_waits_for_a_member_who_logs_in", 1, StopTests::waitsForALogIn);
        tests.test("stop_made_when_the_last_member_left_to_ask_logs_out", 1, StopTests::madeWhenTheLastLeftLogsOut);
        tests.test("stop_take_back_removes_the_ask", 1, StopTests::takeBack);
        tests.test("stop_asks_cleared_by_a_reset_or_a_win", 1, StopTests::clearedByAResetOrAWin);
        tests.test("stop_offer_survives_a_save_and_load", 1, StopTests::survivesASaveAndLoad);
    }

    private static void notOfferedBeforeAReset(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 1, players -> {
            Player player = players.getFirst();
            var menu = open(helper, player);
            helper.assertFalse(menu.stopOffered(), "the lab offered a stop before the team played");
            menu.clickMenuButton(player, Handicap.ENERGY.ordinal());
            helper.assertFalse(menu.stopOffered(), "the lab offered a stop before the attempt reset");
            helper.assertFalse(menu.clickMenuButton(player, LabMenu.ASK), "a forged ask was taken before the attempt reset");
            helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap after a forged ask");
            Challenges.reset(server(helper), team);
            var visitor = helper.makeMockPlayer(GameType.SURVIVAL);
            TestTeams.join(visitor, UUID.randomUUID());
            helper.assertFalse(open(helper, visitor).clickMenuButton(visitor, LabMenu.ASK), "the lab took an ask from another team's player");
            helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap after a visitor's ask");
        });
        helper.succeed();
    }

    /** The lab has no stop's row until the played challenge has reset, and none again once it ends; an open lab follows. */
    private static void rowOnlyWhileOffered(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 1, players -> {
            Player player = players.getFirst();
            var menu = open(helper, player);
            helper.assertFalse(menu.showsStop(), "the lab shows the stop's row before the team played");
            menu.clickMenuButton(player, Handicap.ENERGY.ordinal());
            helper.assertFalse(menu.showsStop(), "the lab shows the stop's row before the attempt reset");
            Challenges.reset(server(helper), team);
            helper.assertTrue(menu.showsStop(), "a lab open through a reset hides the stop's row");
            helper.assertTrue(open(helper, player).showsStop(), "the lab hides the stop's row after a reset");
            Challenges.end(server(helper), team);
            helper.assertFalse(menu.showsStop(), "a lab open through the challenge's end still shows the stop's row");
            helper.assertFalse(open(helper, player).showsStop(), "the lab shows the stop's row after the challenge ended");
        });
        helper.succeed();
    }

    private static void byATeamOfOne(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 1, players -> {
            Player player = players.getFirst();
            var menu = open(helper, player);
            menu.clickMenuButton(player, Handicap.ENERGY.ordinal());
            Challenges.reset(server(helper), team);
            state(helper).deliver(team, 5);
            helper.assertTrue(menu.stopOffered(), "the lab offered no stop after a reset");
            helper.assertTrue(menu.clickMenuButton(player, LabMenu.ASK), "the lab refused the ask");
            helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the team's handicap after the stop");
            helper.assertTrue(state(helper).inflation(team).isEmpty(), "Inflation went on after the stop");
            helper.assertValueEqual(state(helper).delivered(team), 0L, "the Port's count after the stop");
            helper.assertFalse(menu.stopOffered(), "the lab offered a stop after the stop");
        });
        helper.succeed();
    }

    private static void locksTheChainAgain(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(MACHINE, Blocks.BLAST_FURNACE);
        online(helper, team, 1, players -> {
            Player player = players.getFirst();
            var menu = open(helper, player);
            menu.clickMenuButton(player, Handicap.ENERGY.ordinal());
            Chains.unlock(server(helper), player);
            helper.assertTrue(TestStages.holds(player, Handicap.ENERGY.id()), "the energy chain stayed locked once its challenge started");
            helper.assertValueEqual(lockedAt(helper), Optional.empty(), "the machine's lock once energy started");
            Challenges.reset(server(helper), team);
            menu.clickMenuButton(player, LabMenu.ASK);
            helper.assertFalse(TestStages.holds(player, Handicap.ENERGY.id()), "the player kept the energy chain past the stop");
            helper.assertFalse(state(helper).unlocked(team, Handicap.ENERGY), "the team kept the energy chain past the stop");
            helper.assertValueEqual(lockedAt(helper), Optional.of(Handicap.ENERGY), "the machine's lock after the stop");
            for (Handicap handicap : Handicap.values()) {
                helper.assertTrue(menu.canChoose(handicap), "the lab didn't offer " + handicap.id() + " after the stop");
            }
        });
        helper.succeed();
    }

    private static void startAgain(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 1, players -> {
            Player player = players.getFirst();
            var menu = open(helper, player);
            menu.clickMenuButton(player, Handicap.ENERGY.ordinal());
            Challenges.reset(server(helper), team);
            menu.clickMenuButton(player, LabMenu.ASK);
            helper.assertTrue(menu.clickMenuButton(player, Handicap.ENERGY.ordinal()), "the lab refused the stopped challenge");
            Chains.unlock(server(helper), player);
            helper.assertTrue(TestStages.holds(player, Handicap.ENERGY.id()), "starting energy again left its chain locked");
            helper.assertFalse(menu.stopOffered(), "the lab offered a stop before this attempt reset");
            helper.assertFalse(menu.clickMenuButton(player, LabMenu.ASK), "an ask was taken before this attempt reset");
        });
        helper.succeed();
    }

    private static void keepsAnotherChain(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 1, players -> {
            Player player = players.getFirst();
            Challenges.begin(server(helper), team, Handicap.LIFE);
            Challenges.win(server(helper), team);
            var menu = open(helper, player);
            menu.clickMenuButton(player, Handicap.ENERGY.ordinal());
            Challenges.reset(server(helper), team);
            menu.clickMenuButton(player, LabMenu.ASK);
            helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the team's handicap after the stop");
            helper.assertTrue(TestStages.holds(player, Handicap.LIFE.id()), "the stop locked the life chain won earlier");
            helper.assertTrue(state(helper).unlocked(team, Handicap.LIFE), "the team lost the life chain to the stop");
            helper.assertFalse(menu.canChoose(Handicap.LIFE), "the lab offered life, won already");
        });
        helper.succeed();
    }

    private static void waitsForEveryMember(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 2, players -> {
            playedAndReset(helper, team);
            helper.assertTrue(ask(helper, players.get(0)), "the first ask wasn't taken");
            helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap after one ask of two");
            helper.assertFalse(ask(helper, players.get(0)), "the same ask was taken twice");
            helper.assertTrue(ask(helper, players.get(1)), "the second ask wasn't taken");
            helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the team's handicap after both asked");
        });
        helper.succeed();
    }

    private static void dropsALoggedOutAsk(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 2, players -> {
            playedAndReset(helper, team);
            ask(helper, players.get(0));
            // As the server's list does, the player leaves it only once the logout is told.
            Stops.loggedOut(server(helper), players.get(0));
            TestTeams.logOut(players.get(0));
            helper.assertFalse(Stops.asked(team, players.get(0)), "the ask outlived its player's logout");
            helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap after the asker logged out");
            ask(helper, players.get(1));
            helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the team's handicap after the one online asked");
        });
        helper.succeed();
    }

    private static void madeWhenTheLastLeftLogsOut(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 2, players -> {
            playedAndReset(helper, team);
            ask(helper, players.get(0));
            Stops.loggedOut(server(helper), players.get(1));
            TestTeams.logOut(players.get(1));
            helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(),
                    "the team's handicap once the only member who hadn't asked logged out");
        });
        helper.succeed();
    }

    private static void waitsForALogIn(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 2, players -> {
            playedAndReset(helper, team);
            ask(helper, players.get(0));
            var late = member(helper, team);
            TestTeams.logIn(late);
            try {
                ask(helper, players.get(1));
                helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the stop didn't wait for a member who logged in");
                ask(helper, late);
                helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the team's handicap after all three asked");
            } finally {
                TestTeams.logOut(late);
            }
        });
        helper.succeed();
    }

    private static void takeBack(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 2, players -> {
            playedAndReset(helper, team);
            Player first = players.get(0);
            ask(helper, first);
            var menu = open(helper, first);
            helper.assertTrue(menu.asked(), "the lab didn't show the player's ask");
            helper.assertTrue(menu.clickMenuButton(first, LabMenu.TAKE_BACK), "the take-back wasn't taken");
            helper.assertFalse(menu.asked(), "the lab showed the ask taken back");
            helper.assertFalse(menu.clickMenuButton(first, LabMenu.TAKE_BACK), "a take-back was taken with no ask");
            ask(helper, players.get(1));
            helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap after an ask taken back");
        });
        helper.succeed();
    }

    private static void clearedByAResetOrAWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        online(helper, team, 2, players -> {
            playedAndReset(helper, team);
            ask(helper, players.get(0));
            Challenges.reset(server(helper), team);
            helper.assertFalse(Stops.asked(team, players.get(0)), "an ask outlived a reset");
            ask(helper, players.get(0));
            Challenges.win(server(helper), team);
            helper.assertFalse(Stops.asked(team, players.get(0)), "an ask outlived a win");
        });
        helper.succeed();
    }

    /** Through the codec the world saves and loads the team state by, as {@code lab_handicap_survives_a_save_and_load} does. */
    private static void survivesASaveAndLoad(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        playedAndReset(helper, team);
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var codec = TeamState.TYPE.codec();
        Tag saved = codec.encodeStart(ops, state(helper)).getOrThrow();
        TeamState loaded = codec.parse(ops, saved).getOrThrow();
        helper.assertValueEqual(loaded.resets(team), 1, "the attempt's resets after a load");
        Challenges.end(server(helper), team);
        saved = codec.encodeStart(ops, state(helper)).getOrThrow();
        loaded = codec.parse(ops, saved).getOrThrow();
        helper.assertFalse(loaded.unlocked(team, Handicap.ENERGY), "the chain a stop locked was unlocked after a load");
        helper.assertTrue(loaded.started(team, Handicap.ENERGY), "the challenge stopped was never started after a load");
        helper.succeed();
    }

    /** Runs {@code body} with {@code count} members of {@code team} online, logging them out after. */
    private static void online(GameTestHelper helper, UUID team, int count, Consumer<List<Player>> body) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var player = member(helper, team);
            TestTeams.logIn(player);
            players.add(player);
        }
        try {
            body.accept(players);
        } finally {
            players.forEach(TestTeams::logOut);
        }
    }

    private static void playedAndReset(GameTestHelper helper, UUID team) {
        Challenges.begin(server(helper), team, Handicap.ENERGY);
        Challenges.reset(server(helper), team);
    }

    /** Asks for the stop at the lab, as {@code player} would. */
    private static boolean ask(GameTestHelper helper, Player player) {
        return open(helper, player).clickMenuButton(player, LabMenu.ASK);
    }

    private static Player member(GameTestHelper helper, UUID team) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        return player;
    }

    /** Places the lab, if it isn't there yet, and opens it as {@code player} would. */
    private static LabMenu open(GameTestHelper helper, Player player) {
        if (!helper.getBlockState(LAB).is(Lab.BLOCK.get())) {
            helper.setBlock(LAB, Lab.BLOCK.get());
        }
        BlockPos pos = helper.absolutePos(LAB);
        var provider = helper.getBlockState(LAB).getMenuProvider(helper.getLevel(), pos);
        helper.assertTrue(provider != null, "the lab opens nothing");
        AbstractContainerMenu menu = provider.createMenu(0, player.getInventory(), player);
        helper.assertTrue(menu instanceof LabMenu, "the lab opened " + menu);
        return (LabMenu) menu;
    }

    private static Optional<Handicap> lockedAt(GameTestHelper helper) {
        BlockPos absolute = helper.absolutePos(MACHINE);
        return ChainMachines.lockedAt(helper.getLevel(), absolute, helper.getLevel().getBlockState(absolute));
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(server(helper));
    }
}
