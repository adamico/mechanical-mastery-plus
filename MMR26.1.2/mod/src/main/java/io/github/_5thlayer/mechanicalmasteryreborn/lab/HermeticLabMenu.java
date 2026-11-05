// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Refunds;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Spells;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

/**
 * The Hermetic Lab's spells and lines, and its one Quintessence slot over the player's inventory, which only gives: what
 * the lab opens once its team's last stand has started. A button per handicap, by its ordinal, has the team take it on for
 * a spell ({@link Spells#begin}), if the player is in the lab's team; {@link #GIVE_HEART} gives a heart of the player's
 * to the life line ({@link LabBlockEntity#giveHeart}); and {@link #OPEN_REFUND} opens the team's refund, which a protection
 * spell's Assault fills as a challenge's does ({@link RefundMenu}). The client is sent what the Hermetic Lab is doing,
 * which lines count, what each spell has left, which can be taken on, the Nullessence and Essentia Vitae it holds, how far
 * the next Quintessence is, how much of the refund waits, and whether the player is in its team.
 */
public final class HermeticLabMenu extends AbstractContainerMenu {

    /** The button that gives a heart, after one per handicap. */
    public static final int GIVE_HEART = Handicap.values().length;
    /** The button that opens the team's refund, after the heart's. */
    public static final int OPEN_REFUND = GIVE_HEART + 1;

    public static final int WIDTH = 256;
    /** Where the rows start: below the title and two lines of what it is doing. */
    public static final int ROWS_Y = 42;
    /** A row per handicap, then the heart's and the refund's. */
    public static final int ROW = 22;
    public static final int ROWS = Handicap.values().length + 2;
    public static final int INVENTORY_X = 48;
    public static final int INVENTORY_Y = ROWS_Y + ROWS * ROW + 14;
    public static final int HEIGHT = INVENTORY_Y + 82;
    public static final int OUTPUT_X = WIDTH - 8 - 16;
    public static final int OUTPUT_Y = 20;
    static final int OUTPUT_SLOT = 0;
    private static final int FIRST_PLAYER_SLOT = 1;

    private final BlockPos pos;
    private final @Nullable LabBlockEntity lab;
    private final DataSlot phase;
    private final DataSlot lines;
    private final DataSlot[] remaining = new DataSlot[Handicap.values().length];
    private final DataSlot offered;
    private final DataSlot nullessence;
    private final DataSlot essentia;
    private final DataSlot progress;
    private final DataSlot member;
    private final DataSlot refund;

    HermeticLabMenu(int containerId, Inventory inventory, LabBlockEntity lab) {
        super(Lab.HERMETIC_MENU.get(), containerId);
        this.pos = lab.getBlockPos();
        this.lab = lab;
        ServerLevel level = (ServerLevel) lab.getLevel();
        MinecraftServer server = level.getServer();
        UUID team = lab.owner(level);
        this.phase = addDataSlot(computed(() -> lab.phase().ordinal()));
        this.lines = addDataSlot(computed(() -> {
            int bits = 0;
            for (Handicap handicap : Handicap.values()) {
                bits |= lab.counts(handicap) ? 1 << handicap.ordinal() : 0;
            }
            return bits;
        }));
        for (Handicap handicap : Handicap.values()) {
            remaining[handicap.ordinal()] = addDataSlot(computed(() -> TeamState.get(server).spell(team, handicap)
                    .map(spell -> (int) Math.min(Short.MAX_VALUE, (spell.remaining(server.overworld().getGameTime()) + 19) / 20))
                    .orElse(0)));
        }
        this.offered = addDataSlot(computed(() -> {
            int bits = 0;
            for (Handicap handicap : Handicap.values()) {
                bits |= Spells.canBegin(server, team, handicap) ? 1 << handicap.ordinal() : 0;
            }
            return bits;
        }));
        this.nullessence = addDataSlot(computed(() -> Math.min(Short.MAX_VALUE, lab.nullessence())));
        this.essentia = addDataSlot(computed(lab::essentia));
        this.progress = addDataSlot(computed(() -> (int) Math.round(lab.progress() * 1000)));
        this.member = addDataSlot(DataSlot.standalone());
        member.set(team.equals(Teams.of(inventory.player)) ? 1 : 0);
        this.refund = addDataSlot(computed(() -> Math.min(Short.MAX_VALUE, Refunds.get(server).count(team))));
        addSlot(new OutputSlot(lab.outputSlot()));
        addStandardInventorySlots(inventory, INVENTORY_X, INVENTORY_Y);
    }

