// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;

/**
 * Quality Food's quality on an item, read and written through its own component, {@code quality_food:quality}, and its
 * quality types, {@code quality_food:quality_types}, by id and in the shape its codecs write, so the companion mod builds
 * without Quality Food and does nothing without it (#365). A type is a {@link Grade}; its level is the quality, 0 none.
 *
 * <p>A machine of life's chain carries quality through ({@link #carry}): its output takes the average of its food inputs'
 * levels, rounded half up. A planter or pen rolls a product's at harvest ({@link #roll}), as Quality Food rolls a crop's.
 * What takes quality is what Quality Food gives it to.
 */
public final class FoodQuality {

    /** Quality Food's quality component. */
    public static final Identifier COMPONENT = qualityFood("quality");
    /** Quality Food's registry of quality types. */
    public static final ResourceKey<Registry<Object>> TYPES = ResourceKey.createRegistryKey(qualityFood("quality_types"));

    private static final TagKey<Item> BLACKLIST = TagKey.create(Registries.ITEM, qualityFood("blacklist"));
    private static final TagKey<Item> MATERIALS = TagKey.create(Registries.ITEM, qualityFood("material_whitelist"));
    private static final TagKey<Block> QUALITY_BLOCKS = TagKey.create(Registries.BLOCK, qualityFood("quality_blocks"));

    /** A quality type: its id, its level, and the odds Quality Food rolls it at, its {@code chance} and weights. */
    public record Grade(Identifier id, int level, double chance, double weight, double minWeight) {

        /** Quality Food's {@code calculateChance}: how far {@code weight} reaches into this type's, from 0 to 1. */
        double reach(double weight) {
            return this.weight == minWeight ? 0 : Mth.clamp((weight - minWeight) / (this.weight - minWeight), 0, 1);
        }
    }

    private FoodQuality() {
    }

    /** {@code stack}'s quality level, 0 for none, a placed block's or without Quality Food. */
    public static int level(ItemStack stack) {
        return read(stack).map(json -> json.get("level")).filter(JsonElement::isJsonPrimitive)
                .map(JsonElement::getAsInt).filter(level -> level > 0).orElse(0);
    }

    /**
     * Whether Quality Food gives {@code stack} quality, as its {@code Utils.isValidItem} says: a food that feeds, a block of
     * its quality blocks or an item of its material whitelist, and never one of its blacklist.
     */
    public static boolean takesQuality(ItemStack stack) {
        if (stack.isEmpty() || stack.is(BLACKLIST)) {
            return false;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null && (food.nutrition() > 0 || food.saturation() > 0)) {
            return true;
        }
        if (stack.getItem() instanceof BlockItem block && block.getBlock().defaultBlockState().is(QUALITY_BLOCKS)) {
            return true;
        }
        return stack.is(MATERIALS);
    }

    /**
     * The average level of the inputs that {@link #takesQuality take quality}, each weighed by its count and a plain one as
     * 0, rounded half up; 0 with none.
     */
    public static int average(List<ItemStack> inputs) {
        long levels = 0;
        long items = 0;
        for (ItemStack input : inputs) {
            if (takesQuality(input)) {
                levels += (long) level(input) * input.getCount();
                items += input.getCount();
            }
        }
        return items == 0 ? 0 : (int) Math.floorDiv(2 * levels + items, 2 * items);
    }

    /**
     * Gives {@code output} its {@code inputs}' {@link #average} quality, of the type an input of that level has or else the
     * first of that level, or none at 0. An output that doesn't {@link #takesQuality take quality} is left as it is.
     */
    public static void carry(ItemStack output, List<ItemStack> inputs, HolderLookup.Provider registries) {
        if (!takesQuality(output) || component().isEmpty()) {
            return;
        }
        int level = average(inputs);
        Optional<Identifier> type = inputs.stream().filter(input -> level(input) == level)
                .flatMap(input -> read(input).map(json -> json.get("type")).stream())
                .map(json -> Identifier.parse(json.getAsString())).findFirst()
                .or(() -> grade(level, registries).map(Grade::id));
        set(output, type.filter(_ -> level > 0), level, registries);
    }

