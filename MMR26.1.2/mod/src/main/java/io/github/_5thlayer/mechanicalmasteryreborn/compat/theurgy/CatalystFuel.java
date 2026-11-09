// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy;

import java.util.function.IntUnaryOperator;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * The Mercury Catalyst's FE store (ADR 0018, #328). It takes FE from any side, from cables, FE drives or Theurgy's
 * Mercurial Flux Connector, holds {@link #UNITS} units of flux's worth and gives none back. Before each tick's flux
 * the Catalyst asks {@link #convert}, which makes only what the store pays for at {@code energy.fe_per_flux}, and none
 * without it. Knows nothing of Theurgy, so the game tests reach it.
 */
public final class CatalystFuel extends SimpleEnergyHandler {

    /** How many units of flux the store holds the FE for. */
    public static final int UNITS = 1000;

    private static final Identifier CATALYST = Identifier.fromNamespaceAndPath("theurgy", "mercury_catalyst");

    /** Counts the server config's loads, so each store sizes itself again once per load, not on every tick. */
    private static volatile int configLoads;

    private final Runnable changed;
    private int sizedFor = -1;

    /** Lets a cable reach the Catalyst's store, with Theurgy installed, and sizes the stores to each config load. */
    public static void register(IEventBus modBus) {
        modBus.addListener(CatalystFuel::registerCapability);
        modBus.addListener(ModConfigEvent.Loading.class, event -> onConfig(event.getConfig()));
        modBus.addListener(ModConfigEvent.Reloading.class, event -> onConfig(event.getConfig()));
    }

    private static void onConfig(ModConfig config) {
        if (config.getType() == ModConfig.Type.SERVER && MechanicalMasteryReborn.MOD_ID.equals(config.getModId())) {
            configReloaded();
        }
    }

    /** Marks every store to size itself to the server config again, as a load or reload of it does. */
    public static void configReloaded() {
        configLoads++;
    }

    private static void registerCapability(RegisterCapabilitiesEvent event) {
        BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(CATALYST).ifPresent(type -> event.registerBlockEntity(Capabilities.Energy.BLOCK,
                type, (entity, side) -> entity instanceof FueledCatalyst catalyst ? catalyst.mechanicalmasteryreborn$fuel() : null));
    }

    public CatalystFuel(Runnable changed) {
        super(0, 0, 0);
        this.changed = changed;
        fit();
    }

    /**
     * Makes up to {@code flux} units through {@code store}, which answers how many it took, and pays for those taken.
     * Answers the flux made.
     */
    public int convert(int flux, IntUnaryOperator store) {
        if (sizedFor != configLoads) {
            fit();
        }
        int rate = ServerConfig.energyFePerFlux();
        int paid = Math.min(flux, energy / rate);
        if (paid <= 0) {
            return 0;
        }
        int made = store.applyAsInt(paid);
        if (made > 0) {
            set(energy - made * rate);
        }
        return made;
    }

    /**
     * Sizes the store to the config as last loaded. Stored FE is kept up to the new capacity; only a smaller
     * {@code energy.fe_per_flux} spills what no longer fits.
     */
    private void fit() {
        sizedFor = configLoads;
        capacity = ServerConfig.energyFePerFlux() * UNITS;
        maxInsert = capacity;
        if (energy > capacity) {
            set(capacity);
        }
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        changed.run();
    }
}
