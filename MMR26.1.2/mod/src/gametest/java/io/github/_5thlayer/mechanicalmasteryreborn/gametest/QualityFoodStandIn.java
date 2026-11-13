// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.compat.food.FoodQuality;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Quality Food's quality as the game tests' runs have it, which load no Quality Food: its component,
 * {@code quality_food:quality}, and its quality types, {@code quality_food:quality_types}, under its own ids and in the shape
 * its own codecs write them, with the game-test resources' iron, gold and diamond types at its own numbers, and the egg in
 * its material whitelist. Registered only without Quality Food, so {@link FoodQuality} reads and writes them as it would
 * Quality Food's own.
 */
final class QualityFoodStandIn {

    /** {@code Quality}: a type's id and level. */
    record Quality(Identifier type, int level) {
        static final Codec<Quality> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Identifier.CODEC.fieldOf("type").forGetter(Quality::type),
                Codec.INT.fieldOf("level").forGetter(Quality::level)).apply(builder, Quality::new));
    }

    /** {@code QualityType}: the fields its rolls read. */
    record QualityType(int level, double chance, double weight, double minWeight) {
        static final Codec<QualityType> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("level").forGetter(QualityType::level),
                Codec.DOUBLE.fieldOf("chance").forGetter(QualityType::chance),
                Codec.DOUBLE.fieldOf("weight").forGetter(QualityType::weight),
                Codec.DOUBLE.fieldOf("min_weight").forGetter(QualityType::minWeight)).apply(builder, QualityType::new));
    }

    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, FoodQuality.COMPONENT.getNamespace());

    static final Supplier<DataComponentType<Quality>> QUALITY = COMPONENTS.registerComponentType(FoodQuality.COMPONENT.getPath(),
            builder -> builder.persistent(Quality.CODEC));

    @SuppressWarnings("unchecked")
    private static final ResourceKey<Registry<QualityType>> TYPES =
            (ResourceKey<Registry<QualityType>>) (ResourceKey<?>) FoodQuality.TYPES;

    private QualityFoodStandIn() {
    }

    static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        modBus.addListener((DataPackRegistryEvent.NewRegistry event) -> event.dataPackRegistry(TYPES, QualityType.CODEC));
    }
}
