// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.QuintessenceRung;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.voidworks.VoidworksBlocks;
import io.github._5thlayer.voidworks.VoidworksCapabilities;
import io.github._5thlayer.voidworks.VoidworksConfig;
import io.github._5thlayer.voidworks.dormant.Dormancy;
import io.github._5thlayer.voidworks.energy.MoteResource;
import io.github._5thlayer.voidworks.energy.VoidEnergy;
import io.github._5thlayer.voidworks.energy.VoidSpender;
import io.github._5thlayer.voidworks.mutation.Families;
import io.github._5thlayer.voidworks.mutation.MutationChamberBlockEntity;
import io.github._5thlayer.voidworks.mutation.Upgrade;
import io.github._5thlayer.voidworks.mutation.VoidCrucibleBlockEntity;
import io.github._5thlayer.voidworks.pressure.VoidPressure;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.ResourceStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Quintessence's Voidworks way (#262) on Voidworks' own machines, in the {@code suiteGameTestServer} run alone, which
 * loads Craftworks and Voidworks: the pack's Assembler recipe for the Quartessence, and the pack's Family whose Rung a
 * Mutation Chamber climbs from it into Quintessence only once its island's team has started its last stand, saying so
 * to Jade until then, which a Void Crucible never takes, and which locks no other Family. Its lock on the Assembler is the pack's Chapters stage
 * file, which no game test run loads. A Chamber's motes come from a crying obsidian beside it, which this class gives
 * the {@code voidworks:motes} capability, as a void machine's store.
 */
final class SuiteQuintessenceTests {

    private static final BlockPos CHAMBER = new BlockPos(4, 1, 4);
    /** Where the mote store touches the Chamber. */
    private static final BlockPos BESIDE = CHAMBER.east();
    private static final Block STORE = Blocks.CRYING_OBSIDIAN;
    /** The store each placed crying obsidian holds, by its position. */
    private static final Map<BlockPos, MoteStore> STORES = new ConcurrentHashMap<>();

    /** Long enough to wake a Chamber and try many upgrades at Voidworks' interval. */
    private static final int TICKS = 1200;
    /** Upgrades enough that all failing their roll at the Rung's chance never happens. */
    private static final int TRIES = 40;

    private SuiteQuintessenceTests() {
    }

