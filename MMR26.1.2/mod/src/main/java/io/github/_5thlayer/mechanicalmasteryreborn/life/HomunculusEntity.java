// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;

/**
 * The Homunculus (the line's glossary): the made figure whose belly its {@link HomunculusStandBlockEntity} keeps.
 * Awake, it walks within the server config's range of its stand, and comes back to a space by the stand's base to eat, to
 * fall asleep and, hurt, to heal 1 health every 40 ticks. It stands a statue, frozen, grey and facing its stand, while it
 * sleeps, while its team's life chain is locked, and for good once its stand is gone; a player's blow breaks a statue.
 * Dead, it leaves its Vital Spark, spent, and what it holds. Mobs treat it as a passive mob, and it makes no sound.
 *
 * <p>Awake, it works (#368) the nearest block to its stand among those in {@link Homunculus#WORKS}, within its range, that
 * it can reach: one with a space beside it, level with it or one below, that it can walk to. With an empty hand it walks to
 * the Supply a player assigned it ({@link SupplyAssignment}) and takes one item, from the first slot that has any; holding it,
 * it walks to the space and, at each work its stand calls for, right-clicks the centre of the block's face toward it through
 * the player's own click path, never sneaking, as a fake player that carries its kindler's id, so a claim lets it work where
 * its kindler may. It holds what the click leaves in its hand, and drops by the block whatever else the click hands it; what
 * the block throws out it leaves where it falls. It never clicks empty-handed: with no item to take, it has no work.
 *
 * <p>Before any click, it sets a meal down on the Table a player assigned it, while the Table is clear and it has a meal,
 * in {@link Homunculus#PLACES}, in its off hand or its Supply: it takes the meal from the first slot that has one into its
 * off hand, walks to a space beside the Table and, at a work, places it on the block below as a sneaking player does, facing
 * it, through the same click path. It takes its main hand's item from the first slot that holds no meal.
 *
 * <p>A click its block refuses, doing nothing, as a feast does an item that isn't its container, puts the item back in its
 * Supply, and it takes another: it never takes an item that block refused again, until another block stands there.
 */
public final class HomunculusEntity extends PathfinderMob {

    /** What it shows: its stand's {@link HomunculusStandBlockEntity.Doing}, or {@link #STANDLESS}. */
    private static final EntityDataAccessor<Integer> DOING = SynchedEntityData.defineId(HomunculusEntity.class, EntityDataSerializers.INT);
    /** The seconds its belly keeps it working, for Jade. */
    private static final EntityDataAccessor<Integer> FED = SynchedEntityData.defineId(HomunculusEntity.class, EntityDataSerializers.INT);

    /** Whether its stand has a Supply, for Jade. */
    private static final EntityDataAccessor<Boolean> SUPPLIED = SynchedEntityData.defineId(HomunculusEntity.class, EntityDataSerializers.BOOLEAN);

    /** Its {@link #DOING} once its stand is gone. */
    public static final int STANDLESS = HomunculusStandBlockEntity.Doing.values().length;

    private static final int HEAL_INTERVAL = 40;
    private static final int TICKS_PER_SECOND = 20;
    private static final String STAND = "stand";
    /** Ticks between its looks for the block it works and whether its Supply has an item it can fetch. */
    private static final int WORK_LOOK_INTERVAL = 40;
    /** Ticks a work waits for it to get to its click before it is spent, and it looks again. */
    private static final int MAX_WAIT = 200;
    /** How near the centre of its Supply it takes from it, squared: within two blocks. */
    private static final double SUPPLY_REACH_SQR = 4.0;
    /** The name its fake player goes by; its id is its kindler's. */
    private static final String FAKE_NAME = "[Homunculus]";

    /** What a work its stand calls for comes to. */
    public enum Work {
        /** It clicked. */
        CLICKED,
        /** It is on its way to click: the work waits for it. */
        WAITING,
        /** It has nothing to click: the work is spent. */
        SPENT
    }

    private @Nullable BlockPos stand;
    private int healIn = HEAL_INTERVAL;
    /** The block it works, the space it clicks from and the face it clicks, or null with none it can reach. */
    private @Nullable BlockPos target;
    private @Nullable BlockPos targetSpace;
    private Direction targetFace = Direction.NORTH;
    private int nextLook;
    /** Whether its Supply, at its last look, held an item for its empty main hand, and it could walk there. */
    private boolean toolReady;
    /** Whether its Supply, at its last look, held a meal for its empty off hand, and it could walk there. */
    private boolean mealReady;
    /** The space beside its Table it sets a meal down from, at its last look, with the Table clear; or null. */
    private @Nullable BlockPos placeSpace;
    /** The space beside its Supply it walks to, at its last look, or null. */
    private @Nullable BlockPos supplySpace;
    /** The block it works that refused items, and those items: forgotten once another block stands there. */
    private @Nullable BlockPos refusedAt;
    private @Nullable Block refusedBy;
    private final Set<Item> refused = new HashSet<>();
    /** Ticks the work its stand calls for has waited. */
    private int waited;
    /** Its own fake player, as its kindler: one each, so no two share an item's cooldown. */
    private @Nullable FakePlayer clicker;

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
        builder.define(SUPPLIED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new ComeToStandGoal(this));
        goalSelector.addGoal(1, new WorkGoal(this));
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

