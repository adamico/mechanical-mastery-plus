// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import java.util.List;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.neoforge.NeoForgeEmiIngredient;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Decomposing;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingTable;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Yield;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Puts the Recycler's work in EMI, in two tabs whose workstation is its core: {@code mechanicalmasteryreborn:decomposing}
 * recipes, and what every item a recipe makes recycles into, from the table the server sent ({@link RecyclingTable}).
 * An item that recycles into itself has no entry. Each result shows its full amount, with the chance the yield,
 * the same on every core, gives of it ({@link ServerConfig}): a full amount of four at a quarter is four at 25%. A mix too long for
 * one page is split across several ({@link RecyclerEmiPages}). EMI has never heard of either, so
 * without a category they are in no viewer at all.
 *
 * <p>EMI finds this by its annotation and loads it only when EMI is installed; nothing else in the mod names
 * an EMI type, so the mod loads without it.
 */
@EmiEntrypoint
public final class RecyclerEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        EmiStack core = EmiStack.of(Recycler.CORE_ITEM.get());
        var decomposing = new EmiRecipeCategory(id("decomposing"), core);
        var recycling = new EmiRecipeCategory(id("recycling"), core);
        registry.addCategory(decomposing);
        registry.addCategory(recycling);
        for (int tier = 1; tier <= Recycler.TIERS; tier++) {
            EmiStack workstation = EmiStack.of(Recycler.coreItem(tier));
            registry.addWorkstation(decomposing, workstation);
            registry.addWorkstation(recycling, workstation);
        }
        double yield = ServerConfig.yield();
        for (var holder : registry.getRecipeMap().byType(Decomposing.TYPE.get())) {
            var recipe = holder.value();
            addPaged(registry, decomposing, holder.id().identifier(), NeoForgeEmiIngredient.of(recipe.ingredient()),
                    recipe.results().stream().map(result -> atYield(result.create(), result.count(), yield)).toList());
        }
        RecyclingTable.received().table().forEach((item, into) -> addPaged(registry, recycling, recyclingId(item),
                EmiStack.of(item), into.stream().map(share -> atYield(new ItemStack(share.item()), share.amount(), yield)).toList()));
    }

    /**
     * One recipe for each page of {@code results} ({@link RecyclerEmiPages}). The first page keeps {@code id}, so a
     * real recipe's page still links to it; each later one is a synthetic id of its own, beside it.
     */
    private static void addPaged(EmiRegistry registry, EmiRecipeCategory category, Identifier id, EmiIngredient input, List<EmiStack> results) {
        var pages = RecyclerEmiPages.split(results);
        for (int i = 0; i < pages.size(); i++) {
            registry.addRecipe(new RecyclerEmiRecipe(category, i == 0 ? id : pageId(id, i + 1), input, pages.get(i), i + 1, pages.size(), results.size()));
        }
    }

    /** A real recipe's id becomes synthetic, under a leading slash as EMI names those; a synthetic one keeps its path. */
    private static Identifier pageId(Identifier id, int page) {
        String path = id.getPath().startsWith("/") ? id.getPath() : "/" + id.getNamespace() + "/" + id.getPath();
        return id(path + "/page_" + page);
    }

    /**
     * {@code stack} at its full {@code amount}, with the chance {@code yield} gives of it: EMI reads the two together
     * as what comes back on average. A share of an item shows as one, at the chance of a share that small.
     */
    private static EmiStack atYield(ItemStack stack, double amount, double yield) {
        int shown = Math.max(1, Yield.most(amount, 1));
        float chance = (float) (amount * yield / shown);
        EmiStack full = EmiStack.of(stack.getItem(), stack.getComponentsPatch(), shown);
        return chance < 1 ? full.setChance(chance) : full;
    }

    private static Identifier recyclingId(Item item) {
        Identifier key = BuiltInRegistries.ITEM.getKey(item);
        return id("/recycling/" + key.getNamespace() + "/" + key.getPath());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, path);
    }
}
