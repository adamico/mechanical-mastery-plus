// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import net.minecraft.world.entity.player.Player;

/** A player's food data, told which player it feeds, which vanilla's doesn't know, for the life handicap. */
public interface FedPlayer {

    void mechanicalmasteryreborn$feeds(Player player);
}
