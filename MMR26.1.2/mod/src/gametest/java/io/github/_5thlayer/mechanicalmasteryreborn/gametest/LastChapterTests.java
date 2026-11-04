// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastChapter;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * The last chapter, which a team reaches with its fourth win (ADR 0012): its players are given the last chapter's
 * stage, which unlocks the End Portal Frame's recipe ({@link TestStages}). Wins are played as an op's commands play them.
 */
final class LastChapterTests {

    private LastChapterTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("last_chapter_stage_given_with_the_fourth_win", 1, LastChapterTests::stageWithTheFourthWin);
        tests.test("last_chapter_nether_portal_forms_only_after_the_fourth_win", 1, LastChapterTests::portalAfterTheFourthWin);
        tests.test("last_chapter_nether_portal_never_forms_off_an_island", 1, LastChapterTests::noPortalOffAnIsland);
        tests.test("last_chapter_nether_portal_always_forms_in_the_nether", 1, LastChapterTests::portalInTheNether);
    }

    /** Wins {@code handicaps}' challenges for {@code team}, one after another. */
    static void win(GameTestHelper helper, UUID team, Handicap... handicaps) {
        var server = helper.getLevel().getServer();
        TestAccounts.open(team, 0);
        for (Handicap handicap : handicaps) {
            helper.assertTrue(Challenges.begin(server, team, handicap), "the " + handicap.id() + " challenge didn't start");
            helper.assertTrue(Challenges.win(server, team), "the " + handicap.id() + " challenge wasn't won");
        }
    }

    /** No member was online at the win: a player is given the stage as they log in or join the island. */
    private static void stageWithTheFourthWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        win(helper, team, Handicap.PROTECTION, Handicap.LIFE, Handicap.ENERGY);
        Chains.unlock(server, player);
        helper.assertFalse(TestStages.holds(player, LastChapter.STAGE), "the last chapter's stage after three wins");
        win(helper, team, Handicap.MONEY);
        Chains.unlock(server, player);
        helper.assertTrue(TestStages.holds(player, LastChapter.STAGE), "no last chapter's stage after the fourth win");
        helper.succeed();
    }

    /**
     * An obsidian frame on the platform, its bottom in the floor: two wide and three tall inside, along x from
     * {@code (x, 0, z)}, so the inside's lowest left block is {@code (x + 1, 1, z)}.
     */
    static BlockPos frame(GameTestHelper helper, int x, int z) {
        for (int i = 1; i <= 2; i++) {
            helper.setBlock(new BlockPos(x + i, 0, z), Blocks.OBSIDIAN);
            helper.setBlock(new BlockPos(x + i, 4, z), Blocks.OBSIDIAN);
        }
        for (int y = 1; y <= 3; y++) {
            helper.setBlock(new BlockPos(x, y, z), Blocks.OBSIDIAN);
            helper.setBlock(new BlockPos(x + 3, y, z), Blocks.OBSIDIAN);
        }
        return new BlockPos(x + 1, 1, z);
    }

    /** Lights the frame whose inside's lowest left block is {@code inside}, as fire does, and says whether a portal formed. */
    static boolean light(GameTestHelper helper, BlockPos inside) {
        helper.setBlock(inside, Blocks.FIRE);
        return helper.getBlockState(inside).is(Blocks.NETHER_PORTAL);
    }

    /** The lock is the island's team's wins, read as they are, so no Chapters stage is given here. */
    private static void portalAfterTheFourthWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        BlockPos inside = frame(helper, 2, 4);
        win(helper, team, Handicap.PROTECTION, Handicap.LIFE, Handicap.ENERGY);
        helper.assertFalse(light(helper, inside), "a Nether portal formed on the island of a team with three wins");
        helper.setBlock(inside, Blocks.AIR);
        win(helper, team, Handicap.MONEY);
        helper.assertTrue(light(helper, inside), "no Nether portal formed on the island of a team with four wins");
        helper.succeed();
    }

    /** Off any team's island, as in the void beside it, a frame never forms, whatever the nearest team has won. */
    private static void noPortalOffAnIsland(GameTestHelper helper) {
        UUID team = UUID.randomUUID();
        TestTeams.nearest(helper, team);
        BlockPos inside = frame(helper, 2, 4);
        win(helper, team, Handicap.PROTECTION, Handicap.LIFE, Handicap.ENERGY, Handicap.MONEY);
        var at = helper.absolutePos(inside);
        helper.assertTrue(Teams.at(helper.getLevel(), at).equals(team), "the frame's nearest team wasn't the winning one");
        helper.assertFalse(Teams.onIsland(helper.getLevel(), at), "the frame stood on an island");
        helper.assertFalse(light(helper, inside), "a Nether portal formed off any team's island");
        helper.succeed();
    }

    /**
     * In the Nether a frame always forms, so the way home is there, though no team there has won a thing. Read from the
     * lock itself, since no test's structure stands in the Nether.
     */
    private static void portalInTheNether(GameTestHelper helper) {
        TestTeams.claim(helper);
        var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null, "the test server has no Nether");
        helper.assertTrue(LastChapter.mayFormNetherPortal(nether, helper.absolutePos(BlockPos.ZERO)),
                "a Nether portal was refused in the Nether");
        helper.succeed();
    }
}
