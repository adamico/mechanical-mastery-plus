// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Accounts;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Explosions;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Gates;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The Module's game tests, run by the {@code gameTestServer} Gradle run: the Minecraft-integration
 * seam, with a real player on a real server. Each test stands on the {@code gametest/platform}
 * structure, a stone floor that {@code scripts/build-gametest-structures.py} writes, and sets up
 * what it needs itself, so the setup is in the diff. It lives in the {@code gametest} source set, which
 * only the Module's own runs load, and hooks itself in, so the {@code @Mod} class names no game-test
 * code and nothing gates it (libworks ADR 0011).
 */
@EventBusSubscriber(modid = MechanicalMasteryReborn.MOD_ID)
public final class MechanicalMasteryRebornGameTests {

    private static final Identifier PLATFORM = id("gametest/platform");

    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TEST_TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MechanicalMasteryReborn.MOD_ID);

    static {
        TEST_TYPES.register("code", () -> CodeGameTest.CODEC);
    }

    private MechanicalMasteryRebornGameTests() {
    }

    @SubscribeEvent
    static void construct(FMLConstructModEvent event) {
        register(event.getContainer().getEventBus());
    }

    public static void register(IEventBus modBus) {
        TEST_TYPES.register(modBus);
        // Posted only when game tests are enabled, which a production game never is.
        modBus.addListener(MechanicalMasteryRebornGameTests::registerTests);
        GeneratorTests.registerCapabilities(modBus);
        HermeticLabTests.registerCapabilities(modBus);
        if (ModList.get().isLoaded("voidworks")) {
            SuiteQuintessenceTests.registerCapabilities(modBus);
        }
        TestYield.useFull();
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        // Registered rather than borrowed, since the event hands out no lookup for vanilla's.
        var environment = event.registerEnvironment(id("default"), new TestEnvironmentDefinition.AllOf(List.of()));
        var tests = new Registrar(event, environment);
        LoadTests.register(tests);
        GameTestPackTests.register(tests);
        CreativeTabTests.register(tests);
        EssenceTests.register(tests);
        CodTests.register(tests);
        CodWakeTests.register(tests);
        CodRitualTests.register(tests);
        CodTierTests.register(tests);
        LabTests.register(tests);
        LabSaplingTests.register(tests);
        PortTests.register(tests);
        StaleEssenceTests.register(tests);
        OverproductionTests.register(tests);
        OverproductionCapTests.register(tests);
        PaceTests.register(tests);
        GeneratorTests.register(tests);
        HandDecomposeTests.register(tests);
        RecyclerTests.register(tests);
        CoreTierTests.register(tests);
        FloorTests.register(tests);
        FeDriveTests.register(tests);
        DefenceTests.register(tests);
        RecyclerInputTests.register(tests);
        RecyclingTests.register(tests);
        YieldTests.register(tests);
        RecipeTests.register(tests);
        MoneyTests.register(tests);
        LifeTests.register(tests);
        ChainTests.register(tests);
        EnergyChainTests.register(tests);
        HomeGateTests.register(tests);
        LastChapterTests.register(tests);
        LastStandTests.register(tests);
        QuintessenceTests.register(tests);
        ChainMachineTests.register(tests);
        StopTests.register(tests);
        AssaultTests.register(tests);
        AssaultAbsenceTests.register(tests);
        AssaultExplosionsTests.register(tests);
        RefundTests.register(tests);
        HermeticLabTests.register(tests);
        GalleryTests.register(tests);
        // The protection Chain and the End Portal Frame on a real Assembler, in the suiteGameTestServer run alone, which
        // loads Craftworks (#252, #265), and the money Chain's mint (#320), and the Quartessence's way to Quintessence there, which loads Voidworks too (#262).
        if (ModList.get().isLoaded("craftworks")) {
            WardingChainTests.register(tests);
            LastChapterFrameTests.register(tests);
            MintTests.register(tests);
            SuiteGalvanicTests.register(tests);
        }
        if (ModList.get().isLoaded("voidworks")) {
            SuiteQuintessenceTests.register(tests);
        }
        // A stale Transmuted Essence sealed on a real Assembler, matched by the Shop's sell Offers and assembled into a
        // Quartessence, in the suiteGameTestServer run alone, which loads Craftworks and AdminShop (#322).
        if (ModList.get().isLoaded("craftworks") && ModList.get().isLoaded("adminshop")) {
            SuiteStaleEssenceTests.register(tests);
        }
        // Each test that records a decompose claims a team of its own.
        Teams.use(new TestTeams(Teams.installed()));
        // Each test that plays the money challenge opens an Account of its own.
        Accounts.use(new TestAccounts());
        // Chapters' part, each player's stages in memory.
        Chains.use(new TestStages());
        // The dev runs have no Gateways, so an Assault's gate is an armor stand.
        Gates.use(AssaultAbsenceTests.GATES);
        // Nor FTB Chunks, so a team's explosion setting is kept in memory.
        Explosions.use(AssaultExplosionsTests.EXPLOSIONS);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, path);
    }

    /** What a test class is handed: a name, a tick budget and a body per test. */
    record Registrar(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {

        void test(String name, int maxTicks, Consumer<GameTestHelper> body) {
            register(name, maxTicks, body, environment);
        }

        /** A test in a batch of its own: one that turns a datapack on, which reloads the server's data under every test running with it (libworks ADR 0010). */
        void testOnItsOwn(String name, int maxTicks, Consumer<GameTestHelper> body) {
            register(name, maxTicks, body, event.registerEnvironment(id("on_its_own/" + name), new TestEnvironmentDefinition.AllOf(List.of())));
        }

        private void register(String name, int maxTicks, Consumer<GameTestHelper> body, Holder<TestEnvironmentDefinition<?>> batch) {
            var id = id(name);
            CodeGameTest.define(id, body);
            event.registerTest(id, new CodeGameTest(id, new TestData<>(batch, PLATFORM, maxTicks, 0, true, Rotation.NONE)));
        }
    }
}
