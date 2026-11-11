// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * The energy chain's Galvanic Charges (ADR 0018, #330), in the pack this mod's folder sits in: Assemblers pack Theurgy's
 * mercury crystals, or the Basic mix's raw mercury shards, Reformed Salt and Reformed Sulfur, each with redstone, into
 * the three charges; the reformed forms come only from the Reformation Array, which turns any salt or sulfur, from the
 * Distiller, Calcination or Extraction, into them; and every one of those recipes is named by
 * the energy chain's stage file, so Chapters locks it and EMI marks it until the team starts energy. No Theurgy is on
 * the game tests' runs, so its recipes can't load there; the game test {@code EnergyChainTests} plays the lock.
 */
class GalvanicChargeRecipesTest {

    private static final Path DATA = Path.of("../kubejs/data/mechanicalmasteryreborn");
    private static final String MOD = "mechanicalmasteryreborn:";

    @Test
    void assemblersPackMercuryCrystalsIntoAMercuryCharge() throws IOException {
        assertPacks("mercury_charge", Map.of("theurgy:mercury_crystal", 4, "minecraft:redstone", 1), "mercury_charge");
    }

    @Test
    void assemblersPackTheBasicMixsRawMercuryShardsIntoAMercuryCharge() throws IOException {
        assertPacks("mercury_charge_from_shards", Map.of("theurgy:mercury_shard", 16, "minecraft:redstone", 1), "mercury_charge");
    }

    @Test
    void assemblersPackReformedSaltIntoASaltCharge() throws IOException {
        assertPacks("salt_charge", Map.of(MOD + "reformed_salt", 2, "minecraft:redstone", 1), "salt_charge");
    }

    @Test
    void assemblersPackReformedSulfurIntoASulfurCharge() throws IOException {
        assertPacks("sulfur_charge", Map.of(MOD + "reformed_sulfur", 2, "minecraft:redstone", 1), "sulfur_charge");
    }

    @Test
    void reformationTurnsAnySaltIntoReformedSaltOnAMineralSaltPedestal() throws IOException {
        assertReforms("reformed_salt", "#theurgy:alchemical_salts", "theurgy:alchemical_salt_mineral", MOD + "reformed_salt");
    }

    @Test
    void reformationTurnsAnySulfurIntoReformedSulfurOnARedstoneSulfurPedestal() throws IOException {
        assertReforms("reformed_sulfur", "#theurgy:alchemical_sulfurs", "theurgy:alchemical_sulfur_redstone", MOD + "reformed_sulfur");
    }

    @Test
    void theReformedFormsAreMadeOnlyOnTheReformationArray() throws IOException {
        try (var files = Files.walk(DATA.resolve("recipe"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                String json = Files.readString(file);
                if (!(json.contains(MOD + "reformed_salt\"") || json.contains(MOD + "reformed_sulfur\""))) {
                    continue;
                }
                JsonObject recipe = JsonParser.parseString(json).getAsJsonObject();
                String result = recipe.has("result") ? recipe.getAsJsonObject("result").get("id").getAsString() : "";
                if (result.startsWith(MOD + "reformed_")) {
                    assertEquals("theurgy:reformation", recipe.get("type").getAsString(), file + " makes a reformed form");
                }
                if (recipe.has("results")) {
                    for (JsonElement out : recipe.getAsJsonArray("results")) {
                        assertTrue(!out.getAsJsonObject().get("id").getAsString().startsWith(MOD + "reformed_"),
                                file + " makes a reformed form");
                    }
                }
            }
        }
    }

    @Test
    void everyChargeAndReformationRecipeIsTheEnergyChains() throws IOException {
        var stage = new ArrayList<String>();
        JsonParser.parseString(Files.readString(DATA.resolve("chapters/stages/energy.json"))).getAsJsonObject()
                .getAsJsonArray("recipes").forEach(id -> stage.add(id.getAsString()));
        for (String id : List.of("assembling/mercury_charge", "assembling/mercury_charge_from_shards", "assembling/salt_charge",
                "assembling/sulfur_charge", "reformation/reformed_salt", "reformation/reformed_sulfur")) {
            assertTrue(stage.contains(MOD + id), "the energy stage doesn't name " + id);
        }
    }

    private static void assertPacks(String path, Map<String, Integer> inputs, String charge) throws IOException {
        JsonObject recipe = recipe("assembling/" + path);
        assertEquals("craftworks:assembling", recipe.get("type").getAsString());
        var found = new TreeMap<String, Integer>();
        for (JsonElement in : recipe.getAsJsonArray("ingredients")) {
            found.put(in.getAsJsonObject().get("ingredient").getAsString(), in.getAsJsonObject().get("count").getAsInt());
        }
        assertEquals(new TreeMap<>(inputs), found, "what " + path + " packs");
        var results = recipe.getAsJsonArray("results");
        assertEquals(1, results.size(), path + "'s results");
        assertEquals(MOD + charge, results.get(0).getAsJsonObject().get("id").getAsString());
    }

    private static void assertReforms(String path, String source, String target, String result) throws IOException {
        JsonObject recipe = recipe("reformation/" + path);
        assertEquals("theurgy:reformation", recipe.get("type").getAsString());
        assertEquals(source, recipe.getAsJsonArray("sources").get(0).getAsJsonObject().get("ingredient").getAsString());
        assertEquals(target, recipe.get("target").getAsString(), path + "'s target pedestal");
        assertEquals(result, recipe.getAsJsonObject("result").get("id").getAsString());
        assertTrue(recipe.get("mercuryFlux").getAsInt() > 0, path + " costs no mercury flux");
    }

    private static JsonObject recipe(String path) throws IOException {
        return JsonParser.parseString(Files.readString(DATA.resolve("recipe/" + path + ".json"))).getAsJsonObject();
    }
}
