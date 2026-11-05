// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Refunds;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Stops;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The lab's choice of handicap, for the team whose island it stands on. A button per handicap, by its ordinal, starts
 * its challenge, if the player is in that team, the team plays none yet and hasn't won it. {@link #ASK} and
 * {@link #TAKE_BACK} ask to stop the challenge it plays, and take the ask back, once a stop is offered ({@link Stops}), and
 * {@link #OPEN_REFUND} opens the team's refund ({@link RefundMenu}), and {@link #START_LAST_STAND} starts the team's last
 * stand ({@link LastStands}), once it has won all four challenges and if the player is in that team. The
 * client is sent which handicap the team plays, if any, which challenges it has won, whether the player may choose for
 * it, whether a stop is offered and the player asks for it, how many items of the team's refund wait, and which members
 * online have asked and which are left ({@link Asks}), and where the team's last stand is ({@link LastStandPhase}). As it
 * opens, the client is also sent whether a stop is offered, how much of the refund waits and where the last stand is, so
 * its screen opens at the right size with only the rows that show ({@link #showsStop}, {@link #showsRefund},
 * {@link #showsLastStand}); a change while it is open shows or hides a row from the next sync.
 */
public final class LabMenu extends AbstractContainerMenu {

    /** The button that asks to stop the challenge played, after one per handicap. */
    public static final int ASK = Handicap.values().length;
    /** The button that takes back the player's ask to stop it. */
    public static final int TAKE_BACK = ASK + 1;
    /** The button that opens the team's refund. */
    public static final int OPEN_REFUND = TAKE_BACK + 1;
    /** The button that starts the team's last stand. */
    public static final int START_LAST_STAND = OPEN_REFUND + 1;

    /** Where a team's last stand is, as the lab shows it. */
    public enum LastStandPhase {
        /** Not yet offered: the team hasn't won all four challenges. */
        LOCKED,
        /** Offered: the team has won all four and not started it. */
        OFFERED,
        /** Started, and C.O.D. not shut down yet. */
        RUNNING,
        /** Won: C.O.D. shut down for good. */
        WON
    }

    /** What {@link #chosen} holds while the team plays no challenge. */
    private static final int NONE = -1;

    private final ContainerLevelAccess access;
    private final DataSlot chosen;
    private final DataSlot member;
    /** The challenges the team has won, a bit per handicap's ordinal. */
    private final DataSlot won;
    /** Whether the team may stop the challenge it plays, 1 or 0. */
    private final DataSlot stopOffered;
    /** Whether the player asks to stop it, 1 or 0. */
    private final DataSlot asked;
    /** How many items of the team's refund wait, up to what a data slot holds. */
    private final DataSlot refund;
    /** Where the team's last stand is, by its {@link LastStandPhase}'s ordinal. */
    private final DataSlot lastStand;
    /** The server's player, who is sent {@link Asks}; null on the client or for a game test's mock player. */
    private final ServerPlayer viewer;
    /** The asks the client was last sent, or holds. */
    private Asks asks = Asks.NONE;

    /** The client's menu, whose slots the sync fills, starting from the stop, the refund and the last stand as the server wrote them. */
    public LabMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(Lab.MENU.get(), containerId);
        this.access = ContainerLevelAccess.NULL;
        this.chosen = addDataSlot(DataSlot.standalone());
        this.member = addDataSlot(DataSlot.standalone());
        this.won = addDataSlot(DataSlot.standalone());
        this.stopOffered = addDataSlot(DataSlot.standalone());
        this.asked = addDataSlot(DataSlot.standalone());
        this.refund = addDataSlot(DataSlot.standalone());
        this.lastStand = addDataSlot(DataSlot.standalone());
        this.viewer = null;
        chosen.set(NONE);
        stopOffered.set(buffer.readBoolean() ? 1 : 0);
        refund.set(buffer.readVarInt());
        lastStand.set(buffer.readVarInt());
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
        this.stopOffered = addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return Stops.offered(level.getServer(), team) ? 1 : 0;
            }

            @Override
            public void set(int value) {
            }
        });
        this.asked = addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return Stops.asked(team, inventory.player) ? 1 : 0;
            }

            @Override
            public void set(int value) {
            }
        });
        this.refund = addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return Math.min(Short.MAX_VALUE, Refunds.get(level.getServer()).count(team));
            }

            @Override
            public void set(int value) {
            }
        });
        this.lastStand = addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return phase(level.getServer(), team).ordinal();
            }

            @Override
            public void set(int value) {
            }
        });
        this.viewer = inventory.player instanceof ServerPlayer server ? server : null;
    }

    /** Opens the lab at {@code pos} for {@code player}, telling the client which of its optional rows show. */
    public static void open(Player player, ServerLevel level, BlockPos pos) {
        MinecraftServer server = level.getServer();
        UUID team = Teams.at(level, pos);
        boolean stop = Stops.offered(server, team);
        int waiting = Math.min(Short.MAX_VALUE, Refunds.get(server).count(team));
        int phase = phase(server, team).ordinal();
        player.openMenu(level.getBlockState(pos).getMenuProvider(level, pos), buffer -> {
            buffer.writeBoolean(stop);
            buffer.writeVarInt(waiting);
            buffer.writeVarInt(phase);
        });
    }

    /** Where {@code team}'s last stand is. */
    private static LastStandPhase phase(MinecraftServer server, UUID team) {
        var stand = TeamState.get(server).lastStand(team);
        if (stand.isPresent()) {
            return stand.get().won() ? LastStandPhase.WON : LastStandPhase.RUNNING;
        }
        return LastStands.offered(server, team) ? LastStandPhase.OFFERED : LastStandPhase.LOCKED;
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

    /** Whether the lab's team may stop the challenge it plays, which only once it has reset. */
    public boolean stopOffered() {
        return stopOffered.get() == 1;
    }

    /** Whether the player asks to stop the challenge the lab's team plays. */
    public boolean asked() {
        return asked.get() == 1;
    }

    /** How many items of the lab's team's refund wait there, up to {@link Short#MAX_VALUE}. */
    public int refund() {
        return refund.get();
    }

    /** Where the lab's team's last stand is. */
    public LastStandPhase lastStand() {
        LastStandPhase[] phases = LastStandPhase.values();
        int ordinal = lastStand.get();
        return ordinal >= 0 && ordinal < phases.length ? phases[ordinal] : LastStandPhase.LOCKED;
    }

    /** Whether the lab shows the stop's row: while a stop is offered, by the last sync. */
    public boolean showsStop() {
        return stopOffered();
    }

    /** Whether the lab shows the refund's row: while something of the team's refund waits, by the last sync. */
    public boolean showsRefund() {
        return refund() > 0;
    }

    /** Whether the lab shows the last stand's row: once its team has won all four challenges, by the last sync. */
    public boolean showsLastStand() {
        return lastStand() != LastStandPhase.LOCKED;
    }

    /** Whether starting the last stand here would start it. */
    public boolean canStartLastStand() {
        return isMember() && lastStand() == LastStandPhase.OFFERED;
    }

    /** Which of the team's members online have asked to stop its challenge, and which are left, as the client was last sent. */
    public Asks asks() {
        return asks;
    }

    /** Keeps the asks the server sent, on the client. */
    public void receive(Asks received) {
        asks = received;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (viewer == null || viewer.connection == null) {
            return;
        }
        access.execute((level, pos) -> {
            Asks now = Asks.of(level.getServer(), Teams.at((ServerLevel) level, pos), containerId);
            if (!now.equals(asks)) {
                asks = now;
                PacketDistributor.sendToPlayer(viewer, now);
            }
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == OPEN_REFUND) {
            access.execute((level, pos) -> player.openMenu(RefundMenu.provider((ServerLevel) level, pos)));
            return true;
        }
        if (id == START_LAST_STAND) {
            return canStartLastStand() && access.evaluate((level, pos) -> level instanceof ServerLevel server
                    && LastStands.begin(server.getServer(), Teams.at(server, pos)), false);
        }
        if (id == ASK || id == TAKE_BACK) {
            return access.evaluate((level, pos) -> level instanceof ServerLevel server && (id == ASK
                    ? Stops.ask(server.getServer(), Teams.at(server, pos), player)
                    : Stops.takeBack(server.getServer(), Teams.at(server, pos), player)), false);
        }
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

    /**
     * What a lab's menu sends its client of the asks to stop its team's challenge, since a data slot holds no names: the
     * names of the members online who have asked, and of those left.
     */
    public record Asks(int containerId, List<String> asked, List<String> left) implements CustomPacketPayload {

        public static final Type<Asks> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "lab_asks"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Asks> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Asks::containerId,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), Asks::asked,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), Asks::left,
                Asks::new);

        /** No asks, as a lab's client starts with. */
        static final Asks NONE = new Asks(-1, List.of(), List.of());

        public Asks {
            asked = List.copyOf(asked);
            left = List.copyOf(left);
        }

        /** {@code team}'s asks as they stand, for the menu {@code containerId}. */
        static Asks of(MinecraftServer server, UUID team, int containerId) {
            var online = Teams.online(server, team);
            return new Asks(containerId,
                    online.stream().filter(member -> Stops.asked(team, member)).map(member -> member.getName().getString()).sorted().toList(),
                    online.stream().filter(member -> !Stops.asked(team, member)).map(member -> member.getName().getString()).sorted().toList());
        }

        /** Gives the lab's menu the client has open these asks. */
        public static void receive(Asks asks, Player player) {
            if (player.containerMenu instanceof LabMenu menu && menu.containerId == asks.containerId()) {
                menu.receive(asks);
            }
        }

        @Override
        public Type<Asks> type() {
            return TYPE;
        }
    }
}
