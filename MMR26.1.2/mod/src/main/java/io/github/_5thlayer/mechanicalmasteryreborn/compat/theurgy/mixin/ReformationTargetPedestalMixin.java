// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a Reformation Target Pedestal take Theurgy's salts, which the pack's reformation to Reformed Salt takes as its
 * target (#330): the pedestal takes only Theurgy's sulfurs and niters, which are all Theurgy reforms. {@link Pseudo}, as the
 * crafting mixins are.
 */
@Pseudo
@Mixin(targets = "com.klikli_dev.theurgy.content.apparatus.reformationarray.ReformationTargetPedestalBlockEntity$InputInventory")
public abstract class ReformationTargetPedestalMixin {

    @Unique
    private static final TagKey<Item> mechanicalmasteryreborn$SALTS = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("theurgy", "alchemical_salts"));

    @Inject(method = "isValid(ILnet/neoforged/neoforge/transfer/item/ItemResource;)Z", at = @At("RETURN"), cancellable = true)
    private void mechanicalmasteryreborn$salt(int slot, ItemResource resource, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() && resource.toStack(1).is(mechanicalmasteryreborn$SALTS)) {
            callback.setReturnValue(true);
        }
    }
}
