// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The essences' items, one per {@link Essence}, each decomposed by hand with a right-click ({@link EssenceItem}).
 * An essence left on the ground fizzles when it expires, so a pile C.O.D. spilled looks like waste (#199).
 */
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
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, Essences::fizzle);
    }

    /**
     * Dissolves an expiring essence with a few particles and a soft sound, for show only: it is removed as vanilla
     * would remove it, and nothing takes its place. Last, so an item another listener keeps alive doesn't fizzle.
     */
    private static void fizzle(ItemExpireEvent event) {
        ItemEntity item = event.getEntity();
        if (event.getExtraLife() > 0 || !(item.level() instanceof ServerLevel server) || of(item.getItem().getItem()).isEmpty()) {
            return;
        }
        server.sendParticles(ParticleTypes.SMOKE, item.getX(), item.getY() + 0.2, item.getZ(), 6, 0.1, 0.1, 0.1, 0.01);
        server.playSound(null, item.getX(), item.getY(), item.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                0.3F, 1.8F + server.getRandom().nextFloat() * 0.4F);
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
