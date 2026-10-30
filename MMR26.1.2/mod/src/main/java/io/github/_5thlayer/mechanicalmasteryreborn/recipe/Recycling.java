// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Kind;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Made;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Share;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

/**
 * Recycling (the line's glossary: Recycle): the {@code mechanicalmasteryreborn:recycling} recipe type the pack
 * overrides with, and what any item recycles into at full cost, derived from the recipes that make it run
 * backwards by {@link RecyclingRules}. The mod derives it, since the pack could never write one for every item
 * (ADR 0003, amended for #184).
 *
 * <p>Smelting, the assembling types the config names ({@link ServerConfig#assemblingTypes}, Craftworks' in the
 * pack), vanilla crafting, and blasting, smoking and campfire cooking are read. An assembling recipe is read through
 * its own codec, so the mod names no suite mod and still plays alone. A tag ingredient returns its first item, and an
 * ore ({@code #c:ores}) loses to anything else. The recipes are read again after a reload or a config change.
 */
public final class Recycling {

    private static final String NAME = "recycling";

    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MechanicalMasteryReborn.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<RecyclingRecipe>> TYPE =
            TYPES.register(NAME, () -> RecipeType.simple(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecyclingRecipe>> SERIALIZER =
            SERIALIZERS.register(NAME, RecyclingRecipe::serializer);

    /** The recipes last read, and the server's recipes they were read from: a reload makes a new map. */
    private static @Nullable Index index;

    private Recycling() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        SERIALIZERS.register(modBus);
        // The client's handler is the client's own (MechanicalMasteryRebornClient), so a dedicated server never loads it.
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1")
                .playToClient(RecyclingTable.TYPE, RecyclingTable.STREAM_CODEC));
        NeoForge.EVENT_BUS.addListener(Recycling::onDatapackSync);
    }

