// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * The mod id in the code is the one the build names, and one the game accepts as a namespace. The
 * code's id names the game tests and the build's selects them, so a mismatch runs none.
 */
class ModIdTest {

    @Test
    void theCodeNamesTheBuildsModId() throws IOException {
        var properties = new Properties();
        // Gradle runs the tests from the project directory.
        try (Reader reader = Files.newBufferedReader(Path.of("gradle.properties"))) {
            properties.load(reader);
        }
        assertEquals(properties.getProperty("mod_id"), MechanicalMasteryReborn.MOD_ID);
    }

    @Test
    void theModIdIsANamespace() {
        assertTrue(MechanicalMasteryReborn.MOD_ID.matches("[a-z0-9_.-]+"), MechanicalMasteryReborn.MOD_ID);
    }
}
