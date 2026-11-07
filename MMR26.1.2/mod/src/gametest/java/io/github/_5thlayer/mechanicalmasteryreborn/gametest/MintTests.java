// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.UUID;

import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Mint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The money challenge's mint for gold (ADR 0017, #320), the pack's own recipes in {@code suite_pack/} on a real
 * Assembler: melt a gold ingot with lava into molten gold, cast it in a Mold, cool the cast with water and knock out a
 * Gold Coin and Burnt Sand in one step, wash the Burnt Sand back into sand, and make the next Mold from sand and clay.
 * The cool and the shakeout are one recipe, since a cooled cast would be an item that only waits to be knocked out. The
 * recipes are locked under the money challenge's stage, as every chain's are (ADRs 0007, 0013), read from the pack's own
 * stage file. Only the {@code suiteGameTestServer} run registers them. Their Assembler is a {@link SuiteAssembler}.
 */
final class MintTests {

    private static final List<String> RECIPES = List.of("molten_gold", "gold_cast", "gold_coin", "sand_from_burnt_sand", "mold");

    private MintTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("suite_mint_melt_makes_molten_gold_from_a_gold_ingot_and_lava", 40, MintTests::melt);
        tests.test("suite_mint_melt_fills_a_bucket_with_molten_gold", 40, MintTests::meltFillsABucket);
        tests.test("suite_mint_cast_fills_a_mold_with_molten_gold", 40, MintTests::cast);
        tests.test("suite_mint_cast_drains_molten_gold_from_a_bucket", 40, MintTests::castDrainsABucket);
        tests.test("suite_mint_cast_takes_molten_gold_from_a_pipe", 40, MintTests::castTakesFromAPipe);
        tests.test("suite_mint_cool_spends_its_water_and_gives_back_nothing_else", 40, MintTests::cool);
        tests.test("suite_mint_shakeout_knocks_a_gold_coin_and_burnt_sand_out_of_a_cast", 40, MintTests::shakeout);
        tests.test("suite_mint_wash_makes_sand_from_burnt_sand", 40, MintTests::wash);
        tests.test("suite_mint_mold_makes_a_mold_from_sand_and_clay", 40, MintTests::mold);
        tests.test("suite_mint_a_gold_ingot_ends_as_a_gold_coin_and_burnt_sand", 40, MintTests::line);
        tests.test("suite_mint_recipes_are_the_money_chains", 1, MintTests::moneysRecipes);
        tests.test("suite_mint_locked_until_the_money_challenge_starts", 1, MintTests::lockedUntilStarted);
        tests.test("suite_mint_stays_unlocked_after_a_reset", 1, MintTests::staysUnlockedAfterAReset);
        tests.test("suite_mint_locked_again_after_a_stop", 1, MintTests::lockedAgainAfterAStop);
    }

    private static void melt(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "molten_gold");
        SuiteAssembler.put(assembler, 0, Items.GOLD_INGOT, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.LAVA, 100));
        SuiteAssembler.tickUntil(helper, assembler, () -> !assembler.fluids().contents(2).isEmpty());
        FluidStack made = assembler.fluids().contents(2);
        helper.assertTrue(made.getFluid() == Mint.MOLTEN_GOLD.get() && made.getAmount() == 100,
                "Melt made " + made.getAmount() + " mB of " + made.getFluid() + ", not 100 mB of molten gold");
        helper.assertTrue(assembler.fluids().contents(0).isEmpty() && assembler.inventory().getAmountAsInt(0) == 0,
                "Melt left some of its gold or lava");
        helper.succeed();
    }

    /** A melt that tops the output box up to a bucket's worth: the fill slot takes it, and the outputs hold a molten gold bucket. */
    private static void meltFillsABucket(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "molten_gold");
        SuiteAssembler.put(assembler, 0, Items.GOLD_INGOT, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.LAVA, 100));
        assembler.fluids().set(2, new FluidStack(Mint.MOLTEN_GOLD.get(), 900));
        SuiteAssembler.put(assembler, AssemblerSlots.FILL, Items.BUCKET, 1);
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, Mint.MOLTEN_GOLD_BUCKET.get()) > 0);
        helper.assertValueEqual(outputs(assembler, Mint.MOLTEN_GOLD_BUCKET.get()), 1, "the molten gold buckets in the outputs");
        helper.succeed();
    }

    private static void cast(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "gold_cast");
        SuiteAssembler.put(assembler, 0, Mint.MOLD.get(), 1);
        assembler.fluids().set(0, new FluidStack(Mint.MOLTEN_GOLD.get(), 100));
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, Mint.GOLD_CAST.get(), "Cast");
        helper.assertTrue(assembler.fluids().contents(0).isEmpty(), "Cast left some of its molten gold");
        helper.succeed();
    }

    /** A molten gold bucket in the drain slot pours into Cast's box, as a Ward Tallow bucket does into Charge's. */
    private static void castDrainsABucket(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "gold_cast");
        SuiteAssembler.put(assembler, 0, Mint.MOLD.get(), 1);
        SuiteAssembler.put(assembler, AssemblerSlots.DRAIN, Mint.MOLTEN_GOLD_BUCKET.get(), 1);
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, Mint.GOLD_CAST.get()) > 0);
        helper.assertValueEqual(outputs(assembler, Mint.GOLD_CAST.get()), 1, "the gold casts in the outputs");
        helper.assertValueEqual(assembler.fluids().contents(0).getAmount(), 900, "the molten gold left of the bucket after Cast");
        helper.succeed();
    }

    /** Molten gold piped into one of the Assembler's fluid connections, as a pipe or a tank beside it would, fills Cast's box. */
    private static void castTakesFromAPipe(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "gold_cast");
        SuiteAssembler.put(assembler, 0, Mint.MOLD.get(), 1);
        helper.assertTrue(pipeIn(helper, FluidResource.of(Mint.MOLTEN_GOLD.get()), 100), "no fluid connection of the Assembler took molten gold");
        SuiteAssembler.tickUntil(helper, assembler, () -> assembler.inventory().getAmountAsInt(AssemblerSlots.OUTPUT) > 0);
        SuiteAssembler.expectOutput(helper, assembler, Mint.GOLD_CAST.get(), "Cast");
        helper.succeed();
    }

    /** Cooling takes the water and leaves no fluid behind, so nothing is left to void. */
    private static void cool(GameTestHelper helper) {
        AssemblerBlockEntity assembler = coined(helper);
        helper.assertTrue(assembler.fluids().contents(0).isEmpty(), "the cool left some of its water");
        for (int box = 0; box < 3; box++) {
            helper.assertTrue(assembler.fluids().contents(box).isEmpty(), "the cool left a fluid in box " + box + ": "
                    + assembler.fluids().contents(box).getFluid());
        }
        helper.succeed();
    }

    private static void shakeout(GameTestHelper helper) {
        AssemblerBlockEntity assembler = coined(helper);
        helper.assertValueEqual(outputs(assembler, Mint.GOLD_COIN.get()), 1, "the Gold Coins the shakeout knocked out");
        helper.assertValueEqual(outputs(assembler, Mint.BURNT_SAND.get()), 1, "the Burnt Sand the shakeout left");
        helper.assertValueEqual(assembler.inventory().getAmountAsInt(0), 0, "the casts left after the shakeout");
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
    private static void line(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "mold");
        SuiteAssembler.put(assembler, 0, Items.SAND, 1);
        SuiteAssembler.put(assembler, 1, Items.CLAY_BALL, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 100));
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, Mint.MOLD.get()) > 0);
        take(assembler, Mint.MOLD.get());

        hold(helper, assembler, "molten_gold");
        SuiteAssembler.put(assembler, 0, Items.GOLD_INGOT, 1);
        assembler.fluids().set(0, new FluidStack(Fluids.LAVA, 100));
        SuiteAssembler.tickUntil(helper, assembler, () -> !assembler.fluids().contents(2).isEmpty());
        FluidStack molten = assembler.fluids().contents(2);
        assembler.fluids().set(2, FluidStack.EMPTY);

        hold(helper, assembler, "gold_cast");
        SuiteAssembler.put(assembler, 0, Mint.MOLD.get(), 1);
        assembler.fluids().set(0, molten);
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, Mint.GOLD_CAST.get()) > 0);
        take(assembler, Mint.GOLD_CAST.get());

        hold(helper, assembler, "gold_coin");
        SuiteAssembler.put(assembler, 0, Mint.GOLD_CAST.get(), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 250));
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, Mint.GOLD_COIN.get()) > 0);
        helper.assertValueEqual(outputs(assembler, Mint.GOLD_COIN.get()), 1, "the Gold Coins a gold ingot ended as");
        helper.assertValueEqual(outputs(assembler, Mint.BURNT_SAND.get()), 1, "the Burnt Sand a gold ingot's cast left");
        take(assembler, Mint.GOLD_COIN.get());
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
        for (String path : RECIPES) {
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

    /** An Assembler that has cooled and knocked out one gold cast. */
    private static AssemblerBlockEntity coined(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "gold_coin");
        SuiteAssembler.put(assembler, 0, Mint.GOLD_CAST.get(), 1);
        assembler.fluids().set(0, new FluidStack(Fluids.WATER, 250));
        SuiteAssembler.tickUntil(helper, assembler, () -> outputs(assembler, Mint.GOLD_COIN.get()) > 0);
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
