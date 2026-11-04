// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.jspecify.annotations.Nullable;

/**
 * What an Assault's mobs break on their team's island drops nothing and is held for the team's refund (#259, ADR
 * 0015): a blast's blocks, each as a silk-touch pickaxe would mine it, what spills from them and what pops off as they
 * go, and the item frames, paintings and armour stands it knocks down, with what they hold. Its endermen never pick up a
 * block and its blazes' fireballs never set one alight, so a blast is the one way it breaks the base. Anything else —
 * the team's own blasts, a home gate's, another island's blocks, sand that falls — is left to the game.
 */
public final class AssaultBreaks {

    /** The team whose refund the drops spawned now are held for, while a blast of its Assault is taken over. */
    private static @Nullable UUID holding;

    private AssaultBreaks() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ExplosionEvent.Detonate.class, AssaultBreaks::onDetonate);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EntityJoinLevelEvent.class, AssaultBreaks::onJoin);
        NeoForge.EVENT_BUS.addListener(EntityMobGriefingEvent.class, AssaultBreaks::onGrief);
        Refunds.register();
    }

    /** The team whose running Assault {@code mob} came from, if any. */
    public static Optional<UUID> teamOf(MinecraftServer server, @Nullable Entity mob) {
        return mob == null ? Optional.empty() : Assault.owner(mob).flatMap(AssaultState.get(server)::openedBy);
    }

    /**
     * Takes over the blocks and the frames, paintings and armour stands a blast of {@code team}'s Assault reaches on
     * its island: the explosion no longer breaks them, and this breaks them instead, holding what they give.
     */
    private static void onDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ServerExplosion explosion = event.getExplosion();
        Entity source = explosion.getDirectSourceEntity() != null ? explosion.getDirectSourceEntity() : explosion.getIndirectSourceEntity();
        Optional<UUID> found = teamOf(level.getServer(), source);
        if (found.isEmpty()) {
            return;
        }
        UUID team = found.get();
        List<BlockPos> blocks = new ArrayList<>();
        if (explosion.getBlockInteraction() != Explosion.BlockInteraction.KEEP) {
            event.getAffectedBlocks().removeIf(pos -> {
                if (ours(level, pos, team)) {
                    blocks.add(pos.immutable());
                    return true;
                }
                return false;
            });
        }
        List<Entity> knocked = new ArrayList<>();
        event.getAffectedEntities().removeIf(entity -> {
            if (counted(explosion, entity) && ours(level, entity.blockPosition(), team) && reached(explosion, entity)) {
                knocked.add(entity);
                return true;
            }
            return false;
        });
        if (blocks.isEmpty() && knocked.isEmpty()) {
            return;
        }
        Refunds refunds = Refunds.get(level.getServer());
        ItemStack tool = silkTouch(level);
        holding = team;
        try {
            for (BlockPos pos : blocks) {
                // Read now, not as the blast found it: a block this one's removal took with it, as a door's other half,
                // is gone already, and gave what it gives as it went.
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) {
                    continue;
                }
                Block.getDrops(state, level, pos, level.getBlockEntity(pos), null, tool).forEach(drop -> refunds.hold(team, drop));
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            knocked.forEach(entity -> knockDown(level, entity, team));
            if (!blocks.isEmpty()) {
                // A frame or painting hung on a block the blast took falls with it.
                AABB around = AABB.encapsulatingFullBlocks(blocks.getFirst(), blocks.getFirst());
                for (BlockPos pos : blocks) {
                    around = around.minmax(new AABB(pos));
                }
                for (HangingEntity hanging : level.getEntitiesOfClass(HangingEntity.class, around.inflate(1.0),
                        hanging -> !hanging.isRemoved() && !hanging.survives() && ours(level, hanging.blockPosition(), team))) {
                    knockDown(level, hanging, team);
                }
            }
        } finally {
            holding = null;
        }
    }

    /** Holds what a drop spawned while a blast is taken over gives, for the team's refund, instead of letting it fall. */
    private static void onJoin(EntityJoinLevelEvent event) {
        UUID team = holding;
        if (team != null && event.getEntity() instanceof ItemEntity item && event.getLevel() instanceof ServerLevel level) {
            Refunds.get(level.getServer()).hold(team, item.getItem());
            event.setCanceled(true);
        }
    }

    /** An Assault's enderman picks up no block and its blaze's fireball lights none. */
    private static void onGrief(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if ((entity instanceof EnderMan || entity instanceof Blaze) && entity.level() instanceof ServerLevel level
                && teamOf(level.getServer(), entity).isPresent()) {
            event.setCanGrief(false);
        }
    }

    /** Whether {@code pos} is on {@code team}'s island itself. */
    private static boolean ours(ServerLevel level, BlockPos pos, UUID team) {
        return Teams.onIsland(level, pos) && Teams.at(level, pos).equals(team);
    }

    /**
     * Whether a blast knocks {@code entity} down as part of the base: an item frame or a painting, if the blast breaks
     * such things at all, or an armour stand.
     */
    private static boolean counted(ServerExplosion explosion, Entity entity) {
        return entity instanceof HangingEntity ? explosion.shouldAffectBlocklikeEntities() : entity instanceof ArmorStand stand && !stand.isMarker();
    }

    /** Whether the blast would hit {@code entity}, as the explosion itself judges it. */
    private static boolean reached(ServerExplosion explosion, Entity entity) {
        if (entity.ignoreExplosion(explosion)) {
            return false;
        }
        double reach = explosion.radius() * 2.0;
        return Math.sqrt(entity.distanceToSqr(explosion.center())) <= reach && ServerExplosion.getSeenPercent(explosion.center(), entity) > 0.0F;
    }

    /** Knocks {@code entity} down, holding it and what it holds for {@code team}'s refund. */
    private static void knockDown(ServerLevel level, Entity entity, UUID team) {
        if (entity instanceof HangingEntity hanging) {
            // A frame drops itself and what it holds, a painting itself; held as they spawn.
            hanging.dropItem(level, null);
        } else if (entity instanceof ArmorStand stand) {
            Refunds refunds = Refunds.get(level.getServer());
            refunds.hold(team, stand.getPickResult());
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                refunds.hold(team, stand.getItemBySlot(slot));
            }
        }
        entity.discard();
    }

    /** What a careful member mines a block with: a pickaxe with silk touch. */
    private static ItemStack silkTouch(ServerLevel level) {
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        tool.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        return tool;
    }
}
