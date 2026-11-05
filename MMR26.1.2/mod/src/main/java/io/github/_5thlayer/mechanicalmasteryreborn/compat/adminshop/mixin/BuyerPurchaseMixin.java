// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.adminshop.mixin;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.MoneyHandicap;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * The money spell's risen prices (#263): what AdminShop takes from a team for an item or a fluid bought from the Shop by
 * a Buyer, passes through {@link MoneyHandicap#purchase} first. AdminShop has no hook of its own. {@link Pseudo}, so without
 * AdminShop, whose classes it targets by name, it never applies. {@link PurchaseMixin} raises a purchase by hand.
 */
@Pseudo
@Mixin(targets = {"com.ammonium.adminshop.recipes.BuyItemRecipe", "com.ammonium.adminshop.recipes.BuyFluidRecipe"})
public abstract class BuyerPurchaseMixin {

    @ModifyArg(method = "buy",
            at = @At(value = "INVOKE", target = "Lcom/ammonium/adminshop/money/MoneyHelper;removeMoney(Ljava/util/UUID;J)Z"),
            index = 1)
    private long mechanicalmasteryreborn$purchase(UUID account, long price) {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? price : MoneyHandicap.purchase(server, account, price);
    }
}
