// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.UUID;
import java.util.function.Supplier;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.Overproduction;
import io.github._5thlayer.mechanicalmasteryreborn.record.Spell;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The life handicap, a hungrier {@link Diet} (ADR 0011): while a team plays the life challenge, its players' hunger
 * drains {@link ServerConfig#lifeHungerRate faster}, and a food already in a player's Diet feeds them less, in hunger
 * and saturation alike. It hooks vanilla's food data itself, so whatever feeds a player counts; a feeding nothing names,
 * such as a cake's bite, counts as {@link #OTHER}.
 * The Diet is a player's attachment, synced to them for the tooltip, and forgotten when the challenge ends or resets.
 * A team that
 * bears the life spell at its Hermetic Lab (#263) bears it the same way, its hunger faster still, its Diet kept from the
 * spell's start and forgotten as it ends.
 */
public final class LifeHandicap {

    /** What a feeding nothing names counts as in a Diet. */
    public static final Identifier OTHER = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "other_food");

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MechanicalMasteryReborn.MOD_ID);
    public static final Supplier<AttachmentType<Diet>> DIET = ATTACHMENTS.register("diet", () -> AttachmentType.builder(() -> Diet.NONE)
            .serialize(Diet.CODEC).copyOnDeath().sync(Diet.STREAM_CODEC).build());

    /** The food being eaten on this thread, which the feeding it causes counts as. */
    private static final ThreadLocal<Identifier> EATING = new ThreadLocal<>();

    private LifeHandicap() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                refresh(player);
            }
        });
    }

    /** Names {@code stack}'s item as what the feeding until {@link #ate} counts as. */
    public static void eating(ItemStack stack) {
        EATING.set(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** Ends what {@link #eating} named. */
    public static void ate() {
        EATING.remove();
    }

    /** The exhaustion {@code amount} adds to {@code player}: {@link ServerConfig#lifeHungerRate} times it under the handicap. */
    public static float exhaustion(Player player, float amount) {
        if (!applies(player)) {
            return amount;
        }
        boolean spell = !TeamState.get(player.level().getServer()).plays(Teams.of(player), Handicap.LIFE);
        return (float) (amount * (spell ? ServerConfig.hermeticLabLifeHungerRate() : ServerConfig.lifeHungerRate()));
    }

    /**
     * The share of its hunger and saturation a feeding gives {@code player}, which it notes in their Diet: all of it
     * unless the handicap applies.
     */
    public static double feed(Player player) {
        if (!applies(player)) {
            return 1.0;
        }
        Identifier food = EATING.get() != null ? EATING.get() : OTHER;
        Diet diet = current(player);
        double share = Diet.share(diet.repeats(food), ServerConfig.lifeRepeatFalloff(), ServerConfig.lifeFloor());
        player.setData(DIET, diet.eaten(food, ServerConfig.lifeDietSize()));
        return share;
    }

    /** The share {@code stack} would feed {@code player} now, by the Diet synced to them, which the tooltip shows; all of it if none is kept. */
    public static double shareNow(Player player, ItemStack stack) {
        Diet diet = player.getData(DIET);
        if (!diet.kept()) {
            return 1.0;
        }
        return Diet.share(diet.repeats(BuiltInRegistries.ITEM.getKey(stack.getItem())), ServerConfig.lifeRepeatFalloff(), ServerConfig.lifeFloor());
    }

    /** Whether {@code player}'s client is told a Diet is kept. */
    public static boolean kept(Player player) {
        return player.getData(DIET).kept();
    }

    /** Sets the Diet of each of {@code team}'s players online to what its challenge or spell keeps, as one starts, resets or ends. */
    static void refresh(MinecraftServer server, UUID team) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                refresh(player);
            }
        }
    }

    /** Forgets the Diet of each of {@code team}'s players online, as its challenge resets; one offline forgets it on login, by its beginning. */
    static void forget(MinecraftServer server, UUID team) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                player.setData(DIET, Diet.beginning(current(player).began()));
            }
        }
    }

    /** Sets {@code player}'s Diet to what their team's challenge keeps: forgotten if it is not the one it was kept for. */
    public static void refresh(Player player) {
        Diet diet = current(player);
        if (!diet.equals(player.getData(DIET))) {
            player.setData(DIET, diet);
        }
    }

    /** {@code player}'s Diet as their team's challenge keeps it. */
    private static Diet current(Player player) {
        UUID team = Teams.of(player);
        TeamState state = TeamState.get(player.level().getServer());
        if (!state.bears(team, Handicap.LIFE)) {
            return Diet.NONE;
        }
        long began = state.plays(team, Handicap.LIFE) ? state.overproduction(team).map(Overproduction::began).orElse(0L)
                : state.spell(team, Handicap.LIFE).map(Spell::began).orElse(0L);
        Diet diet = player.getData(DIET);
        return diet.began() == began ? diet : Diet.beginning(began);
    }

    private static boolean applies(Player player) {
        return !player.level().isClientSide()
                && TeamState.get(player.level().getServer()).bears(Teams.of(player), Handicap.LIFE);
    }
}
