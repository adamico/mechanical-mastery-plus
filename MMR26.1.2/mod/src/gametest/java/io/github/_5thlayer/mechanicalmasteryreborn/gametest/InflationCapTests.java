// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.Inflation;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.GameType;

/**
 * Inflation's cap (#206): reaching it resets the challenge, Inflation back to nothing and C.O.D. back to its waking
 * pace as though the challenge had just begun, and keeps the island and the team's handicap; nearing it warns the
 * team. The tests play at the configured cap, 256 by default, which must be 5 or more for 80% of it to fall short of
 * it, and count what C.O.D. makes straight into the team's state, so they needn't wait for it. A challenge chosen as of an earlier game time stands for one that has gone on
 * that long. Each test is an island of its own.
 */
final class InflationCapTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    /** Another team's C.O.D., on an island of its own in a corner of the test's. */
    private static final BlockPos OTHER_COD = new BlockPos(7, 2, 7);
    /** A chest on the island, standing for its machines and items. */
    private static final BlockPos CHEST = new BlockPos(3, 2, 1);
    /** The default {@code cod.waking_interval} the tests play at. */
    private static final int WAKING = 40;
    /** Twice the waking pace, ten minutes into a challenge at the default growth. */
    private static final int TWICE = WAKING / 2;
    private static final int TEN_MINUTES = 12000;

    private static final String RESET = "mechanicalmasteryreborn.challenge.reset";
    private static final String WARNING = "mechanicalmasteryreborn.challenge.warning";

    private InflationCapTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("inflation_cap_reached_resets_the_challenge_and_keeps_the_rest", 10, InflationCapTests::resetsAndKeeps);
        tests.test("inflation_cap_reached_leaves_another_team_alone", 10, InflationCapTests::leavesAnotherTeamAlone);
        tests.test("inflation_cap_warns_once_near_it_and_again_after_falling_back", 10, InflationCapTests::warnsOnceAndAgain);
        tests.test("inflation_cap_warning_survives_a_load_and_a_reset_clears_it", 10, InflationCapTests::warningSurvivesALoad);
        tests.test("inflation_cap_warning_waits_for_a_player_online", 10, InflationCapTests::warningWaitsForAPlayer);
    }

    /** Ten minutes in, C.O.D. at twice its waking pace, the team reaches its cap and is told the challenge was reset. */
    private static void resetsAndKeeps(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Listener player = Listener.of(helper, team);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(CHEST, Blocks.CHEST);
        helper.getBlockEntity(CHEST, ChestBlockEntity.class).setItem(0, new ItemStack(Items.IRON_INGOT, 5));
        state(helper).wake(team);
        choose(helper, team, TEN_MINUTES);
        helper.runAtTickTime(1, () -> {
            helper.assertValueEqual(cod(helper).interval(), TWICE, "the interval ten minutes into the challenge");
            make(helper, team, cap() - 1);
        });
        helper.runAtTickTime(2, () -> {
            helper.assertValueEqual(state(helper).inflation(team).orElseThrow().excess(), (long) cap() - 1, "the excess just short of the cap");
            helper.assertValueEqual(player.heard(RESET), 0, "reset announcements short of the cap");
            make(helper, team, 1);
        });
        helper.runAtTickTime(3, () -> {
            long now = helper.getLevel().getServer().overworld().getGameTime();
            Inflation inflation = state(helper).inflation(team).orElseThrow();
            helper.assertValueEqual(inflation.excess(), 0L, "the excess once the cap was reached");
            helper.assertValueEqual(inflation.made(), 0L, "the essence made once the cap was reached");
            helper.assertTrue(now - inflation.began() <= 1, "the challenge didn't begin again as the cap was reached");
            helper.assertValueEqual(cod(helper).interval(), WAKING, "the interval once the cap was reached");
            helper.assertValueEqual(state(helper).handicap(team), Optional.of(Handicap.ENERGY), "the handicap once the cap was reached");
            helper.assertTrue(state(helper).isAwake(team), "C.O.D. fell dormant as the cap was reached");
            helper.assertBlockPresent(Cod.BLOCK.get(), COD);
            ItemStack kept = helper.getBlockEntity(CHEST, ChestBlockEntity.class).getItem(0);
            helper.assertTrue(kept.is(Items.IRON_INGOT) && kept.getCount() == 5, "the chest's items weren't kept: " + kept);
            helper.assertValueEqual(player.heard(RESET), 1, "reset announcements once the cap was reached");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** Two teams play a challenge, ten minutes in; one reaches its cap, the other is one short of it. */
    private static void leavesAnotherTeamAlone(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper, COD, COD);
        UUID other = TestTeams.claim(helper, OTHER_COD, OTHER_COD);
        Listener stranger = Listener.of(helper, other);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(OTHER_COD, Cod.BLOCK.get());
        state(helper).wake(team);
        state(helper).wake(other);
        choose(helper, team, TEN_MINUTES);
        choose(helper, other, TEN_MINUTES);
        helper.runAtTickTime(1, () -> {
            make(helper, team, cap());
            make(helper, other, cap() - 1);
        });
        helper.runAtTickTime(2, () -> {
            helper.assertValueEqual(state(helper).inflation(team).orElseThrow().excess(), 0L, "the excess of the team that reached its cap");
            Inflation inflation = state(helper).inflation(other).orElseThrow();
            helper.assertValueEqual(inflation.excess(), (long) cap() - 1, "the other team's excess");
            helper.assertValueEqual(cod(helper, OTHER_COD).interval(), TWICE, "the other team's interval");
            helper.assertValueEqual(stranger.heard(RESET), 0, "reset announcements to the other team");
            stranger.leave(helper);
            helper.succeed();
        });
    }

    /** The team nears its cap, goes on nearer, falls back below 80% of it and nears it again. */
    private static void warnsOnceAndAgain(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Listener player = Listener.of(helper, team);
        choose(helper, team, 0);
        int near = (int) Math.ceil(cap() * 0.8);
        helper.runAtTickTime(1, () -> make(helper, team, near - 1));
        helper.runAtTickTime(2, () -> {
            helper.assertValueEqual(player.heard(WARNING), 0, "warnings just short of 80% of the cap");
            make(helper, team, 1);
        });
        helper.runAtTickTime(3, () -> {
            helper.assertValueEqual(player.heard(WARNING), 1, "warnings at 80% of the cap");
            make(helper, team, 1);
        });
        helper.runAtTickTime(4, () -> {
            helper.assertValueEqual(player.heard(WARNING), 1, "warnings on past 80% of the cap");
            decomposeByHand(helper, team);
            decomposeByHand(helper, team);
        });
        helper.runAtTickTime(5, () -> {
            helper.assertValueEqual(player.heard(WARNING), 1, "warnings after falling back below 80% of the cap");
            make(helper, team, 1);
        });
        helper.runAtTickTime(6, () -> {
            helper.assertValueEqual(player.heard(WARNING), 2, "warnings at 80% of the cap again");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** Warned near its cap, the team stays warned after a load; once it reaches the cap, nearing it warns again. */
    private static void warningSurvivesALoad(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Listener player = Listener.of(helper, team);
        choose(helper, team, 0);
        int near = (int) Math.ceil(cap() * 0.8);
        helper.runAtTickTime(1, () -> make(helper, team, near));
        helper.runAtTickTime(2, () -> {
            helper.assertValueEqual(player.heard(WARNING), 1, "warnings at 80% of the cap");
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var codec = TeamState.TYPE.codec();
            Tag saved = codec.encodeStart(ops, state(helper)).getOrThrow();
            TeamState loaded = codec.parse(ops, saved).getOrThrow();
            helper.assertTrue(loaded.warned(team), "the team wasn't warned after a load");
            make(helper, team, cap() - near);
        });
        helper.runAtTickTime(3, () -> {
            helper.assertValueEqual(player.heard(RESET), 1, "reset announcements once the cap was reached");
            helper.assertFalse(state(helper).warned(team), "the team was still warned once the challenge started again");
            make(helper, team, near);
        });
        helper.runAtTickTime(4, () -> {
            helper.assertValueEqual(player.heard(WARNING), 2, "warnings at 80% of the cap after the reset");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** The team nears its cap with none of its players online; the first to log in is warned. */
    private static void warningWaitsForAPlayer(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        choose(helper, team, 0);
        helper.runAtTickTime(1, () -> make(helper, team, (int) Math.ceil(cap() * 0.8)));
        helper.runAtTickTime(3, () -> helper.assertFalse(state(helper).warned(team), "the team was warned with nobody online"));
        Listener[] player = new Listener[1];
        helper.runAtTickTime(4, () -> player[0] = Listener.of(helper, team));
        helper.runAtTickTime(6, () -> {
            helper.assertValueEqual(player[0].heard(WARNING), 1, "warnings to the player who logged in near the cap");
            player[0].leave(helper);
            helper.succeed();
        });
    }

    /** Decomposes one Advanced essence by hand, as a player of {@code team}. */
    private static void decomposeByHand(GameTestHelper helper, UUID team) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(Essence.ADVANCED)));
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }

    /** Counts {@code count} essence {@code team}'s awake C.O.D. made, as it does when it makes one. */
    private static void make(GameTestHelper helper, UUID team, int count) {
        for (int i = 0; i < count; i++) {
            state(helper).countMade(team);
        }
    }

    /** Chooses the energy challenge for {@code team}, as though it began {@code ago} ticks before now. */
    private static void choose(GameTestHelper helper, UUID team, long ago) {
        long now = helper.getLevel().getServer().overworld().getGameTime();
        helper.assertTrue(state(helper).choose(team, Handicap.ENERGY, now - ago), "the challenge didn't start");
    }

    private static int cap() {
        return ServerConfig.inflationCap();
    }

    private static CodBlockEntity cod(GameTestHelper helper) {
        return cod(helper, COD);
    }

    private static CodBlockEntity cod(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos, CodBlockEntity.class);
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(helper.getLevel().getServer());
    }

    /** A player of a team, online as {@link GameTestHelper#makeMockServerPlayerInLevel} puts one, who keeps what it reads in chat. */
    private static final class Listener extends ServerPlayer {

        private final List<Component> read = new ArrayList<>();

        private Listener(GameTestHelper helper, CommonListenerCookie cookie) {
            super(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        }

        static Listener of(GameTestHelper helper, UUID team) {
            var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-listener"), false);
            var player = new Listener(helper, cookie);
            // Named in full: inside a ServerPlayer subclass the simple name resolves to another, inherited type.
            var connection = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND);
            new EmbeddedChannel(connection);
            helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
            TestTeams.join(player, team);
            return player;
        }

        @Override
        public void sendSystemMessage(Component message) {
            read.add(message);
            super.sendSystemMessage(message);
        }

        /** How many of the lines it read in chat were {@code key}'s. */
        int heard(String key) {
            return (int) read.stream()
                    .filter(line -> line.getContents() instanceof TranslatableContents contents && contents.getKey().equals(key))
                    .count();
        }

        void leave(GameTestHelper helper) {
            helper.getLevel().getServer().getPlayerList().remove(this);
        }
    }
}
