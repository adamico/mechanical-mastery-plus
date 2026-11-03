// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.UUID;

import io.github._5thlayer.libworks.transfer.GuardedResourceHandler;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodWaking;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Decomposing;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Recycling;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Into;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Refusal;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Refused;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Share;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Yield;
import io.github._5thlayer.mechanicalmasteryreborn.record.DecomposeLog;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * A Recycler's core: its one input buffer, the turns its drives have delivered, and the work. Each turn
 * counts only while the Recycler can work what it holds; the last of its tier's turns ({@link ServerConfig#turnsOf})
 * decomposes one recipe's worth
 * of an essence into its mix, or recycles one of anything else into what made it ({@link Recycling}), onto the
 * outputs. Either returns the {@link Yield} of the full amounts, the same on every core ({@link ServerConfig}). It needs no power
 * and no ticking: drives turn it.
 *
 * <p>Each kind, in the order the work names them, lands on the first position already holding its item with room for
 * the most it can return, or else on the first empty one, read in {@link RecyclerLayout}'s order. When the outputs
 * have positions for every kind, the work stops while any kind fits nowhere. When the work gives more kinds than
 * that, the core picks at random, among the kinds that fit, as many as fit, and the rest are lost; it stops only when
 * no kind fits. It draws the order it picks in once per work, and keeps it until the work is done, or what it holds
 * changes, so what it says it can do is what it does.
 *
 * <p>Each decompose is recorded, on a Recycler, for the team whose island it stands on ({@link DecomposeLog}),
 * and wakes that team's C.O.D. ({@link CodWaking}); a recycle never is, and never does.
 *
 * <p>Only a player's hand takes back what it holds ({@link #takeBack()}), giving up the work under way. Every
 * client is sent what it holds, so a click on an empty core can pass there too.
 */
public final class RecyclerCoreBlockEntity extends BlockEntity {

    private static final String BUFFER_KEY = "buffer";
    private static final String TURNS_KEY = "turns";
    private static final String ORDER_KEY = "order";

    /** Whether the Recycler can work now, and why not. */
    public enum Status {
        /** Whole, with enough of what it holds, and a position for each kind it will return, with room for the most of it. */
        READY,
        /** Its 3x3 lacks a drive, an input or an output, or holds something else. */
        INCOMPLETE,
        /** It holds nothing. */
        EMPTY,
        /** It holds an essence no recipe decomposes, or an item that no longer recycles, since the recipes changed. */
        NO_RECIPE,
        /** It holds fewer than one decompose takes. */
        TOO_FEW,
        /** No room: a kind fits nowhere, or with more kinds than positions, none fits. */
        NO_ROOM
    }

    /** What the Recycler does with what it holds. */
    public enum Work {
        /** It holds nothing. */
        NONE,
        /** An essence, into its mix. */
        DECOMPOSING,
        /** Anything else, into what made it. */
        RECYCLING
    }

    private final Buffer buffer = new Buffer();
    private final ResourceHandler<ItemResource> inputFace = new InputFace(buffer);
    private int turns;
    /** The order the core picks the work's kinds in, by their place in the work, when it has more than positions. */
    private int @Nullable [] order;

    public RecyclerCoreBlockEntity(BlockPos pos, BlockState state) {
        super(Recycler.CORE_ENTITY.get(), pos, state);
    }

    /** This core's tier. */
    public int tier() {
        return ((RecyclerCoreBlock) getBlockState().getBlock()).tier();
    }

    /** The turns a decompose or a recycle takes on this core. */
    public int turnsPerWork() {
        return ServerConfig.turnsOf(tier());
    }

    /** The buffer as its inputs reach it: insert only, and only an essence a recipe decomposes or what recycles. */
    public ResourceHandler<ItemResource> inputFace() {
        return inputFace;
    }

    /** Why an input took none of what a player offered it, which the input tells them on the action bar. */
    public enum Rejection {
        /** It is refined, so it can't be recycled. */
        REFINED,
        /** It is a block a player uncrafts for free. */
        UNCRAFT_INSTEAD,
        /** The Recycler takes no such thing: a filled box, or an essence nothing decomposes. */
        NOT_TAKEN,
        /** The core holds another item. */
        HOLDS_ANOTHER,
        /** The core holds a full stack of it. */
        FULL,
        /** No core claims the input, or two do. */
        NO_RECYCLER;

