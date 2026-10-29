// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.HashSet;
import java.util.List;

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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * A {@code mechanicalmasteryreborn:decomposing} recipe: what one item decomposes into, by hand or on a
 * Recycler (the line's glossary: Decompose, Mix). One ingredient with a count, and its mix: up to
 * {@link #MAX_RESULTS} results in a fixed order, each item once, so a Recycler puts result <i>i</i> on its
 * <i>i</i>th output position. No fluids and no chances. The mod ships none; the pack writes them.
 *
 * <p>A result is a template, not a stack: {@code ItemStack.CODEC} refuses an item whose components are not
 * bound yet, which they are not during the datapack load that reads recipes.
 */
public record DecomposingRecipe(SizedIngredient ingredient, List<ItemStackTemplate> results) implements Recipe<SingleRecipeInput> {

    /** As many as a Recycler's eight places can ever show: three outputs of three positions. */
    public static final int MAX_RESULTS = 9;

    public DecomposingRecipe {
        results = List.copyOf(results);
    }

    /** Whether {@code input} is this recipe's item, whatever its count: a Recycler says when it holds too few. */
    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.ingredient().test(input.item());
    }

    /** The first result; a decompose gives them all, through {@link #mix}. */
    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return results.getFirst().create();
    }

    /** Every result as a new stack, in order. */
    public List<ItemStack> mix() {
        return results.stream().map(ItemStackTemplate::create).toList();
    }

    /** Nothing places one, but vanilla warns at every load of a recipe that is neither placeable nor special. */
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredient.ingredient());
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<DecomposingRecipe> getSerializer() {
        return Decomposing.SERIALIZER.get();
    }

    @Override
    public RecipeType<DecomposingRecipe> getType() {
        return Decomposing.TYPE.get();
    }

    static RecipeSerializer<DecomposingRecipe> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    /** A mix has one to nine results, and names each item once. */
    private static DataResult<DecomposingRecipe> valid(DecomposingRecipe recipe) {
        int count = recipe.results().size();
        if (count == 0 || count > MAX_RESULTS) {
            return DataResult.error(() -> "a decomposing recipe has 1 to " + MAX_RESULTS + " results, not " + count);
        }
        var items = new HashSet<>();
        for (ItemStackTemplate result : recipe.results()) {
            if (!items.add(result.item())) {
                return DataResult.error(() -> "a decomposing recipe names " + result.item().getRegisteredName() + " twice");
            }
        }
        return DataResult.success(recipe);
    }

    private static final MapCodec<DecomposingRecipe> CODEC = RecordCodecBuilder.<DecomposingRecipe>mapCodec(
            instance -> instance.group(
                    SizedIngredient.NESTED_CODEC.fieldOf("ingredient").forGetter(DecomposingRecipe::ingredient),
                    ItemStackTemplate.CODEC.listOf().fieldOf("results").forGetter(DecomposingRecipe::results))
                    .apply(instance, DecomposingRecipe::new))
            .flatXmap(DecomposingRecipe::valid, DecomposingRecipe::valid);

    private static final StreamCodec<RegistryFriendlyByteBuf, DecomposingRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC, DecomposingRecipe::ingredient,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_RESULTS)), DecomposingRecipe::results,
            DecomposingRecipe::new);
}