    /** The client's menu, opened at the position the server wrote; its slot stands over a stub the sync fills. */
    public HermeticLabMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(Lab.HERMETIC_MENU.get(), containerId);
        this.pos = buffer.readBlockPos();
        this.lab = null;
        this.phase = addDataSlot(DataSlot.standalone());
        this.lines = addDataSlot(DataSlot.standalone());
        for (Handicap handicap : Handicap.values()) {
            remaining[handicap.ordinal()] = addDataSlot(DataSlot.standalone());
        }
        this.offered = addDataSlot(DataSlot.standalone());
        this.nullessence = addDataSlot(DataSlot.standalone());
        this.essentia = addDataSlot(DataSlot.standalone());
        this.progress = addDataSlot(DataSlot.standalone());
        this.member = addDataSlot(DataSlot.standalone());
        this.refund = addDataSlot(DataSlot.standalone());
        addSlot(new OutputSlot(new ItemStacksResourceHandler(1)));
        addStandardInventorySlots(inventory, INVENTORY_X, INVENTORY_Y);
    }

    /** A data slot the server reads from {@code value}, which the client only receives. */
    private static DataSlot computed(java.util.function.IntSupplier value) {
        return new DataSlot() {
            @Override
            public int get() {
                return value.getAsInt();
            }

            @Override
            public void set(int ignored) {
            }
        };
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (lab == null || !(lab.getLevel() instanceof ServerLevel level)) {
            return false;
        }
        if (id == OPEN_REFUND) {
            player.openMenu(RefundMenu.provider(level, pos));
            return true;
        }
        if (id == GIVE_HEART) {
            var refused = lab.giveHeart(player);
            refused.ifPresent(player::sendOverlayMessage);
            return refused.isEmpty();
        }
        var handicap = Handicap.byOrdinal(id);
        UUID team = lab.owner(level);
        if (handicap.isEmpty() || !Teams.of(player).equals(team)) {
            return false;
        }
        return Spells.begin(level.getServer(), team, handicap.get());
    }

    /** What the Hermetic Lab is doing. */
    public LabBlockEntity.Phase phase() {
        var phases = LabBlockEntity.Phase.values();
        int ordinal = phase.get();
        return ordinal >= 0 && ordinal < phases.length ? phases[ordinal] : LabBlockEntity.Phase.LOCKED;
    }

    /** Whether {@code handicap}'s line counts. */
    public boolean counts(Handicap handicap) {
        return (lines.get() & 1 << handicap.ordinal()) != 0;
    }

    /** How many lines count. */
    public int linesCounting() {
        return Integer.bitCount(lines.get());
    }

    /** The seconds {@code handicap}'s spell has left, none while none runs. */
    public int remaining(Handicap handicap) {
        return remaining[handicap.ordinal()].get();
    }

    /** Whether {@code handicap} can be taken on for a spell now, by the player, from this Hermetic Lab. */
    public boolean canBegin(Handicap handicap) {
        return isMember() && (offered.get() & 1 << handicap.ordinal()) != 0;
    }

    /** Whether a heart can be given now: the life spell runs and the player is in the Hermetic Lab's team. */
    public boolean canGiveHeart() {
        return isMember() && remaining(Handicap.LIFE) > 0;
    }

    public int nullessence() {
        return nullessence.get();
    }

    /** The Essentia Vitae held, in mB. */
    public int essentia() {
        return essentia.get();
    }

    /** How far the next Quintessence is, in thousandths. */
    public int progress() {
        return progress.get();
    }

    /** How many items of the team's refund wait. */
    public int refund() {
        return refund.get();
    }

    public boolean isMember() {
        return member.get() == 1;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (index != OUTPUT_SLOT || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (!moveItemStackTo(stack, FIRST_PLAYER_SLOT, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        // The slot gives what was moved and keeps the rest, as C.O.D.'s does.
        slot.set(stack);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (lab != null && lab.isRemoved()) {
            return false;
        }
        return player.isWithinBlockInteractionRange(pos, 4.0);
    }

    /** The Quintessence slot takes nothing from the player. */
    private static final class OutputSlot extends ResourceHandlerSlot {

        OutputSlot(ItemStacksResourceHandler handler) {
            super(handler, handler::set, 0, OUTPUT_X, OUTPUT_Y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
