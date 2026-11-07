// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Making;
import io.github._5thlayer.mechanicalmasteryreborn.port.Port;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A reset makes the attempt's Transmuted Essence stale (ADR 0016, #322). What a machine on a team's island makes while the
 * team plays that essence's challenge is made here as a block entity's tick makes it ({@link Making}), the central hook
 * every chain machine's work passes through; the suite run makes a Warding Essence on a real Assembler. A Port refuses
 * earlier essence after a reset, and after a stop and a new start, by hand and from a hopper, and takes the current
 * attempt's; essence with no stamp, from before the change or from a won chain, is current. The Shop and the
 * Quartessence, which take a stale one too, are {@code SuiteStaleEssenceTests}', on the suite run.
 */
final class StaleEssenceTests {

    private static final BlockPos PORT = new BlockPos(4, 2, 4);
    private static final BlockPos HOPPER = PORT.above();
    /** Where a chain machine stands on the island. */
    private static final BlockPos MACHINE = new BlockPos(2, 2, 6);
    /** Ample time for a hopper to push a few items, 8 ticks each. */
    private static final int HOPPER_TICKS = 100;

    private StaleEssenceTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("stale_port_refuses_essence_made_before_a_reset_and_takes_the_attempt_s", 1, StaleEssenceTests::afterAReset);
        tests.test("stale_port_refuses_essence_made_before_a_stop_and_a_new_start", 1, StaleEssenceTests::afterAStopAndAStart);
        tests.test("stale_hopper_keeps_a_stale_essence_and_feeds_the_attempt_s", HOPPER_TICKS + 10, StaleEssenceTests::hopper);
        tests.test("stale_refused_hand_offer_stays_in_the_hand_and_is_told_why", 1, StaleEssenceTests::handIsTold);
        tests.test("stale_port_takes_unstamped_essence_after_a_reset", 1, StaleEssenceTests::unstampedIsCurrent);
        tests.test("stale_won_chain_s_essence_is_never_stale", 1, StaleEssenceTests::wonChainNeverStale);
        tests.test("stale_essence_does_not_stack_with_the_attempt_s", 1, StaleEssenceTests::doesNotStack);
        tests.test("stale_attempt_survives_a_load", 1, StaleEssenceTests::survivesALoad);
    }

    /** After a reset, the Port refuses what the attempt before made, and takes what this one makes. */
    private static void afterAReset(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team, Handicap.ENERGY);
        ItemStack earlier = made(helper, Essence.GALVANIC, 3);
        helper.assertTrue(Challenges.reset(server(helper), team), "the challenge didn't reset");
        ItemStack current = made(helper, Essence.GALVANIC, 2);
        var hand = Hand.of(helper, team);
        helper.assertValueEqual(hand.offer(helper, earlier), 3, "the stale essence a hand keeps after a reset");
        helper.assertValueEqual(hand.offer(helper, current), 0, "the current essence a hand keeps after a reset");
        helper.assertValueEqual(state(helper).delivered(team), 2L, "the essence counted");
        helper.succeed();
    }

    /** A stop and a new start make what the earlier attempt made stale too, so a stop is no way around a reset. */
    private static void afterAStopAndAStart(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team, Handicap.ENERGY);
        ItemStack earlier = made(helper, Essence.GALVANIC, 3);
        helper.assertTrue(Challenges.end(server(helper), team), "the challenge didn't stop");
        start(helper, team, Handicap.ENERGY);
        ItemStack current = made(helper, Essence.GALVANIC, 2);
        var hand = Hand.of(helper, team);
        helper.assertValueEqual(hand.offer(helper, earlier), 3, "the stale essence a hand keeps after a stop and a start");
        helper.assertValueEqual(hand.offer(helper, current), 0, "the current essence a hand keeps after a stop and a start");
        helper.succeed();
    }

    /** A hopper above the Port keeps a stale essence, and feeds the current attempt's. */
    private static void hopper(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team, Handicap.ENERGY);
        ItemStack earlier = made(helper, Essence.GALVANIC, 3);
        helper.assertTrue(Challenges.reset(server(helper), team), "the challenge didn't reset");
        ItemStack current = made(helper, Essence.GALVANIC, 2);
        helper.setBlock(HOPPER, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        var hopper = helper.getBlockEntity(HOPPER, HopperBlockEntity.class);
        hopper.setItem(0, earlier);
        hopper.setItem(1, current);
        helper.runAtTickTime(HOPPER_TICKS, () -> {
            helper.assertValueEqual(hopper.getItem(0).getCount(), 3, "the stale essence a hopper keeps");
            helper.assertTrue(hopper.getItem(1).isEmpty(), "a hopper kept the current essence: " + hopper.getItem(1));
            helper.assertValueEqual(state(helper).delivered(team), 2L, "the essence counted");
            helper.succeed();
        });
    }

    /** A refused hand offer stays in the hand, and the action bar says it is stale. */
    private static void handIsTold(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team, Handicap.ENERGY);
        ItemStack earlier = made(helper, Essence.GALVANIC, 3);
        helper.assertTrue(Challenges.reset(server(helper), team), "the challenge didn't reset");
        var hand = Hand.of(helper, team);
        helper.assertValueEqual(hand.offer(helper, earlier), 3, "the stale essence a hand keeps");
        helper.assertValueEqual(hand.told(), List.of("mechanicalmasteryreborn.port.stale"), "what the action bar said");
        helper.succeed();
    }

    /** Essence with no stamp, as a world saved before the stamp holds, counts as the current attempt's. */
    private static void unstampedIsCurrent(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team, Handicap.ENERGY);
        helper.assertTrue(Challenges.reset(server(helper), team), "the challenge didn't reset");
        helper.assertTrue(Challenges.reset(server(helper), team), "the challenge didn't reset again");
        var hand = Hand.of(helper, team);
        helper.assertValueEqual(hand.offer(helper, new ItemStack(Essences.item(Essence.GALVANIC), 2)), 0,
                "the unstamped essence a hand keeps after a reset");
        helper.succeed();
    }

    /**
     * A won chain goes on making its essence while the team plays another challenge, and none of it is stamped: past a
     * reset and a stop of that other challenge, the essence is current, and its Port takes it once the win is reverted
     * and the challenge started again.
     */
    private static void wonChainNeverStale(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        helper.assertTrue(state(helper).winUnplayed(team, Handicap.ENERGY), "the energy challenge wasn't noted won");
        start(helper, team, Handicap.LIFE);
        ItemStack galvanic = made(helper, Essence.GALVANIC, 2);
        helper.assertTrue(Challenges.reset(server(helper), team), "the life challenge didn't reset");
        helper.assertTrue(Challenges.end(server(helper), team), "the life challenge didn't stop");
        helper.assertTrue(state(helper).revertWin(team, Handicap.ENERGY), "the energy win wasn't reverted");
        start(helper, team, Handicap.ENERGY);
        helper.assertValueEqual(Hand.of(helper, team).offer(helper, galvanic), 0, "the won chain's essence a hand keeps");
        helper.succeed();
    }

    /** A stale essence and a current one of the same kind never stack, and two of the attempt's do. */
    private static void doesNotStack(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        start(helper, team, Handicap.ENERGY);
        ItemStack earlier = made(helper, Essence.GALVANIC, 1);
        helper.assertTrue(Challenges.reset(server(helper), team), "the challenge didn't reset");
        ItemStack current = made(helper, Essence.GALVANIC, 1);
        helper.assertFalse(ItemStack.isSameItemSameComponents(earlier, current), "a stale essence stacks with the attempt's");
        helper.assertTrue(ItemStack.isSameItemSameComponents(current, made(helper, Essence.GALVANIC, 1)),
                "two of the attempt's essence don't stack");
        helper.succeed();
    }

    /** The attempt is saved with the team: after a load, what was made before a reset is still stale. */
    private static void survivesALoad(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team, Handicap.ENERGY);
        ItemStack earlier = made(helper, Essence.GALVANIC, 1);
        helper.assertTrue(Challenges.reset(server(helper), team), "the challenge didn't reset");
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var codec = TeamState.TYPE.codec();
        TeamState loaded = codec.parse(ops, codec.encodeStart(ops, state(helper)).getOrThrow()).getOrThrow();
        helper.assertValueEqual(loaded.attempt(team), state(helper).attempt(team), "the attempt after a load");
        var stack = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, earlier).getOrThrow()).getOrThrow();
        helper.assertValueEqual(Hand.of(helper, team).offer(helper, stack), 1, "the stale essence a hand keeps after a load");
        helper.succeed();
    }

    /** {@code count} of {@code essence}, as a chain machine on the island makes it in its tick. */
    private static ItemStack made(GameTestHelper helper, Essence essence, int count) {
        Making.atBlock(helper.getLevel(), helper.absolutePos(MACHINE));
        try {
            return new ItemStack(Essences.item(essence), count);
        } finally {
            Making.done();
        }
    }

    private static void place(GameTestHelper helper, Handicap handicap) {
        helper.setBlock(PORT, Port.BLOCKS_BY_HANDICAP.get(handicap).get());
    }

    private static void start(GameTestHelper helper, UUID team, Handicap handicap) {
        helper.assertTrue(Challenges.begin(server(helper), team, handicap), "the " + handicap.id() + " challenge didn't start");
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(server(helper));
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }

    /** A survival player of a team, as {@link GameTestHelper#makeMockPlayer} makes one, who keeps what its action bar says. */
    private static final class Hand extends Player {

        private final List<Component> overlay = new ArrayList<>();

        private Hand(GameTestHelper helper) {
            super(helper.getLevel(), new GameProfile(UUID.randomUUID(), "test-hand"));
        }

        static Hand of(GameTestHelper helper, UUID team) {
            var hand = new Hand(helper);
            TestTeams.join(hand, team);
            return hand;
        }

        /** Right-clicks the Port with {@code stack}, and says how many are left in hand. */
        int offer(GameTestHelper helper, ItemStack stack) {
            setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockPos pos = helper.absolutePos(PORT);
            helper.getBlockState(PORT).useItemOn(getMainHandItem(), helper.getLevel(), this, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            return getMainHandItem().getCount();
        }

        @Override
        public void sendOverlayMessage(Component message) {
            overlay.add(message);
            super.sendOverlayMessage(message);
        }

        /** The translation keys of what its action bar said, in order. */
        List<String> told() {
            return overlay.stream().map(line -> line.getContents() instanceof TranslatableContents contents ? contents.getKey() : line.getString())
                    .toList();
        }

        @Override
        public GameType gameMode() {
            return GameType.SURVIVAL;
        }

        @Override
        public boolean isClientAuthoritative() {
            return false;
        }
    }
}
