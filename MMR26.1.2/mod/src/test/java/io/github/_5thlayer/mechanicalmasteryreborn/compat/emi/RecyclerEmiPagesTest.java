// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/** A mix too long for one EMI page splits into pages of at most {@link RecyclerEmiPages#PER_PAGE} results, in order. */
class RecyclerEmiPagesTest {

    private static List<Integer> upTo(int n) {
        return IntStream.range(0, n).boxed().toList();
    }

    @Test
    void aMixThatFitsIsOnePage() {
        assertEquals(List.of(upTo(RecyclerEmiPages.PER_PAGE)), RecyclerEmiPages.split(upTo(RecyclerEmiPages.PER_PAGE)));
        assertEquals(List.of(upTo(1)), RecyclerEmiPages.split(upTo(1)));
    }

    @Test
    void aLongMixSplitsInOrderWithTheRestOnTheLastPage() {
        var pages = RecyclerEmiPages.split(upTo(57));
        assertEquals(3, pages.size());
        assertEquals(upTo(RecyclerEmiPages.PER_PAGE), pages.get(0));
        assertEquals(IntStream.range(RecyclerEmiPages.PER_PAGE, 2 * RecyclerEmiPages.PER_PAGE).boxed().toList(), pages.get(1));
        assertEquals(IntStream.range(2 * RecyclerEmiPages.PER_PAGE, 57).boxed().toList(), pages.get(2));
    }
}
