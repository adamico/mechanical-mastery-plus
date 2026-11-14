// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** A survival player online, as {@link GameTestHelper#makeMockServerPlayerInLevel} puts one, who keeps what its action bar says. */
final class Clicker extends ServerPlayer {

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
        click(helper, relative, Direction.UP, held);
    }

    /** Right-clicks the {@code side} face of {@code relative} holding {@code held}, as the server plays a click. */
    void click(GameTestHelper helper, BlockPos relative, Direction side, ItemStack held) {
        setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockPos absolute = helper.absolutePos(relative);
        var hit = new BlockHitResult(Vec3.atCenterOf(absolute).relative(side, 0.5), side, absolute, false);
        gameMode.useItemOn(this, helper.getLevel(), getMainHandItem(), InteractionHand.MAIN_HAND, hit);
    }

    /** Stands at the bottom centre of {@code relative}. */
    void standAt(GameTestHelper helper, BlockPos relative) {
        var at = helper.absolutePos(relative);
        snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
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
