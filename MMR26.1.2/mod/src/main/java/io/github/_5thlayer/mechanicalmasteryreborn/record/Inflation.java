// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A team's Inflation while it plays a challenge: when the challenge began, in the overworld's game time, and the
 * Advanced essence its awake C.O.D. has made and the team has decomposed, by hand or on a Recycler, since then. The
 * excess is the one less the other, never below zero. {@link TeamState} keeps one per team that plays a challenge
 * and none otherwise; taking a fresh one, {@link #startingAt(long)}, starts the count again.
 */
public record Inflation(long began, long made, long decomposed) {

    static final Codec<Inflation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("began").forGetter(Inflation::began),
            Codec.LONG.optionalFieldOf("made", 0L).forGetter(Inflation::made),
            Codec.LONG.optionalFieldOf("decomposed", 0L).forGetter(Inflation::decomposed))
            .apply(instance, Inflation::new));

    /** How the server sends it to a client, for Jade. */
    public static final StreamCodec<ByteBuf, Inflation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, Inflation::began,
            ByteBufCodecs.VAR_LONG, Inflation::made,
            ByteBufCodecs.VAR_LONG, Inflation::decomposed,
            Inflation::new);

    /** Inflation as a challenge begins at {@code gameTime}: nothing made or decomposed yet. */
    public static Inflation startingAt(long gameTime) {
        return new Inflation(gameTime, 0, 0);
    }

    /**
     * What C.O.D. has made since the challenge began beyond what the team has decomposed, never below zero. The floor
     * is on the totals, not on each step, as the maintainer chose: what the team decomposes beyond what C.O.D. has
     * made, a stockpile from before the challenge, say, is credit against what C.O.D. makes next.
     */
    public long excess() {
        return Math.max(0, made - decomposed);
    }

    /** This, with one more essence made. */
    Inflation plusOneMade() {
        return new Inflation(began, made + 1, decomposed);
    }

    /** This, with {@code count} more essence decomposed. */
    Inflation plusDecomposed(long count) {
        return new Inflation(began, made, decomposed + count);
    }
}
