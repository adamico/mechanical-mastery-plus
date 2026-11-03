// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * The protection handicap's Assault (ADR 0010), for now one gate (#237): when a team starts the protection challenge,
 * a platform of {@link #STONE} in the void {@link #OFFSET} blocks east of its C.O.D., level with the ground C.O.D.
 * stands on, a bridge of it running west toward C.O.D. until the first block in its way, and on the platform the
 * pack's endless gate ({@link Gates}), summoned by one of the team's players on its island, so its mobs hunt the team.
 * A win or a reset removes the gate, what was built and the gate's mobs still around; the team keeps its Rift Shards.
 * A gate or mob in a chunk no one is near can't be seen as the Assault ends, so it is kept out when it loads again.
 * Each piece is built only into air and removed only where it still stands, so nothing else is ever touched.
 */
public final class Assault {

    /** How far east of C.O.D. the platform's middle is. */
    public static final int OFFSET = 48;
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
        if (state.closed(entity.getUUID()) || owner(entity).filter(state::closed).isPresent()) {
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
        BlockPos middle = codPos.offset(OFFSET, -1, 0);
        List<BlockPos> built = new ArrayList<>();
        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                place(level, middle.offset(dx, 0, dz), built);
            }
        }
        for (int x = middle.getX() - PLATFORM_RADIUS - 1; x > codPos.getX(); x--) {
            BlockPos lane = new BlockPos(x, middle.getY(), middle.getZ());
            if (!level.getBlockState(lane).isAir()) {
                break;
            }
            for (int dz = -BRIDGE_RADIUS; dz <= BRIDGE_RADIUS; dz++) {
                place(level, lane.offset(0, 0, dz), built);
            }
        }
        Optional<UUID> gate = Gates.get().open(level, Vec3.atBottomCenterOf(middle.above()), summoner(server, level, team));
        state.putAssault(team, new AssaultState.Built(level.dimension(), built, gate));
        AssaultExplosions.lift(server, team);
        LOGGER.info("Sent team {} its Assault: {} blocks built, gate {}", team, built.size(), gate.map(UUID::toString).orElse("none"));
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
            built.gate().ifPresent(gate -> {
                List<Entity> removed = new ArrayList<>();
                for (Entity entity : level.getAllEntities()) {
                    if (entity.getUUID().equals(gate) || owner(entity).filter(gate::equals).isPresent()) {
                        removed.add(entity);
                    }
                }
                removed.forEach(Entity::discard);
            });
            for (BlockPos pos : built.blocks()) {
                if (level.getBlockState(pos).is(STONE.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            LOGGER.info("Removed team {}'s Assault", team);
        });
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
    private static @Nullable ServerPlayer summoner(MinecraftServer server, ServerLevel level, UUID team) {
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
