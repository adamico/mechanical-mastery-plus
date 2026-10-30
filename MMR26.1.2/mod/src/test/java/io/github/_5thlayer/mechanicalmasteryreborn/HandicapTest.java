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
import java.util.Optional;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import org.junit.jupiter.api.Test;

/**
 * The handicaps' ids, which a world saves each team's by and so never change, and what the lab needs to show each
 * one: a name and its cost. The game tests {@code lab_*} check choosing one.
 */
class HandicapTest {

    /** As the line's glossary names them, in its order: never edit one of these once released. */
    private static final List<String> SPECIFIED = List.of("protection", "life", "energy", "money");

    private static final Path LANG = Path.of("src/main/resources/assets", MechanicalMasteryReborn.MOD_ID, "lang/en_us.json");

    @Test
    void everyHandicapHasItsSpecifiedId() {
        assertEquals(SPECIFIED, Arrays.stream(Handicap.values()).map(Handicap::id).toList());
    }

    @Test
    void everyHandicapIsFoundByItsId() {
        for (Handicap handicap : Handicap.values()) {
            assertEquals(Optional.of(handicap), Handicap.byId(handicap.id()));
        }
        assertEquals(Optional.empty(), Handicap.byId("luck"));
    }

    @Test
    void onlyEnergyIsChoosable() {
        assertEquals(List.of(Handicap.ENERGY), Arrays.stream(Handicap.values()).filter(Handicap::choosable).toList());
    }

    @Test
    void everyHandicapHasANameAndACost() throws IOException {
        String lang = Files.readString(LANG);
        for (Handicap handicap : Handicap.values()) {
            String key = MechanicalMasteryReborn.MOD_ID + ".handicap." + handicap.id();
            assertTrue(lang.contains("\"" + key + "\""), handicap + " has no name");
            assertTrue(lang.contains("\"" + key + ".cost\""), handicap + " has no cost");
        }
    }
}
