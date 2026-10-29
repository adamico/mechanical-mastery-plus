// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence;

/**
 * Every essence the mod registers, under the id a world saves it by, which never changes once shipped
 * (#163): the four Mechanical Essences, the four Transmuted Essences and Quintessence, as the line's
 * glossary names them. Plain data, so a JUnit test reads it without Minecraft.
 */
public enum Essence {

    /** Red. */
    BASIC("basic_mechanical_essence", Kind.MECHANICAL),
    /** Blue. */
    REGULAR("regular_mechanical_essence", Kind.MECHANICAL),
    /** Yellow. */
    IMPROVED("improved_mechanical_essence", Kind.MECHANICAL),
    /** Magenta: the one C.O.D. makes. */
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
    QUINTESSENCE("quintessence", Kind.QUINTESSENCE);

    /** Which family of essences one belongs to. */
    public enum Kind {
        MECHANICAL, TRANSMUTED, QUINTESSENCE
    }

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
     * Whether a player can have it yet: only the Mechanical Essences, until the challenges that make the
     * others exist. The others are registered all the same, so their ids are taken from the first release.
     */
    public boolean obtainable() {
        return kind == Kind.MECHANICAL;
    }
}
