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

    /** More, stronger and special mobs. */
    PROTECTION("protection", false),
    /** Less max health. */
    LIFE("life", false),
    /** Generator blocks disabled. */
    ENERGY("energy", true),
    /** Less money. */
    MONEY("money", false);

    private final String id;
    private final boolean choosable;

    Handicap(String id, boolean choosable) {
        this.id = id;
        this.choosable = choosable;
    }

    /** The name it is saved under, which its translation keys end with too. */
    public String id() {
        return id;
    }

    /** Whether the lab lets a team choose it yet: for now only energy, the others coming later (#171). */
    public boolean choosable() {
        return choosable;
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