    /**
     * {@code held} and {@code incoming} as one stack, of their {@link #average} quality, when they are the same item, take
     * quality and differ in nothing but it, so a slot that holds one item takes another of its quality (#372); empty
     * otherwise, as when they are the same.
     */
    public static Optional<ItemStack> blend(ItemStack held, ItemStack incoming, HolderLookup.Provider registries) {
        Optional<DataComponentType<Object>> component = component();
        if (component.isEmpty() || held.isEmpty() || incoming.isEmpty() || !takesQuality(held)
                || ItemStack.isSameItemSameComponents(held, incoming)) {
            return Optional.empty();
        }
        ItemStack heldPlain = held.copy();
        heldPlain.remove(component.get());
        ItemStack incomingPlain = incoming.copy();
        incomingPlain.remove(component.get());
        if (!ItemStack.isSameItemSameComponents(heldPlain, incomingPlain)) {
            return Optional.empty();
        }
        ItemStack blended = held.copyWithCount(held.getCount() + incoming.getCount());
        carry(blended, List.of(held, incoming), registries);
        return Optional.of(blended);
    }

    /**
     * Rolls {@code product}'s quality, as Quality Food rolls a crafted item's: each type, from the lowest, comes up at its
     * {@code chance} times {@code odds}, plus how far {@code weight} reaches into it, and the highest that came up is kept.
     * A planter's soil and fertilizer raise the {@code odds}, a pen's feed gives the {@code weight}: its quality's
     * {@link #weight}. A product that doesn't {@link #takesQuality take quality}, or has a quality already, is left as it is.
     */
    public static void roll(ItemStack product, double odds, double weight, RandomSource random, HolderLookup.Provider registries) {
        if (!takesQuality(product) || level(product) > 0 || component().isEmpty()) {
            return;
        }
        Grade kept = null;
        for (Grade grade : grades(registries)) {
            if (kept != null && grade.level() <= kept.level()) {
                continue;
            }
            double chance = grade.chance() * odds + grade.reach(weight);
            if (chance > 0 && chance >= random.nextDouble()) {
                kept = grade;
            }
        }
        if (kept != null) {
            set(product, Optional.of(kept.id()), kept.level(), registries);
        }
    }

    /** The weight of the type of {@code level}, as Quality Food averages ingredients by; 0 for none. */
    public static double weight(int level, HolderLookup.Provider registries) {
        return grade(level, registries).map(Grade::weight).orElse(0.0);
    }

    /** The first quality type of {@code level}. */
    private static Optional<Grade> grade(int level, HolderLookup.Provider registries) {
        return grades(registries).stream().filter(grade -> grade.level() == level).findFirst();
    }

