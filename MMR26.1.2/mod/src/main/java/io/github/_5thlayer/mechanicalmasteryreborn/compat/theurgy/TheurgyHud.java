// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy;

import com.klikli_dev.theurgy.config.ClientConfig;
import com.klikli_dev.theurgy.content.render.inworldhud.InWorldHUDBuilder;
import com.klikli_dev.theurgy.content.render.inworldhud.InWorldHUDProvider;
import com.klikli_dev.theurgy.content.render.inworldhud.InWorldHUDRegistry;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Theurgy's in-world HUD, which tells more of its machines than Jade does: a locked apparatus's says why it does no
 * work (#214), as {@link ChainMachines#line}, and Jade stays hidden wherever it shows, so the two never overlap. Loaded
 * only when Theurgy is installed, and nothing else in the mod names a Theurgy type.
 */
public final class TheurgyHud implements InWorldHUDProvider {

    private TheurgyHud() {
    }

    /** Adds the lock's line to the HUD of every Theurgy block, on both sides. */
    public static void register() {
        InWorldHUDRegistry.registerGenericProvider(new TheurgyHud());
    }

    /** Only the server knows what a team has unlocked, so this asks it for every Theurgy machine. */
    @Override
    public boolean applies(Level level, BlockPos pos, BlockState state, BlockEntity entity) {
        return true;
    }

    /** It adds a line to a HUD a machine already shows, and opens none of its own. */
    @Override
    public boolean activatesHUD() {
        return false;
    }

    @Override
    public void appendServerData(InWorldHUDBuilder builder, ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state,
            BlockEntity entity) {
        ChainMachines.lockedAt(level, pos, state)
                .ifPresent(handicap -> builder.addLine(ChainMachines.line(handicap).copy().withStyle(ChatFormatting.GOLD)));
    }

    /** Whether Theurgy's HUD shows for the block at {@code pos}, where Jade stays hidden. Client-only. */
    public static boolean shows(Level level, BlockPos pos) {
        return ClientConfig.get().rendering.enableInWorldHUD.get() && InWorldHUDRegistry.hasProviders(level, pos);
    }
}
