// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * A recipe the Recycler works, of the pack's two types, {@code decomposing} and {@code recycling}: one ingredient with
 * a count, and one or more results at full amounts in a fixed order, each item once. A Recycler short on outputs
 * returns a random choice of them (#216), so no count of outputs caps a recipe. No fluids and no chances: a core's
 * {@link Yield} adds those. No recipe book shows one.
 *
 * <p>A result is a template, not a stack: {@code ItemStack.CODEC} refuses an item whose components are not
 * bound yet, which they are not during the datapack load that reads recipes.
 */
public interface RecyclerRecipe extends Recipe<SingleRecipeInput> {

    SizedIngredient ingredient();

    List<ItemStackTemplate> results();

    /** Whether {@code input} is this recipe's item, whatever its count: a Recycler says when it holds too few. */
    @Override
    default boolean matches(SingleRecipeInput input, Level level) {
        return ingredient().ingredient().test(input.item());
    }

    /** The first result; the Recycler returns its yield of them all. */
    @Override
    default ItemStack assemble(SingleRecipeInput input) {
        return results().getFirst().create();
    }

    /** Nothing places one, but vanilla warns at every load of a recipe that is neither placeable nor special. */
    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredient().ingredient());
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    /** The codec of a {@code type} recipe made by {@code make}, which has a result or more and names each item once. */
    static <R extends RecyclerRecipe> MapCodec<R> codec(String type, BiFunction<SizedIngredient, List<ItemStackTemplate>, R> make) {
        return RecordCodecBuilder.<R>mapCodec(instance -> instance.group(
                SizedIngredient.NESTED_CODEC.fieldOf("ingredient").forGetter(RecyclerRecipe::ingredient),
                ItemStackTemplate.CODEC.listOf().fieldOf("results").forGetter(RecyclerRecipe::results))
                .apply(instance, make))
                .flatXmap(recipe -> valid(type, recipe), recipe -> valid(type, recipe));
    }

    static <R extends RecyclerRecipe> StreamCodec<RegistryFriendlyByteBuf, R> streamCodec(BiFunction<SizedIngredient, List<ItemStackTemplate>, R> make) {
        return StreamCodec.composite(
                SizedIngredient.STREAM_CODEC, RecyclerRecipe::ingredient,
                ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), RecyclerRecipe::results,
                make);
    }

    private static <R extends RecyclerRecipe> DataResult<R> valid(String type, R recipe) {
        if (recipe.results().isEmpty()) {
            return DataResult.error(() -> "a " + type + " recipe has no results");
        }
        var items = new HashSet<>();
        for (ItemStackTemplate result : recipe.results()) {
            if (!items.add(result.item())) {
                return DataResult.error(() -> "a " + type + " recipe names " + result.item().getRegisteredName() + " twice");
            }
        }
        return DataResult.success(recipe);
    }
}
