// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Kind;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Made;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Share;
import org.junit.jupiter.api.Test;

/** What an item recycles into, at full cost, from the recipes that make it run backwards. */
class RecyclingRulesTest {

    private final Map<String, List<Made<String>>> makers = new HashMap<>();

    private void recipe(String result, int count, Kind kind, String id, String... ingredients) {
        var shares = new ArrayList<Share<String>>();
        for (String ingredient : ingredients) {
            String[] parts = ingredient.split(" ");
            shares.add(parts.length == 2 ? new Share<>(parts[1], Integer.parseInt(parts[0])) : new Share<>(ingredient, 1));
        }
        makers.computeIfAbsent(result, key -> new ArrayList<>()).add(new Made<>(kind, id, shares, count));
    }

    private List<Share<String>> recycle(String item) {
        return RecyclingRules.recyclesInto(item, Optional.empty(), key -> makers.getOrDefault(key, List.of()), key -> key.endsWith("_ore"));
    }

    private static List<Share<String>> shares(Object... itemsAndAmounts) {
        var shares = new ArrayList<Share<String>>();
        for (int i = 0; i < itemsAndAmounts.length; i += 2) {
            shares.add(new Share<>((String) itemsAndAmounts[i], ((Number) itemsAndAmounts[i + 1]).doubleValue()));
        }
        return shares;
    }

    @Test
    void anItemNoRecipeMakesRecyclesIntoItself() {
        assertEquals(shares("dirt", 1), recycle("dirt"));
    }

    @Test
    void aCraftedItemRecyclesIntoItsCostInTheOrderEachIngredientFirstAppears() {
        // The piston's pattern, row by row: TTT, #X#, #R#.
        recipe("piston", 1, Kind.CRAFTING, "minecraft:piston", "oak_planks", "oak_planks", "oak_planks", "cobblestone", "iron_ingot",
                "cobblestone", "cobblestone", "redstone", "cobblestone");
        assertEquals(shares("oak_planks", 3, "cobblestone", 4, "iron_ingot", 1, "redstone", 1), recycle("piston"));
    }

    @Test
    void oneOfWhatARecipeMakesSeveralOfTakesItsShare() {
        recipe("stick", 4, Kind.CRAFTING, "minecraft:stick", "oak_planks", "oak_planks");
        assertEquals(shares("oak_planks", 0.5), recycle("stick"));
    }

    @Test
    void anOverrideReplacesTheDerivedCost() {
        recipe("piston", 1, Kind.CRAFTING, "minecraft:piston", "oak_planks", "cobblestone");
        var override = shares("iron_ingot", 2);
        assertEquals(override, RecyclingRules.recyclesInto("piston", Optional.of(override), key -> makers.getOrDefault(key, List.of()),
                key -> false));
    }

    @Test
    void smeltingComesBeforeAssemblingAndAssemblingBeforeCrafting() {
        recipe("iron_ingot", 1, Kind.CRAFTING, "a:crafted", "iron_nugget_pile");
        recipe("iron_ingot", 1, Kind.ASSEMBLING, "b:assembled", "iron_dust");
        assertEquals(shares("iron_dust", 1), recycle("iron_ingot"));
        recipe("iron_ingot", 1, Kind.SMELTING, "c:smelted", "raw_iron");
        assertEquals(shares("raw_iron", 1), recycle("iron_ingot"));
    }

    @Test
    void otherCookingCountsOnlyWhenNothingElseMakesTheItem() {
        recipe("charcoal", 1, Kind.OTHER_COOKING, "a:charcoal_from_blasting", "oak_log");
        assertEquals(shares("oak_log", 1), recycle("charcoal"));
        recipe("charcoal", 1, Kind.CRAFTING, "b:charcoal_from_crafting", "coal");
        assertEquals(shares("coal", 1), recycle("charcoal"));
    }

    @Test
    void withinAKindTheFirstRecipeByIdWins() {
        recipe("glass", 1, Kind.SMELTING, "minecraft:glass_from_sand", "sand");
        recipe("glass", 1, Kind.SMELTING, "minecraft:glass_from_red_sand", "red_sand");
        assertEquals(shares("red_sand", 1), recycle("glass"));
    }

    @Test
    void aRecipeFromAnOreLosesToOneThatIsNot() {
        recipe("iron_ingot", 1, Kind.SMELTING, "minecraft:iron_ingot_from_smelting_deepslate_iron_ore", "deepslate_iron_ore");
        recipe("iron_ingot", 1, Kind.SMELTING, "minecraft:iron_ingot_from_smelting_iron_ore", "iron_ore");
        assertEquals(shares("deepslate_iron_ore", 1), recycle("iron_ingot"));
        recipe("iron_ingot", 1, Kind.SMELTING, "minecraft:iron_ingot_from_smelting_raw_iron", "raw_iron");
        assertEquals(shares("raw_iron", 1), recycle("iron_ingot"));
    }

    @Test
    void anUncompressingRecipeIsNeverRunBackwards() {
        recipe("iron_block", 1, Kind.CRAFTING, "minecraft:iron_block", "9 iron_ingot");
        recipe("iron_ingot", 9, Kind.CRAFTING, "minecraft:iron_ingot_from_iron_block", "iron_block");
        recipe("iron_ingot", 1, Kind.CRAFTING, "minecraft:iron_ingot_from_nuggets", "9 iron_nugget");
        recipe("iron_nugget", 9, Kind.CRAFTING, "minecraft:iron_nugget", "iron_ingot");
        assertEquals(shares("iron_ingot", 9), recycle("iron_block"));
        assertEquals(shares("iron_nugget", 9), recycle("iron_ingot"));
        assertEquals(shares("iron_nugget", 1), recycle("iron_nugget"));
    }

    @Test
    void makingSeveralFromOneIsRunBackwardsWhenNothingMakesItBack() {
        recipe("oak_planks", 4, Kind.CRAFTING, "minecraft:oak_planks", "oak_log");
        assertEquals(shares("oak_log", 0.25), recycle("oak_planks"));
    }
}
