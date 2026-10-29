// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * What an item recycles into at full cost, before yield (the line's glossary: Recycle), over plain
 * descriptions of the recipes that make it, so the rules know nothing of Minecraft.
 */
public final class RecyclingRules {

    private RecyclingRules() {
    }

    /** The kinds of recipe recycling runs backwards. */
    public enum Kind {
        SMELTING,
        /** Blasting, smoking and campfire cooking. */
        OTHER_COOKING,
        ASSEMBLING,
        CRAFTING
    }

    /** {@code amount} of {@code item}, whole or not. */
    public record Share<I>(I item, double amount) {
    }

    /** A recipe that makes {@code count} of an item from {@code ingredients}, one share per slot or sized ingredient. */
    public record Made<I>(Kind kind, String id, List<Share<I>> ingredients, int count) {
    }

    /**
     * What one {@code item} recycles into: {@code override} when the pack wrote one, else the cost of a recipe
     * {@code makers} names for it, else itself.
     */
    public static <I> List<Share<I>> recyclesInto(I item, Optional<List<Share<I>>> override, Function<I, List<Made<I>>> makers,
            Predicate<I> ore) {
        if (override.isPresent()) {
            return override.get();
        }
        List<Made<I>> candidates = makers.apply(item).stream().filter(recipe -> !uncompresses(item, recipe, makers)).toList();
        boolean onlyOtherCooking = candidates.stream().allMatch(recipe -> recipe.kind() == Kind.OTHER_COOKING);
        Comparator<Made<I>> order = Comparator.<Made<I>>comparingInt(recipe -> rank(recipe.kind(), onlyOtherCooking))
                .thenComparing(recipe -> recipe.ingredients().stream().anyMatch(ingredient -> ore.test(ingredient.item())))
                .thenComparing(Made::id);
        return candidates.stream()
                .filter(recipe -> onlyOtherCooking || recipe.kind() != Kind.OTHER_COOKING)
                .min(order)
                .map(RecyclingRules::cost)
                .orElseGet(() -> List.of(new Share<>(item, 1)));
    }

    /** Smelting first, then assembling, then crafting; blasting, smoking and campfire cooking count as smelting when alone. */
    private static int rank(Kind kind, boolean onlyOtherCooking) {
        return switch (kind) {
            case SMELTING -> 0;
            case OTHER_COOKING -> onlyOtherCooking ? 0 : Integer.MAX_VALUE;
            case ASSEMBLING -> 1;
            case CRAFTING -> 2;
        };
    }

    /**
     * Whether {@code recipe} makes more of {@code item} than it takes of its one ingredient, which a recipe also makes
     * back from {@code item} alone: an ingot from a block, nuggets from an ingot. Run backwards, it would recycle up a band.
     */
    private static <I> boolean uncompresses(I item, Made<I> recipe, Function<I, List<Made<I>>> makers) {
        List<Share<I>> cost = cost(recipe);
        if (cost.size() != 1 || cost.getFirst().amount() >= 1) {
            return false;
        }
        I from = cost.getFirst().item();
        return makers.apply(from).stream().anyMatch(back -> {
            List<Share<I>> backCost = cost(back);
            return backCost.size() == 1 && backCost.getFirst().item().equals(item);
        });
    }

    /** A recipe's ingredients, each item once in the order it first appears, divided among what it makes. */
    private static <I> List<Share<I>> cost(Made<I> recipe) {
        var amounts = new LinkedHashMap<I, Double>();
        for (Share<I> ingredient : recipe.ingredients()) {
            amounts.merge(ingredient.item(), ingredient.amount(), Double::sum);
        }
        var cost = new ArrayList<Share<I>>();
        amounts.forEach((ingredient, amount) -> cost.add(new Share<>(ingredient, amount / recipe.count())));
        return cost;
    }
}
