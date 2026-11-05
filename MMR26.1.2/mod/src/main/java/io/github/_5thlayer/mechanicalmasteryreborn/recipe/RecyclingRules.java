// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * What recycling an item comes to, at full cost, before yield (the line's glossary: Recycle), over plain
 * descriptions of the recipes that make it, so the rules know nothing of Minecraft.
 */
public final class RecyclingRules {

    private RecyclingRules() {
    }

    /** The kinds of recipe the rules read. */
    public enum Kind {
        /** Smelting, blasting, smoking and campfire cooking: never run backwards, and what they make is refined. */
        COOKING,
        ASSEMBLING,
        CRAFTING
    }

    /** Why an item can't be recycled. */
    public enum Refusal {
        /**
         * It is refined: a cooking recipe makes it, every recipe that makes it cuts it from one raw kind alone, or the
         * pack names it refined. Recycling stops at refined (ADR 0008).
         */
        REFINED,
        /** A player uncrafts it for free: a block of what it is made of nine of. */
        UNCRAFT_INSTEAD
    }

    /** {@code amount} of {@code item}, whole or not. */
    public record Share<I>(I item, double amount) {
    }

    /** A recipe that makes {@code count} of an item from {@code ingredients}, one share per slot or sized ingredient. */
    public record Made<I>(Kind kind, String id, List<Share<I>> ingredients, int count) {
    }

    /**
     * What the rules ask of an item beyond its recipes: whether it is an {@code ore}, which loses to any other
     * ingredient; whether the pack names it {@code refined}; and whether it is a {@code thing}, worn or a block with a
     * state of its own, as a furnace or a sign, which is never refined however it is made.
     */
    public record Traits<I>(Predicate<I> ore, Predicate<I> refined, Predicate<I> thing) {
    }

    /** What recycling an item comes to: what it recycles into, or why it can't be. */
    public sealed interface Outcome<I> permits Into, Refused {
    }

    /** It recycles into {@code shares}, at full cost, before yield. */
    public record Into<I>(List<Share<I>> shares) implements Outcome<I> {
    }

    /** It can't be recycled, {@code why}. */
    public record Refused<I>(Refusal why) implements Outcome<I> {
    }

    /**
     * What recycling one {@code item} comes to: {@code override} when the pack wrote one, whatever the item is; else
     * refused when it is cooked or named refined, or uncrafted for free, or, but for a thing, cut from one raw kind
     * by every recipe that makes it; else the cost of the crafting or assembling recipe {@code makers} names for it,
     * but one that uncrafts it from a block; else itself, which is how a raw item, a nugget among them, is thrown away.
     */
    public static <I> Outcome<I> recycle(I item, Optional<List<Share<I>>> override, Function<I, List<Made<I>>> makers, Traits<I> traits) {
        if (override.isPresent()) {
            return new Into<>(override.get());
        }
        if (cookedOrNamed(item, makers, traits)) {
            return new Refused<>(Refusal.REFINED);
        }
        if (makers.apply(item).stream().anyMatch(recipe -> compresses(item, recipe, makers))) {
            return new Refused<>(Refusal.UNCRAFT_INSTEAD);
        }
        List<Made<I>> made = runBackwards(item, makers);
        if (!made.isEmpty() && !traits.thing().test(item)
                && made.stream().map(RecyclingRules::cost).allMatch(cost -> cost.size() == 1 && raw(cost.getFirst().item(), makers, traits))) {
            return new Refused<>(Refusal.REFINED);
        }
        Comparator<Made<I>> order = Comparator.<Made<I>>comparingInt(recipe -> recipe.kind() == Kind.ASSEMBLING ? 0 : 1)
                .thenComparing(recipe -> recipe.ingredients().stream().anyMatch(ingredient -> traits.ore().test(ingredient.item())))
                .thenComparing(Made::id);
        return new Into<>(made.stream().min(order).map(RecyclingRules::cost).orElseGet(() -> List.of(new Share<>(item, 1))));
    }

    /**
     * Every item of {@code mixes} that recycles into an item no mix holds, and those items, each once, in order: none
     * when the mixes are closed (the line's glossary: Mix). An item refused, or that recycles into itself, closes.
     */
    public static <I> Map<I, List<I>> unclosed(Collection<? extends Collection<I>> mixes, Function<I, Outcome<I>> recycle) {
        var held = new HashSet<I>();
        mixes.forEach(held::addAll);
        var unclosed = new LinkedHashMap<I, List<I>>();
        for (Collection<I> mix : mixes) {
            for (I item : mix) {
                if (!unclosed.containsKey(item) && recycle.apply(item) instanceof Into<I>(List<Share<I>> into)) {
                    List<I> outside = into.stream().map(Share::item).filter(result -> !held.contains(result)).distinct().toList();
                    if (!outside.isEmpty()) {
                        unclosed.put(item, outside);
                    }
                }
            }
        }
        return unclosed;
    }

    private static <I> boolean cookedOrNamed(I item, Function<I, List<Made<I>>> makers, Traits<I> traits) {
        return traits.refined().test(item) || makers.apply(item).stream().anyMatch(recipe -> recipe.kind() == Kind.COOKING);
    }

    /** The recipes that make {@code item} which recycling may run backwards: all but one that uncrafts it from a block. */
    private static <I> List<Made<I>> runBackwards(I item, Function<I, List<Made<I>>> makers) {
        return makers.apply(item).stream().filter(recipe -> !uncompresses(item, recipe, makers)).toList();
    }

    /** Whether {@code item} is raw: nothing cooks it, the pack doesn't name it refined, and no recipe recycling runs backwards makes it. */
    private static <I> boolean raw(I item, Function<I, List<Made<I>>> makers, Traits<I> traits) {
        return !cookedOrNamed(item, makers, traits) && runBackwards(item, makers).isEmpty();
    }

    /** Whether {@code recipe} makes one {@code item} from several of one kind, which a recipe uncrafts it back into: a block from ingots. */
    private static <I> boolean compresses(I item, Made<I> recipe, Function<I, List<Made<I>>> makers) {
        List<Share<I>> cost = cost(recipe);
        return cost.size() == 1 && cost.getFirst().amount() > 1 && madeBackFrom(cost.getFirst().item(), item, makers);
    }

    /**
     * Whether {@code recipe} makes several {@code item} from one of another, which a recipe also makes back from
     * {@code item} alone: an ingot from a block, nuggets from an ingot. Run backwards, it would recycle up a band.
     */
    private static <I> boolean uncompresses(I item, Made<I> recipe, Function<I, List<Made<I>>> makers) {
        List<Share<I>> cost = cost(recipe);
        return cost.size() == 1 && cost.getFirst().amount() < 1 && madeBackFrom(cost.getFirst().item(), item, makers);
    }

    /** Whether a crafting or assembling recipe makes {@code made} from {@code from} alone. */
    private static <I> boolean madeBackFrom(I made, I from, Function<I, List<Made<I>>> makers) {
        return makers.apply(made).stream().anyMatch(back -> {
            List<Share<I>> backCost = cost(back);
            return back.kind() != Kind.COOKING && backCost.size() == 1 && backCost.getFirst().item().equals(from);
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
