// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;

import io.github._5thlayer.libworks.gallery.GalleryPack;
import io.github._5thlayer.libworks.testkit.GalleryCheck;
import io.github._5thlayer.libworks.testkit.GalleryCheck.Layout;
import io.github._5thlayer.libworks.testkit.GalleryCheck.Scene;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.defence.Defences;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.port.Port;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * The Gallery pack (libworks ADR 0010, #278), through libworks' {@link GalleryCheck}: the pack is offered and
 * off, its functions load, its build runs every command and stands each scene's key block in place, the essences
 * scene shows every essence, a scene's sign builds it again leaving nothing loose, and its clear empties a plot.
 * These check the gallery only: what the scenes' blocks do is the mod's other game tests' to check. Each test
 * turns the pack on, which reloads the server's data, so each runs in a batch of its own.
 */
final class GalleryTests {

    /** Where the gallery is run from: south of the platform, its plots north and east of here. */
    private static final BlockPos ORIGIN = new BlockPos(0, 1, 8);

    /** The scenes in the order {@code build} lays them, each with a key block where, from where the scene is run, its function places it. */
    private static final List<Scene> SCENES = List.of(
            new Scene("recycler", new BlockPos(14, 1, -6), Recycler.CORE.get()),
            new Scene("recycler_fe", new BlockPos(13, 1, -8), Recycler.FE_DRIVE.get()),
            new Scene("cod", new BlockPos(14, 0, -6), Cod.BLOCK.get()),
            new Scene("ports", new BlockPos(8, 0, -6), Port.BLOCKS_BY_HANDICAP.get(Handicap.ENERGY).get()),
            new Scene("essences", new BlockPos(8, 0, -9), Blocks.SMOOTH_STONE),
            new Scene("defence", new BlockPos(14, 1, -6), Defences.FE_DEFENCE.get()));

    /** Where a scene's sign stands, from where the scene is run. */
    private static final BlockPos SIGN = new BlockPos(14, 0, 0);

    private GalleryTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.testOnItsOwn("gallery_builds_a_scene_of_each_feature", GalleryCheck.TICKS, GalleryTests::builds);
        tests.testOnItsOwn("gallery_sign_builds_its_scene_again_leaving_nothing_loose", GalleryCheck.TICKS, GalleryTests::replay);
        tests.testOnItsOwn("gallery_clear_empties_its_plot", GalleryCheck.TICKS,
                helper -> GalleryCheck.clears(helper, MechanicalMasteryReborn.MOD_ID, ORIGIN, Layout.ONE_ROW));
    }

    /** libworks' build check over every plot, written out to add what a key block can't show: every essence on show. */
    private static void builds(GameTestHelper helper) {
        var at = helper.absolutePos(ORIGIN);
        var functions = helper.getLevel().getServer().getFunctions();
        GalleryCheck.withPack(helper, MechanicalMasteryReborn.MOD_ID, ORIGIN, Layout.ONE_ROW, SCENES.size(), (source, steps) -> steps.thenExecute(() -> {
            GalleryCheck.assertLoads(helper, source, MechanicalMasteryReborn.MOD_ID);
            for (var scene : SCENES) {
                helper.assertTrue(functions.get(GalleryPack.scene(MechanicalMasteryReborn.MOD_ID, scene.name())).isPresent(),
                        "there is no scene function " + scene.name());
            }
            GalleryCheck.assertRuns(helper, source, GalleryPack.build(MechanicalMasteryReborn.MOD_ID));
            GalleryCheck.assertKeyBlocks(helper, at, Layout.ONE_ROW, SCENES);
            var essences = Layout.ONE_ROW.sceneOrigin(at, SCENES.indexOf(scene("essences")));
            var shown = helper.getLevel().getEntitiesOfClass(Display.ItemDisplay.class, AABB.of(GalleryCheck.plot(essences))).stream()
                    .map(GalleryTests::shownItem).toList();
            for (Essence essence : Essence.values()) {
                var id = MechanicalMasteryReborn.MOD_ID + ":" + essence.path();
                helper.assertTrue(shown.contains(id), "the essences scene shows no " + id + ", only " + shown);
            }
            helper.assertValueEqual(shown.size(), Essence.values().length, "the essences on show");
        })).thenSucceed();
    }

    /** The Recycler scene's sign, clicked, builds the scene again where it stands: its core taken away first, so only the rebuild can put it back. */
    private static void replay(GameTestHelper helper) {
        var at = helper.absolutePos(ORIGIN);
        var recycler = scene("recycler");
        GalleryCheck.withPack(helper, MechanicalMasteryReborn.MOD_ID, ORIGIN, Layout.ONE_ROW, 1, (source, steps) -> steps.thenExecute(() -> {
            GalleryCheck.assertRuns(helper, source, GalleryPack.scene(MechanicalMasteryReborn.MOD_ID, recycler.name()));
            helper.getLevel().setBlockAndUpdate(at.offset(recycler.key()), Blocks.AIR.defaultBlockState());
            var sign = helper.getLevel().getBlockEntity(at.offset(SIGN));
            helper.assertTrue(sign instanceof SignBlockEntity entity
                            && entity.executeClickCommandsIfPresent(helper.getLevel(), source.getPlayer(), at.offset(SIGN), true),
                    "the Recycler scene's sign runs no command");
            GalleryCheck.assertKeyBlocks(helper, at, List.of(recycler));
            var loose = helper.getLevel().getEntitiesOfClass(ItemEntity.class, AABB.of(GalleryCheck.plot(at)));
            helper.assertTrue(loose.isEmpty(), "building the scene again left " + loose.stream().map(ItemEntity::getItem).toList() + " loose");
        })).thenSucceed();
    }

    /** The id of the item {@code display} shows, read from what it saves, since the display keeps its stack to itself. */
    private static String shownItem(Display.ItemDisplay display) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, display.registryAccess());
        display.saveWithoutId(output);
        return output.buildResult().getCompound("item").flatMap(item -> item.getString("id")).orElse("nothing");
    }

    private static Scene scene(String name) {
        return SCENES.stream().filter(scene -> scene.name().equals(name)).findFirst().orElseThrow();
    }
}
