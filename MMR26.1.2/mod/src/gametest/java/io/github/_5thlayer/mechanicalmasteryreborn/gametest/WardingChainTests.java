// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;
import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import io.github._5thlayer.craftworks.machine.AssemblerTier;
import io.github._5thlayer.craftworks.machine.Assemblers;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.WardTallow;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.Recycling;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * The protection challenge's Chain on Craftworks' Assembler (#252): Render makes Ward Tallow from flesh, bone meal and
 * Wireworks' steam, Charge a Charged Rift from Rift Shards, Fieldworks' tar and the tallow, and Seal the Warding Essence
 * from the Charged Rifts and Wireworks' plastic, each the pack's own recipe in {@code suite_pack/}. Each runs on a tier 1
 * Assembler, the lowest, with its inputs set in its slots and boxes and FE fed to it until the craft is done. Only the
 * {@code suiteGameTestServer} run, which loads those mods, registers them.
 */
final class WardingChainTests {

    private static final BlockPos ORIGIN = new BlockPos(4, 1, 4);
    /** More ticks than any of the Chain's crafts takes. */
    private static final int MOST_TICKS = 400;

    private WardingChainTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("warding_render_makes_ward_tallow_from_steam", 40, WardingChainTests::render);
        tests.test("warding_charge_makes_a_charged_rift", 40, WardingChainTests::charge);
        tests.test("warding_seal_makes_a_warding_essence", 40, WardingChainTests::seal);
        tests.test("warding_a_charged_rift_recycles_from_its_recipe", 1, WardingChainTests::chargedRiftRecycles);
    }

    private static void render(GameTestHelper helper) {
        AssemblerBlockEntity assembler = holding(helper, "ward_tallow");
        put(assembler, 0, Items.ROTTEN_FLESH, 4);
        put(assembler, 1, Items.BONE_MEAL, 2);
        assembler.fluids().set(0, new FluidStack(fluid("wireworks:steam"), 1000));
        int ran = run(helper, assembler, () -> !assembler.fluids().contents(2).isEmpty());
        FluidStack made = assembler.fluids().contents(2);
        helper.assertTrue(made.getFluid() == WardTallow.SOURCE.get() && made.getAmount() == 500,
                "Render made " + made.getAmount() + " mB of " + made.getFluid() + " in " + ran + " ticks, not 500 mB of Ward Tallow");
        helper.assertTrue(assembler.fluids().contents(0).isEmpty() && assembler.inventory().getAmountAsInt(0) == 0
                && assembler.inventory().getAmountAsInt(1) == 0, "Render left some of its flesh, bone meal or steam");
        helper.succeed();
    }

    private static void charge(GameTestHelper helper) {
        AssemblerBlockEntity assembler = holding(helper, "charged_rift");
        put(assembler, 0, RiftShards.ITEM.get(), 2);
        put(assembler, 1, item("fieldworks:tar"), 1);
        put(assembler, 2, Items.SPIDER_EYE, 1);
        put(assembler, 3, Items.SLIME_BALL, 1);
        assembler.fluids().set(0, new FluidStack(WardTallow.SOURCE.get(), 500));
        run(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        expectOutput(helper, assembler, RiftShards.CHARGED.get(), "Charge");
        helper.assertValueEqual(assembler.fluids().contents(0).getAmount(), 250, "the Ward Tallow left after Charge");
        helper.succeed();
    }

    private static void seal(GameTestHelper helper) {
        AssemblerBlockEntity assembler = holding(helper, "warding_essence");
        put(assembler, 0, RiftShards.CHARGED.get(), 2);
        put(assembler, 1, item("wireworks:plastic"), 2);
        put(assembler, 2, Items.STRING, 2);
        put(assembler, 3, Items.GUNPOWDER, 1);
        put(assembler, 4, Items.IRON_INGOT, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 1000));
        run(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        expectOutput(helper, assembler, Essences.item(Essence.WARDING), "Seal");
        helper.assertTrue(assembler.fluids().contents(0).isEmpty(), "Seal left some of its water");
        helper.succeed();
    }

    /** By the usual rule, read off Charge: its items, at full cost, and not the tallow. */
    private static void chargedRiftRecycles(GameTestHelper helper) {
        List<? extends String> before = ServerConfig.ASSEMBLING_TYPES.get();
        // As the pack's config does.
        ServerConfig.ASSEMBLING_TYPES.set(List.of("craftworks:assembling"));
        try {
            var outcome = Recycling.recycle(helper.getLevel().getServer(), RiftShards.CHARGED.get());
            if (!(outcome instanceof RecyclingRules.Into<Item>(List<RecyclingRules.Share<Item>> shares))) {
                helper.fail("a Charged Rift does not recycle: " + outcome);
                return;
            }
            Map<Item, Double> into = shares.stream().collect(Collectors.toMap(RecyclingRules.Share::item, RecyclingRules.Share::amount, Double::sum));
            helper.assertValueEqual(into, Map.of(RiftShards.ITEM.get(), 2.0, item("fieldworks:tar"), 1.0, Items.SPIDER_EYE, 1.0,
                    Items.SLIME_BALL, 1.0), "what a Charged Rift recycles into");
        } finally {
            ServerConfig.ASSEMBLING_TYPES.set(before);
        }
        helper.succeed();
    }

    // -- fixtures -------------------------------------------------------------------------------

    /** A tier 1 Assembler placed at {@link #ORIGIN} by a player's click, holding the pack's recipe {@code assembling/<path>}. */
    private static AssemblerBlockEntity holding(GameTestHelper helper, String path) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "warding_chain"));
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

    private static void put(AssemblerBlockEntity assembler, int slot, Item item, int count) {
        assembler.inventory().set(slot, ItemResource.of(item), count);
    }

    /** Ticks the Assembler, fed FE, until {@code done}; how many ticks it took. */
    private static int run(GameTestHelper helper, AssemblerBlockEntity assembler, java.util.function.BooleanSupplier done) {
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

    private static void expectOutput(GameTestHelper helper, AssemblerBlockEntity assembler, Item made, String step) {
        var output = assembler.inventory().getResource(AssemblerSlots.OUTPUT);
        int count = assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT);
        helper.assertTrue(output.getItem() == made && count == 1, step + " made " + count + " " + output.getItem() + ", not one " + made);
        for (int slot = 0; slot < AssemblerSlots.INPUTS; slot++) {
            helper.assertValueEqual(assembler.inventory().getAmountAsInt(slot), 0, "what " + step + " left in input " + slot);
        }
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
    }

    private static Fluid fluid(String id) {
        return BuiltInRegistries.FLUID.getValue(Identifier.parse(id));
    }
}
