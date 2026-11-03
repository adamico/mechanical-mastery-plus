// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.gateways;

import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Gates;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Gateways to Eternity's part: the Assault's gate is the pack's endless gate {@link #ASSAULT}, opened as a Gate Pearl
 * opens one. Its summoner is the team member the Assault names; with none, a fake player, so Gateways' mobs fall back
 * to the nearest player.
 */
public final class GatewaysGates implements Gates {

    /** The endless gate the pack writes for the Assault. */
    public static final Identifier ASSAULT = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assault");

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public Optional<UUID> open(ServerLevel level, Vec3 at, @Nullable ServerPlayer summoner) {
        return open(level, at, summoner, 0);
    }

    @Override
    public Optional<UUID> open(ServerLevel level, Vec3 at, @Nullable ServerPlayer summoner, int wave) {
        var gate = GatewayRegistry.INSTANCE.holder(ASSAULT);
        if (!gate.isBound()) {
            LOGGER.warn("No gate {} to open for the Assault", ASSAULT);
            return Optional.empty();
        }
        Player player = summoner != null ? summoner : FakePlayerFactory.getMinecraft(level);
        var entity = gate.get().createEntity(level, player);
        entity.setPos(at.x, at.y, at.z);
        entity.getEntityData().set(GatewayEntity.WAVE, wave);
        level.addFreshEntity(entity);
        entity.onGateCreated();
        return Optional.of(entity.getUUID());
    }

    @Override
    public int wave(Entity gate) {
        return gate instanceof GatewayEntity gateway ? gateway.getWave() : 0;
    }
}