        public Component message() {
            return Component.translatable(MechanicalMasteryReborn.MOD_ID + ".recycler.input." + name().toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Why the buffer takes none of {@code stack}: what it is first, refined or to be uncrafted instead, then whether
     * the buffer takes such a thing at all, then what it holds.
     */
    public Rejection rejectionOf(ItemStack stack) {
        if (level instanceof ServerLevel server && Essences.of(stack.getItem()).isEmpty()
                && Recycling.recycle(server.getServer(), stack.getItem()) instanceof Refused<Item>(Refusal why)) {
            return switch (why) {
                case REFINED -> Rejection.REFINED;
                case UNCRAFT_INSTEAD -> Rejection.UNCRAFT_INSTEAD;
            };
        }
        if (!buffer.isValid(0, ItemResource.of(stack))) {
            return Rejection.NOT_TAKEN;
        }
        ItemStack held = held();
        return !held.isEmpty() && !ItemStack.isSameItemSameComponents(held, stack) ? Rejection.HOLDS_ANOTHER : Rejection.FULL;
    }

    /** What the buffer holds. */
    public ItemStack held() {
        return buffer.getResource(0).toStack(buffer.getAmountAsInt(0));
    }

    /** Empties the buffer and gives up the work under way: what it held, for the player who takes it back. */
    public ItemStack takeBack() {
        ItemStack held = held();
        turns = 0;
        order = null;
        buffer.set(0, ItemResource.EMPTY, 0);
        return held;
    }

    /** Turns delivered toward the decompose under way. */
    public int turns() {
        return turns;
    }

    public Status status() {
        return plan().status();
    }

    /**
     * Whether {@code pos} is a place of this core's Recycler: one of the eight around it on its own floor, or one of
     * the nine of a floor above that counts.
     */
    boolean holdsPlace(BlockPos pos) {
        int floor = pos.getY() - worldPosition.getY();
        return floor == 0 ? reaches(pos) : floor > 0 && floor < floors() && reaches(pos);
    }

    /**
     * Whether this core would claim {@code pos} if no other core did: a place on its own floor, or in its 3x3 on a
     * floor above, below the first that isn't full of parts and within its tier.
     */
    boolean reaches(BlockPos pos) {
        Direction front = getBlockState().getValue(RecyclerCoreBlock.FACING);
        int floor = pos.getY() - worldPosition.getY();
        int x = pos.getX() - worldPosition.getX();
        int z = pos.getZ() - worldPosition.getZ();
        if (floor == 0) {
            return RecyclerLayout.isPlace(front.getStepX(), front.getStepZ(), x, z);
        }
        return floor > 0 && floor < builtFloors() && RecyclerLayout.inFootprint(front.getStepX(), front.getStepZ(), x, z);
    }

    /** The floors this Recycler counts: its own, then each one above whose nine places are all its parts alone. */
    public int floors() {
        int built = builtFloors();
        int floors = 1;
        while (floors < built && floorPositions(floors).stream()
                .allMatch(pos -> RecyclerPart.reacherOf(level, pos).filter(core -> core == this).isPresent())) {
            floors++;
        }
        return floors;
    }

    /** Its own floor, then each one above full of parts, up to as many in all as its tier. */
    private int builtFloors() {
        int floors = 1;
        while (floors < tier() && floorPositions(floors).stream().allMatch(pos -> level.getBlockState(pos).getBlock() instanceof RecyclerPart)) {
            floors++;
        }
        return floors;
    }

    /** The places of floor {@code floor}, the core's own 0, in the order outputs are read. */
    private List<BlockPos> floorPositions(int floor) {
        Direction front = getBlockState().getValue(RecyclerCoreBlock.FACING);
        int[][] places = floor == 0 ? RecyclerLayout.places(front.getStepX(), front.getStepZ())
                : RecyclerLayout.upperPlaces(front.getStepX(), front.getStepZ());
        return Arrays.stream(places).map(place -> worldPosition.offset(place[0], floor, place[1])).toList();
    }

    /** What the Recycler does with what it holds, whether or not it can now. */
    public Work work() {
        ItemStack held = held();
        return held.isEmpty() ? Work.NONE : Essences.of(held.getItem()).isPresent() ? Work.DECOMPOSING : Work.RECYCLING;
    }

    /** What a turn did. */
    public enum Turn {
        /** Nothing: the Recycler can't work now. */
        STOPPED,
        /** It counted toward the work under way. */
        COUNTED,
        /** It was the last, and decomposed or recycled: the work was done. */
        WORKED
    }

    /** One turn from a drive. It counts only while the Recycler can work; the last one decomposes or recycles. */
    public Turn turn() {
        if (!(level instanceof ServerLevel server)) {
            return Turn.STOPPED;
        }
        Plan plan = plan();
        if (plan.status() != Status.READY) {
            return Turn.STOPPED;
        }
        turns++;
        setChanged();
        if (turns < turnsPerWork()) {
            return Turn.COUNTED;
        }
        turns = 0;
        order = null;
        work(server, plan);
        return Turn.WORKED;
    }

    private void work(ServerLevel server, Plan plan) {
        ItemStack held = held();
        buffer.set(0, ItemResource.of(held), held.getCount() - plan.take());
        double yield = ServerConfig.yield();
        for (Landing landing : plan.landings()) {
            int count = Yield.roll(landing.kind().amount(), yield, server.getRandom()::nextDouble);
            if (count > 0) {
                landing.output().put(landing.position(), landing.kind().item().create().copyWithCount(count));
            }
        }
        if (plan.work() == Work.DECOMPOSING) {
            UUID team = Teams.at(server, worldPosition);
            Essences.of(held.getItem()).ifPresent(essence -> DecomposeLog.record(server.getServer(), team, essence, plan.take(),
                    DecomposeLog.Way.RECYCLER));
            CodWaking.wake(server.getServer(), team);
        }
    }

    /** What the Recycler would decompose or recycle now, onto which outputs, or why it can't. */
    private Plan plan() {
        if (!(level instanceof ServerLevel server)) {
            return Plan.stopped(Status.INCOMPLETE);
        }
        List<RecyclerOutputBlockEntity> outputs = outputs();
        if (outputs == null) {
            return Plan.stopped(Status.INCOMPLETE);
        }
        ItemStack held = held();
        if (held.isEmpty()) {
            return Plan.stopped(Status.EMPTY);
        }
        Work work = work();
        int take = 1;
        List<Share<ItemStackTemplate>> full;
        if (work == Work.DECOMPOSING) {
            var found = Decomposing.recipeFor(server, held);
            if (found.isEmpty()) {
                return Plan.stopped(Status.NO_RECIPE);
            }
            take = found.get().value().ingredient().count();
            if (held.getCount() < take) {
                return Plan.stopped(Status.TOO_FEW);
            }
            full = found.get().value().results().stream().map(result -> new Share<>(result.withCount(1), result.count())).toList();
        } else {
            if (!(Recycling.recycle(server.getServer(), held.getItem()) instanceof Into<Item>(List<Share<Item>> into))) {
                return Plan.stopped(Status.NO_RECIPE);
            }
            full = into.stream().map(share -> new Share<>(new ItemStackTemplate(share.item()), share.amount())).toList();
        }
        double yield = ServerConfig.yield();
        List<ItemStack> most = full.stream()
                .map(share -> share.item().create().copyWithCount(Yield.most(share.amount(), yield))).toList();
        int positions = outputs.size() * RecyclerLayout.POSITIONS_PER_OUTPUT;
        int[] kinds;
        if (most.stream().filter(stack -> !stack.isEmpty()).count() <= positions) {
            order = null;
            kinds = IntStream.range(0, full.size()).toArray();
        } else {
            kinds = picked(server, outputs, most, positions);
        }
        if (kinds == null) {
            return Plan.stopped(Status.NO_ROOM);
        }
        var claimed = new boolean[positions];
        var landings = new ArrayList<Landing>();
        for (int kind : kinds) {
            if (most.get(kind).isEmpty()) {
                continue;
            }
            int position = landing(outputs, claimed, most.get(kind));
            if (position < 0) {
                return Plan.stopped(Status.NO_ROOM);
            }
            claimed[position] = true;
            landings.add(new Landing(full.get(kind), outputs.get(position / RecyclerLayout.POSITIONS_PER_OUTPUT),
                    position % RecyclerLayout.POSITIONS_PER_OUTPUT));
        }
        return new Plan(Status.READY, work, take, landings);
    }

    /**
     * Which of the work's kinds it returns when those that return anything outnumber the outputs' {@code positions}, by
     * their place in the work and in its order: in the core's order, drawn now if it has none for this work, each that
     * fits beside those before it, up to as many as there are positions; null when none fits. A kind that returns
     * nothing at this yield needs no position, and is left out.
     */
    private int @Nullable [] picked(ServerLevel server, List<RecyclerOutputBlockEntity> outputs, List<ItemStack> most,
            int positions) {
        int kinds = most.size();
        if (order == null || !Arrays.equals(Arrays.stream(order).sorted().toArray(), IntStream.range(0, kinds).toArray())) {
            List<Integer> all = new ArrayList<>(IntStream.range(0, kinds).boxed().toList());
            Util.shuffle(all, server.getRandom());
            order = all.stream().mapToInt(Integer::intValue).toArray();
            setChanged();
        }
        var claimed = new boolean[positions];
        var picked = new ArrayList<Integer>();
        for (int kind : order) {
            if (picked.size() == positions) {
                break;
            }
            int position = most.get(kind).isEmpty() ? -1 : landing(outputs, claimed, most.get(kind));
            if (position >= 0) {
                claimed[position] = true;
                picked.add(kind);
            }
        }
        return picked.isEmpty() ? null : picked.stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    /**
     * The position, counted across {@code outputs} in order, that takes all of {@code most} and no other kind of this
     * work has {@code claimed}: the first already holding its item, or else the first empty one; -1 for none.
     */
    private static int landing(List<RecyclerOutputBlockEntity> outputs, boolean[] claimed, ItemStack most) {
        int empty = -1;
        for (int position = 0; position < claimed.length; position++) {
            if (claimed[position]) {
                continue;
            }
            RecyclerOutputBlockEntity output = outputs.get(position / RecyclerLayout.POSITIONS_PER_OUTPUT);
            int index = position % RecyclerLayout.POSITIONS_PER_OUTPUT;
            if (output.item(index).isEmpty()) {
                empty = empty < 0 ? position : empty;
            } else if (output.fits(index, most)) {
                return position;
            }
        }
        return empty;
    }

    /**
     * The outputs in the order a mix fills them, floor by floor upward, or null when the Recycler isn't whole: every
     * one of the eight places on the core's floor a part of this core's alone, with at least one drive, input and
     * output across the floors that count.
     */
    private @Nullable List<RecyclerOutputBlockEntity> outputs() {
        var outputs = new ArrayList<RecyclerOutputBlockEntity>();
        boolean drive = false;
        boolean input = false;
        int floors = floors();
        for (int floor = 0; floor < floors; floor++) {
            for (BlockPos pos : floorPositions(floor)) {
                if (!(level.getBlockState(pos).getBlock() instanceof RecyclerPart part)
                        || RecyclerPart.coreOf(level, pos).filter(core -> core == this).isEmpty()) {
                    return null;
                }
                switch (part.role()) {
                    case DRIVE -> drive = true;
                    case INPUT -> input = true;
                    case OUTPUT -> {
                        if (!(level.getBlockEntity(pos) instanceof RecyclerOutputBlockEntity output)) {
                            return null;
                        }
                        outputs.add(output);
                    }
                    case CASING -> {
                    }
                }
            }
        }
        return drive && input && !outputs.isEmpty() ? outputs : null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        buffer.serialize(output.child(BUFFER_KEY));
        output.putInt(TURNS_KEY, turns);
        if (order != null) {
            output.putIntArray(ORDER_KEY, order);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        buffer.deserialize(input.childOrEmpty(BUFFER_KEY));
        turns = input.getIntOr(TURNS_KEY, 0);
        order = input.getIntArray(ORDER_KEY).orElse(null);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), held());
        }
    }

    /** The work: how many of what it holds it takes, and where each kind it returns lands. */
    private record Plan(Status status, Work work, int take, List<Landing> landings) {

        static Plan stopped(Status status) {
            return new Plan(status, Work.NONE, 0, List.of());
        }
    }

    /** A kind the work returns, its full amount before yield, and the output position it lands on. */
    private record Landing(Share<ItemStackTemplate> kind, RecyclerOutputBlockEntity output, int position) {
    }

    /** The one slot, which takes only an essence a recipe decomposes, or anything else that recycles. */
    private final class Buffer extends ItemStacksResourceHandler {

        Buffer() {
            super(1);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            ItemStack stack = resource.toStack();
            return level instanceof ServerLevel server
                    && (Decomposing.recipeFor(server, stack).isPresent() || Recycling.recyclable(server.getServer(), stack));
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            if (!ItemStack.isSameItemSameComponents(previousContents, getResource(index).toStack())) {
                order = null;
            }
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** The buffer, from which nothing is taken. */
    private static final class InputFace extends GuardedResourceHandler<ItemResource> {

        InputFace(ResourceHandler<ItemResource> buffer) {
            super(buffer);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
