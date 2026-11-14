// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;

/**
 * A player's right-click on a Recycler input (#218), as the server plays it, falling back to the held item's own use:
 * it inserts what the Recycler takes, or refuses it and says why on the action bar, and never places a block. Only a
 * sneaking right-click places one against it.
 */
final class RecyclerInputTests {

    private static final String WHY = "mechanicalmasteryreborn.recycler.input.";

    private RecyclerInputTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("recycler_input_clicked_with_a_refined_block_places_nothing_and_says_why", 1,
                helper -> refused(helper, new ItemStack(Items.STONE), "refined"));
        tests.test("recycler_input_clicked_with_a_refined_item_says_why", 1, helper -> refused(helper, new ItemStack(Items.IRON_INGOT), "refined"));
        tests.test("recycler_input_clicked_with_rotten_flesh_says_it_is_never_recycled", 1,
                helper -> refused(helper, new ItemStack(Items.ROTTEN_FLESH), "denied"));
        tests.test("recycler_input_clicked_with_a_storage_block_says_to_uncraft_it", 1,
                helper -> refused(helper, new ItemStack(Items.IRON_BLOCK), "uncraft_instead"));
        tests.test("recycler_input_clicked_with_a_recyclable_block_inserts_it", 1, RecyclerInputTests::inserts);
        tests.test("recycler_input_clicked_while_the_core_holds_another_item_says_so", 1,
                helper -> refused(helper, new ItemStack(Items.PISTON), new ItemStack(Items.HOPPER), "holds_another"));
        tests.test("recycler_input_clicked_while_the_core_is_full_says_so", 1,
                helper -> refused(helper, new ItemStack(Items.PISTON, 64), new ItemStack(Items.PISTON), "full"));
        tests.test("recycler_input_clicked_with_what_it_never_takes_says_so_whatever_the_core_holds", 1,
                helper -> refused(helper, new ItemStack(Items.PISTON), filledShulkerBox(), "not_taken"));
        tests.test("recycler_input_of_no_recycler_clicked_places_nothing_and_says_why", 1, RecyclerInputTests::noRecycler);
        tests.test("recycler_input_sneak_clicked_places_a_block", 1, RecyclerInputTests::sneakPlaces);
    }

    private static void refused(GameTestHelper helper, ItemStack held, String why) {
        refused(helper, ItemStack.EMPTY, held, why);
    }

    /** A player clicks the input of a Recycler whose core holds {@code holding}: it keeps {@code held}, nothing is placed, and it is told why. */
    private static void refused(GameTestHelper helper, ItemStack holding, ItemStack held, String why) {
        var build = RecyclerBuild.standard(helper);
        if (!holding.isEmpty()) {
            build.feed(holding.copy());
        }
        expectRefused(helper, build.firstOf(Recycler.INPUT.get()), held, why);
        helper.assertTrue(ItemStack.matches(build.core().held(), holding), "the core holds " + build.core().held() + ", not " + holding);
        helper.succeed();
    }

    private static void noRecycler(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        helper.setBlock(RecyclerBuild.CORE, Blocks.AIR);
        expectRefused(helper, build.firstOf(Recycler.INPUT.get()), new ItemStack(Items.STONE), "no_recycler");
        helper.succeed();
    }

    private static void expectRefused(GameTestHelper helper, BlockPos input, ItemStack held, String why) {
        var player = Clicker.of(helper);
        ItemStack before = held.copy();
        player.click(helper, input, held);
        helper.assertTrue(ItemStack.matches(player.getMainHandItem(), before), "the player holds " + player.getMainHandItem() + ", not " + before);
        helper.assertBlockPresent(Blocks.AIR, input.above());
        helper.assertValueEqual(player.told(), List.of(WHY + why), "what the action bar said");
        player.leave(helper);
    }

    private static ItemStack filledShulkerBox() {
        var filled = new ItemStack(Items.SHULKER_BOX);
        filled.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND))));
        return filled;
    }

    /** A Recycler Casing, a crafted block, goes in rather than against the input. */
    private static void inserts(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        BlockPos input = build.firstOf(Recycler.INPUT.get());
        var player = Clicker.of(helper);
        player.click(helper, input, new ItemStack(Recycler.CASING.get(), 2));
        helper.assertValueEqual(build.core().held().getCount(), 2, "the casings the core took");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the player still holds " + player.getMainHandItem());
        helper.assertBlockPresent(Blocks.AIR, input.above());
        helper.assertValueEqual(player.told(), List.of(), "what the action bar said");
        player.leave(helper);
        helper.succeed();
    }

    private static void sneakPlaces(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        BlockPos input = build.firstOf(Recycler.INPUT.get());
        var player = Clicker.of(helper);
        player.setShiftKeyDown(true);
        player.click(helper, input, new ItemStack(Recycler.CASING.get()));
        helper.assertBlockPresent(Recycler.CASING.get(), input.above());
        helper.assertTrue(build.core().held().isEmpty(), "the core took " + build.core().held());
        helper.assertValueEqual(player.told(), List.of(), "what the action bar said");
        player.leave(helper);
        helper.succeed();
    }
}
