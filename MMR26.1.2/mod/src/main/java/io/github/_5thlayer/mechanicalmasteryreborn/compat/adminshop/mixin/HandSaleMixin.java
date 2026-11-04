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
 * The money handicap's halved sales by hand (#213): what AdminShop credits a team for an item or a fluid sold in the
 * Shop passes through {@link MoneyHandicap#sale} first. {@link Pseudo}, as {@link SellerSaleMixin}, which halves a
 * Seller's.
 */
@Pseudo
@Mixin(targets = "com.ammonium.adminshop.network.PacketSellRequest")
public abstract class HandSaleMixin {

    @ModifyArg(method = {"sellItem", "sellFluid"},
            at = @At(value = "INVOKE", target = "Lcom/ammonium/adminshop/money/MoneyHelper;addMoney(Ljava/util/UUID;J)V"),
            index = 1)
    private long mechanicalmasteryreborn$sale(UUID account, long price) {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? price : MoneyHandicap.sale(server, account, price);
    }
}
