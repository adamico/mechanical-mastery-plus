// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.EssenceItem;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.DecomposeLog;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/**
 * Decomposing by hand (#166): a right-click with an essence decomposes one into its mix, by the game-test
 * pack's recipe, and rests the essence for its cooldown; it is recorded as by hand.
 */
final class HandDecomposeTests {

    private HandDecomposeTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("hand_decomposes_one_into_its_mix_and_starts_the_cooldown", 1, HandDecomposeTests::decomposesAndCoolsDown);
        tests.test("hand_click_during_the_cooldown_does_nothing", 1, HandDecomposeTests::clickDuringCooldown);
        tests.test("hand_essence_with_no_recipe_does_nothing", 1, HandDecomposeTests::noRecipe);
        tests.test("hand_decompose_is_recorded_as_by_hand", 1, HandDecomposeTests::recorded);
    }

    private static void decomposesAndCoolsDown(GameTestHelper helper) {
        var player = holding(helper, Essence.ADVANCED, 2);
        use(helper, player);
        expectHeld(helper, player, 1);
        for (ItemStack result : RecyclerBuild.ADVANCED_MIX) {
            helper.assertValueEqual(player.getInventory().countItem(result.getItem()), 1, "the player's " + result.getItem().getDescriptionId());
        }
        helper.assertTrue(player.getCooldowns().isOnCooldown(player.getMainHandItem()), "the essence is not on its cooldown");
        helper.assertValueEqual(EssenceItem.HAND_COOLDOWN, 160, "the hand cooldown, in ticks");
        helper.succeed();
    }

    private static void clickDuringCooldown(GameTestHelper helper) {
        var player = holding(helper, Essence.ADVANCED, 2);
        use(helper, player);
        use(helper, player);
        expectHeld(helper, player, 1);
        helper.assertValueEqual(player.getInventory().countItem(RecyclerBuild.ADVANCED_MIX.getFirst().getItem()), 1,
                "the pistons after two clicks, one during the cooldown");
        helper.succeed();
    }

    private static void noRecipe(GameTestHelper helper) {
        var player = holding(helper, Essence.BASIC, 1);
        use(helper, player);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "the Basic essence, which the game-test pack decomposes into nothing");
        helper.assertTrue(!player.getCooldowns().isOnCooldown(player.getMainHandItem()), "an essence that decomposed nothing is on its cooldown");
        helper.succeed();
    }

    private static void recorded(GameTestHelper helper) {
        var team = TestTeams.claim(helper);
        var player = holding(helper, Essence.ADVANCED, 1);
        TestTeams.join(player, team);
        use(helper, player);
        var log = DecomposeLog.get(helper.getLevel().getServer());
        helper.assertValueEqual(log.count(team, Essence.ADVANCED, DecomposeLog.Way.HAND), 1L, "the Advanced essence the team decomposed by hand");
        helper.assertValueEqual(log.count(team, Essence.ADVANCED, DecomposeLog.Way.RECYCLER), 0L,
                "the Advanced essence the team decomposed on a Recycler");
        helper.succeed();
    }

    private static Player holding(GameTestHelper helper, Essence essence, int count) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(essence), count));
        return player;
    }

    private static void use(GameTestHelper helper, Player player) {
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }

    private static void expectHeld(GameTestHelper helper, Player player, int count) {
        helper.assertValueEqual(player.getInventory().countItem(Essences.item(Essence.ADVANCED)), count, "the Advanced essence left");
    }
}
