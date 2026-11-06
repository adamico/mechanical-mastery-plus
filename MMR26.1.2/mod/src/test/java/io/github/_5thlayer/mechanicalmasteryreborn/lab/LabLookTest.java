// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * The lab's item wears the look the placed lab has (#303): its item model selects, on the custom model data string an
 * awakened lab's pick carries, the model the block state {@code hermetic=true} stands as, and falls back to the plain
 * lab's. The game tests run no client, so this reads the assets.
 */
class LabLookTest {

    private static final Path ASSETS = Path.of("src/main/resources/assets/mechanicalmasteryreborn");
    /** The custom model data string an awakened lab's pick carries, {@code LabBlock.HERMETIC_LOOK}, out of reach with no Minecraft. */
    private static final String HERMETIC_LOOK = "hermetic";

    @Test
    void itemModelSelectsTheHermeticLookOnThePicksCustomModelData() throws IOException {
        JsonObject select = read(ASSETS.resolve("items/lab.json")).getAsJsonObject("model");
        assertEquals("minecraft:select", select.get("type").getAsString());
        assertEquals("minecraft:custom_model_data", select.get("property").getAsString());
        assertEquals(0, select.get("index").getAsInt(), "the string the pick carries is its first");
        assertEquals(1, select.getAsJsonArray("cases").size(), "a case per look beside the plain lab's");
        JsonObject hermetic = select.getAsJsonArray("cases").get(0).getAsJsonObject();
        assertEquals(HERMETIC_LOOK, hermetic.get("when").getAsString());
        assertEquals(variant("hermetic=true"), hermetic.getAsJsonObject("model").get("model").getAsString());
        assertEquals(variant("hermetic=false"), select.getAsJsonObject("fallback").get("model").getAsString());
    }

    /** The model the placed lab stands as in {@code variant}. */
    private static String variant(String variant) throws IOException {
        return read(ASSETS.resolve("blockstates/lab.json")).getAsJsonObject("variants").getAsJsonObject(variant).get("model").getAsString();
    }

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
