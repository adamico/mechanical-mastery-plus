// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Decomposing;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.resources.Identifier;

/**
 * Puts {@code mechanicalmasteryreborn:decomposing} in EMI, in one tab whose workstation is the Recycler's core.
 * EMI has never heard of the type, so without a category its recipes are in no viewer at all.
 *
 * <p>EMI finds this by its annotation and loads it only when EMI is installed; nothing else in the mod names
 * an EMI type, so the mod loads without it.
 */
@EmiEntrypoint
public final class DecomposingEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        var category = new EmiRecipeCategory(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "decomposing"),
                EmiStack.of(Recycler.CORE_ITEM.get()));
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(Recycler.CORE_ITEM.get()));
        for (var holder : registry.getRecipeMap().byType(Decomposing.TYPE.get())) {
            registry.addRecipe(new DecomposingEmiRecipe(category, holder));
        }
    }
}
