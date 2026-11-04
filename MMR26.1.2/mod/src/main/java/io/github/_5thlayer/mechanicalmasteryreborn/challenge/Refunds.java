// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

/**
 * Each team's refund (#259, ADR 0015): what its Assault's mobs broke on its island, held while the Assault runs, and
 * handed over as it ends, by a win, a reset or a stop, to wait at the team's lab until a member takes it or the lab
 * pushes it out. A later Assault's adds to what still waits; nothing expires. Saved with the world, in the overworld's
 * data, so a restart in between loses none of it.
 */
public final class Refunds extends SavedData {

    /** Told to a team's members online as its Assault ends with something refunded. */
    public static final String HANDED_OVER = MechanicalMasteryReborn.MOD_ID + ".refund.handed_over";
    /** Told to a member who logs in while the team's refund still waits. */
    public static final String WAITING = MechanicalMasteryReborn.MOD_ID + ".refund.waiting";

    private static final Logger LOGGER = LogUtils.getLogger();

    /** A stack list saved without the empty stacks a taken slot leaves, which the item codec refuses. */
    private static final Codec<List<ItemStack>> STACKS = ItemStack.CODEC.listOf()
            .xmap(ArrayList::new, stacks -> stacks.stream().filter(stack -> !stack.isEmpty()).toList());

    private static final Codec<Refunds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, STACKS).optionalFieldOf("held", Map.of()).forGetter(state -> state.held),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, STACKS).optionalFieldOf("waiting", Map.of()).forGetter(state -> state.waiting))
            .apply(instance, Refunds::new));

    public static final SavedDataType<Refunds> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "refunds"), Refunds::new, CODEC);

    /** What each team's running Assault has broken so far. */
    private final Map<UUID, List<ItemStack>> held;
    /** What waits at each team's lab. */
    private final Map<UUID, List<ItemStack>> waiting;

    private Refunds() {
        this(Map.of(), Map.of());
    }

    private Refunds(Map<UUID, List<ItemStack>> held, Map<UUID, List<ItemStack>> waiting) {
        this.held = copy(held);
        this.waiting = copy(waiting);
    }

    private static Map<UUID, List<ItemStack>> copy(Map<UUID, List<ItemStack>> stacks) {
        Map<UUID, List<ItemStack>> copied = new HashMap<>();
        stacks.forEach((team, list) -> copied.put(team, new ArrayList<>(list.stream().map(ItemStack::copy).toList())));
        return copied;
    }

    public static Refunds get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** Tells a member who logs in that the team's refund still waits. */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                int count = get(player.level().getServer()).count(Teams.of(player));
                if (count > 0) {
                    player.sendSystemMessage(Component.translatable(WAITING, count).withStyle(ChatFormatting.GOLD));
                }
            }
        });
    }

    /** Holds {@code stack} for {@code team}, broken by its running Assault, until the Assault ends. */
    public void hold(UUID team, ItemStack stack) {
        if (!stack.isEmpty()) {
            add(held.computeIfAbsent(team, key -> new ArrayList<>()), stack.copy());
            setDirty();
        }
    }

    /** What {@code team}'s running Assault has broken so far, to read. */
    public List<ItemStack> held(UUID team) {
        return List.copyOf(held.getOrDefault(team, List.of()));
    }

    /**
     * Hands what {@code team}'s Assault broke over to its lab, as the Assault ends, and tells its members online, if
     * there was any. Says how many items were handed over.
     */
    public int handOver(MinecraftServer server, UUID team) {
        List<ItemStack> broken = held.remove(team);
        if (broken == null || broken.isEmpty()) {
            return 0;
        }
        List<ItemStack> list = waiting.computeIfAbsent(team, key -> new ArrayList<>());
        int count = 0;
        for (ItemStack stack : broken) {
            count += stack.getCount();
            add(list, stack);
        }
        list.removeIf(ItemStack::isEmpty);
        setDirty();
        Component told = Component.translatable(HANDED_OVER, count).withStyle(ChatFormatting.GOLD);
        for (var player : Teams.online(server, team)) {
            player.sendSystemMessage(told);
        }
        LOGGER.info("Handed team {}'s refund of {} items over to its lab", team, count);
        return count;
    }

    /**
     * What waits at {@code team}'s lab, which its lab's menu and push take from in place: a stack taken whole leaves an
     * empty one where it stood until the next {@link #compact}. Whoever changes it marks it {@link #setDirty dirty}.
     */
    public List<ItemStack> waiting(UUID team) {
        return waiting.computeIfAbsent(team, key -> new ArrayList<>());
    }

    /** How many items wait at {@code team}'s lab. */
    public int count(UUID team) {
        return waiting.getOrDefault(team, List.of()).stream().mapToInt(ItemStack::getCount).sum();
    }

    /** Drops the empty stacks taken ones leave in what waits at {@code team}'s lab. */
    public void compact(UUID team) {
        List<ItemStack> list = waiting.get(team);
        if (list != null && list.removeIf(ItemStack::isEmpty)) {
            setDirty();
        }
    }

    /** Adds {@code stack} to {@code list}, topping up the stacks of the same item first. */
    private static void add(List<ItemStack> list, ItemStack stack) {
        for (ItemStack each : list) {
            if (stack.isEmpty()) {
                return;
            }
            if (!each.isEmpty() && ItemStack.isSameItemSameComponents(each, stack) && each.getCount() < each.getMaxStackSize()) {
                int moved = Math.min(stack.getCount(), each.getMaxStackSize() - each.getCount());
                each.grow(moved);
                stack.shrink(moved);
            }
        }
        while (!stack.isEmpty()) {
            list.add(stack.split(stack.getMaxStackSize()));
        }
    }
}
