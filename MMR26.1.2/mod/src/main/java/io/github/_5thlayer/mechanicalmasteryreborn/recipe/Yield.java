// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.function.DoubleSupplier;

/**
 * What a decompose or a recycle returns of a full amount (the line's glossary: Yield): the whole part of the
 * amount times the yield, and what is left over as the chance of one more, as Factorio's recycler does.
 */
public final class Yield {

    /** Arithmetic slack, so that a third times three is still one. */
    private static final double SLACK = 1e-9;

    private Yield() {
    }

    /** How many of {@code amount} come back at {@code yield}, {@code random} rolling the leftover, in [0, 1). */
    public static int roll(double amount, double yield, DoubleSupplier random) {
        double share = amount * yield;
        int whole = (int) Math.floor(share + SLACK);
        double leftover = share - whole;
        return leftover > SLACK && random.getAsDouble() < leftover ? whole + 1 : whole;
    }

    /** The most a {@link #roll} of {@code amount} at {@code yield} can return. */
    public static int most(double amount, double yield) {
        return (int) Math.ceil(amount * yield - SLACK);
    }
}
