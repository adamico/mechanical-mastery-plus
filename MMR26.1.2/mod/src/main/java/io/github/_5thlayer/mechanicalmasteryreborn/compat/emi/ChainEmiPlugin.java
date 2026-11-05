// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

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
import io.github._5thlayer.mechanicalmasteryreborn.challenge.QuintessenceRung;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

/**
 * Marks every recipe a team's locked chain keeps from running (ADR 0007, #246), naming the challenge that unlocks it:
 * each of a chain's own recipes, by its id in the {@link ChainMachines.Table} the server sent, and each recipe whose
 * category's workstations include a chain machine, Theurgy's pages that EMI shows through JEI's plugins among them.
 * It marks the last chapter's recipes too, such as the End Portal Frame's, until the team has won all four (ADR 0012,
 * #265), and the last stand's until the team has started it (#262): its stage file's, such as the Quartessence's, and
 * the Mutation Chamber's step from a Quartessence to Quintessence. The Void Crucible's step, which the Crucible never
 * takes ({@link QuintessenceRung}), is removed. The mark is asked at every frame, so it goes as soon as the team starts
 * the challenge, reaches the chapter or starts the last stand.
 *
 * <p>EMI finds this by its annotation and loads it only when EMI is installed, as {@link RecyclerEmiPlugin}.
 */
@EmiEntrypoint
public final class ChainEmiPlugin implements EmiPlugin {

    /** The mark, a warning sign in the corner of the recipe. */
    private static final Component MARK = Component.literal("⚠").withStyle(ChatFormatting.GOLD);
    /** What the mark on a last chapter's recipe says. */
    private static final Component LAST_CHAPTER = Component.translatable("mechanicalmasteryreborn.last_chapter.unlocked_by");
    /** What the mark on a last stand's recipe says. */
    private static final Component LAST_STAND = Component.translatable("mechanicalmasteryreborn.last_stand.unlocked_by");
    /**
     * The paths of Voidworks' EMI categories of its upgrades, a Mutation Chamber's and a Void Crucible's: by path alone,
     * since the mod ships no other Module's namespace (libworks ADR 0013), and only among recipes that take a Quartessence.
     */
    private static final String CHAMBER = "mutation_chamber";
    private static final String CRUCIBLE = "void_crucible";

    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipeDecorator(ChainEmiPlugin::decorate);
        registry.removeRecipes(recipe -> recipe.getCategory().getId().getPath().equals(CRUCIBLE) && climbsQuartessence(recipe));
    }

    private static void decorate(EmiRecipe recipe, WidgetHolder widgets) {
        var chain = chainOf(recipe);
        if (chain.isPresent()) {
            Handicap handicap = chain.get();
            widgets.add(new Mark(() -> ChainMachines.Table.received().locked(handicap), ChainMachines.line(handicap), widgets.getWidth()));
        } else if (recipe.getId() != null && ChainMachines.Table.received().lastChapter().contains(recipe.getId())) {
            widgets.add(new Mark(() -> ChainMachines.Table.received().lastChapterLocked(), LAST_CHAPTER, widgets.getWidth()));
        } else if (recipe.getId() != null && ChainMachines.Table.received().lastStand().contains(recipe.getId())
                || recipe.getCategory().getId().getPath().equals(CHAMBER) && climbsQuartessence(recipe)) {
            widgets.add(new Mark(() -> ChainMachines.Table.received().lastStandLocked(), LAST_STAND, widgets.getWidth()));
        }
    }

    /** Whether {@code recipe} takes a Quartessence, as Quintessence's Rung does. */
    private static boolean climbsQuartessence(EmiRecipe recipe) {
        return recipe.getInputs().stream().flatMap(input -> input.getEmiStacks().stream())
                .anyMatch(stack -> !stack.getItemStack().isEmpty() && QuintessenceRung.climbs(stack.getItemStack().getItem()));
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

    /** The mark on a locked recipe, drawn, with its tooltip {@code line}, only while {@code locked} holds. */
    private static final class Mark extends Widget {

        private static final int SIZE = 9;

        private final BooleanSupplier locked;
        private final Component line;
        private final Bounds bounds;

        Mark(BooleanSupplier locked, Component line, int width) {
            this.locked = locked;
            this.line = line;
            this.bounds = new Bounds(width - SIZE, 0, SIZE, SIZE);
        }

        private boolean shown() {
            return locked.getAsBoolean();
        }

        @Override
        public Bounds getBounds() {
            return shown() ? bounds : Bounds.EMPTY;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            if (shown()) {
                graphics.text(Minecraft.getInstance().font, MARK, bounds.x() + 1, bounds.y() + 1, 0xFFFFFFFF, true);
            }
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            if (!shown()) {
                return List.of();
            }
            return List.of(ClientTooltipComponent.create(line.copy()
                    .withStyle(ChatFormatting.GOLD).getVisualOrderText()));
        }
    }
}
