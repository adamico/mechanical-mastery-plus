// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.defence.Defences;
import io.github._5thlayer.mechanicalmasteryreborn.defence.FeDefenceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The FE defence (#236): fed FE, it shoots the nearest hostile mob in reach until it dies, spending
 * {@code defence.fe.fe_per_shot} a shot and sparing a passive mob nearer still; without FE it does nothing; and with
 * no hostile in reach it spends nothing. It shoots a creeper before a nearer hostile, and its electric damage hurts a witch in full (#247). Its hostile is a husk, which daylight doesn't burn.
 */
final class DefenceTests {

    private static final BlockPos DEFENCE = new BlockPos(2, 2, 2);
    private static final BlockPos HOSTILE = new BlockPos(6, 2, 2);
    private static final BlockPos PASSIVE = new BlockPos(4, 2, 2);

    private DefenceTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("defence_kills_a_hostile_with_fe", 200, DefenceTests::killsWithFe);
        tests.test("defence_idles_without_fe", 60, DefenceTests::idlesWithoutFe);
        tests.test("defence_spends_nothing_without_a_hostile", 60, DefenceTests::sparesPassive);
        tests.test("defence_hurts_a_witch_in_full", 60, DefenceTests::hurtsWitch);
        tests.test("defence_shoots_a_creeper_first", 60, DefenceTests::creeperFirst);
    }

    private static void killsWithFe(GameTestHelper helper) {
        helper.setBlock(DEFENCE, Defences.FE_DEFENCE.get());
        int capacity = ServerConfig.feDefenceFePerShot() * ServerConfig.feDefenceStoreShots();
        helper.assertValueEqual(give(helper, capacity), capacity, "the FE an empty defence took");
        var husk = helper.spawnWithNoFreeWill(EntityType.HUSK, HOSTILE);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, PASSIVE);
        helper.succeedWhen(() -> {
            helper.assertTrue(!husk.isAlive(), "the husk is still alive");
            helper.assertValueEqual(pig.getHealth(), pig.getMaxHealth(), "the pig's health");
            int shots = (capacity - (int) face(helper).getAmountAsLong()) / ServerConfig.feDefenceFePerShot();
            int needed = (int) Math.ceil(husk.getMaxHealth() / ServerConfig.feDefenceDamage());
            helper.assertValueEqual(shots, needed, "the shots the husk took");
        });
    }

    private static void idlesWithoutFe(GameTestHelper helper) {
        helper.setBlock(DEFENCE, Defences.FE_DEFENCE.get());
        var husk = helper.spawnWithNoFreeWill(EntityType.HUSK, HOSTILE);
        helper.runAfterDelay(40, () -> {
            helper.assertValueEqual(husk.getHealth(), husk.getMaxHealth(), "the husk's health");
            FeDefenceBlockEntity defence = helper.getBlockEntity(DEFENCE, FeDefenceBlockEntity.class);
            helper.assertValueEqual(defence.status(), FeDefenceBlockEntity.Status.NO_FE, "the defence's status");
            helper.succeed();
        });
    }

    private static void sparesPassive(GameTestHelper helper) {
        helper.setBlock(DEFENCE, Defences.FE_DEFENCE.get());
        int fe = 3 * ServerConfig.feDefenceFePerShot();
        give(helper, fe);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, PASSIVE);
        helper.runAfterDelay(40, () -> {
            helper.assertValueEqual(pig.getHealth(), pig.getMaxHealth(), "the pig's health");
            helper.assertValueEqual(face(helper).getAmountAsLong(), (long) fe, "the FE a defence with no hostile holds");
            helper.succeed();
        });
    }

    /** A witch resists magic (#247): the defence's own damage type isn't magic, so a shot takes its full damage. */
    private static void hurtsWitch(GameTestHelper helper) {
        helper.setBlock(DEFENCE, Defences.FE_DEFENCE.get());
        give(helper, ServerConfig.feDefenceFePerShot());
        var witch = helper.spawnWithNoFreeWill(EntityType.WITCH, HOSTILE);
        helper.assertFalse(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(FeDefenceBlockEntity.ELECTRIC).is(DamageTypeTags.WITCH_RESISTANT_TO), "electric damage is in witch_resistant_to");
        helper.succeedWhen(() -> helper.assertValueEqual(witch.getHealth(), witch.getMaxHealth() - ServerConfig.feDefenceDamage(),
                "the witch's health after one shot"));
    }

    /** A creeper's blast costs the island the most (#241), so it is shot before a nearer husk. */
    private static void creeperFirst(GameTestHelper helper) {
        helper.setBlock(DEFENCE, Defences.FE_DEFENCE.get());
        give(helper, ServerConfig.feDefenceFePerShot());
        var husk = helper.spawnWithNoFreeWill(EntityType.HUSK, PASSIVE);
        var creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, HOSTILE);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(creeper.getHealth(), creeper.getMaxHealth() - ServerConfig.feDefenceDamage(), "the creeper's health");
            helper.assertValueEqual(husk.getHealth(), husk.getMaxHealth(), "the nearer husk's health");
        });
    }

    /** Offers {@code fe} to the defence, as a cable would, and says how much it took. */
    private static int give(GameTestHelper helper, int fe) {
        try (Transaction transaction = Transaction.openRoot()) {
            int taken = face(helper).insert(fe, transaction);
            transaction.commit();
            return taken;
        }
    }

    private static EnergyHandler face(GameTestHelper helper) {
        EnergyHandler face = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(DEFENCE), Direction.UP);
        helper.assertTrue(face != null, "an FE defence has no FE face");
        return face;
    }
}