    /** Sends each player what every item recycles into, before the recipes, so EMI has it when they arrive. */
    private static void onDatapackSync(OnDatapackSyncEvent event) {
        var table = new RecyclingTable(byRecipe(event.getPlayerList().getServer()));
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, table));
    }

    /**
     * Whether a Recycler recycles {@code stack}: anything but an essence, and but an item that holds other items,
     * whose contents would be lost.
     */
    public static boolean recyclable(ItemStack stack) {
        if (stack.isEmpty() || Essences.of(stack.getItem()).isPresent()) {
            return false;
        }
        var container = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        var bundle = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return container.nonEmptyItemCopyStream().findAny().isEmpty() && bundle.isEmpty();
    }

    /** What one {@code item} recycles into at full cost, before yield, each result once, in order. */
    public static List<Share<Item>> recyclesInto(MinecraftServer server, Item item) {
        return indexOf(server).recyclesInto(item);
    }

    /** Every item a recipe makes or an override names, but an essence, and what it recycles into: what EMI shows. */
    public static Map<Item, List<Share<Item>>> byRecipe(MinecraftServer server) {
        Index read = indexOf(server);
        var all = new HashMap<Item, List<Share<Item>>>();
        for (Item item : read.items()) {
            List<Share<Item>> into = read.recyclesInto(item);
            if (Essences.of(item).isEmpty() && !(into.size() == 1 && into.getFirst().item() == item)) {
                all.put(item, into);
            }
        }
        return all;
    }

    private static Index indexOf(MinecraftServer server) {
        RecipeMap recipes = server.getRecipeManager().recipeMap();
        List<Identifier> assembling = ServerConfig.assemblingTypes();
        Index read = index;
        if (read == null || read.recipes() != recipes || !read.assembling().equals(assembling)) {
            read = Index.read(recipes, assembling, server.registryAccess().createSerializationContext(JsonOps.INSTANCE));
            index = read;
        }
        return read;
    }

    private record Index(RecipeMap recipes, List<Identifier> assembling, Map<Item, List<Made<Item>>> makers,
            Map<Item, List<Share<Item>>> overrides, Map<Item, List<Share<Item>>> into) {

        static Index read(RecipeMap recipes, List<Identifier> assembling, DynamicOps<JsonElement> ops) {
            var makers = new HashMap<Item, List<Made<Item>>>();
            for (RecipeHolder<?> holder : recipes.values()) {
                Kind kind = kindOf(holder.value().getType(), assembling);
                if (kind == null || holder.value().isSpecial()) {
                    continue;
                }
                Recycling.read(holder, kind, ops).ifPresent(made -> makers.computeIfAbsent(made.result(), key -> new ArrayList<>()).add(made.made()));
            }
            var overrides = new HashMap<Item, List<Share<Item>>>();
            for (RecipeHolder<RecyclingRecipe> holder : recipes.byType(TYPE.get())) {
                RecyclingRecipe recipe = holder.value();
                int count = recipe.ingredient().count();
                List<Share<Item>> cost = recipe.results().stream()
                        .map(result -> new Share<>(result.item().value(), (double) result.count() / count)).toList();
                recipe.ingredient().ingredient().items().forEach(item -> overrides.putIfAbsent(item.value(), cost));
            }
            return new Index(recipes, assembling, makers, overrides, new HashMap<>());
        }

        List<Item> items() {
            var items = new ArrayList<>(makers.keySet());
            overrides.keySet().stream().filter(item -> !makers.containsKey(item)).forEach(items::add);
            return items;
        }

        List<Share<Item>> recyclesInto(Item item) {
            return into.computeIfAbsent(item, key -> RecyclingRules.recyclesInto(key, Optional.ofNullable(overrides.get(key)),
                    made -> makers.getOrDefault(made, List.of()), Recycling::ore));
        }
    }

    private record Read(Item result, Made<Item> made) {
    }

    private static boolean ore(Item item) {
        return BuiltInRegistries.ITEM.wrapAsHolder(item).is(Tags.Items.ORES);
    }

    private static @Nullable Kind kindOf(RecipeType<?> type, List<Identifier> assembling) {
        if (type == RecipeType.SMELTING) {
            return Kind.SMELTING;
        }
        if (type == RecipeType.BLASTING || type == RecipeType.SMOKING || type == RecipeType.CAMPFIRE_COOKING) {
            return Kind.OTHER_COOKING;
        }
        if (type == RecipeType.CRAFTING) {
            return Kind.CRAFTING;
        }
        if (assembling.contains(BuiltInRegistries.RECIPE_TYPE.getKey(type))) {
            return Kind.ASSEMBLING;
        }
        return null;
    }

    /**
     * A recipe as the rules read it: what it makes, from its {@code result}, or the first of its {@code results}
     * for an assembling recipe, and its ingredients, one per slot, or sized for an assembling recipe. Read off the
     * recipe's own codec, since vanilla keeps a cooking or crafting result to itself and an assembling type is known
     * only by its config id. Empty when a part can't be read, or an ingredient names no item, or when it cooks a tool
     * or armour down: that is recycling already, and run backwards it would recycle a nugget up into a pickaxe.
     */
    private static Optional<Read> read(RecipeHolder<?> holder, Kind kind, DynamicOps<JsonElement> ops) {
        JsonObject json;
        try {
            json = encode(holder.value(), ops).getAsJsonObject();
        } catch (RuntimeException unreadable) {
            return Optional.empty();
        }
        Optional<ItemStackTemplate> product = json.has("result") ? decode(ItemStackTemplate.CODEC, json.get("result"), ops)
                : json.has("results") && json.get("results").isJsonArray() && !json.getAsJsonArray("results").isEmpty()
                        ? decode(ItemStackTemplate.CODEC, json.getAsJsonArray("results").get(0), ops) : Optional.empty();
        if (product.isEmpty()) {
            return Optional.empty();
        }
        List<SizedIngredient> ingredients = kind == Kind.ASSEMBLING
                ? json.has("ingredients") ? decode(SizedIngredient.NESTED_CODEC.listOf(), json.get("ingredients"), ops).orElse(List.of()) : List.of()
                : holder.value().placementInfo().ingredients().stream().map(ingredient -> new SizedIngredient(ingredient, 1)).toList();
        boolean cooking = kind == Kind.SMELTING || kind == Kind.OTHER_COOKING;
        if (cooking && ingredients.stream().anyMatch(ingredient -> ingredient.ingredient().items().anyMatch(Recycling::worn))) {
            return Optional.empty();
        }
        var shares = new ArrayList<Share<Item>>();
        for (SizedIngredient ingredient : ingredients) {
            Optional<Item> first = firstItem(ingredient.ingredient());
            if (first.isEmpty()) {
                return Optional.empty();
            }
            shares.add(new Share<>(first.get(), ingredient.count()));
        }
        if (shares.isEmpty()) {
            return Optional.empty();
        }
        ItemStackTemplate made = product.get();
        return Optional.of(new Read(made.item().value(),
                new Made<>(kind, holder.id().identifier().toString(), shares, made.count())));
    }

    /** Whether {@code item} wears out or is worn: a tool, a weapon, or armour. */
    private static boolean worn(Holder<Item> item) {
        var components = item.value().components();
        return components.has(DataComponents.MAX_DAMAGE) || components.has(DataComponents.EQUIPPABLE);
    }

    private static Optional<Item> firstItem(Ingredient ingredient) {
        return ingredient.items().findFirst().map(Holder::value);
    }

    @SuppressWarnings("unchecked")
    private static <R extends Recipe<?>> JsonElement encode(R recipe, DynamicOps<JsonElement> ops) {
        var serializer = (RecipeSerializer<R>) recipe.getSerializer();
        return serializer.codec().codec().encodeStart(ops, recipe).getOrThrow();
    }

    private static <T> Optional<T> decode(Codec<T> codec, JsonElement json, DynamicOps<JsonElement> ops) {
        return codec.parse(ops, json).result();
    }
}
