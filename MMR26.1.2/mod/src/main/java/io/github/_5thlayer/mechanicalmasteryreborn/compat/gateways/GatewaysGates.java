// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.gateways;

import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import dev.shadowsoffire.gateways.entity.EndlessGatewayEntity;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Gates;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Gateways to Eternity's part: the Assault's gate is the pack's endless gate {@link #ASSAULT}, opened as a Gate Pearl
 * opens one. Its summoner is the team member the Assault names; with none, a fake player, so Gateways' mobs fall back
 * to the nearest player. A gate of an Assault that fails opens again at once at its wave: it has no lives.
 */
public final class GatewaysGates implements Gates {

    /** The endless gate the pack writes for the Assault. */
    public static final Identifier ASSAULT = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assault");

    private static final Logger LOGGER = LogUtils.getLogger();

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
        if (entity instanceof EndlessGatewayEntity endless && wave > 0) {
            // The pause before the next wave is the reached wave's setup time, which Gateways notes only as a wave starts.
            entity.getEntityData().set(EndlessGatewayEntity.SETUP_TIME, endless.getCurrentWave().setupTime());
        }
        level.addFreshEntity(entity);
        entity.onGateCreated();
        return Optional.of(entity.getUUID());
    }

    @Override
    public int wave(Entity gate) {
        return gate instanceof GatewayEntity gateway ? gateway.getWave() : 0;
    }

    /**
     * Reopens an Assault's gate that fails, for its timer, a mob lost or out of range, to play again the wave it had
     * reached, notes each wave started for the team's bar, and moves the gate at a step. Gateways' count is of the
     * waves a gate has finished, and goes up only after its wave-end event: a gate failing, starting or ending wave 7
     * says 6.
     */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(GateEvent.Failed.class, event -> {
            GatewayEntity gate = event.getEntity();
            if (gate.level() instanceof ServerLevel level) {
                Assault.reopen(level, gate.getUUID(), gate.position(), gate.getWave());
            }
        });
        NeoForge.EVENT_BUS.addListener(GateEvent.WaveStarted.class, event -> {
            GatewayEntity gate = event.getEntity();
            if (gate.level() instanceof ServerLevel level) {
                Assault.noteWave(level, gate.getUUID(), gate.getWave() + 1);
            }
        });
        NeoForge.EVENT_BUS.addListener(GateEvent.WaveEnd.class, event -> {
            GatewayEntity gate = event.getEntity();
            if (gate.level() instanceof ServerLevel level) {
                Assault.step(level, gate.getUUID(), gate.getWave() + 1);
            }
        });
    }
}
