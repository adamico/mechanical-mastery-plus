// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
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
 * The lab (#201): a team chooses a handicap at the lab on its island, which starts that handicap's challenge. Only
 * energy can be chosen yet. The handicap is the team's alone, is chosen once while its challenge is played, and is
 * saved with the team's state. Each test is an island of its own, with another team's in a corner.
 */
final class LabTests {

    private static final BlockPos LAB = new BlockPos(4, 2, 4);
    /** Another team's island, in a corner of the test's. */
    private static final BlockPos OTHER_ISLAND = new BlockPos(7, 2, 7);

    private LabTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("lab_offers_only_energy_and_records_it_for_its_team_only", 1, LabTests::offersOnlyEnergy);
        tests.test("lab_handicap_survives_a_save_and_load", 1, LabTests::survivesASaveAndLoad);
        tests.test("lab_chooses_nothing_for_a_player_of_another_team", 1, LabTests::choosesNothingForAnotherTeam);
        tests.test("lab_chooses_once_while_a_challenge_is_played", 1, LabTests::choosesOnce);
        tests.test("lab_shows_its_team_s_handicap", 1, LabTests::showsItsTeamsHandicap);
    }

    private static void offersOnlyEnergy(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        var player = member(helper, team);
        var menu = open(helper, player);
        for (Handicap handicap : Handicap.values()) {
            helper.assertValueEqual(handicap.choosable(), handicap == Handicap.ENERGY, "whether the lab offers " + handicap);
        }
        for (Handicap handicap : new Handicap[] {Handicap.PROTECTION, Handicap.LIFE, Handicap.MONEY}) {
            helper.assertFalse(menu.clickMenuButton(player, handicap.ordinal()), "the lab let the team choose " + handicap);
            helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the team's handicap after choosing " + handicap);
        }
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