    /** Whether its stand has a Supply, as Jade shows it. */
    public boolean supplied() {
        return entityData.get(SUPPLIED);
    }

    /** Whether it has a meal to set down, or a block to work and an item to work it with, held or in its Supply. */
    boolean hasWork() {
        return standEntity() != null && (placing() || target != null && (!getMainHandItem().isEmpty() || toolReady));
    }

    /** Where it walks for its work: beside its Supply to fetch, beside its Table to set a meal down, or to its block's space. */
    @Nullable BlockPos walkTo() {
        if (placing()) {
            return getOffhandItem().isEmpty() ? supplySpace : placeSpace;
        }
        if (getMainHandItem().isEmpty()) {
            return toolReady ? supplySpace : null;
        }
        return targetSpace;
    }

    /** What it faces once it is there: the block under its Table, or the block it works; null while it walks to fetch. */
    @Nullable BlockPos faces() {
        var table = table();
        if (placing() && table != null) {
            return getOffhandItem().isEmpty() ? null : table.below();
        }
        return getMainHandItem().isEmpty() ? null : target;
    }

    /** Its stand's Table, or null. */
    private @Nullable BlockPos table() {
        var entity = standEntity();
        return entity == null ? null : entity.table();
    }

    /** Whether it has a meal to set down now: its Table clear and a space beside it found, and a meal held or in its Supply. */
    private boolean placing() {
        var table = table();
        return table != null && placeSpace != null && tableClear(table) && (!getOffhandItem().isEmpty() || mealReady);
    }

    private boolean tableClear(BlockPos table) {
        return level().isLoaded(table) && level().getBlockState(table).canBeReplaced() && Homunculus.ringed(level(), table.below());
    }

    /**
     * Does a work its stand calls for: a click if it stands at the block it works with an item in hand. One that waits past
     * {@link #MAX_WAIT} ticks, as when it can't land on its space, is spent, and it looks again.
     */
    Work work() {
        var work = tryWork();
        if (work == Work.WAITING && ++waited > MAX_WAIT) {
            nextLook = 0;
            work = Work.SPENT;
        }
        if (work != Work.WAITING) {
            waited = 0;
        }
        return work;
    }

    private Work tryWork() {
        var entity = standEntity();
        if (entity == null || entity.kindler() == null) {
            return Work.SPENT;
        }
        var table = entity.table();
        if (placing() && table != null) {
            if (getOffhandItem().isEmpty() || !blockPosition().equals(placeSpace)) {
                return Work.WAITING;
            }
            use((ServerLevel) level(), entity.kindler(), table.below(), Direction.UP, InteractionHand.OFF_HAND);
            // Its meal is a block it may work: it looks again at once.
            nextLook = 0;
            return Work.CLICKED;
        }
        if (target == null || targetSpace == null || !worksAt(target)) {
            return Work.SPENT;
        }
        if (getMainHandItem().isEmpty()) {
            return toolReady ? Work.WAITING : Work.SPENT;
        }
        if (!blockPosition().equals(targetSpace)) {
            return Work.WAITING;
        }
        var held = getMainHandItem().copy();
        if (!use((ServerLevel) level(), entity.kindler(), target, targetFace, InteractionHand.MAIN_HAND).consumesAction()) {
            refuse(entity, held);
        }
        return Work.CLICKED;
    }

