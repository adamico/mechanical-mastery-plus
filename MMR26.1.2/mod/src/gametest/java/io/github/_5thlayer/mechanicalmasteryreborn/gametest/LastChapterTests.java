// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ArrivalPlatform;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastChapter;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.PolishedCalcite;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The last chapter, which a team reaches with its fourth win (ADR 0012): its players are given the last chapter's
 * stage, which unlocks the End Portal Frame's recipe ({@link TestStages}), and a Nether portal forms on its island. A
 * test notes the team's wins in its state directly.
 */
final class LastChapterTests {

    private LastChapterTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("last_chapter_stage_given_with_the_fourth_win", 1, LastChapterTests::stageWithTheFourthWin);
        tests.test("last_chapter_recipes_are_read_from_its_stage_file", 1, LastChapterTests::recipesFromTheStageFile);
        tests.test("last_chapter_recipes_marked_locked_until_the_fourth_win", 1, LastChapterTests::markedUntilTheFourthWin);
        tests.test("last_chapter_polished_calcite_drops_itself", 1, LastChapterTests::polishedCalciteDropsItself);
        tests.test("last_chapter_nether_portal_forms_only_after_the_fourth_win", 1, LastChapterTests::portalAfterTheFourthWin);
        tests.test("last_chapter_nether_portal_never_forms_off_an_island", 1, LastChapterTests::noPortalOffAnIsland);
        tests.test("last_chapter_nether_portal_always_forms_in_the_nether", 1, LastChapterTests::portalInTheNether);
        tests.test("last_chapter_arrival_platform_rings_the_frame", 1, LastChapterTests::arrivalPlatform);
        tests.test("last_chapter_arrival_platform_under_a_portal_made_in_mid_air", 1, LastChapterTests::platformInMidAir);
    }

    /** Notes {@code handicaps}' challenges won for {@code team}, as winning each does, with none of a win's other effects. */
    private static void win(GameTestHelper helper, UUID team, Handicap... handicaps) {
        TeamState state = TeamState.get(helper.getLevel().getServer());
        for (Handicap handicap : handicaps) {
            helper.assertTrue(state.blockTier(team, handicap), "the " + handicap.id() + " challenge wasn't noted won");
        }
    }

    /** What EMI marks as the last chapter's: every recipe its stage file names, which in the game-test pack is the eye of ender's. */
    private static void recipesFromTheStageFile(GameTestHelper helper) {
        helper.assertValueEqual(ChainMachines.lastChapterRecipes(helper.getLevel().getServer()),
                Set.of(Identifier.withDefaultNamespace("ender_eye")), "the last chapter's stage file's recipes");
        helper.succeed();
    }

    /** The table a team's player is sent marks the last chapter's recipes locked until the fourth win, and never after. */
    private static void markedUntilTheFourthWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        win(helper, team, Handicap.PROTECTION, Handicap.LIFE, Handicap.ENERGY);
        var table = ChainMachines.table(server, team);
        helper.assertTrue(table.lastChapterLocked(), "the last chapter's recipes unmarked after three wins");
        helper.assertTrue(table.lastChapter().contains(Identifier.withDefaultNamespace("ender_eye")), "the table sent lacks the last chapter's recipe");
        win(helper, team, Handicap.MONEY);
        helper.assertFalse(ChainMachines.table(server, team).lastChapterLocked(), "the last chapter's recipes marked after the fourth win");
        helper.succeed();
    }

    /** Polished Calcite, which the End Portal Frame is crafted from, drops itself when mined, as calcite does. */
    private static void polishedCalciteDropsItself(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 2, 1);
        helper.setBlock(at, PolishedCalcite.BLOCK.get());
        var drops = Block.getDrops(helper.getBlockState(at), helper.getLevel(), helper.absolutePos(at), null);
        helper.assertValueEqual(drops.size(), 1, "the kinds Polished Calcite drops");
        helper.assertTrue(drops.getFirst().is(PolishedCalcite.ITEM.get()) && drops.getFirst().getCount() == 1,
                "Polished Calcite dropped " + drops.getFirst() + ", not itself");
        helper.succeed();
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
    private static BlockPos frame(GameTestHelper helper, int x, int z) {
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
    private static boolean light(GameTestHelper helper, BlockPos inside) {
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

    /**
     * A portal made in mid-air, two wide along x with its lowest left block at {@code (3, 2, 4)}, so its frame's floor is
     * at y 1, gets an obsidian floor two blocks past its frame on every side, x 0 to 7 and z 2 to 6, which takes only air:
     * a block already there stays.
     */
    private static void arrivalPlatform(GameTestHelper helper) {
        BlockPos kept = new BlockPos(0, 1, 2);
        helper.setBlock(kept, Blocks.DIRT);
        var portal = new BlockUtil.FoundRectangle(helper.absolutePos(new BlockPos(3, 2, 4)), 2, 3);
        ArrivalPlatform.build(helper.getLevel(), portal, Direction.Axis.X);
        for (int x = 0; x <= 7; x++) {
            for (int z = 2; z <= 6; z++) {
                BlockPos pos = new BlockPos(x, 1, z);
                if (!pos.equals(kept)) {
                    helper.assertBlockPresent(Blocks.OBSIDIAN, pos);
                }
            }
        }
        helper.assertBlockPresent(Blocks.DIRT, kept);
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(8, 1, 4));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 1, 7));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 1, 1));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 2, 3));
        helper.succeed();
    }

    /**
     * The game makes a portal in mid-air where it finds no ground, as between the End's main island and its outer
     * islands, where the test server's End is void, and the portal gets its platform: its far corner, three blocks
     * past the portal's left side and two across, is obsidian, where vanilla's ledge reaches only one.
     */
    private static void platformInMidAir(GameTestHelper helper) {
        var end = helper.getLevel().getServer().getLevel(Level.END);
        helper.assertTrue(end != null, "the test server has no End");
        var portal = end.getPortalForcer().createPortal(new BlockPos(600, 100, 600), Direction.Axis.X);
        helper.assertTrue(portal.isPresent(), "no portal was made in mid-air");
        BlockPos corner = portal.get().minCorner.below().west(3).north(2);
        helper.assertTrue(end.getBlockState(corner).is(Blocks.OBSIDIAN), "no platform under a portal made in mid-air");
        helper.succeed();
    }
}