    static void registerCapabilities(IEventBus modBus) {
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlock(VoidworksCapabilities.MOTES,
                (level, pos, state, entity, side) -> STORES.get(pos.immutable()), STORE));
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("suite_quintessence_quartessence_assembles_from_one_of_each_transmuted_essence", 40, SuiteQuintessenceTests::assembles);
        tests.test("suite_quintessence_rung_climbs_a_quartessence_into_quintessence", 1, SuiteQuintessenceTests::rung);
        tests.test("suite_quintessence_chamber_holds_a_quartessence_until_the_last_stand_starts", TICKS, SuiteQuintessenceTests::holdsUntilItStarts);
        tests.test("suite_quintessence_chamber_climbs_a_quartessence_once_the_last_stand_started", TICKS, SuiteQuintessenceTests::climbsOnceStarted);
        tests.test("suite_quintessence_chamber_climbs_other_families_before_the_last_stand", TICKS, SuiteQuintessenceTests::otherFamilies);
        tests.test("suite_quintessence_crucible_never_takes_a_quartessence", 1, SuiteQuintessenceTests::crucibleRefuses);
    }

    private static void assembles(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "quartessence");
        SuiteAssembler.put(assembler, 0, Essences.item(Essence.GALVANIC), 1);
        SuiteAssembler.put(assembler, 1, Essences.item(Essence.WARDING), 1);
        SuiteAssembler.put(assembler, 2, Essences.item(Essence.VITAL), 1);
        SuiteAssembler.put(assembler, 3, Essences.item(Essence.AUREATE), 1);
        SuiteAssembler.tickUntil(helper, assembler, () -> !assembler.inventory().getResource(AssemblerSlots.OUTPUT).isEmpty());
        SuiteAssembler.expectOutput(helper, assembler, Essences.item(Essence.QUARTESSENCE), "the Quartessence's recipe");
        helper.succeed();
    }

    /** The pack's Family: a Quartessence's next Rung is Quintessence, steeper than any of Voidworks' own. */
    private static void rung(GameTestHelper helper) {
        Optional<Upgrade<Item>> next = Families.next(new ItemStack(Essences.item(Essence.QUARTESSENCE)));
        helper.assertTrue(next.isPresent(), "a Quartessence climbs no Family");
        helper.assertTrue(next.get().result() == Essences.item(Essence.QUINTESSENCE), "a Quartessence climbs into " + next.get().result());
        helper.assertTrue(next.get().count() > 1 && next.get().cost() > 6400, "the Rung isn't steep: " + next.get());
        helper.succeed();
    }

    /** All four won isn't the last stand: awake, with motes for every try, the Chamber spends nothing on its Quartessence. */
    private static void holdsUntilItStarts(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        win(helper, team);
        var upgrade = quintessenceRung();
        var chamber = chamber(helper, Essences.item(Essence.QUARTESSENCE), upgrade.count());
        int grade = grade(helper);
        int awake = dormantMotes(helper);
        var store = store(helper, grade, awake + TRIES * motes(upgrade));
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(chamber.dormancy().awake(), "the Chamber has not woken"))
                .thenIdle(5 * VoidworksConfig.mutationInterval())
                .thenExecute(() -> {
                    helper.assertTrue(chamber.output().isEmpty(), "the Chamber made " + chamber.output() + " before the last stand");
                    helper.assertValueEqual(chamber.inventory().getAmountAsInt(MutationChamberBlockEntity.INPUT), upgrade.count(),
                            "the Quartessence the Chamber holds");
                    helper.assertValueEqual(VoidSpender.countByGrade(store), Map.of(grade, TRIES * motes(upgrade)),
                            "the motes left once the Chamber woke");
                    helper.assertTrue(waits(chamber), "the Chamber doesn't say it waits for the last stand");
                })
                .thenSucceed();
    }

    /** Started, the Chamber climbs its Quartessence into one Quintessence, paying the Rung's cost for every try. */
    private static void climbsOnceStarted(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        win(helper, team);
        helper.assertTrue(LastStands.begin(helper.getLevel().getServer(), team), "the last stand didn't start");
        var upgrade = quintessenceRung();
        var chamber = chamber(helper, Essences.item(Essence.QUARTESSENCE), upgrade.count());
        int grade = grade(helper);
        int awake = dormantMotes(helper);
        var store = store(helper, grade, awake + TRIES * motes(upgrade));
        helper.assertFalse(waits(chamber), "the Chamber says it waits once the last stand started");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(!chamber.output().isEmpty(), "the Chamber made nothing"))
                .thenExecute(() -> {
                    helper.assertTrue(chamber.output().is(Essences.item(Essence.QUINTESSENCE)) && chamber.output().getCount() == 1,
                            "the Chamber made " + chamber.output() + ", not one Quintessence");
                    helper.assertValueEqual(chamber.inventory().getAmountAsInt(MutationChamberBlockEntity.INPUT), 0,
                            "the Quartessence left in the Chamber");
                    int left = VoidSpender.countByGrade(store).getOrDefault(grade, 0);
                    int spent = TRIES * motes(upgrade) - left;
                    helper.assertTrue(spent > 0 && spent % motes(upgrade) == 0,
                            "the Chamber spent " + spent + " motes, not a whole number of the Rung's " + motes(upgrade));
                })
                .thenSucceed();
    }

    /** Before the last stand a Chamber climbs every other Family: copper into iron. */
    private static void otherFamilies(GameTestHelper helper) {
        TestTeams.claim(helper);
        var chamber = chamber(helper, Items.COPPER_INGOT, 1);
        store(helper, grade(helper), dormantMotes(helper) + TRIES);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(chamber.output().is(Items.IRON_INGOT), "the Chamber climbed no copper"))
                .thenSucceed();
    }

    /** A Crucible's input refuses a Quartessence, from a hand or any automation, and takes another Family's item. */
    private static void crucibleRefuses(GameTestHelper helper) {
        helper.setBlock(CHAMBER, VoidworksBlocks.VOID_CRUCIBLE.get());
        var crucible = helper.getBlockEntity(CHAMBER, VoidCrucibleBlockEntity.class);
        helper.assertFalse(crucible.items().accepts(ItemResource.of(Essences.item(Essence.QUARTESSENCE))), "the Crucible's rule takes a Quartessence");
        var hand = new ItemStack(Essences.item(Essence.QUARTESSENCE), 4);
        helper.assertValueEqual(crucible.items().insertFromHand(hand), 0, "the Quartessence a hand puts in the Crucible");
        helper.assertTrue(crucible.items().accepts(ItemResource.of(Items.COPPER_INGOT)), "the Crucible's rule refuses copper");
        helper.succeed();
    }

    /** What Jade asks a Chamber: whether it holds a Quartessence it won't climb until the last stand. */
    private static boolean waits(MutationChamberBlockEntity chamber) {
        return ((QuintessenceRung.Chamber) (Object) chamber).mechanicalmasteryreborn$waitsForTheLastStand();
    }

    private static Upgrade<Item> quintessenceRung() {
        return Families.next(new ItemStack(Essences.item(Essence.QUARTESSENCE))).orElseThrow();
    }

    private static MutationChamberBlockEntity chamber(GameTestHelper helper, Item holding, int count) {
        helper.setBlock(CHAMBER, VoidworksBlocks.MUTATION_CHAMBER.get());
        var chamber = helper.getBlockEntity(CHAMBER, MutationChamberBlockEntity.class);
        chamber.inventory().set(MutationChamberBlockEntity.INPUT, ItemResource.of(holding), count);
        return chamber;
    }

    /** Places a store of {@code count} motes of {@code grade} beside the Chamber. */
    private static MoteStore store(GameTestHelper helper, int grade, int count) {
        var store = new MoteStore().add(grade, count);
        var absolute = helper.absolutePos(BESIDE);
        STORES.put(absolute, store);
        helper.getLevel().setBlockAndUpdate(absolute, STORE.defaultBlockState());
        return store;
    }

    /** The grade one step above the pressure at the Chamber, each mote of which is worth {@link VoidEnergy#BASE}. */
    private static int grade(GameTestHelper helper) {
        return VoidPressure.at(helper.getLevel(), helper.absolutePos(CHAMBER)) + 1;
    }

    /** The Chamber's Dormant cost, in motes of {@link #grade}. */
    private static int dormantMotes(GameTestHelper helper) {
        return (int) (Dormancy.cost(VoidworksBlocks.MUTATION_CHAMBER.get().defaultBlockState()) / VoidEnergy.BASE);
    }

    /** One try's cost, in motes of {@link #grade}. */
    private static int motes(Upgrade<Item> upgrade) {
        return (int) (upgrade.cost() / VoidEnergy.BASE);
    }

    private static void win(GameTestHelper helper, UUID team) {
        for (Handicap handicap : Handicap.values()) {
            helper.assertTrue(TeamState.get(helper.getLevel().getServer()).blockTier(team, handicap),
                    "the " + handicap.id() + " challenge wasn't noted won");
        }
    }

    /** A plain mote store, as Voidworks' own tests have: nine slots of up to a thousand motes of one grade each. */
    private static final class MoteStore extends ResourceStacksResourceHandler<MoteResource> {

        private static final Codec<ResourceStack<MoteResource>> STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MoteResource.CODEC.fieldOf("resource").forGetter(ResourceStack::resource),
                Codec.INT.fieldOf("amount").forGetter(ResourceStack::amount)
        ).apply(instance, ResourceStack::new));

        MoteStore() {
            super(9, MoteResource.EMPTY, STACK_CODEC);
        }

        @Override
        protected int getCapacity(int index, MoteResource resource) {
            return 1000;
        }

        MoteStore add(int grade, int count) {
            try (var transaction = Transaction.openRoot()) {
                int inserted = ResourceHandlerUtil.insertStacking(this, MoteResource.of(grade), count, transaction);
                if (inserted != count) {
                    throw new IllegalStateException("the store took " + inserted + " of " + count + " grade " + grade + " motes");
                }
                transaction.commit();
            }
            return this;
        }
    }
}