    /**
     * Gives each of {@code drops} that, as rolled, would find no room in {@code outputs}, filled in turn as a planter
     * fills its output slots, the quality of a stack of its item there that has room, so a planter, which checks its room by
     * item alone and throws away what doesn't fit, loses no crop to a roll. What has room keeps its roll.
     */
    public static void fitInto(List<ItemStack> drops, List<ItemStack> outputs) {
        List<ItemStack> slots = new ArrayList<>();
        outputs.forEach(output -> slots.add(output.copy()));
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (!fits(slots, drop)) {
                for (ItemStack held : slots) {
                    if (held.is(drop.getItem()) && held.getCount() < held.getMaxStackSize()
                            && !ItemStack.isSameItemSameComponents(held, drop)) {
                        drop = held.copyWithCount(drop.getCount());
                        drops.set(i, drop);
                        break;
                    }
                }
            }
            place(slots, drop);
        }
    }

    /** Whether all of {@code drop} would go into {@code slots}, as {@link #place} puts it. */
    private static boolean fits(List<ItemStack> slots, ItemStack drop) {
        List<ItemStack> trial = new ArrayList<>();
        slots.forEach(slot -> trial.add(slot.copy()));
        return place(trial, drop.copy()) == 0;
    }

    /**
     * Puts {@code drop} into {@code slots} as a planter does, onto stacks of it first, then into empty slots, and says how
     * many found no room.
     */
    private static int place(List<ItemStack> slots, ItemStack drop) {
        int remaining = drop.getCount();
        for (ItemStack held : slots) {
            if (remaining > 0 && !held.isEmpty() && ItemStack.isSameItemSameComponents(held, drop)) {
                int added = Math.min(remaining, held.getMaxStackSize() - held.getCount());
                held.grow(added);
                remaining -= added;
            }
        }
        for (int slot = 0; slot < slots.size() && remaining > 0; slot++) {
            if (slots.get(slot).isEmpty()) {
                int added = Math.min(remaining, drop.getMaxStackSize());
                slots.set(slot, drop.copyWithCount(added));
                remaining -= added;
            }
        }
        return remaining;
    }

    /** Quality Food's quality types, lowest level first; none without Quality Food. */
    public static List<Grade> grades(HolderLookup.Provider registries) {
        Optional<Codec<Object>> codec = typeCodec();
        Optional<? extends HolderLookup.RegistryLookup<Object>> types = registries.lookup(TYPES);
        if (codec.isEmpty() || types.isEmpty()) {
            return List.of();
        }
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        List<Grade> grades = new ArrayList<>();
        types.get().listElements().forEach(type -> codec.get().encodeStart(ops, type.value()).result()
                .map(JsonElement::getAsJsonObject).ifPresent(json -> grades.add(new Grade(type.key().identifier(),
                        json.get("level").getAsInt(), number(json, "chance"), number(json, "weight"), number(json, "min_weight")))));
        grades.sort(Comparator.comparingInt(Grade::level).thenComparing(Grade::id));
        return grades;
    }

    /** Sets {@code stack}'s quality to {@code type} at {@code level}, or takes it off with no type. */
    private static void set(ItemStack stack, Optional<Identifier> type, int level, HolderLookup.Provider registries) {
        DataComponentType<Object> component = component().orElseThrow();
        if (type.isEmpty()) {
            stack.remove(component);
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", type.get().toString());
        json.addProperty("level", level);
        component.codecOrThrow().parse(registries.createSerializationContext(JsonOps.INSTANCE), json).result()
                .ifPresent(quality -> stack.set(component, quality));
    }

    /** {@code stack}'s quality as its codec writes it, as much of it as it can. */
    private static Optional<JsonObject> read(ItemStack stack) {
        Optional<DataComponentType<Object>> component = component();
        if (component.isEmpty()) {
            return Optional.empty();
        }
        Object quality = stack.get(component.get());
        return quality == null ? Optional.empty()
                : component.get().codecOrThrow().encodeStart(JsonOps.INSTANCE, quality).resultOrPartial(_ -> { })
                        .filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject);
    }

    @SuppressWarnings("unchecked")
    private static Optional<DataComponentType<Object>> component() {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(COMPONENT).map(type -> (DataComponentType<Object>) type);
    }

    @SuppressWarnings("unchecked")
    private static Optional<Codec<Object>> typeCodec() {
        for (RegistryDataLoader.RegistryData<?> data : DataPackRegistriesHooks.getDataPackRegistries()) {
            if (data.key().equals(TYPES)) {
                return Optional.of((Codec<Object>) data.elementCodec());
            }
        }
        return Optional.empty();
    }

    private static double number(JsonObject json, String field) {
        return json.has(field) ? json.get(field).getAsDouble() : 0;
    }

    private static Identifier qualityFood(String path) {
        return Identifier.fromNamespaceAndPath("quality_food", path);
    }
}
