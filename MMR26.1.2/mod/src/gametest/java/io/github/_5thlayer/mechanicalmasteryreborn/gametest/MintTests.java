// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ammonium.adminshop.recipes.RecipeManager;
import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Mint;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The money challenge's mint (ADR 0017, #320), the pack's own recipes in {@code suite_pack/} on a real Assembler: melt
 * an ingot of copper, iron or gold with lava into molten metal, cast it in a Mold, cool the cast with water and knock out
 * that metal's Coin and Burnt Sand in one step, wash the Burnt Sand back into sand, and make the next Mold from sand and
 * clay; exchange coins up, two copper for an iron and three iron for a gold, and seal coins of all three metals into an
 * Aureate Essence, which the Shop buys (#323). The molten metal carries the lava's rock as dross, which the shakeout
 * knocks out with the Burnt Sand, so a melt gives as much as its ingot and lava take. The cool and the shakeout are one
 * recipe, since a cooled cast would be an item that only waits to be knocked out. Each metal's steps run the same tests,
 * one each. The recipes are locked under the money challenge's stage, as every chain's are (ADRs 0007, 0013), read from
 * the pack's own stage file. Only the {@code suiteGameTestServer} run registers them. Their Assembler is a
 * {@link SuiteAssembler}.
 */
final class MintTests {

    private static final Map<Mint.Metal, Item> INGOTS = Map.of(Mint.COPPER, Items.COPPER_INGOT, Mint.IRON, Items.IRON_INGOT,
            Mint.GOLD, Items.GOLD_INGOT);

    private MintTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        for (Mint.Metal metal : Mint.METALS) {
            String name = metal.name();
            tests.test("suite_mint_" + name + "_melt_makes_molten_" + name + "_from_an_ingot_and_lava", 40, helper -> melt(helper, metal));
            tests.test("suite_mint_" + name + "_melt_fills_a_bucket", 40, helper -> meltFillsABucket(helper, metal));
            tests.test("suite_mint_" + name + "_cast_fills_a_mold", 40, helper -> cast(helper, metal));
            tests.test("suite_mint_" + name + "_cast_drains_a_bucket", 40, helper -> castDrainsABucket(helper, metal));
            tests.test("suite_mint_" + name + "_cast_takes_molten_" + name + "_from_a_pipe", 40, helper -> castTakesFromAPipe(helper, metal));
            tests.test("suite_mint_" + name + "_cool_spends_its_water_and_gives_back_nothing_else", 40, helper -> cool(helper, metal));
            tests.test("suite_mint_" + name + "_shakeout_knocks_a_coin_and_burnt_sand_out_of_a_cast", 40, helper -> shakeout(helper, metal));
            tests.test("suite_mint_" + name + "_an_ingot_ends_as_a_coin_and_burnt_sand", 40, helper -> line(helper, metal));
        }
        tests.test("suite_mint_exchange_trades_two_copper_coins_for_an_iron_one", 40,
                helper -> exchange(helper, "iron_coin_from_copper_coins", Mint.COPPER, 2, Mint.IRON));
        tests.test("suite_mint_exchange_trades_three_iron_coins_for_a_gold_one", 40,
                helper -> exchange(helper, "gold_coin_from_iron_coins", Mint.IRON, 3, Mint.GOLD));
        tests.test("suite_mint_seal_makes_an_aureate_from_coins_of_all_three_metals", 40, MintTests::seal);
        tests.test("suite_mint_shop_buys_the_aureate", 1, MintTests::shopBuysTheAureate);
        tests.test("suite_mint_wash_makes_sand_from_burnt_sand", 40, MintTests::wash);
        tests.test("suite_mint_mold_makes_a_mold_from_sand_and_clay", 40, MintTests::mold);
        tests.test("suite_mint_recipes_are_the_money_chains", 1, MintTests::moneysRecipes);
        tests.test("suite_mint_locked_until_the_money_challenge_starts", 1, MintTests::lockedUntilStarted);
        tests.test("suite_mint_stays_unlocked_after_a_reset", 1, MintTests::staysUnlockedAfterAReset);
        tests.test("suite_mint_locked_again_after_a_stop", 1, MintTests::lockedAgainAfterAStop);
    }

    private static void melt(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "molten_" + metal.name());
        SuiteAssembler.put(assembler, 0, INGOTS.get(metal), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.LAVA, 100));
        SuiteAssembler.tickUntil(helper, assembler, () -> !assembler.fluids().contents(2).isEmpty());
        FluidStack made = assembler.fluids().contents(2);
        helper.assertTrue(made.getFluid() == metal.molten.get() && made.getAmount() == 200,
                "Melt made " + made.getAmount() + " mB of " + made.getFluid() + ", not 200 mB of molten " + metal.name());
        helper.assertTrue(assembler.fluids().contents(0).isEmpty() && assembler.inventory().getAmountAsInt(0) == 0,
                "Melt left some of its ingot or lava");
        helper.succeed();
    }

    /** A melt that tops the output box up to a bucket's worth: the fill slot takes it, and the outputs hold the metal's bucket. */
    private static void meltFillsABucket(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "molten_" + metal.name());
        SuiteAssembler.put(assembler, 0, INGOTS.get(metal), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.LAVA, 100));
        assembler.fluids().set(2, new FluidStack(metal.molten.get(), 800));
        SuiteAssembler.put(assembler, AssemblerSlots.FILL, Items.BUCKET, 1);
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, metal.bucket.get()) > 0);
        helper.assertValueEqual(outputs(assembler, metal.bucket.get()), 1, "the molten " + metal.name() + " buckets in the outputs");
        helper.succeed();
    }

    private static void cast(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, metal.name() + "_cast");
        SuiteAssembler.put(assembler, 0, Mint.MOLD.get(), 1);
        assembler.fluids().set(0, new FluidStack(metal.molten.get(), 200));
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, metal.cast.get(), "Cast");
        helper.assertTrue(assembler.fluids().contents(0).isEmpty(), "Cast left some of its molten " + metal.name());
        helper.succeed();
    }

    /** The metal's bucket in the drain slot pours into Cast's box, as a Ward Tallow bucket does into Charge's. */
    private static void castDrainsABucket(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, metal.name() + "_cast");
        SuiteAssembler.put(assembler, 0, Mint.MOLD.get(), 1);
        SuiteAssembler.put(assembler, AssemblerSlots.DRAIN, metal.bucket.get(), 1);
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, metal.cast.get()) > 0);
        helper.assertValueEqual(outputs(assembler, metal.cast.get()), 1, "the " + metal.name() + " casts in the outputs");
        helper.assertValueEqual(assembler.fluids().contents(0).getAmount(), 800, "the molten " + metal.name() + " left of the bucket after Cast");
        helper.succeed();
    }

    /** Molten metal piped into one of the Assembler's fluid connections, as a pipe or a tank beside it would, fills Cast's box. */
    private static void castTakesFromAPipe(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, metal.name() + "_cast");
        SuiteAssembler.put(assembler, 0, Mint.MOLD.get(), 1);
        helper.assertTrue(pipeIn(helper, FluidResource.of(metal.molten.get()), 200),
                "no fluid connection of the Assembler took molten " + metal.name());
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, metal.cast.get(), "Cast");
        helper.succeed();
    }

    /** Cooling takes the water and leaves no fluid behind, so nothing is left to void. */
    private static void cool(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = coined(helper, metal);
        for (int box = 0; box < 3; box++) {
            helper.assertTrue(assembler.fluids().contents(box).isEmpty(), "the cool left a fluid in box " + box + ": "
                    + assembler.fluids().contents(box).getFluid());
        }
        helper.succeed();
    }

    private static void shakeout(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = coined(helper, metal);
        helper.assertValueEqual(outputs(assembler, metal.coin.get()), 1, "the " + metal.name() + " coins the shakeout knocked out");
        helper.assertValueEqual(outputs(assembler, Mint.BURNT_SAND.get()), 1, "the Burnt Sand the shakeout left");
        helper.assertValueEqual(assembler.inventory().getAmountAsInt(0), 0, "the casts left after the shakeout");
        helper.succeed();
    }

    /** {@code count} of {@code from}'s coins exchanged up for one of {@code to}'s, by the pack's recipe {@code assembling/<path>}. */
    private static void exchange(GameTestHelper helper, String path, Mint.Metal from, int count, Mint.Metal to) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, path);
        SuiteAssembler.put(assembler, 0, from.coin.get(), count);
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, to.coin.get(), "the exchange of " + from.name() + " coins");
        helper.succeed();
    }

    /** Coins of all three metals sealed into an Aureate Essence (ADR 0017). */
    private static void seal(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "aureate_essence");
        SuiteAssembler.put(assembler, 0, Mint.COPPER.coin.get(), 8);
        SuiteAssembler.put(assembler, 1, Mint.IRON.coin.get(), 4);
        SuiteAssembler.put(assembler, 2, Mint.GOLD.coin.get(), 2);
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, Essences.item(Essence.AUREATE), "the Seal");
        helper.succeed();
    }

    /** The pack's sell Offer for the Aureate, under the money Permit as the other three essences' are. */
    private static void shopBuysTheAureate(GameTestHelper helper) {
        var offer = RecipeManager.isSellItemRecipe(helper.getLevel(), new ItemStack(Essences.item(Essence.AUREATE)));
        helper.assertTrue(offer.isPresent(), "no sell Offer matches the Aureate Essence");
        helper.assertTrue(offer.get().value().getPrice() > 0, "the Aureate's sell Offer pays nothing");
        helper.succeed();
    }

    private static void wash(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "sand_from_burnt_sand");
        SuiteAssembler.put(assembler, 0, Mint.BURNT_SAND.get(), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 250));
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, Items.SAND, "Wash");
        helper.assertTrue(assembler.fluids().contents(0).isEmpty(), "Wash left some of its water");
        helper.succeed();
    }

    private static void mold(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "mold");
        SuiteAssembler.put(assembler, 0, Items.SAND, 1);
        SuiteAssembler.put(assembler, 1, Items.CLAY_BALL, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 100));
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, Mint.MOLD.get(), "Mold");
        helper.succeed();
    }

    /** The whole mint on one Assembler, each step's product carried to the next as a belt or a pipe would. */
    private static void line(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "mold");
        SuiteAssembler.put(assembler, 0, Items.SAND, 1);
        SuiteAssembler.put(assembler, 1, Items.CLAY_BALL, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 100));
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, Mint.MOLD.get()) > 0);
        take(assembler, Mint.MOLD.get());

        hold(helper, assembler, "molten_" + metal.name());
        SuiteAssembler.put(assembler, 0, INGOTS.get(metal), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.LAVA, 100));
        SuiteAssembler.tickUntil(helper, assembler, () -> !assembler.fluids().contents(2).isEmpty());
        FluidStack molten = assembler.fluids().contents(2);
        assembler.fluids().set(2, FluidStack.EMPTY);

        hold(helper, assembler, metal.name() + "_cast");
        SuiteAssembler.put(assembler, 0, Mint.MOLD.get(), 1);
        assembler.fluids().set(0, molten);
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, metal.cast.get()) > 0);
        take(assembler, metal.cast.get());

        hold(helper, assembler, metal.name() + "_coin");
        SuiteAssembler.put(assembler, 0, metal.cast.get(), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 250));
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, metal.coin.get()) > 0);
        helper.assertValueEqual(outputs(assembler, metal.coin.get()), 1, "the " + metal.name() + " coins an ingot ended as");
        helper.assertValueEqual(outputs(assembler, Mint.BURNT_SAND.get()), 1, "the Burnt Sand an ingot's cast left");
        take(assembler, metal.coin.get());
        take(assembler, Mint.BURNT_SAND.get());

        hold(helper, assembler, "sand_from_burnt_sand");
        SuiteAssembler.put(assembler, 0, Mint.BURNT_SAND.get(), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 250));
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, Items.SAND) > 0);
        helper.assertValueEqual(outputs(assembler, Items.SAND), 1, "the sand washed back for the next Mold");
        helper.succeed();
    }

    /** What EMI marks as the money chain's: every mint recipe, read from the pack's own stage file. */
    private static void moneysRecipes(GameTestHelper helper) {
        var recipes = ChainMachines.recipes(helper.getLevel().getServer());
        var paths = new ArrayList<>(List.of("sand_from_burnt_sand", "mold", "iron_coin_from_copper_coins", "gold_coin_from_iron_coins",
                "aureate_essence"));
        for (Mint.Metal metal : Mint.METALS) {
            paths.addAll(List.of("molten_" + metal.name(), metal.name() + "_cast", metal.name() + "_coin"));
        }
        for (String path : paths) {
            helper.assertValueEqual(recipes.get(recipe(path)), Handicap.MONEY, "the chain of the mint's " + path);
        }
        helper.succeed();
    }

    private static void lockedUntilStarted(GameTestHelper helper) {
        UUID team = account(helper);
        var server = helper.getLevel().getServer();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertTrue(ChainMachines.table(server, team).locked(Handicap.MONEY), "the mint was unmarked before the money challenge");
        helper.assertFalse(TestStages.holds(player, Handicap.MONEY.id()), "the mint was unlocked before the money challenge");
        helper.assertTrue(Challenges.begin(server, team, Handicap.MONEY), "the money challenge didn't start");
        Chains.unlock(server, player);
        helper.assertFalse(ChainMachines.table(server, team).locked(Handicap.MONEY), "the mint stayed marked once the money challenge started");
        helper.assertTrue(TestStages.holds(player, Handicap.MONEY.id()), "the mint stayed locked once the money challenge started");
        helper.succeed();
    }

    private static void staysUnlockedAfterAReset(GameTestHelper helper) {
        UUID team = account(helper);
        var server = helper.getLevel().getServer();
        Challenges.begin(server, team, Handicap.MONEY);
        Challenges.reset(server, team);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertFalse(ChainMachines.table(server, team).locked(Handicap.MONEY), "the mint was marked again after a reset");
        helper.assertTrue(TestStages.holds(player, Handicap.MONEY.id()), "the mint was locked again after a reset");
        helper.succeed();
    }

    /** An op's {@code /cod challenge end} is a stop with no vote, which locks the mint again (ADR 0013). */
    private static void lockedAgainAfterAStop(GameTestHelper helper) {
        UUID team = account(helper);
        var server = helper.getLevel().getServer();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        TestTeams.logIn(player);
        try {
            Challenges.begin(server, team, Handicap.MONEY);
            Chains.unlock(server, player);
            helper.assertTrue(Challenges.end(server, team), "the money challenge didn't end");
            helper.assertTrue(ChainMachines.table(server, team).locked(Handicap.MONEY), "the mint stayed unmarked past the stop");
            helper.assertFalse(TestStages.holds(player, Handicap.MONEY.id()), "the mint stayed unlocked past the stop");
        } finally {
            TestTeams.logOut(player);
        }
        helper.succeed();
    }

    // -- fixtures -------------------------------------------------------------------------------

    /** An Assembler that has cooled and knocked out one of {@code metal}'s casts. */
    private static AssemblerBlockEntity coined(GameTestHelper helper, Mint.Metal metal) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, metal.name() + "_coin");
        SuiteAssembler.put(assembler, 0, metal.cast.get(), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 250));
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, metal.coin.get()) > 0);
        return assembler;
    }

    /** Pipes {@code amount} of {@code fluid} into whichever of the Assembler's fluid connections takes all of it; whether one did. */
    private static boolean pipeIn(GameTestHelper helper, FluidResource fluid, int amount) {
        BlockPos origin = helper.absolutePos(SuiteAssembler.ORIGIN);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, 0, -2), origin.offset(2, 2, 2))) {
            for (Direction side : Direction.values()) {
                var connection = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, pos.immutable(), side);
                if (connection == null) {
                    continue;
                }
                try (Transaction piping = Transaction.openRoot()) {
                    if (connection.insert(fluid, amount, piping) == amount) {
                        piping.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** The money challenge needs an Account to start. */
    private static UUID account(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        TestAccounts.open(team, 0);
        return team;
    }

    private static void hold(GameTestHelper helper, AssemblerBlockEntity assembler, String path) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        assembler.setHeldRecipe(recipe(path), player);
    }

    /** Takes every {@code item} out of the outputs, as a belt would. */
    private static void take(AssemblerBlockEntity assembler, Item item) {
        for (int n = 0; n < AssemblerSlots.OUTPUTS; n++) {
            int slot = AssemblerSlots.output(n);
            if (assembler.inventory().getResource(slot).getItem() == item) {
                assembler.inventory().set(slot, ItemResource.EMPTY, 0);
            }
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

    private static Identifier recipe(String path) {
        return Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assembling/" + path);
    }
}
