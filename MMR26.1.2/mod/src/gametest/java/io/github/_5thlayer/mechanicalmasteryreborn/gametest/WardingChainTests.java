// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;
import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import io.github._5thlayer.craftworks.machine.AssemblerTier;
import io.github._5thlayer.craftworks.machine.Assemblers;
import io.github._5thlayer.fieldworks.harvester.HarvesterBlockEntity;
import io.github._5thlayer.fieldworks.harvester.Harvesters;
import io.github._5thlayer.fieldworks.harvester.TierBlock;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ObsidianFlakes;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The protection challenge's Chain on the suite's machines (#252): Render makes Ward Tallow from flesh, bone meal and
 * Wireworks' steam, Flake takes Obsidian Flakes off a placed obsidian block with a Harvester and Fieldworks' magma
 * slurry, Charge makes a Charged Rift from Rift Shards, Fieldworks' tar, the flakes and the tallow, and Seal the Warding
 * Essence from the Charged Rifts and Wireworks' plastic, each the pack's own recipe in {@code suite_pack/}. The
 * Assembler's steps run on a tier 1 Assembler, the lowest, with their inputs set in its slots and boxes and FE fed to
 * it until the craft is done; Flake on a Burner Harvester fed coal and slurry as a hopper and a pipe would. Only the
 * {@code suiteGameTestServer} run, which loads those mods, registers them.
 */
final class WardingChainTests {

    private static final BlockPos ORIGIN = new BlockPos(4, 1, 4);
    /** More ticks than any of the Chain's crafts takes. */
    private static final int MOST_TICKS = 1000;

    private WardingChainTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("warding_render_makes_ward_tallow_from_steam", 40, WardingChainTests::render);
        tests.test("warding_render_fills_a_bucket_with_ward_tallow", 40, WardingChainTests::renderFillsABucket);
        tests.test("warding_flake_takes_obsidian_flakes_off_obsidian_with_magma_slurry", 40, WardingChainTests::flake);
        tests.test("warding_charge_makes_a_charged_rift", 40, WardingChainTests::charge);
        tests.test("warding_charge_drains_ward_tallow_from_a_bucket", 40, WardingChainTests::chargeDrainsABucket);
        tests.test("warding_seal_makes_a_warding_essence", 40, WardingChainTests::seal);
        tests.test("warding_a_charged_rift_recycles_from_its_recipe", 1, WardingChainTests::chargedRiftRecycles);
    }

    private static void render(GameTestHelper helper) {
        AssemblerBlockEntity assembler = holding(helper, "ward_tallow");
        put(assembler, 0, Items.ROTTEN_FLESH, 4);
        put(assembler, 1, Items.BONE_MEAL, 2);
        assembler.fluids().set(0, new FluidStack(fluid("wireworks:steam"), 1000));
        int ran = tickUntil(helper, assembler, () -> !assembler.fluids().contents(2).isEmpty());
        FluidStack made = assembler.fluids().contents(2);
        helper.assertTrue(made.getFluid() == WardTallow.SOURCE.get() && made.getAmount() == 500,
                "Render made " + made.getAmount() + " mB of " + made.getFluid() + " in " + ran + " ticks, not 500 mB of Ward Tallow");
        helper.assertTrue(assembler.fluids().contents(0).isEmpty() && assembler.inventory().getAmountAsInt(0) == 0
                && assembler.inventory().getAmountAsInt(1) == 0, "Render left some of its flesh, bone meal or steam");
        helper.succeed();
    }

    /** One operation breaks the obsidian to air for 4 flakes, which go to a chest at the Harvester's front, and 250 mB of slurry. */
    private static void flake(GameTestHelper helper) {
        BlockPos harvester = new BlockPos(4, 2, 4);
        BlockPos chest = harvester.relative(Direction.NORTH, 2);
        helper.setBlock(harvester.below(), Blocks.OBSIDIAN);
        helper.setBlock(chest, Blocks.CHEST);
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "warding_flake"));
        player.setGameMode(GameType.SURVIVAL);
        // A footprint faces opposite the player who lays it.
        player.setYRot(Direction.SOUTH.toYRot());
        player.setYHeadRot(Direction.SOUTH.toYRot());
        ItemStack stack = new ItemStack(Harvesters.item(TierBlock.HARVESTER_1).get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos floor = helper.absolutePos(harvester.below());
        player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor).relative(Direction.UP, 0.5), Direction.UP, floor, false));
        HarvesterBlockEntity machine = helper.getBlockEntity(harvester, HarvesterBlockEntity.class);
        var slurry = FluidResource.of(fluid("fieldworks:magma_slurry"));
        try (Transaction feeding = Transaction.openRoot()) {
            helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(harvester.west()), Direction.WEST)
                    .insert(ItemResource.of(Items.COAL), 8, feeding);
            helper.assertValueEqual(helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(harvester.east()),
                    Direction.EAST).insert(slurry, 1000, feeding), 1000, "the magma slurry piped in");
            feeding.commit();
        }
        machine.setHeldRecipe(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "harvesting/obsidian_flakes"));
        int ran = 0;
        var flakes = helper.getBlockEntity(chest, ChestBlockEntity.class);
        while (flakes.countItem(ObsidianFlakes.ITEM.get()) == 0 && ran < MOST_TICKS) {
            machine.serverTick(helper.getLevel());
            ran++;
        }
        helper.assertValueEqual(flakes.countItem(ObsidianFlakes.ITEM.get()), 4, "the Obsidian Flakes in the chest after " + ran + " ticks, "
                + machine.idleReason().map(Object::toString).orElse("not idle"));
        helper.assertTrue(helper.getBlockState(harvester.below()).isAir(), "the obsidian outlived its operation");
        var tanks = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(harvester.east()), Direction.EAST);
        int left = 0;
        for (int index = 0; index < tanks.size(); index++) {
            if (tanks.getResource(index).equals(slurry)) {
                left += tanks.getAmountAsInt(index);
            }
        }
        helper.assertValueEqual(left, 750, "the magma slurry left after one operation");
        helper.succeed();
    }

    private static void charge(GameTestHelper helper) {
        AssemblerBlockEntity assembler = holding(helper, "charged_rift");
        put(assembler, 0, RiftShards.ITEM.get(), 2);
        put(assembler, 1, item("fieldworks:tar"), 1);
        put(assembler, 2, ObsidianFlakes.ITEM.get(), 2);
        assembler.fluids().set(0, new FluidStack(WardTallow.SOURCE.get(), 500));
        tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        expectOutput(helper, assembler, RiftShards.CHARGED_RIFT.get(), "Charge");
        helper.assertValueEqual(assembler.fluids().contents(0).getAmount(), 250, "the Ward Tallow left after Charge");
        helper.succeed();
    }

    /** Two Renders make a bucket's worth, which the fill slot takes and the outputs hold as a Ward Tallow bucket. */
    private static void renderFillsABucket(GameTestHelper helper) {
        AssemblerBlockEntity assembler = holding(helper, "ward_tallow");
        put(assembler, 0, Items.ROTTEN_FLESH, 8);
        put(assembler, 1, Items.BONE_MEAL, 4);
        assembler.fluids().set(0, new FluidStack(fluid("wireworks:steam"), 2000));
        put(assembler, AssemblerSlots.FILL, Items.BUCKET, 1);
        tickUntil(helper, assembler, () -> outputs(assembler, WardTallow.BUCKET.get()) > 0);
        helper.assertValueEqual(outputs(assembler, WardTallow.BUCKET.get()), 1, "the Ward Tallow buckets in the outputs");
        helper.succeed();
    }

    /** A Ward Tallow bucket in the drain slot pours into Charge's box, as a crude bucket would into another recipe's. */
    private static void chargeDrainsABucket(GameTestHelper helper) {
        AssemblerBlockEntity assembler = holding(helper, "charged_rift");
        put(assembler, 0, RiftShards.ITEM.get(), 2);
        put(assembler, 1, item("fieldworks:tar"), 1);
        put(assembler, 2, ObsidianFlakes.ITEM.get(), 2);
        put(assembler, AssemblerSlots.DRAIN, WardTallow.BUCKET.get(), 1);
        tickUntil(helper, assembler, () -> outputs(assembler, RiftShards.CHARGED_RIFT.get()) > 0);
        helper.assertValueEqual(outputs(assembler, RiftShards.CHARGED_RIFT.get()), 1, "the Charged Rifts in the outputs");
        helper.assertValueEqual(assembler.fluids().contents(0).getAmount(), 750, "the Ward Tallow left of the bucket after Charge");
        helper.succeed();
    }

    private static void seal(GameTestHelper helper) {
        AssemblerBlockEntity assembler = holding(helper, "warding_essence");
        put(assembler, 0, RiftShards.CHARGED_RIFT.get(), 2);
        put(assembler, 1, item("wireworks:plastic"), 2);
        put(assembler, 2, Items.STRING, 2);
        put(assembler, 3, Items.GUNPOWDER, 1);
        put(assembler, 4, Items.IRON_INGOT, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 1000));
        tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        expectOutput(helper, assembler, Essences.item(Essence.WARDING), "Seal");
        helper.assertTrue(assembler.fluids().contents(0).isEmpty(), "Seal left some of its water");
        helper.succeed();
    }

    /** By the usual rule, read off Charge: its items, at full cost, and not the tallow; the flakes, which only a harvest makes, are raw. */
    private static void chargedRiftRecycles(GameTestHelper helper) {
        List<? extends String> before = ServerConfig.ASSEMBLING_TYPES.get();
        // As the pack's config does.
        ServerConfig.ASSEMBLING_TYPES.set(List.of("craftworks:assembling"));
        try {
            var outcome = Recycling.recycle(helper.getLevel().getServer(), RiftShards.CHARGED_RIFT.get());
            if (!(outcome instanceof RecyclingRules.Into<Item>(List<RecyclingRules.Share<Item>> shares))) {
                helper.fail("a Charged Rift does not recycle: " + outcome);
                return;
            }
            Map<Item, Double> into = shares.stream().collect(Collectors.toMap(RecyclingRules.Share::item, RecyclingRules.Share::amount, Double::sum));
            helper.assertValueEqual(into, Map.of(RiftShards.ITEM.get(), 2.0, item("fieldworks:tar"), 1.0, ObsidianFlakes.ITEM.get(), 2.0),
                    "what a Charged Rift recycles into");
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
    private static int tickUntil(GameTestHelper helper, AssemblerBlockEntity assembler, BooleanSupplier done) {
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

    /** How many of {@code item} the shared outputs hold. */
    private static int outputs(AssemblerBlockEntity assembler, Item item) {
        int count = 0;
        for (int n = 0; n < AssemblerSlots.OUTPUTS; n++) {
            int slot = AssemblerSlots.output(n);
            if (assembler.inventory().getResource(slot).getItem() == item) {
                count += assembler.inventory().getAmountAsInt(slot);
            }
        }
        return count;
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
    }

    private static Fluid fluid(String id) {
        return BuiltInRegistries.FLUID.getValue(Identifier.parse(id));
    }
}
