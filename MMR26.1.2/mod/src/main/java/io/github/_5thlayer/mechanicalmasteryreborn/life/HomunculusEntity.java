// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.Comparator;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;

/**
 * The Homunculus (the line's glossary): the made figure whose belly its {@link HomunculusStandBlockEntity} keeps.
 * Awake, it walks within the server config's range of its stand, and comes back to a space by the stand's base to eat, to
 * fall asleep and, hurt, to heal 1 health every 40 ticks. It stands a statue, frozen, grey and facing its stand, while it
 * sleeps, while its team's life chain is locked, and for good once its stand is gone; a player's blow breaks a statue.
 * Dead, it leaves only its Vital Spark, spent. Mobs treat it as a passive mob, and it makes no sound. Until it clicks
 * (#368), each work is a swing of its arm toward its stand.
 */
public final class HomunculusEntity extends PathfinderMob {

    /** What it shows: its stand's {@link HomunculusStandBlockEntity.Doing}, or {@link #STANDLESS}. */
    private static final EntityDataAccessor<Integer> DOING = SynchedEntityData.defineId(HomunculusEntity.class, EntityDataSerializers.INT);
    /** The seconds its belly keeps it working, for Jade. */
    private static final EntityDataAccessor<Integer> FED = SynchedEntityData.defineId(HomunculusEntity.class, EntityDataSerializers.INT);

    /** Its {@link #DOING} once its stand is gone. */
    public static final int STANDLESS = HomunculusStandBlockEntity.Doing.values().length;

    private static final int HEAL_INTERVAL = 40;
    private static final int TICKS_PER_SECOND = 20;
    private static final String STAND = "stand";

    private @Nullable BlockPos stand;
    private int healIn = HEAL_INTERVAL;

    public HomunculusEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setSilent(true);
    }

    /** 12 health, at a small mob's pace. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DOING, HomunculusStandBlockEntity.Doing.ASLEEP.ordinal());
        builder.define(FED, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new ComeToStandGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** Kindled beside the stand whose base is at {@code base}. */
    void boundTo(BlockPos base) {
        stand = base.immutable();
        setHomeTo(stand, ServerConfig.homunculusRangeAcross());
        faceStand();
    }

    /** What it shows: an ordinal of {@link HomunculusStandBlockEntity.Doing}, or {@link #STANDLESS}. */
    public int doing() {
        return entityData.get(DOING);
    }

    /** Standing as a statue: asleep, locked or standless. */
    public boolean isStatue() {
        return doing() != HomunculusStandBlockEntity.Doing.AWAKE.ordinal();
    }

    /** The seconds its belly keeps it working, as Jade shows them. */
    public int fedSeconds() {
        return entityData.get(FED);
    }

    /** Its stand's base, or null once it is gone. */
    public @Nullable BlockPos stand() {
        return stand;
    }

    /** Its stand, loaded and still its own, or null. */
    @Nullable HomunculusStandBlockEntity standEntity() {
        return stand != null && level().isLoaded(stand) && level().getBlockEntity(stand) instanceof HomunculusStandBlockEntity entity
                && entity.isBoundTo(getUUID()) ? entity : null;
    }

    /** Whether it stands on one of the spaces around its stand's base. */
    public boolean atStand() {
        if (stand == null) {
            return false;
        }
        var at = blockPosition();
        return Math.abs(at.getX() - stand.getX()) <= 1 && Math.abs(at.getZ() - stand.getZ()) <= 1 && Math.abs(at.getY() - stand.getY()) <= 1;
    }

    /** Whether it wants to be at its stand: awake, and hungry or hurt. */
    boolean wantsStand() {
        var entity = standEntity();
        return !isStatue() && entity != null && (entity.hungry() || getHealth() < getMaxHealth());
    }

    /** A work, until it clicks: a swing of its arm toward its stand. */
    void worked() {
        faceStand();
        swing(InteractionHand.MAIN_HAND);
    }

    private void faceStand() {
        if (stand != null) {
            double dx = stand.getX() + 0.5 - getX();
            double dz = stand.getZ() + 0.5 - getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90F;
            setYRot(yaw);
            setYHeadRot(yaw);
            setYBodyRot(yaw);
        }
    }

    /** Within its range of its stand: across, as a mob's home, and only so far up or down. */
    @Override
    public boolean isWithinHome(BlockPos pos) {
        return super.isWithinHome(pos) && (stand == null || Math.abs(pos.getY() - stand.getY()) <= ServerConfig.homunculusRangeUp());
    }

    @Override
    public boolean isWithinHome(Vec3 pos) {
        return isWithinHome(BlockPos.containing(pos));
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel && isAlive()) {
            follow();
        }
    }

    /** Shows what its stand says it is doing, and heals while awake at it. */
    private void follow() {
        if (stand == null) {
            show(STANDLESS, 0);
            return;
        }
        // Its stand's chunk unloaded: it waits as it is.
        if (!level().isLoaded(stand)) {
            return;
        }
        var entity = standEntity();
        if (entity == null) {
            // Broken: it stands a statue for good.
            stand = null;
            clearHome();
            show(STANDLESS, 0);
            return;
        }
        show(entity.doing().ordinal(), Mth.positiveCeilDiv(entity.workLeft(), TICKS_PER_SECOND));
        if (!isStatue() && atStand() && getHealth() < getMaxHealth()) {
            if (--healIn <= 0) {
                healIn = HEAL_INTERVAL;
                heal(1F);
            }
        } else {
            healIn = HEAL_INTERVAL;
        }
    }

    private void show(int doing, int fedSeconds) {
        if (doing != doing()) {
            boolean wasStatue = isStatue();
            entityData.set(DOING, doing);
            if (isStatue() && !wasStatue) {
                getNavigation().stop();
                faceStand();
            }
        }
        entityData.set(FED, fedSeconds);
    }

    /** A statue stands still, and isn't pushed. */
    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isStatue();
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !isStatue();
    }

    /** A statue breaks at a player's blow. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return super.hurtServer(level, source, isStatue() && source.getEntity() instanceof Player ? Float.MAX_VALUE : damage);
    }

    /** Its Vital Spark, spent, and nothing else. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        spawnAtLocation(level, Homunculus.vitalSpark(true));
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
    }

    /** Dead, its stand is free for a new one. */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        releaseStand();
    }

    /** The same once it is gone for good some other way, such as {@code /kill}. */
    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy()) {
            releaseStand();
        }
        super.remove(reason);
    }

    private void releaseStand() {
        var entity = level().isClientSide() ? null : standEntity();
        if (entity != null) {
            entity.release(getUUID());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.storeNullable(STAND, BlockPos.CODEC, stand);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        stand = input.read(STAND, BlockPos.CODEC).orElse(null);
        if (stand != null) {
            setHomeTo(stand, ServerConfig.homunculusRangeAcross());
        } else {
            entityData.set(DOING, STANDLESS);
        }
    }

    /** The spaces around a stand's base, at its height, that a Homunculus can stand on: clear, two blocks up, over a floor. */
    static Optional<BlockPos> spaceBeside(BlockGetter level, BlockPos base, Vec3 nearest) {
        return BlockPos.betweenClosedStream(base.offset(-1, 0, -1), base.offset(1, 0, 1))
                .filter(pos -> !(pos.getX() == base.getX() && pos.getZ() == base.getZ()))
                .filter(pos -> standsOn(level, pos))
                .map(BlockPos::immutable)
                .min(Comparator.comparingDouble(pos -> Vec3.atBottomCenterOf(pos).distanceToSqr(nearest)));
    }

    private static boolean standsOn(BlockGetter level, BlockPos pos) {
        var below = pos.below();
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
