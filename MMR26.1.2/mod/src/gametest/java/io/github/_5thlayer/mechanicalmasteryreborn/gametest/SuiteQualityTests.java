// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * A real Assembler takes food whatever its quality (#372) and gives its product its ingredients' average (#365). An input
 * holds one item resource, so one holding a plain potato would refuse a gold one: it takes it, and the input holds both
 * at their average. The recipe is vanilla's rabbit stew, which Craftworks turns into an assembling recipe as it does the
 * pack's crafting recipes, Pantry's cheese pizza among them; Quality Food is {@link QualityFoodStandIn}'s.
 */
final class SuiteQualityTests {

    private static final Identifier STEW = Identifier.withDefaultNamespace("rabbit_stew_from_brown_mushroom");
    private static final Identifier IRON = Identifier.fromNamespaceAndPath("quality_food", "iron");
    private static final Identifier GOLD = Identifier.fromNamespaceAndPath("quality_food", "gold");
    private static final Item[] INGREDIENTS = {Items.BAKED_POTATO, Items.COOKED_RABBIT, Items.CARROT, Items.BROWN_MUSHROOM, Items.BOWL};

    private SuiteQualityTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("suite_quality_an_input_takes_food_of_another_quality", 1, SuiteQualityTests::pipedIn);
        tests.test("suite_quality_a_shift_click_puts_food_of_another_quality_in", 1, SuiteQualityTests::shiftClicked);
        tests.test("suite_quality_an_assembler_makes_its_product_at_its_ingredients_average", 1, SuiteQualityTests::product);
    }

    /** A pipe's gold potato into the input holding a plain one: both go in, at iron, their average. */
    private static void pipedIn(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, STEW);
        int slot = slotOf(helper, assembler, Items.BAKED_POTATO);
        SuiteAssembler.put(assembler, slot, Items.BAKED_POTATO, 1);
        var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(SuiteAssembler.ORIGIN), Direction.UP);
        helper.assertTrue(face != null, "the Assembler takes no items");
        int moved;
        try (Transaction tx = Transaction.openRoot()) {
            moved = face.insert(ItemResource.of(of(Items.BAKED_POTATO, GOLD, 2)), 1, tx);
            tx.commit();
        }
        helper.assertValueEqual(moved, 1, "the gold potatoes a pipe put into the input holding a plain one");
        expectInput(helper, assembler, slot, 2, IRON, 1, "the input after the pipe");
        helper.succeed();
    }

    /** A shift-click of two gold potatoes onto the input holding two plain ones: all four go in, at iron. */
    private static void shiftClicked(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, STEW);
        int slot = slotOf(helper, assembler, Items.BAKED_POTATO);
        SuiteAssembler.put(assembler, slot, Items.BAKED_POTATO, 2);
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "suite_quality"));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().setItem(0, of(Items.BAKED_POTATO, GOLD, 2).copyWithCount(2));
        AbstractContainerMenu menu = assembler.createMenu(1, player.getInventory(), player);
        int hotbar = -1;
        for (Slot menuSlot : menu.slots) {
            if (menuSlot.container == player.getInventory() && menuSlot.getContainerSlot() == 0) {
                hotbar = menuSlot.index;
            }
        }
        helper.assertTrue(hotbar >= 0, "the Assembler's menu shows no hotbar");
        menu.quickMoveStack(player, hotbar);
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "the gold potatoes left in hand: " + player.getInventory().getItem(0));
        expectInput(helper, assembler, slot, 4, IRON, 1, "the input after the shift-click");
        helper.succeed();
    }

    /** Gold potato, rabbit and carrot, and a plain mushroom and bowl, which aren't food: a gold stew. */
    private static void product(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, STEW);
        for (Item item : INGREDIENTS) {
            ItemStack stack = item == Items.BROWN_MUSHROOM || item == Items.BOWL ? new ItemStack(item) : of(item, GOLD, 2);
            assembler.inventory().set(slotOf(helper, assembler, item), ItemResource.of(stack), 1);
        }
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, Items.RABBIT_STEW, "the rabbit stew's recipe");
        helper.assertValueEqual(assembler.inventory().getResource(AssemblerSlots.OUTPUT).toStack(1).get(QualityFoodStandIn.QUALITY.get()),
                new QualityFoodStandIn.Quality(GOLD, 2), "the quality of a stew of gold ingredients");
        helper.succeed();
    }

    /** The input the held recipe puts {@code item} in. */
    private static int slotOf(GameTestHelper helper, AssemblerBlockEntity assembler, Item item) {
        for (int slot = 0; slot < AssemblerSlots.INPUTS; slot++) {
            if (assembler.accepts(slot, ItemResource.of(item))) {
                return slot;
            }
        }
        throw helper.assertionException("no input of the rabbit stew's recipe takes " + item);
    }

    private static void expectInput(GameTestHelper helper, AssemblerBlockEntity assembler, int slot, int count, Identifier type,
            int level, String what) {
        helper.assertValueEqual(assembler.inventory().getAmountAsInt(slot), count, "the potatoes in " + what);
        helper.assertValueEqual(assembler.inventory().getResource(slot).toStack(1).get(QualityFoodStandIn.QUALITY.get()),
                new QualityFoodStandIn.Quality(type, level), "the quality of " + what);
    }

    private static ItemStack of(Item item, Identifier type, int level) {
        ItemStack stack = new ItemStack(item);
        stack.set(QualityFoodStandIn.QUALITY.get(), new QualityFoodStandIn.Quality(type, level));
        return stack;
    }
}
