// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * Spoiled's food classes (#363), in the pack this mod's folder sits in. Food falls in four spoil classes, each a pack
 * item tag: raw, ingredient and meal each rot into rotten flesh on a spoil recipe of their own, slower in that order,
 * and preserved never rots. No food is in two class tags, and food in none rots on Spoiled's own recipe at its default
 * time, the ingredient's, so every food item is in exactly one class. Which of the pack's foods sit in no tag needs the
 * pack's jars, so {@code scripts/check_spoil_classes.py} lists them. Only Cooking for Blockheads' fridge keeps food; no
 * Spoiled is on the game tests' runs, so the rates and the fridge are checked in game.
 */
class SpoilClassesTest {

    private static final Path DATA = Path.of("../kubejs/data");
    private static final Path CONFIG = Path.of("../config/spoiled-common.toml");
    private static final String CLASSES = "mechanicalmasteryreborn:spoil_classes/";
    private static final List<String> ROTTING = List.of("raw", "ingredient", "meal");
    private static final List<String> ALL = Stream.concat(ROTTING.stream(), Stream.of("preserved")).toList();
    /** Spoiled's own recipe's priority, which a class recipe outranks. */
    private static final int SPOILEDS_PRIORITY = 1;

    @Test
    void everyFoodIsInExactlyOneClass() throws IOException {
        var classOf = new HashMap<String, String>();
        for (String spoilClass : ALL) {
            List<String> items = tag(spoilClass);
            assertFalse(items.isEmpty(), spoilClass + " holds no food");
            for (String item : items) {
                assertFalse(item.startsWith("#"), spoilClass + " names a tag, " + item + ", so its food can't be told apart");
                String other = classOf.put(item, spoilClass);
                assertTrue(other == null, item + " is both " + other + " and " + spoilClass);
            }
        }
    }

    @Test
    void rawIngredientAndMealEachRotIntoRottenFleshSlowerInThatOrder() throws IOException {
        int previous = 0;
        for (String spoilClass : ROTTING) {
            JsonObject recipe = recipe(spoilClass);
            assertEquals("spoiled:spoil_recipe", recipe.get("type").getAsString());
            assertEquals("#" + CLASSES + spoilClass, recipe.get("ingredient").getAsString());
            assertEquals("minecraft:rotten_flesh", recipe.getAsJsonObject("result").get("id").getAsString());
            assertTrue(recipe.get("priority").getAsInt() > SPOILEDS_PRIORITY, spoilClass + " doesn't outrank Spoiled's own recipe");
            int time = recipe.get("spoiltime").getAsInt();
            assertTrue(time > previous, spoilClass + " rots no slower than the class before it");
            previous = time;
        }
    }

    @Test
    void foodInNoClassRotsAsAnIngredient() throws IOException {
        assertEquals(recipe("ingredient").get("spoiltime").getAsInt(), Integer.parseInt(setting("defaultSpoilTime")),
                "Spoiled's own recipe, for food in no class, rots at its default time, which isn't the ingredient's");
        assertEquals("true", setting("initializeSpoiling"), "food in no class doesn't rot at all");
        assertEquals("false", setting("spoilEverything"), "Spoiled ignores the class recipes");
        assertEquals("\"minecraft:rotten_flesh\"", setting("defaultSpoilItem"));
    }

    @Test
    void preservedFoodNeverRots() throws IOException {
        assertFalse(Files.exists(DATA.resolve("mechanicalmasteryreborn/recipe/spoiling/preserved.json")));
        assertTrue(ids(DATA.resolve("spoiled/tags/item/foods_blacklist.json")).contains("#" + CLASSES + "preserved"),
                "Spoiled's blacklist leaves preserved food out");
        assertEquals("[]", setting("spoilBlacklist"), "a food is blacklisted outside the preserved class");
    }

    @Test
    void onlyTheFridgeKeepsFood() throws IOException {
        assertEquals("[\"cookingforblockheads:fridge,0.25\"]", setting("containerModifier"));
        assertEquals("[]", setting("itemContainerModifier"), "a held container, as a shulker box, keeps food");
    }

    private static List<String> tag(String spoilClass) throws IOException {
        return ids(DATA.resolve("mechanicalmasteryreborn/tags/item/spoil_classes/" + spoilClass + ".json"));
    }

    private static List<String> ids(Path tag) throws IOException {
        return JsonParser.parseString(Files.readString(tag)).getAsJsonObject().getAsJsonArray("values").asList().stream()
                .map(JsonElement::getAsString).toList();
    }

    private static JsonObject recipe(String spoilClass) throws IOException {
        return JsonParser.parseString(Files.readString(DATA.resolve("mechanicalmasteryreborn/recipe/spoiling/" + spoilClass + ".json")))
                .getAsJsonObject();
    }

    /** A setting's value as written in Spoiled's common config, which names each key once. */
    private static String setting(String key) throws IOException {
        Matcher match = Pattern.compile("(?m)^\\s*" + key + " = (.+)$").matcher(Files.readString(CONFIG));
        assertTrue(match.find(), "Spoiled's config sets no " + key);
        return match.group(1).strip();
    }
}
