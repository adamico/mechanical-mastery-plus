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
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Into;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Made;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Outcome;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Refusal;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Refused;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Share;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Traits;
import org.junit.jupiter.api.BeforeEach;
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

    /** Ingots are smelted, so refined, as in the game. */
    @BeforeEach
    void smeltIngots() {
        recipe("iron_ingot", 1, Kind.COOKING, "minecraft:iron_ingot", "raw_iron");
        recipe("copper_ingot", 1, Kind.COOKING, "minecraft:copper_ingot", "raw_copper");
    }

    private Outcome<String> recycle(String item) {
        return recycle(item, Optional.empty());
    }

    /**
     * Ores end in {@code _ore}; the refined tag holds the planks; a furnace holds things, and a helmet is worn; the
     * pack denies what is rotten.
     */
    private Outcome<String> recycle(String item, Optional<List<Share<String>>> override) {
        return RecyclingRules.recycle(item, override, key -> makers.getOrDefault(key, List.of()),
                new Traits<>(key -> key.endsWith("_ore"), key -> key.endsWith("_planks"), key -> key.equals("furnace") || key.endsWith("_helmet"),
                        key -> key.startsWith("rotten_")));
    }

    private static Outcome<String> into(Object... itemsAndAmounts) {
        return new Into<>(shares(itemsAndAmounts));
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
        assertEquals(into("dirt", 1), recycle("dirt"));
    }

    @Test
    void aDeniedItemNoRecipeMakesIsRefusedRatherThanThrownAway() {
        assertEquals(new Refused<>(Refusal.DENIED), recycle("rotten_flesh"));
    }

    @Test
    void aDeniedItemIsRefusedWhateverMakesItOrAnOverrideSays() {
        recipe("rotten_flesh", 1, Kind.CRAFTING, "pack:rot", "beef");
        assertEquals(new Refused<>(Refusal.DENIED), recycle("rotten_flesh", Optional.of(shares("beef", 1))));
    }

    @Test
    void aCraftedItemRecyclesIntoItsCostInTheOrderEachIngredientFirstAppears() {
        // The piston's pattern, row by row: TTT, #X#, #R#.
        recipe("piston", 1, Kind.CRAFTING, "minecraft:piston", "oak_planks", "oak_planks", "oak_planks", "cobblestone", "iron_ingot",
                "cobblestone", "cobblestone", "redstone", "cobblestone");
        assertEquals(into("oak_planks", 3, "cobblestone", 4, "iron_ingot", 1, "redstone", 1), recycle("piston"));
    }

    @Test
    void oneOfWhatARecipeMakesSeveralOfTakesItsShare() {
        recipe("stick", 4, Kind.CRAFTING, "minecraft:stick", "oak_planks", "oak_planks");
        assertEquals(into("oak_planks", 0.5), recycle("stick"));
    }

    @Test
    void anOverrideReplacesTheDerivedCost() {
        recipe("piston", 1, Kind.CRAFTING, "minecraft:piston", "oak_planks", "cobblestone");
        assertEquals(into("iron_ingot", 2), recycle("piston", Optional.of(shares("iron_ingot", 2))));
    }

    @Test
    void assemblingComesBeforeCrafting() {
        recipe("gear", 1, Kind.CRAFTING, "a:crafted", "iron_ingot", "iron_ingot", "iron_ingot", "iron_ingot");
        recipe("gear", 1, Kind.ASSEMBLING, "b:assembled", "2 iron_ingot");
        assertEquals(into("iron_ingot", 2), recycle("gear"));
    }

    @Test
    void withinAKindTheFirstRecipeByIdWins() {
        recipe("gear", 1, Kind.CRAFTING, "minecraft:gear_from_iron", "4 iron_ingot");
        recipe("gear", 1, Kind.CRAFTING, "minecraft:gear_from_copper", "4 copper_ingot");
        assertEquals(into("copper_ingot", 4), recycle("gear"));
    }

    @Test
    void aRecipeFromAnOreLosesToOneThatIsNot() {
        recipe("gear", 1, Kind.CRAFTING, "a:gear_from_ore", "iron_ore");
        recipe("gear", 1, Kind.CRAFTING, "b:gear_from_ingots", "4 iron_ingot");
        assertEquals(into("iron_ingot", 4), recycle("gear"));
    }

    @Test
    void anItemACookingRecipeMakesIsRefusedAsRefined() {
        recipe("glass", 1, Kind.COOKING, "minecraft:glass", "sand");
        assertEquals(new Refused<>(Refusal.REFINED), recycle("glass"));
    }

    @Test
    void aCookedItemIsRefusedEvenWhenACraftingRecipeAlsoMakesIt() {
        recipe("iron_ingot", 1, Kind.COOKING, "minecraft:iron_ingot_from_smelting_raw_iron", "raw_iron");
        recipe("iron_ingot", 1, Kind.CRAFTING, "minecraft:iron_ingot_from_nuggets", "9 iron_nugget");
        recipe("iron_nugget", 9, Kind.CRAFTING, "minecraft:iron_nugget", "iron_ingot");
        assertEquals(new Refused<>(Refusal.REFINED), recycle("iron_ingot"));
    }

    @Test
    void anItemInTheRefinedTagIsRefusedThoughCraftingMakesIt() {
        recipe("oak_planks", 4, Kind.CRAFTING, "minecraft:oak_planks", "oak_log");
        assertEquals(new Refused<>(Refusal.REFINED), recycle("oak_planks"));
    }

    @Test
    void aStorageBlockIsRefusedToBeUncraftedInstead() {
        recipe("iron_block", 1, Kind.CRAFTING, "minecraft:iron_block", "9 iron_ingot");
        recipe("iron_ingot", 9, Kind.CRAFTING, "minecraft:iron_ingot_from_iron_block", "iron_block");
        assertEquals(new Refused<>(Refusal.UNCRAFT_INSTEAD), recycle("iron_block"));
    }

    @Test
    void whatOnlyUncraftingMakesRecyclesIntoItself() {
        recipe("raw_iron_block", 1, Kind.CRAFTING, "minecraft:raw_iron_block", "9 raw_iron");
        recipe("raw_iron", 9, Kind.CRAFTING, "minecraft:raw_iron", "raw_iron_block");
        recipe("iron_nugget", 9, Kind.CRAFTING, "minecraft:iron_nugget", "iron_ingot");
        recipe("iron_ingot", 1, Kind.CRAFTING, "minecraft:iron_ingot_from_nuggets", "9 iron_nugget");
        recipe("hay_block", 1, Kind.CRAFTING, "minecraft:hay_block", "9 wheat");
        recipe("wheat", 9, Kind.CRAFTING, "minecraft:wheat", "hay_block");
        assertEquals(into("raw_iron", 1), recycle("raw_iron"));
        assertEquals(into("iron_nugget", 1), recycle("iron_nugget"));
        assertEquals(into("wheat", 1), recycle("wheat"));
    }

    @Test
    void whatEveryRecipeMakesFromOneRawKindAloneIsRefined() {
        recipe("sandstone", 1, Kind.CRAFTING, "minecraft:sandstone", "4 sand");
        recipe("paper", 3, Kind.CRAFTING, "minecraft:paper", "3 sugar_cane");
        assertEquals(new Refused<>(Refusal.REFINED), recycle("sandstone"));
        assertEquals(new Refused<>(Refusal.REFINED), recycle("paper"));
    }

    @Test
    void anItemAlsoUncraftedFromABlockIsRefinedWhenItsOtherRecipeCutsIt() {
        recipe("bone_meal", 3, Kind.CRAFTING, "minecraft:bone_meal", "bone");
        recipe("bone_meal", 9, Kind.CRAFTING, "minecraft:bone_meal_from_bone_block", "bone_block");
        recipe("bone_block", 1, Kind.CRAFTING, "minecraft:bone_block", "9 bone_meal");
        assertEquals(new Refused<>(Refusal.REFINED), recycle("bone_meal"));
    }

    @Test
    void aThingMadeFromOneRawKindIsNotRefined() {
        recipe("furnace", 1, Kind.CRAFTING, "minecraft:furnace", "8 cobblestone");
        recipe("turtle_helmet", 1, Kind.CRAFTING, "minecraft:turtle_helmet", "5 turtle_scute");
        assertEquals(into("cobblestone", 8), recycle("furnace"));
        assertEquals(into("turtle_scute", 5), recycle("turtle_helmet"));
    }

    @Test
    void anItemCutFromARefinedOneRecycles() {
        recipe("stone", 1, Kind.COOKING, "minecraft:stone", "cobblestone");
        recipe("stone_slab", 6, Kind.CRAFTING, "minecraft:stone_slab", "3 stone");
        assertEquals(into("stone", 0.5), recycle("stone_slab"));
    }

    @Test
    void anItemOnlySomeRecipesCutFromRawRecyclesByTheFirst() {
        recipe("stick", 4, Kind.CRAFTING, "minecraft:stick", "2 oak_planks");
        recipe("stick", 1, Kind.CRAFTING, "minecraft:stick_from_bamboo_item", "2 bamboo");
        assertEquals(into("oak_planks", 0.5), recycle("stick"));
    }

    @Test
    void anOverrideWinsOverARefusal() {
        recipe("glass", 1, Kind.COOKING, "minecraft:glass", "sand");
        recipe("iron_block", 1, Kind.CRAFTING, "minecraft:iron_block", "9 iron_ingot");
        recipe("iron_ingot", 9, Kind.CRAFTING, "minecraft:iron_ingot_from_iron_block", "iron_block");
        assertEquals(into("sand", 1), recycle("glass", Optional.of(shares("sand", 1))));
        assertEquals(into("iron_ingot", 8), recycle("iron_block", Optional.of(shares("iron_ingot", 8))));
        assertEquals(into("oak_log", 1), recycle("oak_planks", Optional.of(shares("oak_log", 1))));
    }

    @Test
    void mixesAreClosedWhenEveryItemRecyclesIntoWhatAMixHolds() {
        recipe("furnace", 1, Kind.CRAFTING, "minecraft:furnace", "8 cobblestone");
        recipe("oak_planks", 4, Kind.CRAFTING, "minecraft:oak_planks", "oak_log");
        assertEquals(Map.of(), RecyclingRules.unclosed(List.of(List.of("furnace", "iron_ingot"), List.of("cobblestone", "oak_planks")), this::recycle));
    }

    @Test
    void mixesAreUnclosedByWhatTheirItemsRecycleIntoOutsideThem() {
        recipe("piston", 1, Kind.CRAFTING, "minecraft:piston", "3 oak_planks", "4 cobblestone", "iron_ingot", "redstone");
        recipe("hopper", 1, Kind.CRAFTING, "minecraft:hopper", "5 iron_ingot", "chest");
        assertEquals(Map.of("piston", List.of("iron_ingot", "redstone"), "hopper", List.of("iron_ingot", "chest")),
                RecyclingRules.unclosed(List.of(List.of("piston", "hopper"), List.of("cobblestone", "oak_planks")), this::recycle));
    }
}
