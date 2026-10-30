// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Decomposing;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Yield;
import io.github._5thlayer.mechanicalmasteryreborn.record.DecomposeLog;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;

/**
 * An essence, which a right-click decomposes by hand (the line's glossary: Decompose): one recipe's worth
 * of it becomes the lowest core's yield of its mix ({@link ServerConfig#byHand}), straight into the inventory,
 * and whatever doesn't fit drops. The essence then rests for {@link #HAND_COOLDOWN} ticks, shown on the hotbar,
 * so a Recycler is much the faster way. A decompose by hand is recorded as one, and never wakes C.O.D. (#167).
 */
public final class EssenceItem extends Item {

    /**
     * Ticks between two decomposes by hand: four times what one hand drive takes on a tier-1 core, 8 turns
     * of 5 ticks (a placeholder #168 balances).
     */
    public static final int HAND_COOLDOWN = 160;

    public EssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            // The client has no recipes to look in; a Mechanical Essence is the one kind the pack decomposes.
            return Essences.of(stack.getItem()).filter(essence -> essence.kind() == Essence.Kind.MECHANICAL).isPresent()
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }
        var found = Decomposing.recipeFor(server, stack);
        if (found.isEmpty()) {
            return InteractionResult.PASS;
        }
        var recipe = found.get().value();
        int count = recipe.ingredient().count();
        if (stack.getCount() < count) {
            return InteractionResult.FAIL;
        }
        ItemStack decomposed = stack.copyWithCount(count);
        stack.shrink(count);
        double yield = ServerConfig.byHand();
        for (ItemStackTemplate result : recipe.results()) {
            int returned = Yield.roll(result.count(), yield, server.getRandom()::nextDouble);
            if (returned > 0) {
                player.getInventory().placeItemBackInInventory(result.create().copyWithCount(returned));
            }
        }
        player.getCooldowns().addCooldown(decomposed, HAND_COOLDOWN);
        Essences.of(decomposed.getItem()).ifPresent(essence -> DecomposeLog.record(server.getServer(), Teams.of(player), essence, count,
                DecomposeLog.Way.HAND));
        return InteractionResult.SUCCESS_SERVER;
    }
}
