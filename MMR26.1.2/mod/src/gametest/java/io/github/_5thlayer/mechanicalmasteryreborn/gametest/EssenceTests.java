// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Every essence is an item in the game's registry under its id (#164). An expiring essence fizzles, for show only:
 * it is removed as before and nothing takes its place, and an item that isn't an essence is left to vanilla (#199).
 */
final class EssenceTests {

    /** Ticks a dropped item lives in these tests, instead of vanilla's 5 minutes. */
    private static final int EXPIRY = 20;
    private static final BlockPos DROP = new BlockPos(4, 2, 4);

    private EssenceTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("essence_every_id_is_a_registered_item", 1, EssenceTests::everyIdRegistered);
        tests.test("essence_mechanical_fizzles_away_when_it_expires", EXPIRY + 10, helper -> expiresLeavingNothing(helper, Essences.item(Essence.ADVANCED)));
        tests.test("essence_transmuted_fizzles_away_when_it_expires", EXPIRY + 10, helper -> expiresLeavingNothing(helper, Essences.item(Essence.GALVANIC)));
        tests.test("essence_quintessence_fizzles_away_when_it_expires", EXPIRY + 10, helper -> expiresLeavingNothing(helper, Essences.item(Essence.QUINTESSENCE)));
        tests.test("essence_expiry_of_other_items_is_left_to_vanilla", EXPIRY + 10, helper -> expiresLeavingNothing(helper, Items.COBBLESTONE));
    }

    private static void everyIdRegistered(GameTestHelper helper) {
        for (Essence essence : Essence.values()) {
            var id = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, essence.path());
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id), id + " is not a registered item");
        }
        helper.succeed();
    }

    private static void expiresLeavingNothing(GameTestHelper helper, Item item) {
        BlockPos at = helper.absolutePos(DROP);
        var dropped = new ItemEntity(helper.getLevel(), at.getX() + 0.5, at.getY(), at.getZ() + 0.5, new ItemStack(item), 0, 0, 0);
        dropped.lifespan = EXPIRY;
        helper.getLevel().addFreshEntity(dropped);
        helper.runAtTickTime(EXPIRY / 2, () -> helper.assertTrue(dropped.isAlive(), item + " expired before its lifespan"));
        helper.runAtTickTime(EXPIRY + 5, () -> {
            helper.assertTrue(dropped.isRemoved(), item + " is still there after its lifespan");
            var left = helper.getLevel().getEntitiesOfClass(Entity.class, helper.getBounds());
            helper.assertTrue(left.isEmpty(), "entities left after " + item + " expired: " + left);
            helper.succeed();
        });
    }
}
