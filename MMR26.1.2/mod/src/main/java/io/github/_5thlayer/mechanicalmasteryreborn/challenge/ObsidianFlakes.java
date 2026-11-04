// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Obsidian Flakes (the line's glossary), what the protection challenge's Chain flakes off obsidian with a Harvester and
 * magma slurry, and charges Rift Shards with (#252). Only that harvest makes them, which the pack writes, so they are
 * raw and recycle into nothing but less of themselves.
 */
public final class ObsidianFlakes {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);

    public static final DeferredItem<Item> ITEM = ITEMS.registerSimpleItem("obsidian_flakes");

    private ObsidianFlakes() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
