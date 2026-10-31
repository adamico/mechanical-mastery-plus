// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.github._5thlayer.mechanicalmasteryreborn.recipe.Decomposing;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.DecomposingRecipe;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * What a decomposing recipe the pack writes may hold (#216, #209): any number of results, each item once, and at
 * least one. A Recycler short on outputs returns a random choice of them, so no count of outputs caps a mix.
 */
final class RecipeTests {

    private RecipeTests() {
    }

    /** Twenty kinds, as many as Advanced's mix holds. */
    private static final List<String> TWENTY = List.of("piston", "hopper", "comparator", "anvil", "blast_furnace",
            "chest", "barrel", "furnace", "crafting_table", "lever", "repeater", "observer", "dropper", "dispenser",
            "rail", "minecart", "bucket", "shears", "compass", "clock");

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("recipe_a_decomposing_recipe_holds_twenty_kinds", 1, RecipeTests::twenty);
        tests.test("recipe_a_decomposing_recipe_with_no_results_is_refused", 1, RecipeTests::none);
        tests.test("recipe_a_decomposing_recipe_naming_an_item_twice_is_refused", 1, RecipeTests::twice);
    }

    private static void twenty(GameTestHelper helper) {
        DataResult<DecomposingRecipe> read = decode(helper, TWENTY);
        if (read.isError()) {
            helper.fail("a decomposing recipe of twenty results was refused: " + read.error().orElseThrow().message());
            return;
        }
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        var codec = Decomposing.SERIALIZER.get().streamCodec();
        codec.encode(buffer, read.getOrThrow());
        helper.assertValueEqual(codec.decode(buffer).results().size(), 20, "the results a client receives");
        helper.succeed();
    }

    private static void none(GameTestHelper helper) {
        helper.assertTrue(decode(helper, List.of()).isError(), "a decomposing recipe with no results loads");
        helper.succeed();
    }

    private static void twice(GameTestHelper helper) {
        helper.assertTrue(decode(helper, List.of("piston", "hopper", "piston")).isError(),
                "a decomposing recipe naming the piston twice loads");
        helper.succeed();
    }

    /** The recipe of one Advanced essence into {@code items}, one of each, read as a datapack reads it. */
    private static DataResult<DecomposingRecipe> decode(GameTestHelper helper, List<String> items) {
        var json = new JsonObject();
        var ingredient = new JsonObject();
        ingredient.addProperty("ingredient", "mechanicalmasteryreborn:advanced_mechanical_essence");
        ingredient.addProperty("count", 1);
        json.add("ingredient", ingredient);
        var results = new JsonArray();
        for (String item : items) {
            var result = new JsonObject();
            result.addProperty("id", "minecraft:" + item);
            results.add(result);
        }
        json.add("results", results);
        var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        return Decomposing.SERIALIZER.get().codec().codec().parse(ops, json);
    }
}
