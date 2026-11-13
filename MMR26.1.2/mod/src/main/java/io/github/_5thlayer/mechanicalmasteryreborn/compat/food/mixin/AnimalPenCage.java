// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.PenFeed;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * An Animal Pen's cage, the item its animals are kept in, on which {@link PenFeed} keeps their feed quality.
 * {@link Pseudo}, so without Animal Pen it never applies.
 */
@Pseudo
@Mixin(targets = "lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity")
public interface AnimalPenCage {

    @Invoker("getItemStack")
    ItemStack mechanicalmasteryreborn$cage();
}
