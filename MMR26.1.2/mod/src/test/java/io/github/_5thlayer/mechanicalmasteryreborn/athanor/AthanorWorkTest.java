// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.athanor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * The Athanor's pace and cost by how many lines count (#263, ADR 0014), at their defaults: each more line is faster and
 * cheaper, and only all four make a Quintessence for fewer Nullessence than it cancels; and its recipe in the pack this
 * mod's folder sits in, which takes something of each challenge's own and which no Chapters stage locks.
 */
class AthanorWorkTest {

    private static final Path DATA = Path.of("../kubejs/data/mechanicalmasteryreborn");

    @Test
    void noLineMakesNothing() {
        assertEquals(0.0, AthanorWork.progressPerTick(0, AthanorWork.DEFAULT_TICKS));
    }

    @Test
    void eachMoreLineIsFasterAndCheaper() {
        for (int lines = 2; lines <= AthanorWork.LINES; lines++) {
            assertTrue(AthanorWork.progressPerTick(lines, AthanorWork.DEFAULT_TICKS) > AthanorWork.progressPerTick(lines - 1, AthanorWork.DEFAULT_TICKS),
                    lines + " lines aren't faster than " + (lines - 1));
            assertTrue(AthanorWork.cost(lines, AthanorWork.DEFAULT_COSTS) < AthanorWork.cost(lines - 1, AthanorWork.DEFAULT_COSTS),
                    lines + " lines aren't cheaper than " + (lines - 1));
        }
    }

    @Test
    void onlyAllFourBeatBreakEven() {
        int cancels = AthanorWork.DEFAULT_QUINTESSENCE_CANCELS;
        assertTrue(AthanorWork.cost(AthanorWork.LINES, AthanorWork.DEFAULT_COSTS) < cancels, "all four lines don't beat break-even");
        for (int lines = 1; lines < AthanorWork.LINES; lines++) {
            assertTrue(AthanorWork.cost(lines, AthanorWork.DEFAULT_COSTS) > cancels, lines + " lines beat break-even");
        }
    }

    @Test
    void recipeTakesSomethingOfEachChallengesOwn() throws IOException {
        JsonObject recipe = JsonParser.parseString(Files.readString(DATA.resolve("recipe/assembling/athanor.json"))).getAsJsonObject();
        assertEquals("mechanicalmasteryreborn:athanor", recipe.getAsJsonArray("results").get(0).getAsJsonObject().get("id").getAsString());
        List<String> ingredients = recipe.getAsJsonArray("ingredients").asList().stream()
                .map(JsonElement::getAsJsonObject).map(ingredient -> ingredient.get("ingredient").getAsString()).toList();
        for (String own : List.of("theurgy:", "neovitae:", "mechanicalmasteryreborn:rift_shard", "mechanicalmasteryreborn:aureate_essence")) {
            assertTrue(ingredients.stream().anyMatch(ingredient -> ingredient.startsWith(own)), "the Athanor's recipe takes no " + own);
        }
    }

    /** Its crafting is never locked: no Chapters stage names it. */
    @Test
    void noStageLocksItsCrafting() throws IOException {
        try (Stream<Path> stages = Files.walk(DATA.resolve("chapters"))) {
            for (Path stage : stages.filter(path -> path.toString().endsWith(".json")).toList()) {
                assertTrue(!Files.readString(stage).contains("mechanicalmasteryreborn:athanor"), stage + " locks the Athanor");
            }
        }
    }
}
