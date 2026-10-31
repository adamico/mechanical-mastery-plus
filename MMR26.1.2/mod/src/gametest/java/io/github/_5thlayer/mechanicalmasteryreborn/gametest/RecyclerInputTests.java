// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A player's right-click on a Recycler input (#218), as the server plays it, falling back to the held item's own use:
 * it inserts what the Recycler takes, or refuses it and says why on the action bar, and never places a block. Only a
 * sneaking right-click places one against it.
 */
final class RecyclerInputTests {

    private static final String WHY = "mechanicalmasteryreborn.recycler.input.";

    private RecyclerInputTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("recycler_input_clicked_with_a_refined_block_places_nothing_and_says_why", 1,
                helper -> refused(helper, new ItemStack(Items.STONE), "refined"));
        tests.test("recycler_input_clicked_with_a_refined_item_says_why", 1, helper -> refused(helper, new ItemStack(Items.IRON_INGOT), "refined"));
        tests.test("recycler_input_clicked_with_a_storage_block_says_to_uncraft_it", 1,
                helper -> refused(helper, new ItemStack(Items.IRON_BLOCK), "uncraft_instead"));
        tests.test("recycler_input_clicked_with_a_recyclable_block_inserts_it", 1, RecyclerInputTests::inserts);
        tests.test("recycler_input_clicked_while_the_core_holds_another_item_says_so", 1,
                helper -> refused(helper, new ItemStack(Items.PISTON), new ItemStack(Items.HOPPER), "holds_another"));
        tests.test("recycler_input_clicked_while_the_core_is_full_says_so", 1,
                helper -> refused(helper, new ItemStack(Items.PISTON, 64), new ItemStack(Items.PISTON), "full"));
        tests.test("recycler_input_clicked_with_what_it_never_takes_says_so_whatever_the_core_holds", 1,
                helper -> refused(helper, new ItemStack(Items.PISTON), filledShulkerBox(), "not_taken"));
        tests.test("recycler_input_of_no_recycler_clicked_places_nothing_and_says_why", 1, RecyclerInputTests::noRecycler);
        tests.test("recycler_input_sneak_clicked_places_a_block", 1, RecyclerInputTests::sneakPlaces);
    }

    private static void refused(GameTestHelper helper, ItemStack held, String why) {
        refused(helper, ItemStack.EMPTY, held, why);
    }

    /** A player clicks the input of a Recycler whose core holds {@code holding}: it keeps {@code held}, nothing is placed, and it is told why. */
    private static void refused(GameTestHelper helper, ItemStack holding, ItemStack held, String why) {
        var build = RecyclerBuild.standard(helper);
        if (!holding.isEmpty()) {
            build.feed(holding.copy());
        }
        expectRefused(helper, build.firstOf(Recycler.INPUT.get()), held, why);
        helper.assertTrue(ItemStack.matches(build.core().held(), holding), "the core holds " + build.core().held() + ", not " + holding);
        helper.succeed();
    }

    private static void noRecycler(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        helper.setBlock(RecyclerBuild.CORE, Blocks.AIR);
        expectRefused(helper, build.firstOf(Recycler.INPUT.get()), new ItemStack(Items.STONE), "no_recycler");
        helper.succeed();
    }

    private static void expectRefused(GameTestHelper helper, BlockPos input, ItemStack held, String why) {
        var player = Clicker.of(helper);
        ItemStack before = held.copy();
        player.click(helper, input, held);
        helper.assertTrue(ItemStack.matches(player.getMainHandItem(), before), "the player holds " + player.getMainHandItem() + ", not " + before);
        helper.assertBlockPresent(Blocks.AIR, input.above());
        helper.assertValueEqual(player.told(), List.of(WHY + why), "what the action bar said");
        player.leave(helper);
    }

    private static ItemStack filledShulkerBox() {
        var filled = new ItemStack(Items.SHULKER_BOX);
        filled.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND))));
        return filled;
    }

    /** A Recycler Casing, a crafted block, goes in rather than against the input. */
    private static void inserts(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        BlockPos input = build.firstOf(Recycler.INPUT.get());
        var player = Clicker.of(helper);
        player.click(helper, input, new ItemStack(Recycler.CASING.get(), 2));
        helper.assertValueEqual(build.core().held().getCount(), 2, "the casings the core took");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the player still holds " + player.getMainHandItem());
        helper.assertBlockPresent(Blocks.AIR, input.above());
        helper.assertValueEqual(player.told(), List.of(), "what the action bar said");
        player.leave(helper);
        helper.succeed();
    }

    private static void sneakPlaces(GameTestHelper helper) {
        var build = RecyclerBuild.standard(helper);
        BlockPos input = build.firstOf(Recycler.INPUT.get());
        var player = Clicker.of(helper);
        player.setShiftKeyDown(true);
        player.click(helper, input, new ItemStack(Recycler.CASING.get()));
        helper.assertBlockPresent(Recycler.CASING.get(), input.above());
        helper.assertTrue(build.core().held().isEmpty(), "the core took " + build.core().held());
        helper.assertValueEqual(player.told(), List.of(), "what the action bar said");
        player.leave(helper);
        helper.succeed();
    }

    /** A survival player online, as {@link GameTestHelper#makeMockServerPlayerInLevel} puts one, who keeps what its action bar says. */
    private static final class Clicker extends ServerPlayer {

        private final List<Component> overlay = new ArrayList<>();

        private Clicker(GameTestHelper helper, CommonListenerCookie cookie) {
            super(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        }

        static Clicker of(GameTestHelper helper) {
            var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-clicker"), false);
            var player = new Clicker(helper, cookie);
            // Named in full: inside a ServerPlayer subclass the simple name resolves to another, inherited type.
            var connection = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND);
            new EmbeddedChannel(connection);
            helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
            player.setGameMode(GameType.SURVIVAL);
            return player;
        }

        /** Right-clicks the top of {@code relative} holding {@code held}, as the server plays a click. */
        void click(GameTestHelper helper, BlockPos relative, ItemStack held) {
            setItemInHand(InteractionHand.MAIN_HAND, held);
            BlockPos absolute = helper.absolutePos(relative);
            var hit = new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.UP, 0.5), Direction.UP, absolute, false);
            gameMode.useItemOn(this, helper.getLevel(), getMainHandItem(), InteractionHand.MAIN_HAND, hit);
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

        void leave(GameTestHelper helper) {
            helper.getLevel().getServer().getPlayerList().remove(this);
        }
    }
}
