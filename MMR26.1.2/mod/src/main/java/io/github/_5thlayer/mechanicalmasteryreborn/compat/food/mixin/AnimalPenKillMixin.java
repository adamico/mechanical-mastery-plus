// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.PenFeed;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * What an animal a player kills in an Animal Pen drops, its meat among it, rolls its quality by the feed the pen last gave
 * its animals ({@link PenFeed#roll}, #365): each drop, as {@code attackThePen}'s loot passes it to the pen's output. A pen
 * the kill emptied has dropped its cage already, so its last drops roll at Quality Food's own odds. {@link Pseudo}, so
 * without Animal Pen it never applies.
 */
@Pseudo
@Mixin(targets = "lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity")
public abstract class AnimalPenKillMixin {

    @Inject(method = "lambda$attackThePen$1(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void mechanicalmasteryreborn$quality(Level level, ItemStack drop, CallbackInfo callback) {
        PenFeed.roll(((AnimalPenCage) this).mechanicalmasteryreborn$cage(), drop, level.getRandom(), level.registryAccess());
    }
}
