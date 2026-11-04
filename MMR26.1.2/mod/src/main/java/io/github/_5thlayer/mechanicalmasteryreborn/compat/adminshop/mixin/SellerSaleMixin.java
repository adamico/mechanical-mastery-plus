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
 * The money handicap's halved sales by a Seller (#213): what AdminShop credits a team when a Seller sells an item or
 * a fluid passes through {@link MoneyHandicap#sale} first. AdminShop has no hook of its own. {@link Pseudo}, so without
 * AdminShop, whose classes it targets by name, it never applies. {@link HandSaleMixin} halves sales by hand.
 */
@Pseudo
@Mixin(targets = {"com.ammonium.adminshop.recipes.SellItemRecipe", "com.ammonium.adminshop.recipes.SellFluidRecipe"})
public abstract class SellerSaleMixin {

    @ModifyArg(method = "sell",
            at = @At(value = "INVOKE", target = "Lcom/ammonium/adminshop/money/MoneyHelper;addMoney(Ljava/util/UUID;J)V"),
            index = 1)
    private long mechanicalmasteryreborn$sale(UUID account, long price) {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? price : MoneyHandicap.sale(server, account, price);
    }
}
