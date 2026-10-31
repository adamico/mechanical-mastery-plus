// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import java.util.List;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * One decompose or recycle as EMI draws it: the ingredient, with its count, an arrow, and the results in rows of
 * three, as a Recycler with an output for each row lays them out on its empty outputs. With more kinds than one output
 * holds, the arrow's tooltip says that a Recycler with fewer positions than kinds returns only some of them.
 */
final class RecyclerEmiRecipe extends BasicEmiRecipe {

    private static final int SLOT = 18;
    private static final int ARROW = 24;
    private static final int COLUMNS = RecyclerLayout.POSITIONS_PER_OUTPUT;

    RecyclerEmiRecipe(EmiRecipeCategory category, Identifier id, EmiIngredient input, List<EmiStack> results) {
        super(category, id, 0, 0);
        inputs.add(input);
        outputs.addAll(results);
        int rows = (outputs.size() + COLUMNS - 1) / COLUMNS;
        this.width = SLOT + 4 + ARROW + 4 + COLUMNS * SLOT;
        this.height = Math.max(SLOT, rows * SLOT);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int middle = (height - SLOT) / 2;
        widgets.addSlot(inputs.getFirst(), 0, middle);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, SLOT + 4, middle + 1);
        if (outputs.size() > COLUMNS) {
            widgets.addTooltipText(List.of(Component.translatable("emi.mechanicalmasteryreborn.recycler.short_on_outputs", outputs.size())),
                    SLOT + 4, middle + 1, ARROW, SLOT - 1);
        }
        int left = SLOT + 4 + ARROW + 4;
        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), left + (i % COLUMNS) * SLOT, (i / COLUMNS) * SLOT).recipeContext(this);
        }
    }
}
