// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import java.util.Optional;

import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A shift-click puts food into the Assembler's input that holds it at another quality, as a pipe does (#372): what fits
 * is {@link FoodQuality#blend blended} with what the input holds, and the click then goes on as Craftworks has it.
 * The menu's slots take the stack as it is, so the blend comes first. {@link Pseudo}, so without Craftworks it never
 * applies.
 */
@Pseudo
@Mixin(targets = "io.github._5thlayer.craftworks.machine.AssemblerMenu")
public abstract class AssemblerMenuBlendMixin extends AbstractContainerMenu {

    private AssemblerMenuBlendMixin() {
        super(null, 0);
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void mechanicalmasteryreborn$blend(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        Slot from = slots.get(index);
        if (!(from.container instanceof Inventory) || !from.hasItem()) {
            return;
        }
        ItemStack stack = from.getItem();
        for (Slot input : slots) {
            if (input.container instanceof Inventory || !input.hasItem() || stack.isEmpty()) {
                continue;
            }
            ItemStack held = input.getItem();
            Optional<ItemStack> probe = FoodQuality.blend(held, stack.copyWithCount(1), player.level().registryAccess());
            if (probe.isEmpty() || !input.mayPlace(stack)) {
                continue;
            }
            int moved = Math.min(stack.getCount(), input.getMaxStackSize(probe.get()) - held.getCount());
            if (moved <= 0) {
                continue;
            }
            FoodQuality.blend(held, stack.copyWithCount(moved), player.level().registryAccess()).ifPresent(input::set);
            stack.shrink(moved);
            from.set(stack);
        }
    }
}
