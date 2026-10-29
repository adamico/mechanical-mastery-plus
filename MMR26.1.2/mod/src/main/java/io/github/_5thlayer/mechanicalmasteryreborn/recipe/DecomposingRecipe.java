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
 * A {@code mechanicalmasteryreborn:decomposing} recipe: what one essence decomposes into, by hand or on a Recycler
 * (the line's glossary: Decompose, Mix), as a {@link RecyclerRecipe}: one ingredient with a count, and its mix at full
 * amounts, of which a decompose returns its {@link Yield}. The mod ships none; the pack writes them.
 */
public record DecomposingRecipe(SizedIngredient ingredient, List<ItemStackTemplate> results) implements RecyclerRecipe {

    public DecomposingRecipe {
        results = List.copyOf(results);
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

    private static final MapCodec<DecomposingRecipe> CODEC = RecyclerRecipe.codec("decomposing", DecomposingRecipe::new);

    private static final StreamCodec<RegistryFriendlyByteBuf, DecomposingRecipe> STREAM_CODEC = RecyclerRecipe.streamCodec(DecomposingRecipe::new);
}
