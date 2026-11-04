// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

/**
 * Marks every recipe a team's locked chain keeps from running (ADR 0007, #246), naming the challenge that unlocks it:
 * each of a chain's own recipes, by its id in the {@link ChainMachines.Table} the server sent, and each recipe whose
 * category's workstations include a chain machine, Theurgy's pages that EMI shows through JEI's plugins among them.
 * The mark is asked at every frame, so it goes as soon as the team starts the challenge.
 *
 * <p>EMI finds this by its annotation and loads it only when EMI is installed, as {@link RecyclerEmiPlugin}.
 */
@EmiEntrypoint
public final class ChainEmiPlugin implements EmiPlugin {

    /** The mark, a warning sign in the corner of the recipe. */
    private static final Component MARK = Component.literal("⚠").withStyle(ChatFormatting.GOLD);

    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipeDecorator(ChainEmiPlugin::decorate);
    }

    private static void decorate(EmiRecipe recipe, WidgetHolder widgets) {
        chainOf(recipe).ifPresent(handicap -> widgets.add(new Mark(handicap, widgets.getWidth())));
    }

    /** The chain whose challenge {@code recipe} waits for, locked or not: the team's progress is asked as it draws. */
    static Optional<Handicap> chainOf(EmiRecipe recipe) {
        if (recipe.getId() != null) {
            Handicap own = ChainMachines.Table.received().recipes().get(recipe.getId());
            if (own != null) {
                return Optional.of(own);
            }
        }
        List<EmiIngredient> workstations = EmiApi.getRecipeManager().getWorkstations(recipe.getCategory());
        for (EmiIngredient workstation : workstations) {
            for (EmiStack stack : workstation.getEmiStacks()) {
                var machine = stack.getItemStack();
                if (!machine.isEmpty()) {
                    var chain = ChainMachines.chainOf(machine.getItem());
                    if (chain.isPresent()) {
                        return chain;
                    }
                }
            }
        }
        return Optional.empty();
    }

    /** The mark on a recipe of {@code handicap}'s chain, drawn, with its tooltip, only while the team hasn't started it. */
    private static final class Mark extends Widget {

        private static final int SIZE = 9;

        private final Handicap handicap;
        private final Bounds bounds;

        Mark(Handicap handicap, int width) {
            this.handicap = handicap;
            this.bounds = new Bounds(width - SIZE, 0, SIZE, SIZE);
        }

        private boolean shown() {
            return ChainMachines.Table.received().locked(handicap);
        }

        @Override
        public Bounds getBounds() {
            return shown() ? bounds : Bounds.EMPTY;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            if (shown()) {
                graphics.text(Minecraft.getInstance().font, MARK, bounds.x() + 1, bounds.y() + 1, 0xFFFFAA00, true);
            }
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            if (!shown()) {
                return List.of();
            }
            return List.of(ClientTooltipComponent.create(ChainMachines.line(handicap).copy()
                    .withStyle(ChatFormatting.GOLD).getVisualOrderText()));
        }
    }
}
