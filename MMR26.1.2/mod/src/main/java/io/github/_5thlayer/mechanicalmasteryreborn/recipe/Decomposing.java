// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The {@code mechanicalmasteryreborn:decomposing} recipe type and its serializer (see {@link DecomposingRecipe}),
 * and its recipes sent to every client: a client is sent only the types asked for, and EMI on a dedicated
 * server's client has nothing else to draw them from.
 */
public final class Decomposing {

    private static final String NAME = "decomposing";

    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MechanicalMasteryReborn.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DecomposingRecipe>> TYPE =
            TYPES.register(NAME, () -> RecipeType.simple(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecomposingRecipe>> SERIALIZER =
            SERIALIZERS.register(NAME, DecomposingRecipe::serializer);

    private Decomposing() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        SERIALIZERS.register(modBus);
        NeoForge.EVENT_BUS.addListener(Decomposing::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(MixesCommand::register);
    }

    /** The recipe {@code stack} decomposes by, if any, whatever its count. */
    public static Optional<RecipeHolder<DecomposingRecipe>> recipeFor(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return level.recipeAccess().getRecipeFor(TYPE.get(), new SingleRecipeInput(stack), level);
    }

    /**
     * Every item of the pack's mixes that recycles into an item no mix holds, and those items (the line's glossary:
     * Mix): none when the mixes are closed.
     */
    public static Map<Item, List<Item>> unclosed(MinecraftServer server) {
        List<List<Item>> mixes = server.getRecipeManager().recipeMap().byType(TYPE.get()).stream()
                .map(holder -> holder.value().results().stream().map(result -> result.item().value()).toList()).toList();
        return RecyclingRules.unclosed(mixes, item -> Recycling.recycle(server, item));
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(TYPE.get());
    }
}
