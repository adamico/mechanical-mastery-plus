// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * The pack's own lists of each chain's machines (#246), the tags {@code ChainMachines} reads, in the pack this mod's
 * folder sits in: Theurgy's nine processing machines for energy, Neo Vitae's two for life, and none for protection or
 * money. No Theurgy or Neo Vitae is on the game tests' runs, so the game test
 * {@code chain_machines_of_every_chain_idle_until_its_challenge_starts} plays the same lock on the game-test pack's
 * stand-ins.
 */
class ChainMachineListTest {

    private static final Path TAGS = Path.of("../kubejs/data/mechanicalmasteryreborn/tags/item/chain_machines");

    private static final Pattern ID = Pattern.compile("\"([a-z0-9_.-]+:[a-z0-9_/.-]+)\"");

    @Test
    void energysMachinesAreTheurgysProcessingMachinesAndReformationPedestals() throws IOException {
        assertEquals(List.of(
                "theurgy:sal_ammoniac_accumulator",
                "theurgy:liquefaction_cauldron",
                "theurgy:fermentation_vat",
                "theurgy:digestion_vat",
                "theurgy:calcination_oven",
                "theurgy:distiller",
                "theurgy:mercury_catalyst",
                "theurgy:incubator",
                "theurgy:sulfuric_flux_emitter",
                "theurgy:reformation_source_pedestal",
                "theurgy:reformation_target_pedestal",
                "theurgy:reformation_result_pedestal"), ids(Handicap.ENERGY));
    }

    @Test
    void lifesMachinesAreTheAraVitaeAndTheHellfireForge() throws IOException {
        assertEquals(List.of("neovitae:ara_vitae", "neovitae:hellfire_forge"), ids(Handicap.LIFE));
    }

    @Test
    void protectionAndMoneyHaveNoMachines() {
        assertFalse(Files.exists(file(Handicap.PROTECTION)), "protection's chain has a machine list");
        assertFalse(Files.exists(file(Handicap.MONEY)), "money's chain has a machine list");
    }

    private static List<String> ids(Handicap handicap) throws IOException {
        return ID.matcher(Files.readString(file(handicap))).results().map(match -> match.group(1)).toList();
    }

    private static Path file(Handicap handicap) {
        return TAGS.resolve(handicap.id() + ".json");
    }
}
