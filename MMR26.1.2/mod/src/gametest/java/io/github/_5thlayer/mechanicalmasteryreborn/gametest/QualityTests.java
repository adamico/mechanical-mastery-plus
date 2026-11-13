// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.PenFeed;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Quality Food's quality survives life's chain (#365): a machine's output takes its inputs' average quality, rounded, the
 * food among them only, and a planter or pen rolls a product's quality with odds its fertilizer, soil or feed raise. The
 * machine is a test double, which makes its output as the chain's machines' mixins do, and Quality Food a stand-in of its
 * component, types and material whitelist ({@link QualityFoodStandIn}): no food mod is on the game tests' runs.
 */
final class QualityTests {

    private static final Identifier IRON = Identifier.fromNamespaceAndPath("quality_food", "iron");
    private static final Identifier GOLD = Identifier.fromNamespaceAndPath("quality_food", "gold");
    private static final Identifier DIAMOND = Identifier.fromNamespaceAndPath("quality_food", "diamond");

    private QualityTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("quality_an_output_takes_its_inputs_average", 1, QualityTests::average);
        tests.test("quality_an_average_halfway_rounds_up", 1, QualityTests::halfwayRoundsUp);
        tests.test("quality_plain_food_counts_as_none", 1, QualityTests::plainCountsAsNone);
        tests.test("quality_what_is_not_food_is_left_out", 1, QualityTests::notFoodLeftOut);
        tests.test("quality_plain_inputs_make_a_plain_output", 1, QualityTests::plainMakesPlain);
        tests.test("quality_an_output_that_is_not_food_takes_none", 1, QualityTests::outputNotFood);
        tests.test("quality_a_roll_at_no_odds_gives_none", 1, QualityTests::noOdds);
        tests.test("quality_a_roll_at_the_best_odds_gives_the_top_type", 1, QualityTests::bestOdds);
        tests.test("quality_a_product_of_quality_feed_rolls_at_least_its_type", 1, QualityTests::qualityFeed);
        tests.test("quality_a_pen_rolls_its_products_by_its_last_feed", 1, QualityTests::penFeed);
        tests.test("quality_a_drop_with_no_room_for_its_roll_takes_a_held_stacks", 1, QualityTests::noRoomForTheRoll);
        tests.test("quality_a_drop_with_room_for_its_roll_keeps_it", 1, QualityTests::roomForTheRoll);
    }

    private static void average(GameTestHelper helper) {
        ItemStack made = make(helper, Items.RABBIT_STEW, of(Items.BAKED_POTATO, DIAMOND, 3), of(Items.CARROT, IRON, 1));
        assertQuality(helper, made, GOLD, 2, "the stew of a diamond potato and an iron carrot");
        helper.succeed();
    }

    private static void halfwayRoundsUp(GameTestHelper helper) {
        ItemStack made = make(helper, Items.RABBIT_STEW, of(Items.BAKED_POTATO, GOLD, 2), of(Items.CARROT, IRON, 1));
        assertQuality(helper, made, GOLD, 2, "the stew of a gold potato and an iron carrot");
        helper.succeed();
    }

    private static void plainCountsAsNone(GameTestHelper helper) {
        ItemStack made = make(helper, Items.RABBIT_STEW, of(Items.BAKED_POTATO, GOLD, 2), new ItemStack(Items.CARROT),
                new ItemStack(Items.COOKED_RABBIT));
        assertQuality(helper, made, IRON, 1, "the stew of a gold potato, a plain carrot and plain rabbit");
        helper.succeed();
    }

    private static void notFoodLeftOut(GameTestHelper helper) {
        ItemStack made = make(helper, Items.RABBIT_STEW, of(Items.BAKED_POTATO, GOLD, 2), new ItemStack(Items.BOWL),
                new ItemStack(Items.WATER_BUCKET));
        assertQuality(helper, made, GOLD, 2, "the stew of a gold potato, a bowl and water");
        helper.succeed();
    }

    private static void plainMakesPlain(GameTestHelper helper) {
        ItemStack made = make(helper, Items.RABBIT_STEW, new ItemStack(Items.BAKED_POTATO), new ItemStack(Items.CARROT));
        helper.assertTrue(made.get(QualityFoodStandIn.QUALITY.get()) == null, "the quality of a plain stew");
        helper.succeed();
    }

    private static void outputNotFood(GameTestHelper helper) {
        ItemStack made = make(helper, Items.BOWL, of(Items.BAKED_POTATO, GOLD, 2));
        helper.assertTrue(made.get(QualityFoodStandIn.QUALITY.get()) == null, "the quality of a bowl");
        helper.succeed();
    }

    /** No odds and no feed: no type can come up, however many rolls. */
    private static void noOdds(GameTestHelper helper) {
        RandomSource random = RandomSource.create(365);
        for (int i = 0; i < 100; i++) {
            ItemStack product = new ItemStack(Items.WHEAT);
            FoodQuality.roll(product, 0, 0, random, helper.getLevel().registryAccess());
            helper.assertTrue(product.get(QualityFoodStandIn.QUALITY.get()) == null, "the quality of wheat rolled at no odds");
        }
        helper.succeed();
    }

    /** Odds so high every type comes up: the top one is kept. */
    private static void bestOdds(GameTestHelper helper) {
        RandomSource random = RandomSource.create(365);
        for (int i = 0; i < 100; i++) {
            ItemStack product = new ItemStack(Items.CARROT);
            FoodQuality.roll(product, 1000, 0, random, helper.getLevel().registryAccess());
            assertQuality(helper, product, DIAMOND, 3, "a carrot rolled at the best odds");
        }
        helper.succeed();
    }

    /** A gold feed's weight makes gold certain, as Quality Food's own crafting does with its ingredients, at plain odds. */
    private static void qualityFeed(GameTestHelper helper) {
        RandomSource random = RandomSource.create(365);
        double gold = FoodQuality.weight(2, helper.getLevel().registryAccess());
        for (int i = 0; i < 100; i++) {
            ItemStack product = new ItemStack(Items.EGG);
            FoodQuality.roll(product, 1, gold, random, helper.getLevel().registryAccess());
            helper.assertTrue(FoodQuality.level(product) >= 2, "an egg of gold-fed chickens is at least gold");
        }
        helper.succeed();
    }

    /** A pen's cage remembers its last feed's quality, and its products roll by it; a plain feed forgets it. */
    private static void penFeed(GameTestHelper helper) {
        RandomSource random = RandomSource.create(365);
        ItemStack cage = new ItemStack(Items.CHEST);
        PenFeed.fed(cage, of(Items.WHEAT, GOLD, 2));
        for (int i = 0; i < 100; i++) {
            ItemStack egg = new ItemStack(Items.EGG);
            PenFeed.roll(cage, egg, random, helper.getLevel().registryAccess());
            helper.assertTrue(FoodQuality.level(egg) >= 2, "an egg of a pen last fed gold wheat is at least gold");
        }
        PenFeed.fed(cage, new ItemStack(Items.WHEAT));
        helper.assertTrue(cage.get(PenFeed.FEED_QUALITY.get()) == null, "a pen last fed plain wheat remembers no quality");
        helper.succeed();
    }

    /** A planter's outputs full but for a gold wheat stack: an iron wheat drop would be thrown away, so it goes in as gold. */
    private static void noRoomForTheRoll(GameTestHelper helper) {
        List<ItemStack> outputs = fullBut(of(Items.WHEAT, GOLD, 2).copyWithCount(10));
        List<ItemStack> drops = new ArrayList<>(List.of(of(Items.WHEAT, IRON, 1).copyWithCount(3)));
        FoodQuality.fitInto(drops, outputs);
        assertQuality(helper, drops.getFirst(), GOLD, 2, "an iron wheat drop with room only beside gold wheat");
        helper.assertValueEqual(drops.getFirst().getCount(), 3, "the wheat dropped");
        helper.succeed();
    }

    private static void roomForTheRoll(GameTestHelper helper) {
        List<ItemStack> outputs = new ArrayList<>(fullBut(of(Items.WHEAT, GOLD, 2).copyWithCount(10)));
        outputs.set(1, ItemStack.EMPTY);
        List<ItemStack> drops = new ArrayList<>(List.of(of(Items.WHEAT, IRON, 1).copyWithCount(3)));
        FoodQuality.fitInto(drops, outputs);
        assertQuality(helper, drops.getFirst(), IRON, 1, "an iron wheat drop with an empty slot");
        helper.succeed();
    }

    /** A planter's twelve output slots, {@code held} in the first and full stacks of dirt in the rest. */
    private static List<ItemStack> fullBut(ItemStack held) {
        List<ItemStack> outputs = new ArrayList<>();
        outputs.add(held);
        for (int slot = 1; slot < 12; slot++) {
            outputs.add(new ItemStack(Items.DIRT, 64));
        }
        return outputs;
    }

    /** The test double: makes {@code output} of {@code inputs}, carrying their quality as the chain's machines do. */
    private static ItemStack make(GameTestHelper helper, Item output, ItemStack... inputs) {
        ItemStack made = new ItemStack(output);
        FoodQuality.carry(made, List.of(inputs), helper.getLevel().registryAccess());
        return made;
    }

    private static ItemStack of(Item item, Identifier type, int level) {
        ItemStack stack = new ItemStack(item);
        stack.set(QualityFoodStandIn.QUALITY.get(), new QualityFoodStandIn.Quality(type, level));
        return stack;
    }

    private static void assertQuality(GameTestHelper helper, ItemStack stack, Identifier type, int level, String what) {
        helper.assertValueEqual(stack.get(QualityFoodStandIn.QUALITY.get()), new QualityFoodStandIn.Quality(type, level), "the quality of " + what);
    }
}
