// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import org.junit.jupiter.api.Test;

/**
 * The essences' ids are the ones #163 fixed, which a world saves its items by and so never change, and
 * each has what the client needs to show it. The game test {@code essence_every_id_is_a_registered_item}
 * checks the game registers each one.
 */
class EssenceIdsTest {

    /** As #163 and the line's glossary name them, in order: never edit one of these once released. */
    private static final List<String> SPECIFIED = List.of(
            "basic_mechanical_essence",
            "regular_mechanical_essence",
            "improved_mechanical_essence",
            "advanced_mechanical_essence",
            "galvanic_essence",
            "warding_essence",
            "vital_essence",
            "aureate_essence",
            "quintessence");

    private static final Path ASSETS = Path.of("src/main/resources/assets", MechanicalMasteryReborn.MOD_ID);

    @Test
    void everyEssenceHasItsSpecifiedId() {
        assertEquals(SPECIFIED, Arrays.stream(Essence.values()).map(Essence::path).toList());
    }

    @Test
    void onlyTheMechanicalEssencesAreObtainable() {
        assertEquals(List.of(Essence.BASIC, Essence.REGULAR, Essence.IMPROVED, Essence.ADVANCED),
                Arrays.stream(Essence.values()).filter(Essence::obtainable).toList());
    }

    @Test
    void everyEssenceHasAModelATextureAndAName() throws IOException {
        String lang = Files.readString(ASSETS.resolve("lang/en_us.json"));
        for (Essence essence : Essence.values()) {
            String path = essence.path();
            assertTrue(Files.isRegularFile(ASSETS.resolve("items/" + path + ".json")), path + " has no item definition");
            assertTrue(Files.isRegularFile(ASSETS.resolve("models/item/" + path + ".json")), path + " has no item model");
            assertTrue(Files.isRegularFile(ASSETS.resolve("textures/item/" + path + ".png")), path + " has no texture");
            assertTrue(lang.contains("\"item." + MechanicalMasteryReborn.MOD_ID + "." + path + "\""), path + " has no name");
        }
    }
}
