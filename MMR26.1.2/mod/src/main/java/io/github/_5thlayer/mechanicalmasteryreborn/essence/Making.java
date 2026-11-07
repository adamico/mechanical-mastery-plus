// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * What is making things now on this thread, the one central hook {@link Attempts} stamps a Transmuted Essence through
 * (#322): a block entity ticking in a server level, as every machine does its work, or a server player ticking, as
 * Craftworks' Personal Assembler crafts. Whatever is made meanwhile passes through {@link #made}: every item stack, and
 * every item resource made from a recipe's result, as Craftworks' Assembler makes its output; the mixins in
 * {@code essence.mixin} call it. So it is kept cheap: nothing is looked up unless something is making and the item is an
 * essence. Kept apart from {@link Attempts}, so the first item stack the game makes loads no registry.
 */
public final class Making {

    private static final ThreadLocal<Maker> MAKER = ThreadLocal.withInitial(Maker::new);

    private Making() {
    }

    /** Notes that the block entity at {@code pos} in {@code level} works, until {@link #done}. */
    public static void atBlock(ServerLevel level, BlockPos pos) {
        Maker maker = MAKER.get();
        maker.level = level;
        maker.pos = pos;
        maker.player = null;
    }

    /** Notes that {@code player} ticks, until {@link #done}. */
    public static void byPlayer(ServerPlayer player) {
        Maker maker = MAKER.get();
        maker.level = null;
        maker.pos = null;
        maker.player = player;
    }

    /** Notes that nothing makes anything on this thread any more. */
    public static void done() {
        Maker maker = MAKER.get();
        maker.level = null;
        maker.pos = null;
        maker.player = null;
    }

    /** Stamps {@code stack}, just made, if it is a Transmuted Essence that the maker's team makes while it plays its challenge. */
    public static void made(ItemStack stack) {
        Maker maker = MAKER.get();
        if (maker.making() && stack.getItem() instanceof EssenceItem && !stack.has(Attempts.ATTEMPT.get())) {
            maker.stampFor(stack.getItem()).ifPresent(attempt -> stack.set(Attempts.ATTEMPT.get(), attempt));
        }
    }

    /** {@code resource}, just made from a recipe's result, stamped as {@link #made(ItemStack)} stamps a stack. */
    public static ItemResource made(ItemResource resource) {
        Maker maker = MAKER.get();
        if (!maker.making() || !(resource.getItem() instanceof EssenceItem) || resource.getComponents().has(Attempts.ATTEMPT.get())) {
            return resource;
        }
        return maker.stampFor(resource.getItem()).map(attempt -> resource.with(Attempts.ATTEMPT.get(), attempt)).orElse(resource);
    }

    private static final class Maker {
        @Nullable ServerLevel level;
        @Nullable BlockPos pos;
        @Nullable ServerPlayer player;

        boolean making() {
            return player != null || level != null;
        }

        /** The attempt {@code item} is stamped with, made by this maker's team. */
        Optional<Attempt> stampFor(Item item) {
            // Looked up first, since a Mechanical Essence, made far more often, is never stamped.
            if (Essences.of(item).filter(essence -> essence.kind() == Essence.Kind.TRANSMUTED).isEmpty()) {
                return Optional.empty();
            }
            if (player != null) {
                return Attempts.stampFor(item, player.level().getServer(), Teams.of(player));
            }
            if (level != null && pos != null) {
                UUID team = Teams.at(level, pos);
                return Attempts.stampFor(item, level.getServer(), team);
            }
            return Optional.empty();
        }
    }
}
