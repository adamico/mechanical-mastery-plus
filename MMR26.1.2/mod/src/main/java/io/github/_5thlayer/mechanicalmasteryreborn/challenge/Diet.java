// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * A player's Diet, as the line's glossary has it: the foods they ate lately, oldest first, while their team plays the
 * life challenge begun at {@code began}, the game time its Inflation counts from. A Diet of another beginning is
 * forgotten, so a reset forgets it too. {@link #NONE} while the team plays no life challenge. Plain data, so a JUnit test
 * reads it without Minecraft.
 */
public record Diet(long began, List<Identifier> foods) {

    /** No life challenge played. */
    public static final Diet NONE = new Diet(-1, List.of());

    public static final MapCodec<Diet> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.fieldOf("began").forGetter(Diet::began),
            Identifier.CODEC.listOf().fieldOf("foods").forGetter(Diet::foods))
            .apply(instance, Diet::new));

    public static final StreamCodec<ByteBuf, Diet> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, Diet::began, Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), Diet::foods, Diet::new);

    public Diet {
        foods = List.copyOf(foods);
    }

    /** An empty Diet for the challenge begun at {@code began}. */
    public static Diet beginning(long began) {
        return new Diet(began, List.of());
    }

    /** Whether it is kept for a life challenge being played. */
    public boolean kept() {
        return began >= 0;
    }

    /** How often {@code food} is in it. */
    public int repeats(Identifier food) {
        return (int) foods.stream().filter(food::equals).count();
    }

    /** The Diet once {@code food} is eaten: last, the oldest forgotten beyond {@code size} foods. */
    public Diet eaten(Identifier food, int size) {
        List<Identifier> next = new ArrayList<>(foods);
        next.add(food);
        while (next.size() > Math.max(0, size)) {
            next.removeFirst();
        }
        return new Diet(began, next);
    }

    /** The share of its hunger and saturation a food in it {@code repeats} times feeds: {@code falloff} less a repeat, down to {@code floor}. */
    public static double share(int repeats, double falloff, double floor) {
        return Math.min(1.0, Math.max(floor, 1.0 - falloff * repeats));
    }
}
