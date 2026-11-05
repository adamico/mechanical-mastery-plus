// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence;

import java.util.List;
import java.util.Optional;

/**
 * Every essence the mod registers, under the id a world saves it by, which never changes once shipped
 * (#163): the four Mechanical Essences, the four Transmuted Essences, Quintessence and Nullessence (#261), as the
 * line's glossary names them. Plain data, so a JUnit test reads it without Minecraft.
 */
public enum Essence {

    /** Red. */
    BASIC("basic_mechanical_essence", Kind.MECHANICAL),
    /** Blue. */
    REGULAR("regular_mechanical_essence", Kind.MECHANICAL),
    /** Yellow. */
    IMPROVED("improved_mechanical_essence", Kind.MECHANICAL),
    /** Magenta: the one C.O.D. makes first. */
    ADVANCED("advanced_mechanical_essence", Kind.MECHANICAL),
    /** The energy challenge's. */
    GALVANIC("galvanic_essence", Kind.TRANSMUTED),
    /** The protection challenge's. */
    WARDING("warding_essence", Kind.TRANSMUTED),
    /** The life challenge's. */
    VITAL("vital_essence", Kind.TRANSMUTED),
    /** The money challenge's. */
    AUREATE("aureate_essence", Kind.TRANSMUTED),
    /** Made from the four Transmuted Essences in the last chapter. */
    QUINTESSENCE("quintessence", Kind.QUINTESSENCE),
    /** What C.O.D. makes in its last stand, the opposite of Quintessence. */
    NULLESSENCE("nullessence", Kind.NULLESSENCE);

    /** Which family of essences one belongs to. */
    public enum Kind {
        MECHANICAL, TRANSMUTED, QUINTESSENCE, NULLESSENCE
    }

    /** The Mechanical Essences as C.O.D. makes them, highest tier first: Advanced until it is blocked, then the next. */
    public static final List<Essence> TIERS = List.of(ADVANCED, IMPROVED, REGULAR, BASIC);

    private final String path;
    private final Kind kind;

    Essence(String path, Kind kind) {
        this.path = path;
        this.kind = kind;
    }

    /** The item's id within the mod's namespace. */
    public String path() {
        return path;
    }

    public Kind kind() {
        return kind;
    }

    /**
     * Whether a player can have it yet: the Mechanical and the Transmuted Essences, which the challenges' chains
     * make and their Ports take, and Nullessence, which C.O.D. makes in its last stand, but not Quintessence, until a
     * way to make it exists (#262, #263). It is registered all the
     * same, so its id is taken from the first release.
     */
    public boolean obtainable() {
        return kind != Kind.QUINTESSENCE;
    }

    /** The essence saved under {@code path}, if any. */
    public static Optional<Essence> byPath(String path) {
        for (Essence essence : values()) {
            if (essence.path.equals(path)) {
                return Optional.of(essence);
            }
        }
        return Optional.empty();
    }
}
