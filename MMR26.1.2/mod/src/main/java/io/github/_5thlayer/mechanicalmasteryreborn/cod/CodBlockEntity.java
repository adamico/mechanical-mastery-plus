// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.Overproduction;
import io.github._5thlayer.mechanicalmasteryreborn.record.LastStand;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * A placed C.O.D. It makes essence of its team's {@link TeamState#tier tier}, the highest the team hasn't blocked,
 * and nothing once all four are. Placed, its reserve holds a stack of Advanced essence, so a new island has no wait.
 * When the tier drops, what the reserve still holds stays for the team to take, and the new tier goes onto the pile
 * on its top, as an awake C.O.D.'s overflow does, until the reserve is emptied. While
 * dormant it makes one more each {@link ServerConfig#codDormantInterval interval} its reserve holds less than
 * a stack, and nothing while it holds one: a steady pace, never an unlimited supply, and it puts nothing out of
 * itself. What it makes while dormant never counts toward Overproduction.
 *
 * <p>Once its team is awake ({@link TeamState}), it makes one each {@link ServerConfig#codWakingInterval waking
 * interval} without stop, every one counted for the team as made. Each goes into the reserve, which pushes into
 * whatever takes items beside it; once the reserve is full, onto a pile on its top, of up to a stack, unless a
 * block stands there; past that, nowhere, so the pile shows the team it is falling behind and lag stays bounded.
 * While the team plays a challenge, each one it makes counts toward its {@link Overproduction}, and it makes them ever faster
 * with the time since the challenge began ({@link CodPace}), back at its waking pace once the challenge ends.
 *
 * <p>Once its team starts its last stand ({@link LastStands}), it makes Nullessence instead, whatever its team's tier, at
 * the {@link LastStand#rate rate} the team's pace sets, awake or not, every one counted against the team's Lead, and it
 * takes Quintessence on every side ({@link CodIntake}) and by hand. A hopper that feeds it is never pushed into. Once
 * the Lead reaches the margin it shuts down for good: it makes nothing more, takes nothing more, and stands dark
 * ({@link CodBlock#SHUT_DOWN}).
 */
public final class CodBlockEntity extends BlockEntity implements MenuProvider {

    private static final String RESERVE_KEY = "reserve";
    private static final String TICKS_TOWARD_NEXT_KEY = "ticks_toward_next";

    private final CodReserve reserve = new CodReserve(this::setChanged);
    private final CodIntake intake = new CodIntake(this);
    /** What automation reaches on every side: the reserve to take from, then the intake to put Quintessence into. */
    private final ResourceHandler<ItemResource> face = new CombinedResourceHandler<>(reserve, intake);
    /**
     * Ticks toward the next essence; none while the reserve holds a stack, so an emptied one starts afresh. Saved
     * whenever anything else marks C.O.D. changed, as making an essence does, rather than every tick.
     */
    private int ticksTowardNext;
    /** Whether its team is awake, as of its last tick; the team's state holds it, so it is never saved here. */
    private boolean awake;
    /** The tier it makes, as of its last tick; the team's state holds it, so it is never saved here. */
    private Optional<Essence> tier = Optional.of(Essence.ADVANCED);
    /** Whether its team plays a challenge, as of its last tick; the team's state holds it, so it is never saved here. */
    private boolean challenged;
    /** Its team's last stand, as of its last tick; the team's state holds it, so it is never saved here. */
    private Optional<LastStand> lastStand = Optional.empty();
    /** The team whose island it stands on, found on its first tick: a C.O.D. never moves. */
    private @Nullable UUID team;
    /** What takes items on each side, found once it first pushes. */
    private @Nullable Map<Direction, BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction>> neighbours;

    public CodBlockEntity(BlockPos pos, BlockState state) {
        super(Cod.BLOCK_ENTITY.get(), pos, state);
        reserve.fill(Essences.item(Essence.ADVANCED));
    }

    /** The reserve, as the menu reaches it: extract only. */
    public ResourceHandler<ItemResource> reserve() {
        return reserve;
    }

    /** What automation reaches on every side: the reserve, extract only, and the Quintessence intake, insert only. */
    public ResourceHandler<ItemResource> face() {
        return face;
    }

    CodReserve reserveSlot() {
        return reserve;
    }

    /** The tier it makes, the highest its team hasn't blocked; none once all four are. */
    public Optional<Essence> tier() {
        return tier;
    }

    /** Its team's last stand, once started, as of its last tick: on the server only. */
    public Optional<LastStand> lastStand() {
        return lastStand;
    }

    /**
     * What it makes: Nullessence while its team's last stand runs, nothing once it is won, and otherwise its team's tier,
     * none once all four are blocked.
     */
    public Optional<Essence> making() {
        if (lastStand.isPresent()) {
            return lastStand.get().won() ? Optional.empty() : Optional.of(Essence.NULLESSENCE);
        }
        return tier;
    }

    /** Whether it makes without stop, as once awake or in its last stand, rather than only up to a stack, as while dormant. */
    private boolean active() {
        return awake || lastStand.filter(LastStand::running).isPresent();
    }

    /**
     * Whether C.O.D. is making essence: never once all four tiers are blocked, outside its last stand, nor once that is
     * won; otherwise always once awake or in its last stand, and while dormant only until its reserve holds a stack of
     * its tier, so what an earlier tier left still lets it spill.
     */
    public boolean isMaking() {
        Optional<Essence> made = making();
        return made.isPresent() && (active() || !reserve.isFullOf(Essences.item(made.get())));
    }

    /**
     * Whether C.O.D. is awake but has nowhere to put what it makes: its reserve is full, after pushing into what is
     * beside it, and its top is covered or its pile holds a stack. It still counts each one as made.
     */
    public boolean isBlocked() {
        Optional<Essence> made = making();
        return active() && made.isPresent() && reserve.isFullFor(Essences.item(made.get())) && !canSpill();
    }

    /**
     * The ticks it takes to make one essence: at its waking pace once awake, and faster with the time since its team's
     * challenge began while it plays one; on the server only, so a client sees the waking pace. In its last stand, a
     * minute over its rate.
     */
    public int interval() {
        if (lastStand.isPresent()) {
            return lastStand.get().interval();
        }
        if (!awake) {
            return ServerConfig.codDormantInterval();
        }
        int waking = ServerConfig.codWakingInterval();
        if (!(level instanceof ServerLevel server)) {
            return waking;
        }
        long now = server.getServer().overworld().getGameTime();
        return overproduction()
                .map(played -> CodPace.interval(waking, ServerConfig.codPaceGrowth(), now - played.began()))
                .orElse(waking);
    }

    /** Its team's Overproduction, while the team plays a challenge; on the server only. */
    public Optional<Overproduction> overproduction() {
        if (!(level instanceof ServerLevel server)) {
            return Optional.empty();
        }
        UUID owner = team != null ? team : Teams.at(server, worldPosition);
        return TeamState.get(server.getServer()).overproduction(owner);
    }

    /** The ticks done toward the next essence, out of {@link #interval}; none while it isn't making. */
    public int ticksTowardNext() {
        return ticksTowardNext;
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, CodBlockEntity cod) {
        var server = (ServerLevel) level;
        if (cod.team == null) {
            cod.team = Teams.at(server, pos);
            Assault.noteCod(server, cod.team, pos);
        }
        UUID team = cod.team;
        TeamState teams = TeamState.get(server.getServer());
        cod.read(teams);
        cod.showState();
        if (cod.active()) {
            cod.push(server);
        }
        if (!cod.tick(cod.interval())) {
            return;
        }
        if (cod.lastStand.isPresent()) {
            teams.countNullessenceMade(team, ServerConfig.lastStandMargin());
        } else if (cod.awake) {
            teams.countMade(team);
        }
    }

    /** Takes its team's state as it stands: whether it is awake, its tier and its last stand. */
    private void read(TeamState teams) {
        awake = teams.isAwake(team);
        tier = teams.tier(team);
        challenged = teams.handicap(team).isPresent();
        lastStand = teams.lastStand(team);
    }

    /**
     * Stands dark once its team's last stand is won, and lit otherwise, and shows whether its team is awake and whether
     * it plays a challenge, if any changed.
     */
    private void showState() {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(CodBlock.SHUT_DOWN) || !state.hasProperty(CodBlock.AWAKE) || !state.hasProperty(CodBlock.CHALLENGED)) {
            return;
        }
        BlockState shown = state.setValue(CodBlock.SHUT_DOWN, lastStand.filter(LastStand::won).isPresent())
                .setValue(CodBlock.AWAKE, awake)
                .setValue(CodBlock.CHALLENGED, challenged);
        if (shown != state) {
            level.setBlock(worldPosition, shown, Block.UPDATE_ALL);
        }
    }

    /** How many Quintessence its team's last stand still takes to reach the margin; none while none runs, or on a client. */
    int quintessenceNeeded() {
        return level instanceof ServerLevel server ? LastStands.needed(server.getServer(), owner(server)) : 0;
    }

    /** Counts {@code count} Quintessence put into it toward its team's Lead, and shuts down at once if that won the pack. */
    void takeQuintessence(int count) {
        if (count <= 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        LastStands.take(server.getServer(), owner(server), count);
        read(TeamState.get(server.getServer()));
        showState();
    }

    /**
     * Puts as much of {@code stack} into the intake as it takes, as a player's click does, and says how many it took; the
     * stack is left with the rest.
     */
    public int feed(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        int taken;
        try (var transaction = Transaction.openRoot()) {
            taken = intake.insert(0, ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
        }
        stack.shrink(taken);
        return taken;
    }

    /** The team whose island it stands on. */
    private UUID owner(ServerLevel server) {
        if (team == null) {
            team = Teams.at(server, worldPosition);
        }
        return team;
    }

    /** Counts a tick toward the next essence, and says whether it made one. */
    private boolean tick(int interval) {
        if (!isMaking()) {
            ticksTowardNext = 0;
            return false;
        }
        if (++ticksTowardNext < interval) {
            return false;
        }
        ticksTowardNext = 0;
        Item essence = Essences.item(making().orElseThrow());
        if (!reserve.isFullFor(essence)) {
            reserve.makeOne(essence);
        } else {
            spill(essence);
        }
        return true;
    }

    /**
     * Pushes what the reserve holds into whatever takes items beside it, but a hopper that faces into C.O.D., which
     * would fill with what it can't put back.
     */
    private void push(ServerLevel server) {
        if (neighbours == null) {
            neighbours = new EnumMap<>(Direction.class);
            for (Direction side : Direction.values()) {
                neighbours.put(side, BlockCapabilityCache.create(Capabilities.Item.BLOCK, server, worldPosition.relative(side), side.getOpposite()));
            }
        }
        for (var neighbour : neighbours.entrySet()) {
            if (reserve.isEmpty()) {
                return;
            }
            BlockState beside = server.getBlockState(worldPosition.relative(neighbour.getKey()));
            if (beside.hasProperty(HopperBlock.FACING) && beside.getValue(HopperBlock.FACING) == neighbour.getKey().getOpposite()) {
                continue;
            }
            ResourceHandlerUtil.move(reserve, neighbour.getValue().getCapability(), resource -> true, Integer.MAX_VALUE, null);
        }
    }

    /** Drops one essence onto the pile on top, if it {@link #canSpill can}. */
    private void spill(Item essence) {
        if (level != null && canSpill()) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                    new ItemStack(essence), 0, 0, 0));
        }
    }

    /** Whether an essence can drop onto the pile on top: no block stands there, and the pile holds less than a stack of what it makes. */
    private boolean canSpill() {
        BlockPos top = worldPosition.above();
        if (level == null || !level.getBlockState(top).getCollisionShape(level, top).isEmpty()) {
            return false;
        }
        Optional<Essence> made = making();
        if (made.isEmpty()) {
            return false;
        }
        Item essence = Essences.item(made.get());
        int piled = 0;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(top).inflate(1, 0.5, 1))) {
            piled += item.getItem().is(essence) ? item.getItem().getCount() : 0;
        }
        return piled < essence.getDefaultMaxStackSize();
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
