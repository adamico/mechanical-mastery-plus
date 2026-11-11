// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.io.IOException;
import java.io.Reader;
import java.util.UUID;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import io.github._5thlayer.craftworks.assembler.PersonalAssembler;
import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * The Galvanic Essence is made on an Assembler only, so making it always spends FE (#338). The pack's recipe stands on
 * Theurgy's items, which the suite doesn't load, so the test's own recipe, {@code gametest_galvanic_on_an_assembler},
 * takes redstone with the pack recipe's category and {@code hand_craftable}, and the test first checks the two agree.
 */
final class SuiteGalvanicTests {

    private static final String RECIPE = "gametest_galvanic_on_an_assembler";

    private SuiteGalvanicTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("suite_galvanic_essence_is_refused_by_the_personal_assembler", 1, SuiteGalvanicTests::refusedByHand);
        tests.test("suite_galvanic_essence_is_made_on_a_tier_1_assembler", 1, SuiteGalvanicTests::madeOnAnAssembler);
    }

    private static void refusedByHand(GameTestHelper helper) {
        sameCategoryAsThePack(helper);
        ServerPlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "suite_galvanic"));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(Items.REDSTONE));
        helper.assertFalse(PersonalAssembler.craft(player, id(RECIPE), 1), "the Personal Assembler queued a Galvanic Essence");
        helper.assertValueEqual(player.getInventory().countItem(Items.REDSTONE), 1, "the redstone left in hand");
        helper.succeed();
    }

    private static void madeOnAnAssembler(GameTestHelper helper) {
        sameCategoryAsThePack(helper);
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, RECIPE);
        SuiteAssembler.put(assembler, 0, Items.REDSTONE, 1);
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, Essences.item(Essence.GALVANIC), "the Galvanic Essence's recipe");
        helper.succeed();
    }

    /** The test's recipe has the category and {@code hand_craftable} of the pack's {@code galvanic_essence}. */
    private static void sameCategoryAsThePack(GameTestHelper helper) {
        for (String key : new String[] {"category", "hand_craftable"}) {
            helper.assertValueEqual(field(helper, RECIPE, key), field(helper, "galvanic_essence", key),
                    "the " + key + " of the test's Galvanic recipe beside the pack's");
        }
    }

    /** {@code key} of the assembling recipe {@code recipe} as its file writes it, or null if it writes none. */
    private static String field(GameTestHelper helper, String recipe, String key) {
        Identifier file = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "recipe/assembling/" + recipe + ".json");
        try (Reader reader = helper.getLevel().getServer().getResourceManager().openAsReader(file)) {
            var value = JsonParser.parseReader(reader).getAsJsonObject().get(key);
            return value == null ? null : value.getAsString();
        } catch (IOException e) {
            throw new IllegalStateException("no " + file, e);
        }
    }

    private static Identifier id(String recipe) {
        return Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assembling/" + recipe);
    }
}
