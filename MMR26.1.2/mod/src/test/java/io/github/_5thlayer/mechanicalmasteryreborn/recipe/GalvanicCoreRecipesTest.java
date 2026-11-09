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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * The energy chain's Galvanic Core (ADR 0018, #331), in the pack this mod's folder sits in: the Incubator makes a core
 * from one charge of each branch, and Assemblers make the Galvanic Essence from several cores and more alchemy. Both
 * recipes are named by the energy stage, so Chapters locks them and EMI marks them until the team starts energy, and
 * the Incubator's old essence recipe is gone. Theurgy doesn't load on the game tests' runs, so its recipes are read
 * here.
 */
class GalvanicCoreRecipesTest {

    private static final Path DATA = Path.of("../kubejs/data/mechanicalmasteryreborn");
    private static final String MOD = "mechanicalmasteryreborn:";

    @Test
    void theIncubatorMakesAGalvanicCoreFromOneChargeOfEachBranch() throws IOException {
        JsonObject recipe = recipe("incubation/galvanic_core");
        assertEquals("theurgy:incubation", recipe.get("type").getAsString());
        assertEquals(MOD + "mercury_charge", recipe.get("mercury").getAsString());
        assertEquals(MOD + "salt_charge", recipe.get("salt").getAsString());
        assertEquals(MOD + "sulfur_charge", recipe.get("sulfur").getAsString());
        assertEquals(MOD + "galvanic_core", recipe.getAsJsonObject("result").get("id").getAsString());
    }

    @Test
    void theIncubatorNoLongerMakesTheGalvanicEssence() {
        assertFalse(Files.exists(DATA.resolve("recipe/incubation/galvanic_essence.json")));
    }

    @Test
    void assemblersMakeTheGalvanicEssenceFromSeveralCoresAndMoreAlchemy() throws IOException {
        JsonObject recipe = recipe("assembling/galvanic_essence");
        assertEquals("craftworks:assembling", recipe.get("type").getAsString());
        int cores = 0;
        boolean alchemy = false;
        for (JsonElement in : recipe.getAsJsonArray("ingredients")) {
            String id = in.getAsJsonObject().get("ingredient").getAsString();
            if (id.equals(MOD + "galvanic_core")) {
                cores = in.getAsJsonObject().get("count").getAsInt();
            } else {
                alchemy |= id.startsWith("theurgy:");
            }
        }
        assertTrue(cores > 1, "the essence takes " + cores + " cores");
        assertTrue(alchemy, "the essence takes no more alchemy");
        assertEquals(MOD + "galvanic_essence", recipe.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
    }

    @Test
    void theCoreAndEssenceRecipesAreTheEnergyChainsAndTheOldOneIsNot() throws IOException {
        var stage = new ArrayList<String>();
        JsonParser.parseString(Files.readString(DATA.resolve("chapters/stages/energy.json"))).getAsJsonObject()
                .getAsJsonArray("recipes").forEach(id -> stage.add(id.getAsString()));
        assertTrue(stage.contains(MOD + "incubation/galvanic_core"));
        assertTrue(stage.contains(MOD + "assembling/galvanic_essence"));
        assertFalse(stage.contains(MOD + "incubation/galvanic_essence"));
    }

    private static JsonObject recipe(String path) throws IOException {
        return JsonParser.parseString(Files.readString(DATA.resolve("recipe/" + path + ".json"))).getAsJsonObject();
    }
}
