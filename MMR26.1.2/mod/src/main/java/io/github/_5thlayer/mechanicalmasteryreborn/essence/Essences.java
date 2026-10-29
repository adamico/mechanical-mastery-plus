// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The essences' items, one per {@link Essence}, each decomposed by hand with a right-click ({@link EssenceItem}). */
public final class Essences {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final Map<Essence, DeferredItem<EssenceItem>> BY_ESSENCE = new EnumMap<>(Essence.class);

    static {
        for (Essence essence : Essence.values()) {
            BY_ESSENCE.put(essence, ITEMS.registerItem(essence.path(), EssenceItem::new));
        }
    }

    private Essences() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static Item item(Essence essence) {
        return BY_ESSENCE.get(essence).get();
    }

    /** The essence {@code item} is, if it is one. */
    public static Optional<Essence> of(Item item) {
        return item instanceof EssenceItem ? BY_ESSENCE.entrySet().stream()
                .filter(entry -> entry.getValue().get() == item).map(Map.Entry::getKey).findFirst() : Optional.empty();
    }
}
