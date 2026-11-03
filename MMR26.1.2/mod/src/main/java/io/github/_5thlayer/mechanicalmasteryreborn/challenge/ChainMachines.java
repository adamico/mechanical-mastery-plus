// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Recycling;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A challenge's chain machines (ADR 0007, #214): what the recipes in its chain's stage file make, such as Theurgy's
 * apparatus in {@code data/mechanicalmasteryreborn/chapters/stages/energy.json}, of whatever type: in the pack,
 * Craftworks makes Theurgy's crafting recipes its own assembling type, under the same ids. Chapters locks only the
 * crafting of them, so a chain machine that comes from a mix does no work, by the machine's own mod's hook, while the
 * team whose island it stands on hasn't unlocked its chain. The stage file is the one list: it is read again after a
 * reload, and sent to each client, where EMI's and Jade's lines name the challenge.
 */
public final class ChainMachines {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** The stage files read last, and the server's recipes they were resolved against: a reload makes a new map. */
    private static volatile @Nullable Read read;

    private ChainMachines() {
    }

    public static void register(IEventBus modBus) {
        // The client's handler is the client's own (MechanicalMasteryRebornClient), so a dedicated server never loads it.
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1")
                .playToClient(Table.TYPE, Table.STREAM_CODEC));
        NeoForge.EVENT_BUS.addListener(ChainMachines::onDatapackSync);
    }

    /** Sends each player the chain machines, as {@link Recycling} sends its table; a game test's mock player is skipped. */
    private static void onDatapackSync(OnDatapackSyncEvent event) {
        var table = new Table(of(event.getPlayerList().getServer()));
        event.getRelevantPlayers().filter(player -> player.connection.hasChannel(Table.TYPE))
                .forEach(player -> PacketDistributor.sendToPlayer(player, table));
    }

    /** Every chain machine, and the handicap whose challenge unlocks it. */
    public static Map<Item, Handicap> of(MinecraftServer server) {
        RecipeMap recipes = server.getRecipeManager().recipeMap();
        Read found = read;
        if (found == null || found.recipes() != recipes) {
            found = new Read(recipes, readStages(server, recipes));
            read = found;
        }
        return found.machines();
    }

    /**
     * The handicap whose challenge the block at {@code pos} waits for, if it is a chain machine whose island's team
     * hasn't unlocked that chain. Server-side only: anywhere else, empty.
     */
    public static Optional<Handicap> lockedAt(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server)) {
            return Optional.empty();
        }
        Handicap handicap = of(server.getServer()).get(state.getBlock().asItem());
        if (handicap == null || TeamState.get(server.getServer()).unlocked(Teams.at(server, pos), handicap)) {
            return Optional.empty();
        }
        return Optional.of(handicap);
    }

    /** What EMI's tooltip on a chain machine and Jade on one that does no work both say: which challenge unlocks it. */
    public static Component line(Handicap handicap) {
        return Component.translatable("mechanicalmasteryreborn.chain.unlocked_by",
                Component.translatable("mechanicalmasteryreborn.handicap." + handicap.id()));
    }

    private static Map<Item, Handicap> readStages(MinecraftServer server, RecipeMap recipes) {
        var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var machines = new HashMap<Item, Handicap>();
        for (Handicap handicap : Handicap.values()) {
            var file = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "chapters/stages/" + handicap.id() + ".json");
            var resource = server.getResourceManager().getResource(file);
            if (resource.isEmpty()) {
                continue;
            }
            JsonElement stage;
            try (Reader reader = resource.get().openAsReader()) {
                stage = JsonParser.parseReader(reader);
            } catch (IOException | RuntimeException unreadable) {
                LOGGER.warn("Can't read the {} chain's stage file {}", handicap.id(), file, unreadable);
                continue;
            }
            if (!stage.isJsonObject() || !stage.getAsJsonObject().has("recipes")
                    || !stage.getAsJsonObject().get("recipes").isJsonArray()) {
                continue;
            }
            for (JsonElement id : stage.getAsJsonObject().getAsJsonArray("recipes")) {
                Identifier recipe = id.isJsonPrimitive() ? Identifier.tryParse(id.getAsString()) : null;
                RecipeHolder<?> holder = recipe == null ? null : recipes.byKey(ResourceKey.create(Registries.RECIPE, recipe));
                Optional<Item> made = holder == null ? Optional.empty() : Recycling.made(holder, ops).filter(item -> item != Items.AIR);
                if (made.isPresent()) {
                    machines.putIfAbsent(made.get(), handicap);
                } else {
                    LOGGER.warn("The {} chain's stage file names {}, which is no recipe the server has, or makes nothing it can read",
                            handicap.id(), id);
                }
            }
            LOGGER.info("The {} chain has {} machines", handicap.id(),
                    machines.values().stream().filter(handicap::equals).count());
        }
        return Map.copyOf(machines);
    }

    private record Read(RecipeMap recipes, Map<Item, Handicap> machines) {
    }

    /**
     * The chain machines, sent to each client with the recipes, since a client is sent no stage file; the client keeps
     * the last one in {@link #received}.
     */
    public record Table(Map<Item, Handicap> machines) implements CustomPacketPayload {

        public static final Type<Table> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "chain_machines"));

        private static final StreamCodec<RegistryFriendlyByteBuf, Handicap> HANDICAP = ByteBufCodecs.VAR_INT
                .map(ordinal -> Handicap.byOrdinal(ordinal).orElseThrow(), Handicap::ordinal).cast();

        public static final StreamCodec<RegistryFriendlyByteBuf, Table> STREAM_CODEC = ByteBufCodecs
                .<RegistryFriendlyByteBuf, Item, Handicap, Map<Item, Handicap>>map(HashMap::new,
                        ByteBufCodecs.registry(Registries.ITEM), HANDICAP)
                .map(Table::new, Table::machines);

        /** The table the client was last sent: empty until a server sends one. */
        private static volatile Table received = new Table(Map.of());

        public static Table received() {
            return received;
        }

        /** Keeps {@code table} as the one the client was last sent. */
        public static void receive(Table table) {
            received = table;
        }

        @Override
        public Type<Table> type() {
            return TYPE;
        }
    }
}
