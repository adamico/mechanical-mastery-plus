// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The attempt a Transmuted Essence was made in (ADR 0016): the team that made it while playing its challenge, and that
 * team's attempt number then ({@link io.github._5thlayer.mechanicalmasteryreborn.record.TeamState#attempt}).
 */
public record Attempt(UUID team, int number) {

    public static final Codec<Attempt> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("team").forGetter(Attempt::team),
            Codec.INT.fieldOf("number").forGetter(Attempt::number))
            .apply(instance, Attempt::new));

    public static final StreamCodec<ByteBuf, Attempt> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, Attempt::team,
            ByteBufCodecs.VAR_INT, Attempt::number,
            Attempt::new);
}
