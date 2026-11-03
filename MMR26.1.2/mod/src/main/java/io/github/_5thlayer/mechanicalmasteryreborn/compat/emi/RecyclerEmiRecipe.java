// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import java.util.List;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * One decompose or recycle as EMI draws it: the ingredient, with its count, an arrow, and the results in rows of
 * three, as a Recycler with an output for each row lays them out on its empty outputs. With more kinds than one output
 * holds, the arrow's tooltip says that a Recycler with fewer positions than the mix's kinds returns only some of
 * them. One page of a mix split across several shows its number under the arrow.
 */
final class RecyclerEmiRecipe extends BasicEmiRecipe {

    private static final int SLOT = 18;
    private static final int ARROW = 24;
    /** The page number's height under the arrow; a page of one row grows by this much above and below, to fit it with a margin. */
    private static final int LABEL = 10;
    private static final int ARROW_X = SLOT + 4;
    private static final int COLUMNS = RecyclerLayout.POSITIONS_PER_OUTPUT;

    private final int page;
    private final int pages;
    private final int kinds;

    RecyclerEmiRecipe(EmiRecipeCategory category, Identifier id, EmiIngredient input, List<EmiStack> results, int page, int pages, int kinds) {
        super(category, id, 0, 0);
        this.page = page;
        this.pages = pages;
        this.kinds = kinds;
        inputs.add(input);
        outputs.addAll(results);
        int rows = (outputs.size() + COLUMNS - 1) / COLUMNS;
        this.width = ARROW_X + ARROW + 4 + COLUMNS * SLOT;
        this.height = Math.max(pages > 1 ? SLOT + 2 * LABEL + 2 : SLOT, rows * SLOT);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int middle = (height - SLOT) / 2;
        widgets.addSlot(inputs.getFirst(), 0, middle);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, middle + 1);
        if (kinds > COLUMNS) {
            widgets.addTooltipText(List.of(Component.translatable("emi.mechanicalmasteryreborn.recycler.short_on_outputs", kinds)),
                    ARROW_X, middle + 1, ARROW, SLOT - 1);
        }
        if (pages > 1) {
            widgets.addText(Component.translatable("emi.mechanicalmasteryreborn.recycler.page", page, pages),
                    ARROW_X + ARROW / 2, middle + SLOT + 1, 0xFF404040, false).horizontalAlign(TextWidget.Alignment.CENTER);
        }
        int left = ARROW_X + ARROW + 4;
        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), left + (i % COLUMNS) * SLOT, (i / COLUMNS) * SLOT).recipeContext(this);
        }
    }
}
