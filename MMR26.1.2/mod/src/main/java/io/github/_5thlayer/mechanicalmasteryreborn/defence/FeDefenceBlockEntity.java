// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.defence;

import java.util.Comparator;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import org.jspecify.annotations.Nullable;

/**
 * An FE defence's store and clock, as an FE drive's are. The store takes FE from any side, holds
 * {@code defence.fe.store_shots} shots' worth, and gives none back. Every {@code defence.fe.interval} ticks, when the
 * store holds a shot's worth ({@code defence.fe.fe_per_shot}) and a hostile mob ({@link Enemy}) is in reach and in
 * sight, it shoots the nearest creeper, or else the nearest, and spends it. Players and passive mobs are never {@link Enemy}, so never targets; a
 * defence with nothing to shoot spends nothing, so an idle one fills up and then takes no more.
 */
public final class FeDefenceBlockEntity extends BlockEntity {

    /** What a defence is doing, for Jade. */
    public enum Status {
        /** It has a shot's worth and shoots whatever comes in reach. */
        READY,
        /** Its store holds less than a shot's worth: it does nothing until it is fed. */
        NO_FE
    }

    /** Its shots' damage, its own so no mob's resistance to magic, such as a witch's, softens it (#247). */
    public static final ResourceKey<DamageType> ELECTRIC = ResourceKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "electric"));

    private static final String STORE_KEY = "store";
    private static final String COOLDOWN_KEY = "cooldown";

    private final Store store = new Store();
    /** Ticks until it may shoot again. */
    private int cooldown;

    public FeDefenceBlockEntity(BlockPos pos, BlockState state) {
        super(Defences.FE_DEFENCE_ENTITY.get(), pos, state);
    }

    /** The store as a cable reaches it: insert only. */
    public EnergyHandler face() {
        return store;
    }

    public Status status() {
        return store.getAmountAsInt() < ServerConfig.feDefenceFePerShot() ? Status.NO_FE : Status.READY;
    }

    public int stored() {
        return store.getAmountAsInt();
    }

    public int capacity() {
        return store.getCapacityAsInt();
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, FeDefenceBlockEntity defence) {
        defence.store.fit();
        if (defence.cooldown > 0) {
            defence.cooldown--;
            return;
        }
        int cost = ServerConfig.feDefenceFePerShot();
        if (defence.store.getAmountAsInt() < cost) {
            return;
        }
        ServerLevel server = (ServerLevel) level;
        LivingEntity target = target(server, pos);
        if (target == null) {
            return;
        }
        defence.store.set(defence.store.getAmountAsInt() - cost);
        // This tick counts: the next shot is the interval away.
        defence.cooldown = ServerConfig.feDefenceInterval() - 1;
        shoot(server, pos, target);
    }

    /**
     * The nearest living creeper within reach that the defence can see, since its blast costs the island the most (#241),
     * or else the nearest such hostile mob, or none.
     */
    static @Nullable LivingEntity target(ServerLevel level, BlockPos pos) {
        Vec3 centre = Vec3.atCenterOf(pos);
        int range = ServerConfig.feDefenceRange();
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(range),
                        entity -> entity instanceof Enemy && entity.isAlive() && !entity.isInvulnerable()
                                && entity.distanceToSqr(centre) <= (double) range * range && sees(level, centre, entity))
                .stream()
                .min(Comparator.<LivingEntity>comparingInt(entity -> entity instanceof Creeper ? 0 : 1)
                        .thenComparingDouble(entity -> entity.distanceToSqr(centre)))
                .orElse(null);
    }

    /** Whether a ray from the defence's face toward {@code entity}'s eyes reaches it without hitting a block. */
    private static boolean sees(ServerLevel level, Vec3 centre, LivingEntity entity) {
        Vec3 eyes = entity.getEyePosition();
        // Start the ray outside the defence's own block, which it would otherwise hit first.
        Vec3 from = centre.add(eyes.subtract(centre).normalize().scale(0.9));
        var hit = level.clip(new ClipContext(from, eyes, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS;
    }

    private static void shoot(ServerLevel level, BlockPos pos, LivingEntity target) {
        target.hurtServer(level, level.damageSources().source(ELECTRIC), ServerConfig.feDefenceDamage());
        Vec3 from = Vec3.atCenterOf(pos);
        Vec3 to = target.getEyePosition();
        int steps = Math.max(1, (int) (from.distanceTo(to) * 2));
        for (int step = 1; step <= steps; step++) {
            Vec3 at = from.lerp(to, (double) step / steps);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 0.6F, 1.6F);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        store.serialize(output.child(STORE_KEY));
        output.putInt(COOLDOWN_KEY, cooldown);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        store.deserialize(input.childOrEmpty(STORE_KEY));
        cooldown = input.getIntOr(COOLDOWN_KEY, 0);
    }

    /** The store: as many shots' worth as the config says, which it follows, and nothing out. */
    private final class Store extends SimpleEnergyHandler {

        Store() {
            super(0, 0, 0);
            fit();
        }

        /** Sizes the store to the config, which may have changed since it was made, spilling what no longer fits. */
        void fit() {
            capacity = ServerConfig.feDefenceFePerShot() * ServerConfig.feDefenceStoreShots();
            maxInsert = capacity;
            if (energy > capacity) {
                set(capacity);
            }
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    }
}
