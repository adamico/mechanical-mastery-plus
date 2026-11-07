// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;
import java.util.function.Consumer;

import com.ammonium.adminshop.recipes.RecipeManager;
import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import com.mojang.authlib.GameProfile;
import io.github._5thlayer.craftworks.assembler.PersonalAssembler;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Mint;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Making;
import io.github._5thlayer.mechanicalmasteryreborn.port.Port;
import io.github._5thlayer.mechanicalmasteryreborn.port.PortBlock;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * A stale Transmuted Essence on the suite's machines and AdminShop (#322), in the {@code suiteGameTestServer} run alone:
 * a Warding Essence sealed on a real Assembler, ticking as the world ticks it, on the island of a team playing protection
 * is stamped with its attempt, as an Aureate sealed from the mint's coins by a team playing money is (#323), so after a reset the Port refuses it and takes the next one sealed, as it refuses one
 * crafted on a member's Personal Assembler before the reset; and a stale essence,
 * like an unstamped or a won chain's one, still matches the Shop's sell Offer, the pack's own, and still goes into a
 * Quartessence. Its Assembler is a {@link SuiteAssembler}.
 */
final class SuiteStaleEssenceTests {

    private static final BlockPos PORT = new BlockPos(1, 1, 1);
    /** Where a chain machine stands on the island, for an essence made without a real one. */
    private static final BlockPos MACHINE = new BlockPos(1, 1, 6);
    /** Long enough for a tier 1 Assembler to seal twice. */
    private static final int TICKS = 2 * SuiteAssembler.MOST_TICKS;

