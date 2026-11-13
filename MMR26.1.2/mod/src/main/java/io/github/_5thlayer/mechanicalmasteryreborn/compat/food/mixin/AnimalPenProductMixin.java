// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.PenFeed;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * What an Animal Pen's animals give, eggs, milk, wool and every other product of its interactions, rolls its quality by
 * the feed the pen at {@code pos} last gave them ({@link PenFeed#roll}, #365), whether a player, a dispenser or the pen's
 * own timer takes it. {@link Pseudo}, so without Animal Pen it never applies.
 */
@Pseudo
@Mixin(targets = "lv.id.bonne.animalpen.interaction.loot.LootEntry")
public abstract class AnimalPenProductMixin {

    @ModifyReturnValue(method = "processLootTable", at = @At("RETURN"))
    private List<ItemStack> mechanicalmasteryreborn$quality(List<ItemStack> products, ServerLevel level, Mob animal, BlockPos pos,
                                                         long animals, int consumed) {
        ItemStack cage = level.getBlockEntity(pos) instanceof AnimalPenCage pen ? pen.mechanicalmasteryreborn$cage() : ItemStack.EMPTY;
        for (ItemStack product : products) {
            PenFeed.roll(cage, product, level.getRandom(), level.registryAccess());
        }
        return products;
    }
}
