// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Work;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/**
 * Yield (#184): a decompose returns the config's yield of its mix, on a core of every tier (#189) and by hand alike. The
 * game-test pack's Regular essence decomposes into 4 cobblestone and 8 oak planks, so a quarter is exactly 1 and 2.
 */
final class YieldTests {

    private YieldTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        for (int tier = 1; tier <= Recycler.TIERS; tier++) {
            int t = tier;
            tests.test("yield_a_tier_" + tier + "_decompose_returns_the_configs_share_of_the_mix", 1, helper -> recycler(helper, t));
        }
        tests.test("yield_a_decompose_by_hand_returns_the_configs_share_of_the_mix", 1, YieldTests::byHand);
    }

    private static void recycler(GameTestHelper helper, int tier) {
        TestYield.at(0.25, () -> {
            var build = RecyclerBuild.standard(helper);
            build.swapCore(tier);
            build.feed(new ItemStack(Essences.item(Essence.REGULAR)));
            helper.assertValueEqual(build.core().work(), Work.DECOMPOSING, "what a Recycler holding an essence does");
            build.turn(ServerConfig.turnsOf(tier));
            build.expectOutputs(new ItemStack(Items.COBBLESTONE), new ItemStack(Items.OAK_PLANKS, 2));
        });
        helper.succeed();
    }

    private static void byHand(GameTestHelper helper) {
        TestYield.at(0.25, () -> {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(Essence.REGULAR)));
            player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(player.getInventory().countItem(Items.COBBLESTONE), 1, "the cobblestone a quarter of the mix returned");
            helper.assertValueEqual(player.getInventory().countItem(Items.OAK_PLANKS), 2, "the oak planks a quarter of the mix returned");
        });
        helper.succeed();
    }
}
