// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The game test server switches on the game-test pack, {@code src/gametest/resources/gametest_pack},
 * which never ships (libworks ADR 0004): its one tag holds a stick. A Module's own game-test pack replaces
 * this one, and this test with it.
 */
final class GameTestPackTests {

    private static final TagKey<Item> GAMETEST_PACK = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "gametest_pack"));

    private GameTestPackTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("switches_on_the_game_test_pack", 1, GameTestPackTests::switchesOnTheGameTestPack);
    }

    private static void switchesOnTheGameTestPack(GameTestHelper helper) {
        if (!new ItemStack(Items.STICK).is(GAMETEST_PACK)) {
            helper.fail("the game-test pack is off: #" + GAMETEST_PACK.location() + " holds no stick");
            return;
        }
        helper.succeed();
    }
}
