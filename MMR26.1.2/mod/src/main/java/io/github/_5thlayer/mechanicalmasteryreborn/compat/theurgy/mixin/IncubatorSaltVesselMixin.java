// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.GalvanicParts;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the Incubator's salt vessel take the Salt Galvanic Charge, which the Galvanic Core's incubation takes (#331): the
 * vessel takes only items tagged as Theurgy's salts, and tagging the charge so would let it stand for one in Theurgy's
 * crafting recipes too. {@link Pseudo}, as the crafting mixins are.
 */
@Pseudo
@Mixin(targets = "com.klikli_dev.theurgy.content.apparatus.incubator.IncubatorSaltVesselBlockEntity$InputInventory")
public abstract class IncubatorSaltVesselMixin {

    @Inject(method = "isValid(ILnet/neoforged/neoforge/transfer/item/ItemResource;)Z", at = @At("RETURN"), cancellable = true)
    private void mechanicalmasteryreborn$charge(int slot, ItemResource resource, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() && resource.is(GalvanicParts.SALT.get())) {
            callback.setReturnValue(true);
        }
    }
}
