// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.compat.gateways.GatewaysGates;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * The protection handicap's Assault (ADR 0010, #237, #239): when a team starts the protection challenge, on each of
 * the four sides of its C.O.D. a platform of {@link #STONE} in the void {@link #OFFSET} blocks off, level with the
 * ground C.O.D. stands on, a bridge of it running back toward C.O.D. until the first block in its way, and on the
 * platform the pack's endless gate ({@link Gates}), summoned by one of the team's players on its island, so its mobs
 * hunt the team. The four share one gate, whose waves step up with no ceiling; one that fails opens again at its wave.
 * A win or a reset removes the gate, what was built and the gate's mobs still around; the team keeps its Rift Shards.
 * A gate or mob in a chunk no one is near can't be seen as the Assault ends, so it is kept out when it loads again.
 * Each piece is built only into air and removed only where it still stands, so nothing else is ever touched.
 */
public final class Assault {

    /** How far off C.O.D. each platform's middle is. */
    public static final int OFFSET = 48;
    /** Every how many waves the gate steps up, as {@code assault.json}'s step-ups and breathers do, and moves side. */
    public static final int STEP = 5;
    /** How far the platform reaches from its middle each way: 7 by 7. */
    public static final int PLATFORM_RADIUS = 3;
    /** How far the bridge reaches each side of its middle: 3 wide. */
    public static final int BRIDGE_RADIUS = 1;

    /** The persistent-data key Gateways marks each wave's mob with: its gate entity's id. */
    private static final String OWNER_KEY = "gateways.owner";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MechanicalMasteryReborn.MOD_ID);

    /** What the platform and bridge are built of: unbreakable as C.O.D. is, and dropping nothing. */
    public static final DeferredBlock<Block> STONE = BLOCKS.registerSimpleBlock("assault_stone",
            props -> props.mapColor(MapColor.COLOR_PURPLE).strength(-1.0F, 3600000.0F).sound(SoundType.DEEPSLATE_BRICKS).noLootTable());

    private Assault() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        NeoForge.EVENT_BUS.addListener(Assault::onJoin);
        AssaultAbsence.register();
        NeoForge.EVENT_BUS.addListener(AssaultCommand::register);
        if (ModList.get().isLoaded("gateways")) {
            GatewaysGates.register();
        }
    }

    /**
     * Keeps out a removed Assault's gate and mobs that were in a chunk no one was near as it was removed, which an end
     * can't see, when that chunk loads again.
     */
    private static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = event.getEntity();
        AssaultState state = AssaultState.get(level.getServer());
        if (AssaultAbsence.keptOut(state, entity) || state.closed(entity.getUUID()) || owner(entity).filter(state::closed).isPresent()) {
            event.setCanceled(true);
        }
    }

    /** Notes where {@code team}'s C.O.D. stands, which its Assault is built from. */
    public static void noteCod(ServerLevel level, UUID team, BlockPos pos) {
        AssaultState.get(level.getServer()).noteCod(team, GlobalPos.of(level.dimension(), pos.immutable()));
    }

    /** Sends {@code team} its Assault, as its protection challenge starts, and says whether it could: only with a C.O.D. found. */
    static boolean start(MinecraftServer server, UUID team) {
        AssaultState state = AssaultState.get(server);
        if (state.assault(team).isPresent()) {
            return true;
        }
        Optional<GlobalPos> cod = state.cod(team);
        ServerLevel level = cod.map(pos -> server.getLevel(pos.dimension())).orElse(null);
        if (level == null) {
            LOGGER.warn("Team {} has no C.O.D. found, so no Assault is sent at it", team);
            return false;
        }
        BlockPos codPos = cod.get().pos();
        ServerPlayer summoner = summoner(server, level, team);
        List<BlockPos> built = new ArrayList<>();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            buildSide(level, codPos, side, built);
        }
        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom());
        List<UUID> gates = Gates.get().open(level, gateAt(codPos, side), summoner, 0).stream().toList();
        state.putAssault(team, new AssaultState.Built(level.dimension(), built, gates, Optional.of(side), 0));
        AssaultExplosions.lift(server, team);
        LOGGER.info("Sent team {} its Assault: {} blocks built, gate {} on the {} side", team, built.size(), gates, side);
        return true;
    }

    /** Removes {@code team}'s Assault, as its protection challenge ends or resets: its gate, what was built, and the gate's mobs. */
    static void end(MinecraftServer server, UUID team) {
        AssaultExplosions.restore(server, team);
        AssaultState.get(server).takeAssault(team).ifPresent(built -> {
            ServerLevel level = server.getLevel(built.dimension());
            if (level == null) {
                return;
            }
            AssaultState state = AssaultState.get(server);
            List<UUID> gates = built.gates();
            List<Entity> removed = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (gates.contains(entity.getUUID()) || owner(entity).filter(state::closed).isPresent()) {
                    removed.add(entity);
                }
            }
            removed.forEach(Entity::discard);
            for (BlockPos pos : built.blocks()) {
                if (level.getBlockState(pos).is(STONE.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            LOGGER.info("Removed team {}'s Assault", team);
        });
    }

    /**
     * Opens again at once, on the side it is on (or else at {@code at}) and at the wave {@code wave} it had reached, the
     * Assault's gate {@code failed} that failed, for its timer, a mob lost or out of range: nothing stops the Assault but
     * its end. A gate of no Assault is left alone.
     */
    public static void reopen(ServerLevel level, UUID failed, Vec3 at, int wave) {
        AssaultState state = AssaultState.get(level.getServer());
        state.teamOf(failed).ifPresent(team -> {
            Vec3 where = gateAt(state, team).orElse(at);
            Optional<UUID> reopened = Gates.get().open(level, where, summoner(level.getServer(), level, team), wave);
            state.replaceGate(team, failed, reopened);
            LOGGER.info("Reopened team {}'s failed Assault gate {} at wave {}: {}", team, failed, wave,
                    reopened.map(UUID::toString).orElse("none"));
        });
    }

    /**
     * Moves the Assault's gate {@code gate} to one of the other three sides at random as it ends wave {@code wave}, if
     * that wave, counted from 1, was a step up ({@link #STEP}): Gateways pauses after a wave by that wave's setup
     * time, so the step's breather follows the step's first wave, and the gate moves in it. The old gate closes, kept out
     * though its leftover mobs may stay, and the gate opens on the new side's platform at the wave reached. A gate of no
     * Assault, or a wave before no step, is left alone.
     */
    public static void step(ServerLevel level, UUID gate, int wave) {
        if (wave <= 0 || wave % STEP != 0) {
            return;
        }
        AssaultState state = AssaultState.get(level.getServer());
        state.teamOf(gate).ifPresent(team -> {
            AssaultState.Built built = state.assault(team).orElseThrow();
            Optional<GlobalPos> cod = state.cod(team);
            if (cod.isEmpty()) {
                return;
            }
            List<Direction> others = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
            built.side().ifPresent(others::remove);
            Direction side = others.get(level.getRandom().nextInt(others.size()));
            Optional<UUID> moved = Gates.get().open(level, gateAt(cod.get().pos(), side), summoner(level.getServer(), level, team), wave);
            state.moveGate(team, gate, moved, side);
            Entity old = level.getEntity(gate);
            if (old != null) {
                old.discard();
            }
            Component where = Component.translatable(MechanicalMasteryReborn.MOD_ID + ".assault.moved", sideName(side));
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                if (Teams.of(player).equals(team)) {
                    player.sendOverlayMessage(where);
                }
            }
            LOGGER.info("Team {}'s Assault gate moves to the {} side at wave {}: {}", team, side, wave, moved.map(UUID::toString).orElse("none"));
        });
    }

    /**
     * Puts {@code team}'s open gate at wave {@code wave}, counted from 1, for checking and balancing: the gate closes, kept
     * out as a failed one is, and opens again where it stood with the waves before {@code wave} behind it. Says whether
     * the team had an open gate.
     */
    public static boolean setWave(MinecraftServer server, UUID team, int wave) {
        AssaultState state = AssaultState.get(server);
        Optional<AssaultState.Built> built = state.assault(team);
        if (built.isEmpty() || built.get().gates().isEmpty()) {
            return false;
        }
        ServerLevel level = server.getLevel(built.get().dimension());
        if (level == null) {
            return false;
        }
        UUID gate = built.get().gates().getFirst();
        Entity old = level.getEntity(gate);
        Vec3 where = gateAt(state, team).orElseGet(() -> old != null ? old.position() : Vec3.ZERO);
        Optional<UUID> opened = Gates.get().open(level, where, summoner(server, level, team), wave - 1);
        state.replaceGate(team, gate, opened);
        state.noteWave(team, wave);
        if (old != null) {
            old.discard();
        }
        LOGGER.info("Set team {}'s Assault gate to wave {}: {}", team, wave, opened.map(UUID::toString).orElse("none"));
        return true;
    }

    /** Notes that the Assault's gate {@code gate} started wave {@code wave}, for the team's bar. A gate of no Assault is left alone. */
    public static void noteWave(ServerLevel level, UUID gate, int wave) {
        AssaultState state = AssaultState.get(level.getServer());
        state.teamOf(gate).ifPresent(team -> state.noteWave(team, wave));
    }

    /** The side's name, as "north", for the team's bar and the action bar's word that the gate moved. */
    public static Component sideName(Direction side) {
        return Component.translatable(MechanicalMasteryReborn.MOD_ID + ".assault.side." + side.getSerializedName());
    }

    /** Where {@code team}'s gate stands on the side it is on now, if its C.O.D. and side are known. */
    public static Optional<Vec3> gateAt(AssaultState state, UUID team) {
        return state.cod(team).flatMap(cod -> state.assault(team).flatMap(AssaultState.Built::side).map(side -> gateAt(cod.pos(), side)));
    }

    /**
     * Builds the platform {@link #OFFSET} blocks off C.O.D. at {@code cod} toward {@code side}, level with the ground,
     * and its bridge back toward C.O.D. until the first block in its way, and says the platform's middle.
     */
    private static BlockPos buildSide(ServerLevel level, BlockPos cod, Direction side, List<BlockPos> built) {
        BlockPos middle = cod.relative(side, OFFSET).below();
        Direction across = side.getClockWise();
        for (int along = -PLATFORM_RADIUS; along <= PLATFORM_RADIUS; along++) {
            for (int wide = -PLATFORM_RADIUS; wide <= PLATFORM_RADIUS; wide++) {
                place(level, middle.relative(side, along).relative(across, wide), built);
            }
        }
        for (int step = OFFSET - PLATFORM_RADIUS - 1; step > 0; step--) {
            BlockPos lane = cod.below().relative(side, step);
            if (!level.getBlockState(lane).isAir()) {
                break;
            }
            for (int wide = -BRIDGE_RADIUS; wide <= BRIDGE_RADIUS; wide++) {
                place(level, lane.relative(across, wide), built);
            }
        }
        return middle;
    }

    /** Where {@code side}'s gate stands, off C.O.D. at {@code cod}: on its platform's middle. */
    public static Vec3 gateAt(BlockPos cod, Direction side) {
        return Vec3.atBottomCenterOf(cod.relative(side, OFFSET));
    }

    /** The gate entity whose wave {@code entity} came from, by Gateways' mark, if any. */
    public static Optional<UUID> owner(Entity entity) {
        return entity.getPersistentData().read(OWNER_KEY, UUIDUtil.CODEC);
    }

    /** Marks {@code entity} as a mob of the gate {@code gate}, as Gateways does, for the game tests. */
    public static void markOwner(Entity entity, UUID gate) {
        entity.getPersistentData().store(OWNER_KEY, UUIDUtil.CODEC, gate);
    }

    private static void place(ServerLevel level, BlockPos pos, List<BlockPos> built) {
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, STONE.get().defaultBlockState(), Block.UPDATE_ALL);
            built.add(pos.immutable());
        }
    }

    /** One of {@code team}'s players online on its island, or else anywhere, or none. */
    static @Nullable ServerPlayer summoner(MinecraftServer server, ServerLevel level, UUID team) {
        ServerPlayer anywhere = null;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!Teams.of(player).equals(team)) {
                continue;
            }
            if (player.level() == level && Teams.at(level, player.blockPosition()).equals(team)) {
                return player;
            }
            if (anywhere == null) {
                anywhere = player;
            }
        }
        return anywhere;
    }
}
