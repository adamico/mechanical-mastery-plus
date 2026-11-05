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
 * The money spell's risen prices (#263): what AdminShop takes from a team for an item or a fluid bought in the Shop by
 * hand, passes through {@link MoneyHandicap#purchase} first. AdminShop has no hook of its own. {@link Pseudo}, so without
 * AdminShop, whose class it targets by name, it never applies. {@link BuyerPurchaseMixin} raises a Buyer's.
 */
@Pseudo
@Mixin(targets = "com.ammonium.adminshop.network.PacketBuyRequest")
public abstract class PurchaseMixin {

    @ModifyArg(method = {"buyItemTransaction", "buyFluidTransaction"},
            at = @At(value = "INVOKE", target = "Lcom/ammonium/adminshop/money/MoneyHelper;removeMoney(Ljava/util/UUID;J)Z"),
            index = 1)
    private long mechanicalmasteryreborn$purchase(UUID account, long price) {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? price : MoneyHandicap.purchase(server, account, price);
    }
}