    private SuiteStaleEssenceTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("suite_stale_warding_sealed_before_a_reset_is_refused_by_the_port", TICKS,
                helper -> sealedBeforeAReset(helper, Handicap.PROTECTION, "warding_essence", SuiteStaleEssenceTests::fillSeal));
        tests.test("suite_stale_aureate_sealed_before_a_reset_is_refused_by_the_port", TICKS,
                helper -> sealedBeforeAReset(helper, Handicap.MONEY, "aureate_essence", SuiteStaleEssenceTests::fillAureateSeal));
        tests.test("suite_stale_essence_crafted_on_the_personal_assembler_before_a_reset_is_refused_by_the_port", 1,
                SuiteStaleEssenceTests::craftedByHand);
        tests.test("suite_stale_essence_matches_the_shop_s_sell_offer", 1, SuiteStaleEssenceTests::shopBuysIt);
        tests.test("suite_stale_unstamped_and_won_chain_essence_match_the_shop_s_sell_offer", 1, SuiteStaleEssenceTests::shopBuysCurrent);
        tests.test("suite_stale_essence_goes_into_a_quartessence", 40, SuiteStaleEssenceTests::quartessence);
    }

    /** {@code handicap}'s essence sealed by the pack's recipe {@code assembling/<recipe>}, its inputs put in by {@code fill}. */
    private static void sealedBeforeAReset(GameTestHelper helper, Handicap handicap, String recipe, Consumer<AssemblerBlockEntity> fill) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(PORT, Port.BLOCKS_BY_HANDICAP.get(handicap).get());
        helper.assertTrue(state(helper).begin(team, handicap, helper.getLevel().getGameTime()), handicap.id() + " didn't start");
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, recipe);
        var supply = new SimpleEnergyHandler(1_000_000, 1_000_000, 1_000_000, 1_000_000);
        // FE fed each tick, as a cable would, while the world ticks the Assembler as it ticks every machine.
        helper.onEachTick(() -> {
            var face = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(SuiteAssembler.ORIGIN), Direction.UP);
            if (face != null) {
                EnergyHandlerUtil.move(supply, face, 1000, null);
            }
        });
        fill.accept(assembler);
        ItemStack[] earlier = new ItemStack[1];
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(sealed(assembler), "the Assembler sealed nothing"))
                .thenExecute(() -> {
                    earlier[0] = takeOutput(assembler);
                    helper.assertTrue(state(helper).resetChallenge(team, helper.getLevel().getGameTime()), handicap.id() + " didn't reset");
                    fill.accept(assembler);
                })
                .thenWaitUntil(() -> helper.assertTrue(sealed(assembler), "the Assembler sealed nothing after the reset"))
                .thenExecute(() -> {
                    ItemStack current = takeOutput(assembler);
                    helper.assertValueEqual(offer(helper, team, earlier[0]), 1, "the essence sealed before the reset a hand keeps");
                    helper.assertValueEqual(offer(helper, team, current), 0, "the essence sealed after the reset a hand keeps");
                    helper.assertValueEqual(state(helper).delivered(team), 1L, "the essence counted");
                })
                .thenSucceed();
    }

    /**
     * A Galvanic Essence crafted on a member's Personal Assembler while the team plays energy, by the game tests' own
     * hand recipe ({@code suite_pack/.../gametest_galvanic_by_hand.json}, the pack having none), is stamped as the player's
     * tick makes it, so after a reset the Port refuses it.
     */
    private static void craftedByHand(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.assertTrue(state(helper).begin(team, Handicap.ENERGY, 0), "energy didn't start");
        // A fake player, which logs in nowhere and drops whatever is sent to it, as Craftworks' and Voidworks' syncs send.
        ServerPlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "suite_hand"));
        player.setGameMode(GameType.SURVIVAL);
        TestTeams.join(player, team);
        player.getInventory().add(new ItemStack(Items.REDSTONE));
        helper.assertTrue(PersonalAssembler.craft(player,
                Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assembling/gametest_galvanic_by_hand"), 1),
                "the Personal Assembler queued nothing");
        var galvanic = Essences.item(Essence.GALVANIC);
        for (int tick = 0; tick < SuiteAssembler.MOST_TICKS && player.getInventory().countItem(galvanic) == 0; tick++) {
            // A player's tick as its events frame it, which Craftworks' Personal Assembler crafts in; a fake player has
            // no tick of its own.
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(player));
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        }
        ItemStack crafted = ItemStack.EMPTY;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(galvanic)) {
                crafted = player.getInventory().getItem(slot).copy();
            }
        }
        helper.assertFalse(crafted.isEmpty(), "the Personal Assembler crafted no Galvanic Essence");
        helper.assertTrue(state(helper).resetChallenge(team, 0), "energy didn't reset");
        helper.assertValueEqual(offer(helper, team, crafted), 1, "the hand-crafted essence a hand keeps after the reset");
        helper.succeed();
    }

    /** A stale essence, made before a reset, matches the Shop's sell Offer for its kind. */
    private static void shopBuysIt(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.assertTrue(state(helper).begin(team, Handicap.ENERGY, 0), "energy didn't start");
        ItemStack stale = made(helper, Essence.GALVANIC);
        helper.assertTrue(state(helper).resetChallenge(team, 0), "energy didn't reset");
        helper.assertValueEqual(offer(helper, team, stale), 1, "the stale essence a Port refused");
        expectSells(helper, stale, "a stale Galvanic Essence");
        helper.succeed();
    }

    /** An unstamped essence and a won chain's, which is never stamped, match the Shop's sell Offer too. */
    private static void shopBuysCurrent(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        expectSells(helper, new ItemStack(Essences.item(Essence.GALVANIC)), "an unstamped Galvanic Essence");
        helper.assertTrue(state(helper).winUnplayed(team, Handicap.ENERGY), "energy wasn't noted won");
        helper.assertTrue(state(helper).begin(team, Handicap.LIFE, 0), "life didn't start");
        ItemStack wonChain = made(helper, Essence.GALVANIC);
        helper.assertTrue(state(helper).resetChallenge(team, 0), "life didn't reset");
        expectSells(helper, wonChain, "a won chain's Galvanic Essence");
        helper.succeed();
    }

    /** The pack's Quartessence recipe takes a stale Transmuted Essence as any other. */
    private static void quartessence(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.assertTrue(state(helper).begin(team, Handicap.ENERGY, 0), "energy didn't start");
        ItemStack stale = made(helper, Essence.GALVANIC);
        helper.assertTrue(state(helper).resetChallenge(team, 0), "energy didn't reset");
        helper.assertValueEqual(offer(helper, team, stale), 1, "the stale essence a Port refused");
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "quartessence");
        assembler.inventory().set(0, ItemResource.of(stale), 1);
        SuiteAssembler.put(assembler, 1, Essences.item(Essence.WARDING), 1);
        SuiteAssembler.put(assembler, 2, Essences.item(Essence.VITAL), 1);
        SuiteAssembler.put(assembler, 3, Essences.item(Essence.AUREATE), 1);
        SuiteAssembler.tickUntil(helper, assembler, () -> sealed(assembler));
        SuiteAssembler.expectOutput(helper, assembler, Essences.item(Essence.QUARTESSENCE), "the Quartessence's recipe");
        helper.succeed();
    }

    private static void expectSells(GameTestHelper helper, ItemStack stack, String what) {
        var offer = RecipeManager.isSellItemRecipe(helper.getLevel(), stack);
        helper.assertTrue(offer.isPresent(), "no sell Offer matches " + what);
        helper.assertTrue(offer.get().value().getPrice() > 0, "the sell Offer for " + what + " pays nothing");
    }

    /** One {@code essence}, as a chain machine on the island makes it in its tick. */
    private static ItemStack made(GameTestHelper helper, Essence essence) {
        Making.atBlock(helper.getLevel(), helper.absolutePos(MACHINE));
        try {
            return new ItemStack(Essences.item(essence));
        } finally {
            Making.done();
        }
    }

    /** A protection Port at {@link #PORT}, if none is there, right-clicked with {@code stack}; says how many are left in hand. */
    private static int offer(GameTestHelper helper, UUID team, ItemStack stack) {
        if (!(helper.getBlockState(PORT).getBlock() instanceof PortBlock)) {
            helper.setBlock(PORT, Port.BLOCKS_BY_HANDICAP.get(Port.handicapOf(Essences.of(stack.getItem()).orElseThrow()).orElseThrow()).get());
        }
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
        BlockPos pos = helper.absolutePos(PORT);
        helper.getBlockState(PORT).useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        return player.getMainHandItem().getCount();
    }

    /** The Warding Essence's inputs, the pack's Seal, in the Assembler's slots and box. */
    private static void fillSeal(AssemblerBlockEntity assembler) {
        SuiteAssembler.put(assembler, 0, RiftShards.CHARGED_RIFT.get(), 2);
        SuiteAssembler.put(assembler, 1, BuiltInRegistries.ITEM.getValue(Identifier.parse("wireworks:plastic")), 2);
        SuiteAssembler.put(assembler, 2, Items.STRING, 2);
        SuiteAssembler.put(assembler, 3, Items.GUNPOWDER, 1);
        SuiteAssembler.put(assembler, 4, Items.IRON_INGOT, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 1000));
    }

    /** The Aureate Essence's inputs, the mint's Seal, coins of all three metals, in the Assembler's slots. */
    private static void fillAureateSeal(AssemblerBlockEntity assembler) {
        SuiteAssembler.put(assembler, 0, Mint.COPPER.coin.get(), 8);
        SuiteAssembler.put(assembler, 1, Mint.IRON.coin.get(), 4);
        SuiteAssembler.put(assembler, 2, Mint.GOLD.coin.get(), 2);
    }

    private static boolean sealed(AssemblerBlockEntity assembler) {
        return assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0;
    }

    /** What the Assembler made, taken out of it as automation would. */
    private static ItemStack takeOutput(AssemblerBlockEntity assembler) {
        var resource = assembler.inventory().getResource(AssemblerSlots.OUTPUT);
        int count = assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT);
        assembler.inventory().set(AssemblerSlots.OUTPUT, ItemResource.EMPTY, 0);
        return resource.toStack(count);
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(helper.getLevel().getServer());
    }
}
