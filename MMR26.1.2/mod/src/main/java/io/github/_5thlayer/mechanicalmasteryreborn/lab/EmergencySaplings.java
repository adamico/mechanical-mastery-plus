// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * The lab's emergency sapling (#302): a member of the lab's team who right-clicks it holding dirt, and carries no sapling,
 * swaps one dirt for an oak sapling, at most once each {@link ServerConfig#labSaplingCooldown() cooldown} for the team, so
 * a team whose leaves gave no sapling, and that has no Shop yet, can always grow a tree again, and the lab is never a
 * source of wood. When each team was last given one is saved with the world, in the overworld's data.
 */
public final class EmergencySaplings extends SavedData {

    public static final String GIVEN = MechanicalMasteryReborn.MOD_ID + ".lab.sapling.given";
    public static final String HAS_SAPLING = MechanicalMasteryReborn.MOD_ID + ".lab.sapling.has_sapling";
    public static final String COOLING_DOWN = MechanicalMasteryReborn.MOD_ID + ".lab.sapling.cooling_down";
    public static final String NOT_MEMBER = MechanicalMasteryReborn.MOD_ID + ".lab.sapling.not_member";

    private static final Codec<EmergencySaplings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.LONG).optionalFieldOf("given", Map.of()).forGetter(state -> state.given))
            .apply(instance, EmergencySaplings::new));

    public static final SavedDataType<EmergencySaplings> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "emergency_saplings"), EmergencySaplings::new, CODEC);

    /** The game time each team was last given an emergency sapling. */
    private final Map<UUID, Long> given;

    private EmergencySaplings() {
        this(Map.of());
    }

    private EmergencySaplings(Map<UUID, Long> given) {
        this.given = new HashMap<>(given);
    }

    public static EmergencySaplings get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** Whether {@code stack} is what the lab swaps for a sapling. */
    static boolean asks(ItemStack stack) {
        return stack.is(Items.DIRT);
    }

    /**
     * {@code player}'s ask at the lab at {@code pos}, holding {@code dirt}: swaps one for an oak sapling if they are of the
     * lab's team, carry no sapling and their team's cooldown is over, and otherwise tells them why not on the action bar.
     * Says whether it gave one.
     */
    static boolean ask(ServerLevel level, BlockPos pos, Player player, ItemStack dirt) {
        UUID team = Teams.at(level, pos);
        if (!Teams.of(player).equals(team)) {
            player.sendOverlayMessage(Component.translatable(NOT_MEMBER));
            return false;
        }
        if (player.getInventory().contains(stack -> stack.is(ItemTags.SAPLINGS))) {
            player.sendOverlayMessage(Component.translatable(HAS_SAPLING));
            return false;
        }
        EmergencySaplings saplings = get(level.getServer());
        long now = level.getServer().overworld().getGameTime();
        long left = saplings.ticksLeft(team, now);
        if (left > 0) {
            // In whole minutes, rounded up, so it never says 0 while it still waits.
            player.sendOverlayMessage(Component.translatable(COOLING_DOWN, (left + 1199) / 1200));
            return false;
        }
        dirt.consume(1, player);
        ItemStack sapling = new ItemStack(Items.OAK_SAPLING);
        if (!player.getInventory().add(sapling)) {
            player.drop(sapling, false);
        }
        saplings.given.put(team, now);
        saplings.setDirty();
        player.sendOverlayMessage(Component.translatable(GIVEN));
        return true;
    }

    /** The ticks {@code team} still waits, at game time {@code now}, before its lab gives another sapling. */
    public long ticksLeft(UUID team, long now) {
        Long last = given.get(team);
        return last == null ? 0 : Math.max(0, last + ServerConfig.labSaplingCooldown() - now);
    }
}