    /**
     * Right-clicks the centre of {@code face} of {@code pos} with what it holds in {@code hand}, as {@code kindler}: with its
     * main hand never sneaking, a click; with its off hand sneaking, so the block doesn't take the click, a placing.
     */
    private InteractionResult use(ServerLevel level, UUID kindler, BlockPos pos, Direction face, InteractionHand hand) {
        face(pos);
        if (clicker == null || clicker.level() != level || !clicker.getUUID().equals(kindler)) {
            clicker = new FakePlayer(level, new GameProfile(kindler, FAKE_NAME));
        }
        FakePlayer player = clicker;
        Inventory inventory = player.getInventory();
        inventory.clearContent();
        player.snapTo(getX(), getY(), getZ(), getYRot(), 0F);
        player.setShiftKeyDown(hand == InteractionHand.OFF_HAND);
        player.setItemInHand(hand, getItemInHand(hand));
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).relative(face, 0.5), face, pos, false);
        var result = player.gameMode.useItemOn(player, level, player.getItemInHand(hand), hand, hit);
        setItemInHand(hand, player.getItemInHand(hand));
        player.setItemInHand(hand, ItemStack.EMPTY);
        player.setShiftKeyDown(false);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            var handed = inventory.removeItemNoUpdate(slot);
            if (!handed.isEmpty()) {
                Block.popResourceFromFace(level, pos, face, handed);
            }
        }
        swing(hand);
        return result;
    }

    /**
     * Its block refused {@code held}: it keeps from taking that item for the block again, and puts what it holds back in its
     * Supply if it is still {@code held}, all of it fitting, so it takes another at once.
     */
    private void refuse(HomunculusStandBlockEntity entity, ItemStack held) {
        if (target == null) {
            return;
        }
        forgetRefusals(target);
        refusedAt = target;
        refusedBy = level().getBlockState(target).getBlock();
        refused.add(held.getItem());
        var handler = entity.supplyHandler();
        var holds = getMainHandItem();
        if (handler == null || !ItemStack.isSameItemSameComponents(holds, held)) {
            return;
        }
        try (var transaction = Transaction.openRoot()) {
            if (handler.insert(ItemResource.of(holds), holds.getCount(), transaction) == holds.getCount()) {
                transaction.commit();
                setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                nextLook = 0;
            }
        }
    }

    /** Forgets the items refused, unless by the same block at {@code pos}. */
    private void forgetRefusals(@Nullable BlockPos pos) {
        if (refusedAt != null && (pos == null || !pos.equals(refusedAt) || !level().getBlockState(pos).is(refusedBy))) {
            refusedAt = null;
            refusedBy = null;
            refused.clear();
        }
    }

    /**
     * Looks at its stand's Supply: whether it holds an item for its empty main hand and a meal for its empty off hand, and the
     * space beside it it can walk to, which it keeps as {@link #supplySpace}: a path to the storage itself would end on top
     * of it, where a mob can't always stand.
     */
    private void lookAtSupply(HomunculusStandBlockEntity entity) {
        supplySpace = null;
        toolReady = false;
        mealReady = false;
        var handler = entity.supplyHandler();
        if (handler == null || firstFilled(handler) < 0) {
            return;
        }
        supplySpace = reachableBeside(entity.supply());
        if (supplySpace != null) {
            toolReady = getMainHandItem().isEmpty() && firstFilled(handler, false) >= 0;
            mealReady = getOffhandItem().isEmpty() && firstFilled(handler, true) >= 0;
        }
    }

    /** The first space beside {@code pos} it can walk to, nearest it first, or null. */
    private @Nullable BlockPos reachableBeside(@Nullable BlockPos pos) {
        if (pos == null) {
            return null;
        }
        for (var space : spacesBeside(pos)) {
            var path = getNavigation().createPath(space, 0);
            if (path != null && path.canReach()) {
                return space;
            }
        }
        return null;
    }

    /** The first slot of {@code handler} that holds anything, or -1. */
    private static int firstFilled(ResourceHandler<ItemResource> handler) {
        for (int slot = 0; slot < handler.size(); slot++) {
            if (!handler.getResource(slot).isEmpty() && handler.getAmountAsInt(slot) > 0) {
                return slot;
            }
        }
        return -1;
    }

    /** The first slot of {@code handler} that holds a meal, if {@code meal}, or anything else its block hasn't refused; or -1. */
    private int firstFilled(ResourceHandler<ItemResource> handler, boolean meal) {
        for (int slot = 0; slot < handler.size(); slot++) {
            var resource = handler.getResource(slot);
            if (!resource.isEmpty() && handler.getAmountAsInt(slot) > 0 && Homunculus.meal(resource.toStack(1)) == meal
                    && (meal || !refused.contains(resource.getItem()))) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Beside its Supply: with a meal to set down and an empty off hand, takes one meal into it; else, with an empty main hand,
     * one item that is no meal into that.
     */
    private void takeFromSupply(HomunculusStandBlockEntity entity) {
        var supply = entity.supply();
        if (supply == null || position().distanceToSqr(Vec3.atCenterOf(supply)) > SUPPLY_REACH_SQR) {
            return;
        }
        if (getOffhandItem().isEmpty() && placing()) {
            if (!take(entity, supply, InteractionHand.OFF_HAND)) {
                mealReady = false;
            }
        } else if (getMainHandItem().isEmpty() && !take(entity, supply, InteractionHand.MAIN_HAND)) {
            toolReady = false;
        }
    }

    /** Takes one item into {@code hand} from its Supply: a meal into its off hand, anything else into its main hand. */
    private boolean take(HomunculusStandBlockEntity entity, BlockPos supply, InteractionHand hand) {
        var handler = entity.supplyHandler();
        int slot = handler == null ? -1 : firstFilled(handler, hand == InteractionHand.OFF_HAND);
        if (slot < 0) {
            return false;
        }
        var resource = handler.getResource(slot);
        try (var transaction = Transaction.openRoot()) {
            if (handler.extract(slot, resource, 1, transaction) == 1) {
                transaction.commit();
                setItemInHand(hand, resource.toStack(1));
                face(supply);
                swing(hand);
            }
        }
        return true;
    }

    /**
     * Looks for the block it works: the nearest to its stand in {@link Homunculus#WORKS}, within its range, with a space
     * beside it, level with it or one below, that it can walk to, the space nearest it first.
     */
    private void lookForWork(HomunculusStandBlockEntity entity) {
        findTarget(entity);
        forgetRefusals(target);
        lookAtSupply(entity);
        var table = entity.table();
        placeSpace = table != null && tableClear(table) ? reachableBeside(table) : null;
    }

    /** The block it works, the space it clicks from and the face it clicks, as {@link #lookForWork} finds them. */
    private void findTarget(HomunculusStandBlockEntity entity) {
        target = null;
        targetSpace = null;
        int across = ServerConfig.homunculusRangeAcross();
        int up = ServerConfig.homunculusRangeUp();
        var base = entity.getBlockPos();
        var level = level();
        var found = BlockPos.betweenClosedStream(base.offset(-across, -up, -across), base.offset(across, up, across))
                .filter(pos -> level.isLoaded(pos) && worksAt(pos))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(base)))
                .toList();
        for (var pos : found) {
            for (var space : spacesBeside(pos)) {
                var path = getNavigation().createPath(space, 0);
                if (path != null && path.canReach()) {
                    target = pos;
                    targetSpace = space;
                    targetFace = Direction.getApproximateNearest(space.getX() - pos.getX(), 0, space.getZ() - pos.getZ());
                    return;
                }
            }
        }
    }

    /**
     * Whether the block at {@code pos} is one it works now: in {@link Homunculus#WORKS}, and, if it stores items, holding
     * some. An empty one, such as a Cutting Board, would take what it holds, as its click puts it there.
     */
    private boolean worksAt(BlockPos pos) {
        if (!level().getBlockState(pos).is(Homunculus.WORKS)) {
            return false;
        }
        var stored = level().getCapability(Capabilities.Item.BLOCK, pos, null);
        return stored == null || firstFilled(stored) >= 0;
    }

    /** The spaces it could stand on to reach {@code pos}, within its range: beside it, level or one below, nearest it first. */
    private List<BlockPos> spacesBeside(BlockPos pos) {
        var spaces = new ArrayList<BlockPos>();
        for (var side : Direction.Plane.HORIZONTAL) {
            for (var space : List.of(pos.relative(side), pos.relative(side).below())) {
                // A space its pathfinding walks on: any floor, a hopper's rim too, not only a sturdy top.
                if (isWithinHome(space) && WalkNodeEvaluator.getPathTypeStatic(this, space) == PathType.WALKABLE) {
                    spaces.add(space);
                }
            }
        }
        spaces.sort(Comparator.comparingDouble(space -> Vec3.atBottomCenterOf(space).distanceToSqr(position())));
        return spaces;
    }

    /** Faces {@code pos}. */
    void face(BlockPos pos) {
        double dx = pos.getX() + 0.5 - getX();
        double dz = pos.getZ() + 0.5 - getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90F;
        setYRot(yaw);
        setYHeadRot(yaw);
        setYBodyRot(yaw);
    }

    private void faceStand() {
        if (stand != null) {
            face(stand);
        }
    }

    /** Empty-handed and sneaking, a player assigns its Supply ({@link SupplyAssignment}). */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.isSecondaryUseActive() && player.getMainHandItem().isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer) {
                SupplyAssignment.toggle(serverPlayer, this);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
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
        entityData.set(SUPPLIED, entity.supply() != null);
        if (clicker != null) {
            // A fake player never ticks: its item cooldowns run out with its Homunculus's ticks.
            clicker.getCooldowns().tick();
        }
        if (!isStatue()) {
            if (tickCount >= nextLook) {
                nextLook = tickCount + WORK_LOOK_INTERVAL;
                lookForWork(entity);
            }
            takeFromSupply(entity);
        }
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

    /** Its Vital Spark, spent, and what it holds. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        spawnAtLocation(level, Homunculus.vitalSpark(true));
        for (var hand : InteractionHand.values()) {
            var held = getItemInHand(hand);
            if (!held.isEmpty()) {
                spawnAtLocation(level, held.copy());
                setItemInHand(hand, ItemStack.EMPTY);
            }
        }
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
