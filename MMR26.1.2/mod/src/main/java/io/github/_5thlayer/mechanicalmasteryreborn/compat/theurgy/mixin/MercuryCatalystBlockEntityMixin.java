// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.CatalystFuel;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.FueledCatalyst;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives the Mercury Catalyst its FE store, saved with it (#328). {@link Pseudo}, as the crafting mixins are. */
@Pseudo
@Mixin(targets = "com.klikli_dev.theurgy.content.apparatus.mercurycatalyst.MercuryCatalystBlockEntity")
public abstract class MercuryCatalystBlockEntityMixin implements FueledCatalyst {

    @Unique
    private static final String mechanicalmasteryreborn$FUEL_KEY = "mechanicalmasteryreborn_fuel";

    @Unique
    private CatalystFuel mechanicalmasteryreborn$fuel;

    @Override
    public CatalystFuel mechanicalmasteryreborn$fuel() {
        if (mechanicalmasteryreborn$fuel == null) {
            mechanicalmasteryreborn$fuel = new CatalystFuel(((BlockEntity) (Object) this)::setChanged);
        }
        return mechanicalmasteryreborn$fuel;
    }

    @Inject(method = "saveAdditional(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("TAIL"))
    private void mechanicalmasteryreborn$save(ValueOutput output, CallbackInfo callback) {
        mechanicalmasteryreborn$fuel().serialize(output.child(mechanicalmasteryreborn$FUEL_KEY));
    }

    @Inject(method = "loadAdditional(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void mechanicalmasteryreborn$load(ValueInput input, CallbackInfo callback) {
        mechanicalmasteryreborn$fuel().deserialize(input.childOrEmpty(mechanicalmasteryreborn$FUEL_KEY));
    }
}
