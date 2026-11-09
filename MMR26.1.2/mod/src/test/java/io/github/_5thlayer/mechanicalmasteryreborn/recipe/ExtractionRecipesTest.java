// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * Extraction (ADR 0018, #332), in the pack this mod's folder sits in: the Liquefaction Cauldron breaks an Aureate,
 * Warding or Vital Essence into raw alchemy, each into its own raw form, since a liquefaction recipe has one result and
 * the cauldron picks one recipe per item. The ingredient is the bare item, so a stale essence, which only differs by a
 * data component, matches too. Every extraction recipe is named by the energy stage, so Chapters locks it until energy
 * starts. No Theurgy is on the game tests' runs; {@code EnergyChainTests} plays the lock.
 */
class ExtractionRecipesTest {

    private static final Path DATA = Path.of("../kubejs/data/mechanicalmasteryreborn");
    private static final String MOD = "mechanicalmasteryreborn:";
    private static final List<String> ESSENCES = List.of("aureate", "warding", "vital");
    private static final Set<String> RAW = Set.of("theurgy:mercury_shard", "theurgy:alchemical_salt_strata",
            "theurgy:alchemical_sulfur_rotten_flesh");

    @Test
    void theCauldronExtractsEachTransmutedEssenceIntoARawForm() throws IOException {
        for (String essence : ESSENCES) {
            JsonObject recipe = recipe(essence);
            assertEquals("theurgy:liquefaction", recipe.get("type").getAsString());
            assertEquals(MOD + essence + "_essence", recipe.get("ingredient").getAsString(), essence + " extracts the bare item, stale too");
            String result = recipe.getAsJsonObject("result").get("id").getAsString();
            assertTrue(RAW.contains(result), essence + " extracts into " + result + ", not a raw form");
            assertTrue(recipe.getAsJsonObject("result").get("count").getAsInt() > 0);
        }
    }

    @Test
    void eachEssenceExtractsIntoItsOwnForm() throws IOException {
        var seen = new HashSet<String>();
        for (String essence : ESSENCES) {
            assertTrue(seen.add(recipe(essence).getAsJsonObject("result").get("id").getAsString()), essence + " shares its form");
        }
    }

    @Test
    void theGalvanicEssenceIsNeverExtracted() {
        assertFalse(Files.exists(DATA.resolve("recipe/extraction/galvanic_essence.json")));
    }

    @Test
    void everyExtractionRecipeIsTheEnergyChains() throws IOException {
        var stage = new ArrayList<String>();
        JsonParser.parseString(Files.readString(DATA.resolve("chapters/stages/energy.json"))).getAsJsonObject()
                .getAsJsonArray("recipes").forEach(id -> stage.add(id.getAsString()));
        for (String essence : ESSENCES) {
            assertTrue(stage.contains(MOD + "extraction/" + essence + "_essence"), "the energy stage doesn't name " + essence);
        }
    }

    private static JsonObject recipe(String essence) throws IOException {
        return JsonParser.parseString(Files.readString(DATA.resolve("recipe/extraction/" + essence + "_essence.json"))).getAsJsonObject();
    }
}
