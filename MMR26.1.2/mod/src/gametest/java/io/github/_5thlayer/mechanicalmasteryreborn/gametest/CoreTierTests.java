// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Status;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Higher Recycler cores (#189): each of the four tiers takes its own turns to decompose, 8, 6, 4 and 2 by default,
 * and every one returns the same; a higher core takes the place of a lower one in the same frame, which gives back
 * what the old one held.
 */
final class CoreTierTests {

    private CoreTierTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        for (int tier = 1; tier <= Recycler.TIERS; tier++) {
            int t = tier;
            tests.test("core_tier_" + tier + "_decomposes_in_its_turns", 1, helper -> decomposesInItsTurns(helper, t));
        }
        tests.test("core_tier_turns_fall_by_tier", 1, CoreTierTests::turnsFall);
        tests.test("core_tier_swapped_in_the_same_frame_works", 1, CoreTierTests::swapped);
    }

    private static void decomposesInItsTurns(GameTestHelper helper, int tier) {
        var build = RecyclerBuild.standard(helper);
        build.swapCore(tier);
        helper.assertValueEqual(build.core().tier(), tier, "the core's tier");
        build.feed(1);
        int turns = ServerConfig.turnsOf(tier);
        build.turn(turns - 1);
        helper.assertTrue(build.outputsEmpty(), "a tier-" + tier + " core decomposed one turn short");
        build.turn(1);
        build.expectAdvancedMix();
        helper.succeed();
    }

    private static void turnsFall(GameTestHelper helper) {
        int[] expected = {8, 6, 4, 2};
        for (int tier = 1; tier <= Recycler.TIERS; tier++) {
            helper.assertValueEqual(ServerConfig.turnsOf(tier), expected[tier - 1], "a tier-" + tier + " core's turns");
        }
        helper.succeed();
    }

    private static void swapped(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(2);
        build.turn(3);
        build.swapCore(2);
        helper.assertItemEntityPresent(Essences.item(Essence.ADVANCED), RecyclerBuild.CORE, 2.0);
        helper.assertTrue(build.core().held().isEmpty(), "the new core holds " + build.core().held());
        helper.assertValueEqual(build.core().turns(), 0, "the new core's turns");
        helper.assertValueEqual(helper.getBlockState(RecyclerBuild.CORE).getValue(RecyclerCoreBlock.FACING), build.front, "the new core's front");
        build.feed(1);
        helper.assertValueEqual(build.core().status(), Status.READY, "the Recycler with its new core");
        build.turn(ServerConfig.turnsOf(2));
        build.expectAdvancedMix();
        helper.succeed();
    }
}
