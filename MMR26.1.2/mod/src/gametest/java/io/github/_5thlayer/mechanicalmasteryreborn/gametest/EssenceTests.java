// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;

/** Every essence is an item in the game's registry under its id (#164). */
final class EssenceTests {

    private EssenceTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("essence_every_id_is_a_registered_item", 1, EssenceTests::everyIdRegistered);
    }

    private static void everyIdRegistered(GameTestHelper helper) {
        for (Essence essence : Essence.values()) {
            var id = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, essence.path());
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id), id + " is not a registered item");
        }
        helper.succeed();
    }
}
