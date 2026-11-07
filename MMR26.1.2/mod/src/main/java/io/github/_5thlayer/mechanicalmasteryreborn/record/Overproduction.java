// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A team's Overproduction while it plays a challenge: when the challenge began, in the overworld's game time, and the
 * essence of the tier C.O.D. makes that its awake C.O.D. has made and the team has decomposed, by hand or on a Recycler, since then. The
 * excess is the one less the other, never below zero. {@link TeamState} keeps one per team that plays a challenge
 * and none otherwise; taking a fresh one, {@link #startingAt(long)}, starts the count again.
 */
public record Overproduction(long began, long made, long decomposed) {

    static final Codec<Overproduction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("began").forGetter(Overproduction::began),
            Codec.LONG.optionalFieldOf("made", 0L).forGetter(Overproduction::made),
            Codec.LONG.optionalFieldOf("decomposed", 0L).forGetter(Overproduction::decomposed))
            .apply(instance, Overproduction::new));

    /** How the server sends it to a client, for Jade. */
    public static final StreamCodec<ByteBuf, Overproduction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, Overproduction::began,
            ByteBufCodecs.VAR_LONG, Overproduction::made,
            ByteBufCodecs.VAR_LONG, Overproduction::decomposed,
            Overproduction::new);

    /** Overproduction as a challenge begins at {@code gameTime}: nothing made or decomposed yet. */
    public static Overproduction startingAt(long gameTime) {
        return new Overproduction(gameTime, 0, 0);
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
    Overproduction plusOneMade() {
        return new Overproduction(began, made + 1, decomposed);
    }

    /** This, with {@code count} more essence decomposed. */
    Overproduction plusDecomposed(long count) {
        return new Overproduction(began, made, decomposed + count);
    }
}
