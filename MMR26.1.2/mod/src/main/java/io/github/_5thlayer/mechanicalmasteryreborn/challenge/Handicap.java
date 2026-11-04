// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Optional;

/**
 * What a challenge costs the team while it plays it, and only then, as the line's glossary has them. A team chooses
 * one at the lab, which starts that handicap's challenge. Each is saved under its name, which never changes once
 * shipped. Plain data, so a JUnit test reads it without Minecraft.
 */
public enum Handicap {

    /** An Assault on the island (ADR 0010). */
    PROTECTION("protection"),
    /** Faster hunger, and a Diet: the same food feeding less each time it is eaten (ADR 0011). */
    LIFE("life"),
    /** Generator blocks disabled. */
    ENERGY("energy"),
    /** Less money. */
    MONEY("money");

    private final String id;

    Handicap(String id) {
        this.id = id;
    }

    /** The name it is saved under, which its translation keys end with too. */
    public String id() {
        return id;
    }

    /** The handicap whose ordinal is {@code ordinal}, as the lab's buttons and its sync send it, if any. */
    public static Optional<Handicap> byOrdinal(int ordinal) {
        Handicap[] all = values();
        return ordinal < 0 || ordinal >= all.length ? Optional.empty() : Optional.of(all[ordinal]);
    }

    /** The handicap saved under {@code id}, if any. */
    public static Optional<Handicap> byId(String id) {
        for (Handicap handicap : values()) {
            if (handicap.id.equals(id)) {
                return Optional.of(handicap);
            }
        }
        return Optional.empty();
    }
}
