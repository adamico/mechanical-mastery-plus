// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.emi;

import java.util.List;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.life.Pelican;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * An info page on the Vital Essence and the Pelican (#371): the Vital Essence has no recipe, since the Pelican makes it
 * by eating meals, so the page says how, with no numbers, which are the server's config. EMI finds this by its annotation
 * and loads it only when EMI is installed, as {@link RecyclerEmiPlugin}.
 */
@EmiEntrypoint
public final class PelicanEmiPlugin implements EmiPlugin {

    /** EMI's synthetic ids start their path with a slash. */
    private static final Identifier ID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "/info/pelican");

    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipe(new EmiInfoRecipe(
                List.of(EmiStack.of(Essences.item(Essence.VITAL)), EmiStack.of(Pelican.ITEM.get())),
                List.of(Component.translatable("mechanicalmasteryreborn.emi.info.pelican.made"),
                        Component.translatable("mechanicalmasteryreborn.emi.info.pelican.batch"),
                        Component.translatable("mechanicalmasteryreborn.emi.info.pelican.lost")),
                ID));
    }
}
