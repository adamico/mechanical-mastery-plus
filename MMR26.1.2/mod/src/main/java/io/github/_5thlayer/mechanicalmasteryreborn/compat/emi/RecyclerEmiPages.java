// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import java.util.ArrayList;
import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerLayout;

/**
 * How a mix too long for EMI's recipe screen is cut into pages: whole rows of an output's positions, at most
 * {@link #PER_PAGE} results each, in order, so EMI pages through them as recipes of their own. Plain lists, so a
 * JUnit test reads it without EMI.
 */
final class RecyclerEmiPages {

    /** Rows of results one page shows. */
    private static final int ROWS = 8;

    /** Results one page shows. */
    static final int PER_PAGE = ROWS * RecyclerLayout.POSITIONS_PER_OUTPUT;

    private RecyclerEmiPages() {
    }

    /** {@code results} as pages of at most {@link #PER_PAGE}; a mix that fits is one page. */
    static <T> List<List<T>> split(List<T> results) {
        List<List<T>> pages = new ArrayList<>();
        for (int from = 0; from < results.size(); from += PER_PAGE) {
            pages.add(List.copyOf(results.subList(from, Math.min(results.size(), from + PER_PAGE))));
        }
        return pages;
    }
}
