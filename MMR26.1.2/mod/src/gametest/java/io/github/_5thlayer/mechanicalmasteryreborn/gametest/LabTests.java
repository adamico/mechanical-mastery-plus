// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabMenu;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.GameType;

/**
 * The lab (#201): a team chooses a handicap at the lab on its island, which starts that handicap's challenge, any of
 * the four (#242). The handicap is the team's alone, is chosen once while its challenge is played, and is
 * saved with the team's state. An op starts and ends the challenge of the island they stand on. Each test is an island
 * of its own, with another team's in a corner.
 */
final class LabTests {

    private static final BlockPos LAB = new BlockPos(4, 2, 4);
    /** Where protection's test places C.O.D., which the Assault is built around. */
    private static final BlockPos COD = new BlockPos(1, 2, 1);
    /** Ticks for a placed C.O.D. to have ticked, noting where it stands. */
    private static final int TICKED = 2;
    /** Another team's island, in a corner of the test's. */
    private static final BlockPos OTHER_ISLAND = new BlockPos(7, 2, 7);

    private LabTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("lab_records_a_handicap_for_its_team_only", 1, LabTests::recordsForItsTeamOnly);
        tests.test("lab_handicap_survives_a_save_and_load", 1, LabTests::survivesASaveAndLoad);
        tests.test("lab_chooses_nothing_for_a_player_of_another_team", 1, LabTests::choosesNothingForAnotherTeam);
        tests.test("lab_starts_life", 1, LabTests::startsLife);
        tests.test("lab_starts_protection", 20, LabTests::startsProtection);
        tests.test("lab_chooses_once_while_a_challenge_is_played", 1, LabTests::choosesOnce);
        tests.test("lab_shows_its_team_s_handicap", 1, LabTests::showsItsTeamsHandicap);
        tests.test("lab_op_starts_and_ends_the_challenge_of_the_island_stood_on", 1, LabTests::opStartsAndEnds);
    }

    private static void recordsForItsTeamOnly(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        var player = member(helper, team);
        var menu = open(helper, player);
        helper.assertTrue(menu.clickMenuButton(player, Handicap.ENERGY.ordinal()), "the lab refused energy");
        helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap after choosing energy");
        helper.assertValueEqual(state(helper).handicap(other), Optional.<Handicap>empty(), "another team's handicap");
        helper.succeed();
    }

    /** Through the codec the world saves and loads the team state by, as {@code cod_team_state_survives_a_save_and_load} does. */
    private static void survivesASaveAndLoad(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var player = member(helper, team);
        open(helper, player).clickMenuButton(player, Handicap.ENERGY.ordinal());
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var codec = TeamState.TYPE.codec();
        Tag saved = codec.encodeStart(ops, state(helper)).getOrThrow();
        TeamState loaded = codec.parse(ops, saved).getOrThrow();
        helper.assertValueEqual(loaded.handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap after a load");
        helper.assertFalse(loaded.isAwake(team), "choosing a handicap woke the team's C.O.D.");
        helper.assertValueEqual(loaded.handicap(UUID.randomUUID()), Optional.<Handicap>empty(), "a team that never chose, after a load");
        helper.succeed();
    }

    private static void choosesNothingForAnotherTeam(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID visitors = UUID.randomUUID();
        var visitor = member(helper, visitors);
        helper.assertFalse(open(helper, visitor).clickMenuButton(visitor, Handicap.ENERGY.ordinal()),
                "the lab let a player of another team choose");
        helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the island's team's handicap");
        helper.assertValueEqual(state(helper).handicap(visitors), Optional.<Handicap>empty(), "the visiting team's handicap");
        helper.succeed();
    }

    private static void startsLife(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var player = member(helper, team);
        helper.assertTrue(open(helper, player).clickMenuButton(player, Handicap.LIFE.ordinal()), "the lab refused life");
        helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.LIFE), "the team's handicap after choosing life");
        helper.succeed();
    }

    /** Choosing protection sends the team its Assault, as the op's command does (#239); {@code assault_*} checks the Assault. */
    private static void startsProtection(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var player = member(helper, team);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            helper.assertTrue(open(helper, player).clickMenuButton(player, Handicap.PROTECTION.ordinal()), "the lab refused protection");
            helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.PROTECTION), "the team's handicap after choosing protection");
            helper.assertTrue(AssaultState.get(helper.getLevel().getServer()).assault(team).isPresent(), "choosing protection sent no Assault");
            Challenges.end(helper.getLevel().getServer(), team);
            helper.succeed();
        });
    }

    private static void choosesOnce(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var player = member(helper, team);
        var menu = open(helper, player);
        helper.assertTrue(menu.clickMenuButton(player, Handicap.ENERGY.ordinal()), "the lab refused energy");
        helper.assertFalse(menu.clickMenuButton(player, Handicap.ENERGY.ordinal()), "the lab started a challenge while one is played");
        helper.assertFalse(menu.clickMenuButton(player, -1), "the lab took a button it doesn't have");
        helper.assertFalse(menu.clickMenuButton(player, Handicap.values().length), "the lab took a button it doesn't have");
        helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap");
        helper.succeed();
    }

    private static void showsItsTeamsHandicap(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var player = member(helper, team);
        var menu = open(helper, player);
        helper.assertValueEqual(menu.chosen(), Optional.<Handicap>empty(), "the handicap the lab shows before choosing");
        menu.clickMenuButton(player, Handicap.ENERGY.ordinal());
        helper.assertValueEqual(menu.chosen(), Optional.of(Handicap.ENERGY), "the handicap the lab shows after choosing");
        helper.assertValueEqual(open(helper, player).chosen(), Optional.of(Handicap.ENERGY), "the handicap the lab shows on opening again");
        helper.succeed();
    }

    private static void opStartsAndEnds(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        var server = helper.getLevel().getServer();
        var op = server.createCommandSourceStack().withLevel(helper.getLevel())
                .withPosition(helper.absolutePos(LAB).getCenter()).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(op, "cod challenge start life");
        // An op may start a challenge without the lab (#235).
        helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.LIFE), "the team's handicap after an op started life");
        server.getCommands().performPrefixedCommand(op, "cod challenge end");
        server.getCommands().performPrefixedCommand(op, "cod challenge start energy");
        helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap after an op started energy");
        helper.assertValueEqual(state(helper).handicap(other), Optional.<Handicap>empty(), "another team's handicap");
        state(helper).wake(team);
        server.getCommands().performPrefixedCommand(op, "cod challenge end");
        helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the team's handicap after an op ended it");
        helper.assertTrue(state(helper).isAwake(team), "ending the challenge put the team's C.O.D. back to dormant");
        server.getCommands().performPrefixedCommand(op, "cod challenge start energy");
        helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the team's handicap, started again");
        helper.succeed();
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

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(helper.getLevel().getServer());
    }
}
