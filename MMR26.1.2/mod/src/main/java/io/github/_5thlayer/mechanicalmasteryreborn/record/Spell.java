// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

/**
 * A handicap a team took on at its Hermetic Lab (#263, ADR 0014), for a fixed spell that can't be ended early: its {@code id},
 * which each Null Shard its Assault drops is stamped with, the overworld's game time it {@code began} at, and the one it
 * {@code ends} at. {@link TeamState} keeps one per handicap while it runs. Plain data, so a JUnit test reads it without
 * Minecraft.
 */
public record Spell(UUID id, long began, long ends) {

    static final Codec<Spell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(Spell::id),
            Codec.LONG.fieldOf("began").forGetter(Spell::began),
            Codec.LONG.fieldOf("ends").forGetter(Spell::ends))
            .apply(instance, Spell::new));

    /** A new spell, begun at {@code now} and running {@code length} ticks. */
    public static Spell starting(long now, int length) {
        return new Spell(UUID.randomUUID(), now, now + Math.max(1, length));
    }

    /** Whether it is over at {@code now}. */
    public boolean over(long now) {
        return now >= ends;
    }

    /** The ticks it still runs at {@code now}, none once over. */
    public long remaining(long now) {
        return Math.max(0, ends - now);
    }
}
