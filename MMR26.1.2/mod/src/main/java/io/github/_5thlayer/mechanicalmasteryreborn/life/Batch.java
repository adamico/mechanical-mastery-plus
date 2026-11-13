// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.HashSet;
import java.util.Set;

/**
 * A Pelican's batch (#366): the meals it has eaten since its last Vital Essence. Its saturation is the meals' own, added
 * up; its kinds are the items eaten, whatever their quality; its quality is the plain mean of the meals' quality levels,
 * none counting as 0. It is {@link #ready} once all three reach their thresholds together.
 */
public record Batch(double saturation, Set<String> kinds, int levels, int meals) {

    public static final Batch EMPTY = new Batch(0, Set.of(), 0, 0);

    /** What a batch must reach, all together, to be digested: saturation, kinds of meal and average quality. */
    public record Thresholds(double saturation, int kinds, double quality) {
    }

    public Batch {
        kinds = Set.copyOf(kinds);
    }

    /** This batch with one more meal of {@code kind}, giving {@code saturation}, at quality {@code level}. */
    public Batch eat(String kind, double saturation, int level) {
        Set<String> eaten = new HashSet<>(kinds);
        eaten.add(kind);
        return new Batch(this.saturation + saturation, eaten, levels + level, meals + 1);
    }

    /** The plain mean of its meals' quality levels; 0 with none. */
    public double quality() {
        return meals == 0 ? 0 : (double) levels / meals;
    }

    /** Whether it holds a meal and reaches every one of {@code thresholds}. */
    public boolean ready(Thresholds thresholds) {
        return meals > 0 && saturation >= thresholds.saturation() && kinds.size() >= thresholds.kinds()
                && quality() >= thresholds.quality();
    }
}
