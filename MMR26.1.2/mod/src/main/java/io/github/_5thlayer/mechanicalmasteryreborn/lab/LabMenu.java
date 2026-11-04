// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

/**
 * The lab's choice of handicap, for the team whose island it stands on. A button per handicap, by its ordinal, starts
 * its challenge, if the player is in that team, the team plays none yet and hasn't won it. The
 * client is sent which handicap the team plays, if any, which challenges it has won, and whether the player may
 * choose for it.
 */
public final class LabMenu extends AbstractContainerMenu {

    /** What {@link #chosen} holds while the team plays no challenge. */
    private static final int NONE = -1;

    private final ContainerLevelAccess access;
    private final DataSlot chosen;
    private final DataSlot member;
    /** The challenges the team has won, a bit per handicap's ordinal. */
    private final DataSlot won;

    /** The client's menu, whose slots the sync fills. */
    public LabMenu(int containerId, Inventory inventory) {
        super(Lab.MENU.get(), containerId);
        this.access = ContainerLevelAccess.NULL;
        this.chosen = addDataSlot(DataSlot.standalone());
        this.member = addDataSlot(DataSlot.standalone());
        this.won = addDataSlot(DataSlot.standalone());
        chosen.set(NONE);
    }

    LabMenu(int containerId, Inventory inventory, ServerLevel level, BlockPos pos) {
        super(Lab.MENU.get(), containerId);
        this.access = ContainerLevelAccess.create(level, pos);
        UUID team = Teams.at(level, pos);
        UUID players = Teams.of(inventory.player);
        this.chosen = addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return TeamState.get(level.getServer()).handicap(team).map(Handicap::ordinal).orElse(NONE);
            }

            @Override
            public void set(int value) {
            }
        });
        this.member = addDataSlot(DataSlot.standalone());
        member.set(team.equals(players) ? 1 : 0);
        this.won = addDataSlot(new DataSlot() {
            @Override
            public int get() {
                TeamState state = TeamState.get(level.getServer());
                int bits = 0;
                for (Handicap handicap : Handicap.values()) {
                    bits |= state.won(team, handicap) ? 1 << handicap.ordinal() : 0;
                }
                return bits;
            }

            @Override
            public void set(int value) {
            }
        });
    }

    /** The handicap whose challenge the lab's team plays, if any. */
    public Optional<Handicap> chosen() {
        return Handicap.byOrdinal(chosen.get());
    }

    /** Whether the player is in the team whose island the lab stands on, and so may choose for it. */
    public boolean isMember() {
        return member.get() == 1;
    }

    /** Whether the lab's team has won {@code handicap}'s challenge, which can't be started again. */
    public boolean won(Handicap handicap) {
        return (won.get() & 1 << handicap.ordinal()) != 0;
    }

    /** Whether choosing {@code handicap} would start its challenge. */
    public boolean canChoose(Handicap handicap) {
        return isMember() && chosen().isEmpty() && !won(handicap);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        Optional<Handicap> handicap = Handicap.byOrdinal(id).filter(this::canChoose);
        return handicap.isPresent() && access.evaluate((level, pos) -> level instanceof ServerLevel server
                && Challenges.begin(server.getServer(), Teams.at(server, pos), handicap.get()), false);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Lab.BLOCK.get());
    }
}
