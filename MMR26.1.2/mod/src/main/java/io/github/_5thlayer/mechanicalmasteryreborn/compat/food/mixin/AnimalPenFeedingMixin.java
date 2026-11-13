// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.PenFeed;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * An Animal Pen's feeding, by a player or a dispenser, remembers the feed's quality on the cage it feeds (#365), which
 * {@link PenFeed} rolls its products' quality by. {@link Pseudo}, so without Animal Pen it never applies.
 */
@Pseudo
@Mixin(targets = "lv.id.bonne.animalpen.processing.function.core.Feeding")
public abstract class AnimalPenFeedingMixin {

    @Inject(method = "processData", at = @At("HEAD"))
    private void mechanicalmasteryreborn$feed(ServerLevel level, ItemStack feed, int amount, Mob animal, ItemStack cage,
                                             BlockPos pos, CallbackInfo callback) {
        PenFeed.fed(cage, feed);
    }
}
