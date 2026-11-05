// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.record.Spell;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The Null Shard (the line's glossary), the drop only the mobs of an Assault taken on at the Athanor give (#263), which
 * its protection line uses up. While a team bears the protection spell, every Rift Shard its Assault's mobs would drop
 * drops as a Null Shard instead, stamped with {@link #SPELL the spell} it fell in, so the Athanor takes only that spell's
 * and none saved from an earlier one. Nothing crafts it.
 */
public final class NullShards {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MechanicalMasteryReborn.MOD_ID);

    public static final DeferredItem<Item> ITEM = ITEMS.registerSimpleItem("null_shard");
    /** The id of the spell a Null Shard fell in. */
    public static final Supplier<DataComponentType<UUID>> SPELL = COMPONENTS.registerComponentType("spell",
            builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));

    private NullShards() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        COMPONENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(NullShards::onDrops);
    }

    /** {@code count} Null Shards stamped with {@code spell}. */
    public static ItemStack of(Spell spell, int count) {
        ItemStack stack = new ItemStack(ITEM.get(), count);
        stack.set(SPELL.get(), spell.id());
        return stack;
    }

    /** The id of the spell {@code stack} fell in, if it is a Null Shard stamped with one. */
    public static Optional<UUID> spellOf(ItemStack stack) {
        return stack.is(ITEM.get()) ? Optional.ofNullable(stack.get(SPELL.get())) : Optional.empty();
    }

    /** Turns the Rift Shards a mob of a protection spell's Assault drops into Null Shards of that spell. */
    private static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        Optional<Spell> spell = Assault.owner(event.getEntity())
                .flatMap(AssaultState.get(level.getServer())::openedBy)
                .flatMap(team -> TeamState.get(level.getServer()).spell(team, Handicap.PROTECTION));
        if (spell.isEmpty()) {
            return;
        }
        for (ItemEntity drop : event.getDrops()) {
            if (drop.getItem().is(RiftShards.ITEM.get())) {
                drop.setItem(of(spell.get(), drop.getItem().getCount()));
            }
        }
    }
}
