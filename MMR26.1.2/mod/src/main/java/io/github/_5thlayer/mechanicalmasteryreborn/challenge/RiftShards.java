// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The Rift Shard (the line's glossary), the drop only an Assault's mobs and a home gate give, which the protection
 * challenge's Chain takes (ADR 0010, #235). Nothing crafts it, so it is raw and recycles into nothing but less of
 * itself: no Recycler can make one, and the quota can't be stockpiled ahead. The Charged Rift, the Chain's step between
 * the shards and the Warding Essence, is made from two of them and recycles by the usual rule, from its recipe (#252).
 */
public final class RiftShards {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);

    public static final DeferredItem<Item> ITEM = ITEMS.registerSimpleItem("rift_shard");
    public static final DeferredItem<Item> CHARGED = ITEMS.registerSimpleItem("charged_rift");

    private RiftShards() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
