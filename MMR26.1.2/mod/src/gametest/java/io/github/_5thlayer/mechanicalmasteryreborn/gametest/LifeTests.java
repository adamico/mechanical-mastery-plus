// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Diet;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LifeHandicap;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/**
 * The life handicap (#244): while a team plays life its players' hunger drains twice as fast, and a food already in a
 * player's Diet feeds them a quarter less a repeat, in hunger and saturation, while a varied meal feeds in full; a win
 * or a reset forgets the Diet. Each test is a team of its own, its players mock ones.
 */
final class LifeTests {

    private LifeTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("life_hunger_drains_faster_only_for_the_playing_team", 1, LifeTests::fasterHunger);
        tests.test("life_a_repeat_feeds_less", 1, LifeTests::repeatFeedsLess);
        tests.test("life_a_varied_meal_feeds_in_full", 1, LifeTests::variedFeedsInFull);
        tests.test("life_diet_forgotten_on_a_reset", 3, LifeTests::forgottenOnAReset);
        tests.test("life_diet_forgotten_on_a_win", 1, LifeTests::forgottenOnAWin);
    }

    private static void fasterHunger(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Player playing = player(helper, team);
        Player other = player(helper, UUID.randomUUID());
        Challenges.begin(server(helper), team, Handicap.LIFE);
        playing.causeFoodExhaustion(1.0F);
        other.causeFoodExhaustion(1.0F);
        helper.assertValueEqual(TestFood.exhaustion(playing), 2.0F, "the exhaustion of a player whose team plays life");
        helper.assertValueEqual(TestFood.exhaustion(other), 1.0F, "the exhaustion of a player of another team");
        Challenges.end(server(helper), team);
        playing.causeFoodExhaustion(1.0F);
        helper.assertValueEqual(TestFood.exhaustion(playing), 3.0F, "the exhaustion after the challenge");
        helper.succeed();
    }

    /** Bread is 5 hunger: 4 again once in the Diet (3.75 rounded), its saturation a quarter less too. */
    private static void repeatFeedsLess(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Player player = player(helper, team);
        Challenges.begin(server(helper), team, Handicap.LIFE);
        eat(helper, player, Items.BREAD);
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 5, "the hunger bread fed first");
        float full = player.getFoodData().getSaturationLevel();
        eat(helper, player, Items.BREAD);
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 4, "the hunger bread fed once in the Diet");
        helper.assertTrue(player.getFoodData().getSaturationLevel() < full, "bread fed as much saturation once in the Diet");
        helper.assertValueEqual(player.getData(LifeHandicap.DIET).repeats(Items.BREAD.builtInRegistryHolder().key().identifier()), 2,
                "the bread in the Diet");
        helper.succeed();
    }

    private static void variedFeedsInFull(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Player player = player(helper, team);
        Challenges.begin(server(helper), team, Handicap.LIFE);
        eat(helper, player, Items.BREAD);
        eat(helper, player, Items.APPLE);
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 4, "the hunger an apple fed after bread");
        eat(helper, player, Items.COOKED_BEEF);
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 8, "the hunger cooked beef fed after bread and an apple");
        helper.succeed();
    }

    /** A tick apart, as a reset can't come in the tick its challenge began. */
    private static void forgottenOnAReset(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Player player = player(helper, team);
        Challenges.begin(server(helper), team, Handicap.LIFE);
        eat(helper, player, Items.BREAD);
        helper.runAfterDelay(1, () -> {
            Challenges.reset(server(helper), team);
            eat(helper, player, Items.BREAD);
            helper.assertValueEqual(player.getFoodData().getFoodLevel(), 5, "the hunger bread fed after a reset");
            helper.succeed();
        });
    }

    private static void forgottenOnAWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Player player = player(helper, team);
        Challenges.begin(server(helper), team, Handicap.LIFE);
        eat(helper, player, Items.BREAD);
        Challenges.win(server(helper), team);
        LifeHandicap.refresh(player);
        helper.assertValueEqual(player.getData(LifeHandicap.DIET), Diet.NONE, "the Diet after a win");
        eat(helper, player, Items.BREAD);
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 5, "the hunger bread fed after a win");
        helper.succeed();
    }

    /** Eats {@code item} as by hand, from no hunger or saturation. */
    private static void eat(GameTestHelper helper, Player player, net.minecraft.world.item.Item item) {
        player.getFoodData().setFoodLevel(0);
        player.getFoodData().setSaturation(0.0F);
        new ItemStack(item).finishUsingItem(helper.getLevel(), player);
    }

    private static Player player(GameTestHelper helper, UUID team) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        return player;
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
