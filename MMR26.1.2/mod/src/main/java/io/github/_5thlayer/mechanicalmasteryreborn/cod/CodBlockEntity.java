// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * A placed C.O.D. Placed, its reserve holds a stack of Advanced essence, so a new island has no wait. While
 * dormant it makes one more each {@link ServerConfig#codDormantInterval interval} its reserve holds less than
 * a stack, and nothing while it holds one: a steady pace, never an unlimited supply, and it puts nothing out of
 * itself. What it makes while dormant never counts toward Inflation.
 *
 * <p>Once its team is awake ({@link TeamState}), it makes one each {@link ServerConfig#codWakingInterval waking
 * interval} without stop, every one counted for the team as made. Each goes into the reserve, which pushes into
 * whatever takes items beside it; once the reserve is full, onto a pile on its top, of up to a stack, unless a
 * block stands there; past that, nowhere, so the pile shows the team it is falling behind and lag stays bounded.
 * A challenge's faster pace is #168's.
 */
public final class CodBlockEntity extends BlockEntity implements MenuProvider {

    private static final String RESERVE_KEY = "reserve";
    private static final String TICKS_TOWARD_NEXT_KEY = "ticks_toward_next";

    private final CodReserve reserve = new CodReserve(() -> Essences.item(Essence.ADVANCED), this::setChanged);
    /**
     * Ticks toward the next essence; none while the reserve holds a stack, so an emptied one starts afresh. Saved
     * whenever anything else marks C.O.D. changed, as making an essence does, rather than every tick.
     */
    private int ticksTowardNext;
    /** Whether its team is awake, as of its last tick; the team's state holds it, so it is never saved here. */
    private boolean awake;
    /** The team whose island it stands on, found on its first tick: a C.O.D. never moves. */
    private @Nullable UUID team;
    /** What takes items on each side, found once it first pushes. */
    private @Nullable Map<Direction, BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction>> neighbours;

    public CodBlockEntity(BlockPos pos, BlockState state) {
        super(Cod.BLOCK_ENTITY.get(), pos, state);
        reserve.fill();
    }

    /** The reserve, as automation and the menu reach it: extract only. */
    public ResourceHandler<ItemResource> reserve() {
        return reserve;
    }

    CodReserve reserveSlot() {
        return reserve;
    }

    /** Whether C.O.D. is making essence: always once awake, and while dormant only while its reserve holds less than a stack. */
    public boolean isMaking() {
        return awake || !reserve.isFull();
    }

    /** The ticks it takes to make one essence, at its waking pace once awake. */
    public int interval() {
        return awake ? ServerConfig.codWakingInterval() : ServerConfig.codDormantInterval();
    }

    /** The ticks done toward the next essence, out of {@link #interval}; none while it isn't making. */
    public int ticksTowardNext() {
        return ticksTowardNext;
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, CodBlockEntity cod) {
        var server = (ServerLevel) level;
        if (cod.team == null) {
            cod.team = Teams.at(server, pos);
        }
        UUID team = cod.team;
        TeamState teams = TeamState.get(server.getServer());
        cod.awake = teams.isAwake(team);
        if (cod.awake) {
            cod.push(server);
        }
        if (cod.tick(cod.interval()) && cod.awake) {
            teams.countMade(team);
        }
    }

    /** Counts a tick toward the next essence, and says whether it made one. */
    private boolean tick(int interval) {
        if (!awake && reserve.isFull()) {
            ticksTowardNext = 0;
            return false;
        }
        if (++ticksTowardNext < interval) {
            return false;
        }
        ticksTowardNext = 0;
        if (!reserve.isFull()) {
            reserve.makeOne();
        } else {
            spill();
        }
        return true;
    }

    /** Pushes what the reserve holds into whatever takes items beside it. */
    private void push(ServerLevel server) {
        if (neighbours == null) {
            neighbours = new EnumMap<>(Direction.class);
            for (Direction side : Direction.values()) {
                neighbours.put(side, BlockCapabilityCache.create(Capabilities.Item.BLOCK, server, worldPosition.relative(side), side.getOpposite()));
            }
        }
        for (var neighbour : neighbours.values()) {
            if (reserve.isEmpty()) {
                return;
            }
            ResourceHandlerUtil.move(reserve, neighbour.getCapability(), resource -> true, Integer.MAX_VALUE, null);
        }
    }

    /** Drops one essence onto the pile on top, unless the pile already holds a stack or a block stands there. */
    private void spill() {
        BlockPos top = worldPosition.above();
        if (level == null || !level.getBlockState(top).getCollisionShape(level, top).isEmpty()) {
            return;
        }
        Item essence = Essences.item(Essence.ADVANCED);
        int piled = 0;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(top).inflate(1, 0.5, 1))) {
            piled += item.getItem().is(essence) ? item.getItem().getCount() : 0;
        }
        if (piled < essence.getDefaultMaxStackSize()) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                    new ItemStack(essence), 0, 0, 0));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        reserve.serialize(output.child(RESERVE_KEY));
        output.putInt(TICKS_TOWARD_NEXT_KEY, ticksTowardNext);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        reserve.deserialize(input.childOrEmpty(RESERVE_KEY));
        ticksTowardNext = input.getIntOr(TICKS_TOWARD_NEXT_KEY, 0);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CodMenu(containerId, inventory, this);
    }
}
