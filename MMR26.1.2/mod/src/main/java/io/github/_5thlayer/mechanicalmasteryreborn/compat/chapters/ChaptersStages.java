// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.chapters;

import com.gabinx.chapters.api.ChaptersAPI;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Chapters' stages. With FTB Teams installed Chapters keeps a stage on the player's FTB party, which is its island's
 * (#225), so one player given it gives it the whole team. Loaded only when Chapters is installed, and nothing else in the
 * mod names a Chapters type.
 */
public final class ChaptersStages implements Chains.Stages {

    @Override
    public void grant(Player player, String stage) {
        Identifier id = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, stage);
        if (player instanceof ServerPlayer server && !ChaptersAPI.hasStage(server, id)) {
            ChaptersAPI.addStage(server, id);
        }
    }

    @Override
    public void revoke(Player player, String stage) {
        Identifier id = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, stage);
        if (player instanceof ServerPlayer server && ChaptersAPI.hasStage(server, id)) {
            ChaptersAPI.removeStage(server, id);
        }
    }
}
