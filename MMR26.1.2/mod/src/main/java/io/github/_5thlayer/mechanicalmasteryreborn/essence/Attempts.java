// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.port.Port;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

/**
 * A reset makes the attempt's Transmuted Essence stale (ADR 0016, #322). Each one a team makes while it plays that
 * essence's challenge is stamped with {@link #ATTEMPT the attempt} it was made in, through one central hook,
 * {@link Making}: whatever machine or Personal Assembler makes it, the Aureate's seal too. A stamped essence is stale once
 * its team's attempt number has moved on, by a reset or a stop and a new start, unless the team has won its challenge
 * since; an unstamped one, from a world older than the stamp or a won chain, is never stale. Stale matters to a Port
 * alone: the Shop's sell Offers and the Quartessence's recipe match an essence by its item, whatever its stamp. A client
 * is sent its team and that team's attempt, as {@link Viewer}, so a stale essence's tooltip and dimmed look are judged
 * against the viewing team's current attempt.
 */
public final class Attempts {

    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MechanicalMasteryReborn.MOD_ID);

    /** The attempt a Transmuted Essence was made in. */
    public static final Supplier<DataComponentType<Attempt>> ATTEMPT = COMPONENTS.registerComponentType("attempt",
            builder -> builder.persistent(Attempt.CODEC).networkSynchronized(Attempt.STREAM_CODEC));

    private Attempts() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        // The client's handler is the client's own (MechanicalMasteryRebornClient), so a dedicated server never loads it.
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1")
                .playToClient(Viewer.TYPE, Viewer.STREAM_CODEC));
        // A player's whole tick, Craftworks' Personal Assembler's crafting with it, counts as that player making things.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (PlayerTickEvent.Pre event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                Making.byPlayer(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (PlayerTickEvent.Post event) -> {
            if (event.getEntity() instanceof ServerPlayer) {
                Making.done();
            }
        });
    }

    /**
     * The attempt {@code item}, just made by {@code team}, is stamped with: the team's current one, if {@code item} is a
     * Transmuted Essence and the team plays its challenge.
     */
    static Optional<Attempt> stampFor(Item item, @Nullable MinecraftServer server, UUID team) {
        if (server == null) {
            return Optional.empty();
        }
        Optional<Handicap> handicap = Essences.of(item).flatMap(Port::handicapOf);
        TeamState state = TeamState.get(server);
        return handicap.filter(played -> state.plays(team, played)).map(played -> new Attempt(team, state.attempt(team)));
    }

    /** The attempt {@code components}, an essence's, was stamped with, if any. */
    public static Optional<Attempt> of(DataComponentGetter components) {
        return Optional.ofNullable(components.get(ATTEMPT.get()));
    }

    /**
     * Whether {@code components}, a Transmuted Essence's of {@code essence}, were stamped with an earlier attempt of
     * their team's than its current one, and the team hasn't won that essence's challenge since.
     */
    public static boolean stale(MinecraftServer server, Essence essence, DataComponentGetter components) {
        Optional<Attempt> attempt = of(components);
        if (attempt.isEmpty()) {
            return false;
        }
        TeamState state = TeamState.get(server);
        UUID team = attempt.get().team();
        return attempt.get().number() < state.attempt(team)
                && Port.handicapOf(essence).filter(handicap -> state.won(team, handicap)).isEmpty();
    }

    /** Whether {@code stack} is a stale Transmuted Essence, as its viewer on a client sees it: against its own team's attempt. */
    public static boolean staleToViewer(ItemStack stack) {
        Optional<Attempt> attempt = of(stack);
        if (attempt.isEmpty()) {
            return false;
        }
        Viewer viewer = Viewer.received();
        return attempt.get().team().equals(viewer.team()) && attempt.get().number() < viewer.attempt()
                && Essences.of(stack.getItem()).flatMap(Port::handicapOf).filter(viewer.won()::contains).isEmpty();
    }

    /** Sends {@code player} its team's attempt; a game test's mock player is skipped. Sent again on every start, reset and win. */
    public static void send(MinecraftServer server, ServerPlayer player) {
        if (player.connection == null || !player.connection.hasChannel(Viewer.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, viewer(server, Teams.of(player)));
    }

    /** Sends {@code team}'s players online its attempt, as a reset moves it on. */
    public static void send(MinecraftServer server, UUID team) {
        for (Player player : Teams.online(server, team)) {
            if (player instanceof ServerPlayer serverPlayer) {
                send(server, serverPlayer);
            }
        }
    }

    /** What a player of {@code team} is sent. */
    public static Viewer viewer(MinecraftServer server, UUID team) {
        TeamState state = TeamState.get(server);
        var won = EnumSet.noneOf(Handicap.class);
        for (Handicap handicap : Handicap.values()) {
            if (state.won(team, handicap)) {
                won.add(handicap);
            }
        }
        return new Viewer(team, state.attempt(team), won);
    }

    /** What a client is sent of its team's attempt: the team, its attempt number, and the challenges it has won. */
    public record Viewer(UUID team, int attempt, Set<Handicap> won) implements CustomPacketPayload {

        public static final Type<Viewer> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "attempt"));

        private static final StreamCodec<RegistryFriendlyByteBuf, Handicap> HANDICAP = ByteBufCodecs.VAR_INT
                .map(ordinal -> Handicap.byOrdinal(ordinal).orElseThrow(), Handicap::ordinal).cast();

        public static final StreamCodec<RegistryFriendlyByteBuf, Viewer> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC.cast(), Viewer::team,
                ByteBufCodecs.VAR_INT.cast(), Viewer::attempt,
                HANDICAP.apply(ByteBufCodecs.collection(HashSet::new)), viewer -> new HashSet<>(viewer.won()),
                Viewer::new);

        /** What the client was last sent: nothing is stale until a server sends one. */
        private static volatile Viewer received = new Viewer(Teams.WORLD, 0, Set.of());

        public Viewer {
            won = Set.copyOf(won);
        }

        public static Viewer received() {
            return received;
        }

        /** Keeps {@code viewer} as what the client was last sent. */
        public static void receive(Viewer viewer) {
            received = viewer;
        }

        @Override
        public Type<Viewer> type() {
            return TYPE;
        }
    }
}
