// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Recycling;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules;
import io.github._5thlayer.mechanicalmasteryreborn.record.DecomposeLog;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Status;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Work;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerLayout;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;

/**
 * Recycling on the Recycler (#184, #218): any item that isn't an essence recycles one band down, by the crafting
 * recipe that makes it run backwards or by the game-test pack's override, and an item no recipe makes into itself;
 * a refined item, or one a player uncrafts for free, is refused. The game tests play at a full yield
 * ({@link TestYield}) unless a test says otherwise, so the amounts are exact.
 */
final class RecyclingTests {

    private RecyclingTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("recycling_a_piston_returns_its_recipe_in_the_order_it_names_them", 1, RecyclingTests::piston);
        tests.test("recycling_a_piston_returns_its_yield_of_its_recipe", 1, RecyclingTests::pistonAtAQuarter);
        tests.test("recycling_a_piston_on_one_output_returns_a_random_choice_of_its_recipe", 1, RecyclingTests::pistonShortOnOutputs);
        tests.test("recycling_an_override_replaces_what_the_recipe_returns", 1, RecyclingTests::override);
        tests.test("recycling_an_item_no_recipe_makes_shrinks_to_nothing", 1, RecyclingTests::shrinks);
        tests.test("recycling_a_rift_shard_shrinks_to_nothing", 1, RecyclingTests::riftShardShrinks);
        tests.test("recycling_a_hopper_returns_its_iron_and_its_chest", 1, RecyclingTests::hopper);
        tests.test("recycling_input_refuses_what_is_refined_or_uncrafted_for_free", 1, RecyclingTests::refused);
        tests.test("recycling_input_takes_what_only_uncrafting_makes", 1, RecyclingTests::uncrafted);
        tests.test("recycling_emi_leaves_out_what_is_refused", 1, RecyclingTests::table);
        tests.test("recycling_one_of_what_a_recipe_makes_several_of_returns_its_share", 1, RecyclingTests::stoneBricks);
        tests.test("recycling_a_furnace_made_of_one_raw_kind_returns_it", 1, RecyclingTests::furnace);
        tests.test("recycling_is_never_recorded", 1, RecyclingTests::neverRecorded);
        tests.test("recycling_input_refuses_a_filled_shulker_box", 1, RecyclingTests::shulkerBoxes);
        tests.test("recycling_input_refuses_an_essence_nothing_decomposes", 1, RecyclingTests::essenceWithNoRecipe);
        tests.test("recycling_ignores_damage_and_enchantments", 1, RecyclingTests::damagedTool);
    }

    private static void piston(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.PISTON));
        helper.assertValueEqual(build.core().work(), Work.RECYCLING, "what a Recycler holding a piston does");
        build.turn(ServerConfig.turnsOf(1));
        build.expectOutputs(new ItemStack(Items.OAK_PLANKS, 3), new ItemStack(Items.COBBLESTONE, 4), new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.REDSTONE));
        helper.assertTrue(build.core().held().isEmpty(), "the core still holds " + build.core().held());
        helper.succeed();
    }

    /** One output, three positions, for a piston's four kinds: three of them come back, and the fourth is lost. */
    private static void pistonShortOnOutputs(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.place(1, Blocks.AIR);
        build.place(1, Recycler.CASING.get());
        build.feed(new ItemStack(Items.PISTON));
        build.turn(ServerConfig.turnsOf(1));
        helper.assertTrue(build.core().held().isEmpty(), "the core still holds " + build.core().held());
        build.expectSomeOfTheMix(List.of(new ItemStack(Items.OAK_PLANKS, 3), new ItemStack(Items.COBBLESTONE, 4), new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.REDSTONE)), RecyclerLayout.POSITIONS_PER_OUTPUT);
        helper.succeed();
    }

    /** At a quarter, a piston's four cobblestone come back as one; its planks, ingot and redstone each at most one. */
    private static void pistonAtAQuarter(GameTestHelper helper) {
        TestYield.at(0.25, () -> {
            var build = RecyclerBuild.standard(helper);
            build.feed(new ItemStack(Items.PISTON));
            build.turn(ServerConfig.turnsOf(1));
            build.expectPosition(1, new ItemStack(Items.COBBLESTONE));
            List<Item> chances = List.of(Items.OAK_PLANKS, Items.COBBLESTONE, Items.IRON_INGOT, Items.REDSTONE);
            for (int i : new int[] {0, 2, 3}) {
                ItemStack held = build.outputEntity(i / 3).item(i % 3);
                helper.assertTrue(held.isEmpty() || held.is(chances.get(i)) && held.getCount() == 1,
                        "output position " + i + " holds " + held + ", not one " + chances.get(i) + " or nothing");
            }
        });
        helper.succeed();
    }

    private static void override(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.COMPARATOR));
        build.turn(ServerConfig.turnsOf(1));
        build.expectOutputs(new ItemStack(Items.QUARTZ, 3));
        helper.succeed();
    }

    /** Dirt, which no recipe makes, recycles into a quarter of itself: fed back pass after pass, it is soon gone. */
    /** A Rift Shard, which nothing crafts, recycles into less of itself, as dirt does, so no Recycler makes one (#235). */
    private static void riftShardShrinks(GameTestHelper helper) {
        Item shard = RiftShards.ITEM.get();
        helper.assertValueEqual(Recycling.recycle(helper.getLevel().getServer(), shard),
                new RecyclingRules.Into<>(List.of(new RecyclingRules.Share<>(shard, 1))), "what a Rift Shard recycles into");
        helper.succeed();
    }

    private static void shrinks(GameTestHelper helper) {
        TestYield.at(0.25, () -> {
            var build = RecyclerBuild.standard(helper);
            build.feed(new ItemStack(Items.DIRT));
            for (int pass = 0; pass < 64 && !build.core().held().isEmpty(); pass++) {
                build.turn(ServerConfig.turnsOf(1));
                ItemStack back = build.outputEntity(0).item(0).isEmpty() ? ItemStack.EMPTY : build.take(0);
                helper.assertTrue(back.isEmpty() || back.is(Items.DIRT) && back.getCount() == 1, "a pass of dirt returned " + back);
                if (!back.isEmpty()) {
                    build.feed(back);
                }
            }
            helper.assertTrue(build.core().held().isEmpty() && build.outputsEmpty(), "the dirt outlived 64 passes at a quarter");
        });
        helper.succeed();
    }

    private static void hopper(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.HOPPER));
        build.turn(ServerConfig.turnsOf(1));
        build.expectOutputs(new ItemStack(Items.IRON_INGOT, 5), new ItemStack(Items.CHEST));
        helper.succeed();
    }

    /**
     * What is refined (cooked, the planks, or cut from one raw kind alone) and what a player uncrafts for free (a block
     * of ingots, of raw iron, of wheat).
     */
    private static final List<Item> REFUSED = List.of(Items.IRON_INGOT, Items.OAK_PLANKS, Items.STONE, Items.GLASS, Items.COOKED_BEEF,
            Items.SANDSTONE, Items.PAPER, Items.BONE_MEAL, Items.RED_DYE, Items.IRON_BLOCK, Items.RAW_IRON_BLOCK, Items.HAY_BLOCK);

    private static void refused(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        for (Item item : REFUSED) {
            helper.assertValueEqual(build.feed(new ItemStack(item)), 0, "the " + item + " an input took");
        }
        helper.assertValueEqual(build.core().status(), Status.EMPTY, "the status after the input refused them");
        helper.succeed();
    }

    /** A nugget, raw iron and wheat are raw: only uncrafting a block makes them, so each recycles into less of itself. */
    private static void uncrafted(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        for (Item item : List.of(Items.IRON_NUGGET, Items.RAW_IRON, Items.WHEAT)) {
            helper.assertValueEqual(build.feed(new ItemStack(item)), 1, "the " + item + " an input took");
            build.turn(ServerConfig.turnsOf(1));
            build.expectOutputs(new ItemStack(item));
            build.take(0);
        }
        helper.succeed();
    }

    private static void table(GameTestHelper helper) {
        var table = Recycling.byRecipe(helper.getLevel().getServer());
        for (Item item : REFUSED) {
            helper.assertFalse(table.containsKey(item), "EMI is sent what " + item + " recycles into: " + table.get(item));
        }
        helper.assertTrue(table.containsKey(Items.HOPPER), "EMI is sent nothing for a hopper");
        helper.succeed();
    }

    /** Four stone make four stone bricks, so one stone brick recycles into one stone, not four. */
    private static void stoneBricks(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.STONE_BRICKS));
        build.turn(ServerConfig.turnsOf(1));
        build.expectOutputs(new ItemStack(Items.STONE));
        helper.succeed();
    }

    /** A furnace is cut from cobblestone alone, but it is a thing, not a material, so it recycles. */
    private static void furnace(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.FURNACE));
        build.turn(ServerConfig.turnsOf(1));
        build.expectOutputs(new ItemStack(Items.COBBLESTONE, 8));
        helper.succeed();
    }

    private static void neverRecorded(GameTestHelper helper) {
        var team = TestTeams.claim(helper);
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.PISTON));
        build.turn(ServerConfig.turnsOf(1));
        var log = DecomposeLog.get(helper.getLevel().getServer());
        for (Essence essence : Essence.values()) {
            for (DecomposeLog.Way way : DecomposeLog.Way.values()) {
                helper.assertValueEqual(log.count(team, essence, way), 0L, "the " + essence + " " + way + " decomposes a recycle recorded");
            }
        }
        helper.succeed();
    }

    private static void shulkerBoxes(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        var filled = new ItemStack(Items.SHULKER_BOX);
        filled.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND))));
        helper.assertValueEqual(build.feed(filled), 0, "the filled shulker boxes an input took");
        helper.assertValueEqual(build.feed(new ItemStack(Items.SHULKER_BOX)), 1, "the empty shulker boxes an input took");
        helper.succeed();
    }

    private static void essenceWithNoRecipe(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        helper.assertValueEqual(build.feed(new ItemStack(Essences.item(Essence.BASIC))), 0,
                "the Basic essence, which the game-test pack decomposes into nothing, an input took");
        helper.assertValueEqual(build.core().status(), Status.EMPTY, "the status after the input refused it");
        helper.succeed();
    }

    /** A worn shovel comes back as a new one's iron and sticks would. */
    private static void damagedTool(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        var worn = new ItemStack(Items.IRON_SHOVEL);
        worn.setDamageValue(200);
        build.feed(worn);
        build.turn(ServerConfig.turnsOf(1));
        build.expectOutputs(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.STICK, 2));
        helper.succeed();
    }
}
