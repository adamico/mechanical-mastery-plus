// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.athanor;

import java.util.List;

/**
 * How fast an Athanor works and what each Quintessence costs it, by how many of its supply lines count (#263, ADR 0014):
 * none makes nothing, and each more line is faster and cheaper. Only all four beat break-even on their own: fewer
 * Nullessence a Quintessence than one {@link #DEFAULT_QUINTESSENCE_CANCELS cancels}. Plain data, so a JUnit test reads it
 * without Minecraft; the server config holds the values played with, these being its defaults.
 */
public final class AthanorWork {

    /** The supply lines there are, one per handicap. */
    public static final int LINES = 4;
    /** The ticks one Quintessence takes with one, two, three and four lines counting. */
    public static final List<Integer> DEFAULT_TICKS = List.of(2400, 1200, 600, 300);
    /** The Nullessence one Quintessence takes with one, two, three and four lines counting. */
    public static final List<Integer> DEFAULT_COSTS = List.of(256, 128, 96, 48);
    /** The default of what one Quintessence cancels in C.O.D.'s last stand, which the costs are set against. */
    public static final int DEFAULT_QUINTESSENCE_CANCELS = 64;

    private AthanorWork() {
    }

    /** The share of a Quintessence one tick makes with {@code lines} counting, out of {@code ticks} by line count; none with none. */
    public static double progressPerTick(int lines, List<Integer> ticks) {
        return lines <= 0 ? 0 : 1.0 / Math.max(1, ticks.get(Math.min(lines, LINES) - 1));
    }

    /** The Nullessence a Quintessence takes with {@code lines} counting, out of {@code costs} by line count; at least one line. */
    public static int cost(int lines, List<Integer> costs) {
        return costs.get(Math.clamp(lines, 1, LINES) - 1);
    }
}
