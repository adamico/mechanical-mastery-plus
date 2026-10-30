// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.List;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * A {@code mechanicalmasteryreborn:recycling} recipe, an override: what one item recycles into on a Recycler (the
 * line's glossary: Recycle), in place of what {@link Recycling} derives from the recipes that make it. As a
 * {@link RecyclerRecipe}, its results are the full cost of the ingredient's count, of which a recycle returns its
 * {@link Yield}, one item at a time. The mod ships none; the pack writes them.
 */
public record RecyclingRecipe(SizedIngredient ingredient, List<ItemStackTemplate> results) implements RecyclerRecipe {

    public RecyclingRecipe {
        results = List.copyOf(results);
    }

    @Override
    public RecipeSerializer<RecyclingRecipe> getSerializer() {
        return Recycling.SERIALIZER.get();
    }

    @Override
    public RecipeType<RecyclingRecipe> getType() {
        return Recycling.TYPE.get();
    }

    static RecipeSerializer<RecyclingRecipe> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    private static final MapCodec<RecyclingRecipe> CODEC = RecyclerRecipe.codec("recycling", RecyclingRecipe::new);

    private static final StreamCodec<RegistryFriendlyByteBuf, RecyclingRecipe> STREAM_CODEC = RecyclerRecipe.streamCodec(RecyclingRecipe::new);
}
