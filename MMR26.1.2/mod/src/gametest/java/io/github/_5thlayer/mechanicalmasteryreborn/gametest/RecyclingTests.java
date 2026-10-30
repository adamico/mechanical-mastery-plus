// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.DecomposeLog;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Status;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Work;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Recycling on the Recycler (#184): any item that isn't an essence recycles one band down, by the recipe that
 * makes it run backwards or by the game-test pack's override, and an item no recipe makes into itself. The game
 * tests play at a full yield ({@link TestYield}) unless a test says otherwise, so the amounts are exact.
 */
final class RecyclingTests {

    private RecyclingTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("recycling_a_piston_returns_its_recipe_in_the_order_it_names_them", 1, RecyclingTests::piston);
        tests.test("recycling_a_piston_returns_its_yield_of_its_recipe", 1, RecyclingTests::pistonAtAQuarter);
        tests.test("recycling_an_override_replaces_what_the_recipe_returns", 1, RecyclingTests::override);
        tests.test("recycling_an_item_no_recipe_makes_shrinks_to_nothing", 1, RecyclingTests::shrinks);
        tests.test("recycling_an_iron_ingot_returns_raw_iron", 1, RecyclingTests::ingot);
        tests.test("recycling_an_iron_nugget_returns_itself_not_the_tools_smelted_into_it", 1, RecyclingTests::nugget);
        tests.test("recycling_one_of_what_a_recipe_makes_several_of_returns_its_share", 1, RecyclingTests::paper);
        tests.test("recycling_is_never_recorded", 1, RecyclingTests::neverRecorded);
        tests.test("recycling_input_refuses_a_filled_shulker_box", 1, RecyclingTests::shulkerBoxes);
        tests.test("recycling_input_refuses_an_essence_nothing_decomposes", 1, RecyclingTests::essenceWithNoRecipe);
        tests.test("recycling_ignores_damage_and_enchantments", 1, RecyclingTests::damagedTool);
    }

    private static void piston(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.PISTON));
        helper.assertValueEqual(build.core().work(), Work.RECYCLING, "what a Recycler holding a piston does");
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectOutputs(new ItemStack(Items.OAK_PLANKS, 3), new ItemStack(Items.COBBLESTONE, 4), new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.REDSTONE));
        helper.assertTrue(build.core().held().isEmpty(), "the core still holds " + build.core().held());
        helper.succeed();
    }

    /** At a quarter, a piston's four cobblestone come back as one; its planks, ingot and redstone each at most one. */
    private static void pistonAtAQuarter(GameTestHelper helper) {
        TestYield.at(0.25, () -> {
            var build = RecyclerBuild.standard(helper);
            build.feed(new ItemStack(Items.PISTON));
            build.turn(RecyclerCoreBlockEntity.TURNS);
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
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectOutputs(new ItemStack(Items.QUARTZ, 3));
        helper.succeed();
    }

    /** Dirt, which no recipe makes, recycles into a quarter of itself: fed back pass after pass, it is soon gone. */
    private static void shrinks(GameTestHelper helper) {
        TestYield.at(0.25, () -> {
            var build = RecyclerBuild.standard(helper);
            build.feed(new ItemStack(Items.DIRT));
            for (int pass = 0; pass < 64 && !build.core().held().isEmpty(); pass++) {
                build.turn(RecyclerCoreBlockEntity.TURNS);
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

    private static void ingot(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.IRON_INGOT));
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectOutputs(new ItemStack(Items.RAW_IRON));
        helper.succeed();
    }

    /**
     * Nuggets are made from an ingot, which uncompresses, and by smelting tools and armour down, which is recycling
     * already: neither runs backwards, so a nugget recycles into itself, and never up into a pickaxe.
     */
    private static void nugget(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.IRON_NUGGET));
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectOutputs(new ItemStack(Items.IRON_NUGGET));
        helper.succeed();
    }

    /** Three sugar cane make three paper, so one paper recycles into one cane, not three. */
    private static void paper(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.PAPER));
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectOutputs(new ItemStack(Items.SUGAR_CANE));
        helper.succeed();
    }

    private static void neverRecorded(GameTestHelper helper) {
        var team = TestTeams.claim(helper);
        var build = RecyclerBuild.standard(helper);
        build.feed(new ItemStack(Items.PISTON));
        build.turn(RecyclerCoreBlockEntity.TURNS);
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
        build.turn(RecyclerCoreBlockEntity.TURNS);
        build.expectOutputs(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.STICK, 2));
        helper.succeed();
    }
}
