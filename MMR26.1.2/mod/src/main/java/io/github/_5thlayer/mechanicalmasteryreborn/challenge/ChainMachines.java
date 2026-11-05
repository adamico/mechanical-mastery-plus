// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.io.IOException;
import java.io.Reader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A challenge's chain machines and its own recipes (ADR 0007, #246). A chain's machines are the items its tag
 * {@code mechanicalmasteryreborn:chain_machines/<handicap>} holds, such as Theurgy's processing apparatus in the pack's
 * {@code chain_machines/energy}: each does no work, by the machine's own mod's hook, while the team whose island it
 * stands on hasn't started its challenge, though its crafting is never locked. A chain's own recipes, such as the
 * Galvanic Essence's incubation, are the ones its Chapters stage file names
 * ({@code data/mechanicalmasteryreborn/chapters/stages/<handicap>.json}), which Chapters and the pack's Craftworks lock
 * keep from running. Item tags reach each client by themselves; the chain's recipes and the challenges the player's team
 * has started are sent to it in a {@link Table}, so EMI marks each locked recipe, and a chain machine's tooltip names
 * its challenge, until the team has started it. The last chapter's recipes, such as the End Portal Frame's, are the
 * ones its own stage file names ({@code chapters/stages/last_chapter.json}), and the Table marks them until the team has
 * reached it (ADR 0012, #265).
 */
public final class ChainMachines {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** The stage files read last, and the server's recipes they were resolved against: a reload makes a new map. */
    private static volatile @Nullable Read read;

    private ChainMachines() {
    }

    public static void register(IEventBus modBus) {
        // The client's handler is the client's own (MechanicalMasteryRebornClient), so a dedicated server never loads it.
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("3")
                .playToClient(Table.TYPE, Table.STREAM_CODEC));
        NeoForge.EVENT_BUS.addListener(ChainMachines::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(ChainMachines::onTagsUpdated);
    }

    /** The tag that holds {@code handicap}'s chain machines, filled by the pack. */
    public static TagKey<Item> tag(Handicap handicap) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID,
                "chain_machines/" + handicap.id()));
    }

    /** The handicap whose chain {@code item} is a machine of, if any: the first whose tag holds it. Either side. */
    public static Optional<Handicap> chainOf(Item item) {
        var holder = item.builtInRegistryHolder();
        for (Handicap handicap : Handicap.values()) {
            if (holder.is(tag(handicap))) {
                return Optional.of(handicap);
            }
        }
        return Optional.empty();
    }

    /** Says how many machines each chain has, as the server's tags load, so a launch's log shows an empty or short list. */
    private static void onTagsUpdated(TagsUpdatedEvent.ServerDataLoad event) {
        var items = event.getRegistries().lookupOrThrow(Registries.ITEM);
        for (Handicap handicap : Handicap.values()) {
            int count = items.get(tag(handicap)).map(set -> set.size()).orElse(0);
            LOGGER.info("The {} chain has {} machines", handicap.id(), count);
        }
    }

    /** Sends each player its Table, as {@link Recycling} sends its table. */
    private static void onDatapackSync(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        event.getRelevantPlayers().forEach(player -> send(server, player));
    }

    /**
     * Sends {@code player} the chains' own recipes and the challenges its team has started; a game test's mock player
     * is skipped. Sent again whenever the team starts one ({@link Chains#unlock}).
     */
    public static void send(MinecraftServer server, ServerPlayer player) {
        if (player.connection == null || !player.connection.hasChannel(Table.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, table(server, Teams.of(player)));
    }

    /** What a player of {@code team} is sent: every locked recipe, and what its team has unlocked. */
    public static Table table(MinecraftServer server, UUID team) {
        TeamState state = TeamState.get(server);
        var unlocked = EnumSet.noneOf(Handicap.class);
        for (Handicap handicap : Handicap.values()) {
            if (state.unlocked(team, handicap)) {
                unlocked.add(handicap);
            }
        }
        return new Table(recipes(server), unlocked, lastChapterRecipes(server), !LastChapter.reached(server, team));
    }

    /** Every chain's own recipe, by its id, and the handicap whose challenge unlocks it. */
    public static Map<Identifier, Handicap> recipes(MinecraftServer server) {
        return stages(server).byId();
    }

    /** Every recipe the last chapter unlocks, by its id. */
    public static Set<Identifier> lastChapterRecipes(MinecraftServer server) {
        return stages(server).lastChapter();
    }

    private static Read stages(MinecraftServer server) {
        RecipeMap recipes = server.getRecipeManager().recipeMap();
        Read found = read;
        if (found == null || found.recipes() != recipes) {
            var byId = new HashMap<Identifier, Handicap>();
            for (Handicap handicap : Handicap.values()) {
                for (Identifier recipe : readStage(server, recipes, handicap.id())) {
                    byId.putIfAbsent(recipe, handicap);
                }
            }
            found = new Read(recipes, Map.copyOf(byId), Set.copyOf(readStage(server, recipes, LastChapter.STAGE)));
            read = found;
        }
        return found;
    }

    /**
     * The handicap whose challenge the block at {@code pos} waits for, if it is a chain machine whose island's team
     * hasn't unlocked that chain. Server-side only: anywhere else, empty.
     */
    public static Optional<Handicap> lockedAt(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server)) {
            return Optional.empty();
        }
        return chainOf(state.getBlock().asItem())
                .filter(handicap -> !TeamState.get(server.getServer()).unlocked(Teams.at(server, pos), handicap));
    }

    /** What EMI's mark on a locked recipe, a chain machine's tooltip and Jade on one that does no work all say. */
    public static Component line(Handicap handicap) {
        return Component.translatable("mechanicalmasteryreborn.chain.unlocked_by",
                Component.translatable("mechanicalmasteryreborn.handicap." + handicap.id()));
    }

    /** The recipes the stage file {@code chapters/stages/<stage>.json} names that the server has, if it has the file. */
    private static Set<Identifier> readStage(MinecraftServer server, RecipeMap recipes, String stage) {
        var file = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "chapters/stages/" + stage + ".json");
        var resource = server.getResourceManager().getResource(file);
        if (resource.isEmpty()) {
            return Set.of();
        }
        JsonElement json;
        try (Reader reader = resource.get().openAsReader()) {
            json = JsonParser.parseReader(reader);
        } catch (IOException | RuntimeException unreadable) {
            LOGGER.warn("Can't read the {} stage file {}", stage, file, unreadable);
            return Set.of();
        }
        if (!json.isJsonObject() || !json.getAsJsonObject().has("recipes")
                || !json.getAsJsonObject().get("recipes").isJsonArray()) {
            return Set.of();
        }
        var found = new LinkedHashSet<Identifier>();
        for (JsonElement id : json.getAsJsonObject().getAsJsonArray("recipes")) {
            Identifier recipe = id.isJsonPrimitive() ? Identifier.tryParse(id.getAsString()) : null;
            if (recipe != null && recipes.byKey(ResourceKey.create(Registries.RECIPE, recipe)) != null) {
                found.add(recipe);
            } else {
                LOGGER.warn("The {} stage file names {}, which is no recipe the server has", stage, id);
            }
        }
        return found;
    }

    private record Read(RecipeMap recipes, Map<Identifier, Handicap> byId, Set<Identifier> lastChapter) {
    }

    /**
     * What a client is sent of the chains, since it is sent no stage file and knows no team: each chain's own recipes,
     * the challenges the player's team has started, the last chapter's recipes and whether the team has yet to reach
     * it. The client keeps the last one in {@link #received}.
     */
    public record Table(Map<Identifier, Handicap> recipes, Set<Handicap> unlocked, Set<Identifier> lastChapter,
                        boolean lastChapterLocked) implements CustomPacketPayload {

        public static final Type<Table> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "chain_machines"));

        private static final StreamCodec<RegistryFriendlyByteBuf, Handicap> HANDICAP = ByteBufCodecs.VAR_INT
                .map(ordinal -> Handicap.byOrdinal(ordinal).orElseThrow(), Handicap::ordinal).cast();

        public static final StreamCodec<RegistryFriendlyByteBuf, Table> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.<RegistryFriendlyByteBuf, Identifier, Handicap, Map<Identifier, Handicap>>map(HashMap::new,
                        Identifier.STREAM_CODEC.cast(), HANDICAP), Table::recipes,
                HANDICAP.apply(ByteBufCodecs.collection(HashSet::new)), table -> new HashSet<>(table.unlocked()),
                Identifier.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)).cast(), table -> new HashSet<>(table.lastChapter()),
                ByteBufCodecs.BOOL.cast(), Table::lastChapterLocked,
                Table::new);

        /** The table the client was last sent: nothing locked is known until a server sends one. */
        private static volatile Table received = new Table(Map.of(), EnumSet.allOf(Handicap.class), Set.of(), false);

        public Table {
            recipes = Map.copyOf(recipes);
            unlocked = Set.copyOf(unlocked);
            lastChapter = Set.copyOf(lastChapter);
        }

        public static Table received() {
            return received;
        }

        /** Keeps {@code table} as the one the client was last sent. */
        public static void receive(Table table) {
            received = table;
        }

        /** Whether the player's team hasn't started {@code handicap}'s challenge yet. */
        public boolean locked(Handicap handicap) {
            return !unlocked.contains(handicap);
        }

        @Override
        public Type<Table> type() {
            return TYPE;
        }
    }
}
