// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;
import java.util.function.BooleanSupplier;

import com.mojang.authlib.GameProfile;
import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import io.github._5thlayer.craftworks.machine.AssemblerTier;
import io.github._5thlayer.craftworks.machine.Assemblers;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * A suite test's Assembler, Craftworks' own on the {@code suiteGameTestServer} run: a tier 1 Assembler, the lowest,
 * holding one of the pack's assembling recipes in {@code suite_pack/}, its inputs set in its slots and FE fed to it until
 * the craft is done (#252, #265).
 */
final class SuiteAssembler {

    static final BlockPos ORIGIN = new BlockPos(4, 1, 4);
    /** More ticks than any of the pack's assembling recipes takes. */
    static final int MOST_TICKS = 1000;

    private SuiteAssembler() {
    }

    /** A tier 1 Assembler placed at {@link #ORIGIN} by a player's click, holding the pack's recipe {@code assembling/<path>}. */
    static AssemblerBlockEntity holding(GameTestHelper helper, String path) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "suite_assembler"));
        player.setGameMode(GameType.SURVIVAL);
        ItemStack stack = new ItemStack(Assemblers.item(AssemblerTier.ONE).get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos floor = helper.absolutePos(ORIGIN.below());
        player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor).relative(Direction.UP, 0.5), Direction.UP, floor, false));
        AssemblerBlockEntity assembler = helper.getBlockEntity(ORIGIN, AssemblerBlockEntity.class);
        assembler.setHeldRecipe(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assembling/" + path), player);
        return assembler;
    }

    static void put(AssemblerBlockEntity assembler, int slot, Item item, int count) {
        assembler.inventory().set(slot, ItemResource.of(item), count);
    }

    /** Ticks the Assembler, fed FE, until {@code done}; how many ticks it took. */
    static int tickUntil(GameTestHelper helper, AssemblerBlockEntity assembler, BooleanSupplier done) {
        var supply = new SimpleEnergyHandler(1_000_000, 1_000_000, 1_000_000, 1_000_000);
        var face = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(ORIGIN), Direction.UP);
        helper.assertTrue(face != null, "the Assembler takes no FE");
        int ran = 0;
        while (!done.getAsBoolean() && ran < MOST_TICKS) {
            EnergyHandlerUtil.move(supply, face, 1000, null);
            assembler.serverTick(helper.getLevel());
            ran++;
        }
        helper.assertTrue(done.getAsBoolean(), "the Assembler made nothing in " + MOST_TICKS + " ticks: " + assembler.state());
        return ran;
    }

    static void expectOutput(GameTestHelper helper, AssemblerBlockEntity assembler, Item made, String step) {
        var output = assembler.inventory().getResource(AssemblerSlots.OUTPUT);
        int count = assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT);
        helper.assertTrue(output.getItem() == made && count == 1, step + " made " + count + " " + output.getItem() + ", not one " + made);
        for (int slot = 0; slot < AssemblerSlots.INPUTS; slot++) {
            helper.assertValueEqual(assembler.inventory().getAmountAsInt(slot), 0, "what " + step + " left in input " + slot);
        }
    }
}
