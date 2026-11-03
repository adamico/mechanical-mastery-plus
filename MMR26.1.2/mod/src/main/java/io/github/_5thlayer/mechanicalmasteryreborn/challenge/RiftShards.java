// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The Rift Shard (the line's glossary), the drop only an Assault's mobs and a home gate give, which the pack's one
 * Warding Essence recipe takes (ADR 0010, #235). Nothing crafts it, so it is raw and recycles into nothing but less of
 * itself: no Recycler can make one, and the quota can't be stockpiled ahead.
 */
public final class RiftShards {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);

    public static final DeferredItem<Item> ITEM = ITEMS.registerSimpleItem("rift_shard");

    private RiftShards() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
