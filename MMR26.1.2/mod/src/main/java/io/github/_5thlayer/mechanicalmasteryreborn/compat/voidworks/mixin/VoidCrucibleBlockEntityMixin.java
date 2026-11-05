// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.voidworks.mixin;

import java.util.function.Predicate;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.QuintessenceRung;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * A Void Crucible never takes a Quartessence (#262), so it never climbs Quintessence's Rung, which is the Mutation
 * Chamber's alone: the rule its input takes items by refuses one, from a hand or any automation, as it refuses an item
 * with no upgrade. Voidworks has no hook of its own, and the mod compiles against no suite mod, so this names only
 * NeoForge's types. {@link Pseudo}, so without Voidworks, whose class it targets by name, it never applies.
 */
@Pseudo
@Mixin(targets = "io.github._5thlayer.voidworks.mutation.VoidCrucibleBlockEntity")
public abstract class VoidCrucibleBlockEntityMixin {

    @ModifyArg(method = "<init>", index = 1, at = @At(value = "INVOKE",
            target = "Lio/github/_5thlayer/voidworks/mutation/MutationItems;<init>(ILjava/util/function/Predicate;Ljava/lang/Runnable;)V"))
    private static Predicate<ItemResource> mechanicalmasteryreborn$refuseQuartessence(Predicate<ItemResource> accepts) {
        return resource -> accepts.test(resource) && !QuintessenceRung.climbs(resource.getItem());
    }
}
