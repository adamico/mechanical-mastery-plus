// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Refunds;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.lab.RefundMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * What an Assault's mobs break is refunded at the lab (#259, ADR 0015): a blast of one of its creepers drops nothing on
 * the team's island and holds each block as a silk-touch pickaxe mines it, what spills from it and pops off with it,
 * and the frames and armour stands it knocks down with what they hold; another blast is the game's. Its endermen and
 * blazes grief nothing. The refund is handed over as the Assault ends, by a win, a reset or a stop, adds to one not
 * yet taken, and outlasts a load; the lab gives it by hand to its team alone.
 */
final class RefundTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    /** Where the blasts go off, clear of C.O.D. */
    private static final BlockPos BLAST = new BlockPos(5, 2, 5);
    private static final BlockPos LAB = new BlockPos(3, 2, 3);
    private static final int TICKED = 2;

    private RefundTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("refund_holds_an_assault_blast_silk_touched_and_drops_nothing", 40, RefundTests::blastHeld);
        tests.test("refund_holds_a_frame_and_an_armour_stand_with_what_they_hold", 40, RefundTests::entitiesHeld);
        tests.test("refund_leaves_another_blast_to_the_game", 40, RefundTests::otherBlast);
        tests.test("refund_keeps_assault_endermen_and_blazes_from_griefing", 40, RefundTests::noGriefing);
        tests.test("refund_is_handed_over_by_a_win_a_reset_and_a_stop", 40, RefundTests::handedOver);
        tests.test("refund_adds_to_one_not_yet_taken_and_outlasts_a_load", 40, RefundTests::addsAndLoads);
        tests.test("refund_lab_gives_by_hand_to_its_team_alone", 20, RefundTests::byHand);
    }

    /** Starts the protection challenge for a new team, then runs {@code body} with the team and its Assault's gate. */
    private static void assault(GameTestHelper helper, Started body) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            helper.assertTrue(Challenges.begin(server(helper), team, Handicap.PROTECTION), "the protection challenge didn't start");
            UUID gate = AssaultState.get(server(helper)).assault(team).orElseThrow().gates().getFirst();
            body.run(team, gate);
        });
    }

    private interface Started {
        void run(UUID team, UUID gate);
    }

    private static void blastHeld(GameTestHelper helper) {
        assault(helper, (team, gate) -> {
            helper.setBlock(BLAST.east(), Blocks.STONE);
            helper.setBlock(BLAST.east().above(), Blocks.TORCH);
            helper.setBlock(BLAST.west(), Blocks.GLASS);
            helper.setBlock(BLAST.north(), Blocks.CHEST);
            if (helper.getBlockEntity(BLAST.north(), ChestBlockEntity.class) instanceof ChestBlockEntity chest) {
                chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
            }
            explode(helper, mob(helper, EntityType.CREEPER, gate), 2.5F);
            List<ItemStack> held = Refunds.get(server(helper)).held(team);
            expectHeld(helper, held, Items.STONE, "a stone, silk-touched");
            expectHeld(helper, held, Items.GLASS, "a glass, silk-touched");
            expectHeld(helper, held, Items.TORCH, "the torch that popped off");
            expectHeld(helper, held, Items.CHEST, "the chest");
            expectHeld(helper, held, Items.DIAMOND, "what spilled from the chest");
            helper.assertFalse(held.stream().anyMatch(stack -> stack.is(Items.COBBLESTONE)), "a stone was held as cobblestone");
            helper.assertTrue(helper.getBlockState(BLAST.east()).isAir(), "the blast left the stone");
            helper.assertTrue(items(helper).isEmpty(), "the blast dropped " + items(helper));
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    private static void entitiesHeld(GameTestHelper helper) {
        assault(helper, (team, gate) -> {
            helper.setBlock(BLAST.east(2), Blocks.STONE);
            ItemFrame frame = new ItemFrame(helper.getLevel(), helper.absolutePos(BLAST.east()), Direction.WEST);
            frame.setItem(new ItemStack(Items.EMERALD));
            helper.assertTrue(helper.getLevel().addFreshEntity(frame), "the frame wasn't hung");
            ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, BLAST.west());
            stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            explode(helper, mob(helper, EntityType.CREEPER, gate), 2.5F);
            List<ItemStack> held = Refunds.get(server(helper)).held(team);
            expectHeld(helper, held, Items.ITEM_FRAME, "the frame");
            expectHeld(helper, held, Items.EMERALD, "what the frame held");
            expectHeld(helper, held, Items.ARMOR_STAND, "the armour stand");
            expectHeld(helper, held, Items.IRON_HELMET, "what the armour stand wore");
            helper.assertTrue(frame.isRemoved() && stand.isRemoved(), "the blast left the frame or the armour stand");
            helper.assertTrue(items(helper).isEmpty(), "the blast dropped " + items(helper));
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    /** A blast of a creeper of no Assault, and one of no one, as the team's own TNT is: the game's, nothing held. */
    private static void otherBlast(GameTestHelper helper) {
        assault(helper, (team, gate) -> {
            helper.setBlock(BLAST.east(), Blocks.STONE);
            explode(helper, mob(helper, EntityType.CREEPER, null), 2.5F);
            helper.setBlock(BLAST.west(), Blocks.STONE);
            Vec3 at = helper.absoluteVec(Vec3.atCenterOf(BLAST));
            helper.getLevel().explode(null, at.x, at.y, at.z, 2.5F, Level.ExplosionInteraction.TNT);
            helper.assertTrue(helper.getBlockState(BLAST.east()).isAir() && helper.getBlockState(BLAST.west()).isAir(),
                    "the blasts broke nothing");
            helper.assertTrue(Refunds.get(server(helper)).held(team).isEmpty(), "another blast was held: " + Refunds.get(server(helper)).held(team));
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    private static void noGriefing(GameTestHelper helper) {
        assault(helper, (team, gate) -> {
            var level = helper.getLevel();
            helper.assertFalse(EventHooks.canEntityGrief(level, mob(helper, EntityType.ENDERMAN, gate)), "an Assault enderman may grief");
            helper.assertFalse(EventHooks.canEntityGrief(level, mob(helper, EntityType.BLAZE, gate)), "an Assault blaze may grief");
            helper.assertTrue(EventHooks.canEntityGrief(level, mob(helper, EntityType.CREEPER, gate)), "an Assault creeper may not blast");
            helper.assertTrue(EventHooks.canEntityGrief(level, mob(helper, EntityType.ENDERMAN, null)), "an enderman of no Assault may not grief");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    /** Each way an Assault ends hands its refund over, told; holding goes on until then. */
    private static void handedOver(GameTestHelper helper) {
        MinecraftServer server = server(helper);
        for (int way = 0; way < 3; way++) {
            UUID team = UUID.randomUUID();
            AssaultState.get(server).noteCod(team, net.minecraft.core.GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(COD)));
            helper.assertTrue(Challenges.begin(server, team, Handicap.PROTECTION), "the protection challenge didn't start");
            Refunds refunds = Refunds.get(server);
            refunds.hold(team, new ItemStack(Items.STONE, 5));
            helper.assertValueEqual(refunds.count(team), 0, "what waits before the Assault ends");
            String how = switch (way) {
                case 0 -> Challenges.win(server, team) ? "a win" : null;
                case 1 -> Challenges.reset(server, team) ? "a reset" : null;
                default -> Challenges.end(server, team) ? "a stop" : null;
            };
            helper.assertTrue(how != null, "the Assault didn't end, way " + way);
            helper.assertValueEqual(refunds.count(team), 5, "what waits after " + how);
            helper.assertTrue(refunds.held(team).isEmpty(), "something is still held after " + how);
            Challenges.end(server, team);
        }
        helper.succeed();
    }

    private static void addsAndLoads(GameTestHelper helper) {
        MinecraftServer server = server(helper);
        UUID team = UUID.randomUUID();
        Refunds refunds = Refunds.get(server);
        refunds.hold(team, new ItemStack(Items.STONE, 40));
        refunds.handOver(server, team);
        refunds.hold(team, new ItemStack(Items.STONE, 40));
        refunds.hold(team, new ItemStack(Items.GLASS, 2));
        helper.assertValueEqual(refunds.handOver(server, team), 42, "what the second Assault handed over");
        helper.assertValueEqual(refunds.count(team), 82, "what waits after two Assaults");
        refunds.hold(team, new ItemStack(Items.DIAMOND));
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var codec = Refunds.TYPE.codec();
        Refunds loaded = codec.parse(ops, codec.encodeStart(ops, refunds).getOrThrow()).getOrThrow();
        helper.assertValueEqual(loaded.count(team), 82, "what waits after a load");
        helper.assertValueEqual(loaded.held(team).size(), 1, "what is held after a load");
        loaded.handOver(server, team);
        refunds.handOver(server, team);
        refunds.waiting(team).clear();
        helper.succeed();
    }

    private static void byHand(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        MinecraftServer server = server(helper);
        Refunds refunds = Refunds.get(server);
        refunds.hold(team, new ItemStack(Items.STONE, 64));
        refunds.hold(team, new ItemStack(Items.GLASS, 10));
        refunds.hold(team, new ItemStack(Items.DIAMOND, 2));
        refunds.handOver(server, team);
        helper.setBlock(LAB, Lab.BLOCK.get());
        Player visitor = helper.makeMockPlayer(GameType.SURVIVAL);
        RefundMenu theirs = open(helper, visitor);
        theirs.quickMoveStack(visitor, 0);
        theirs.clicked(1, 0, ContainerInput.PICKUP, visitor);
        helper.assertFalse(theirs.clickMenuButton(visitor, RefundMenu.TAKE_ALL), "a visitor took all");
        helper.assertValueEqual(refunds.count(team), 76, "what waits after a visitor's clicks");
        helper.assertTrue(visitor.getInventory().isEmpty() && visitor.containerMenu.getCarried().isEmpty(), "a visitor took some of the refund");
        Player member = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(member, team);
        RefundMenu ours = open(helper, member);
        ours.quickMoveStack(member, 0);
        helper.assertValueEqual(member.getInventory().countItem(Items.STONE), 64, "the stone a shift-click took");
        helper.assertTrue(ours.clickMenuButton(member, RefundMenu.TAKE_ALL), "take all took nothing");
        helper.assertValueEqual(member.getInventory().countItem(Items.GLASS), 10, "the glass take all took");
        helper.assertValueEqual(member.getInventory().countItem(Items.DIAMOND), 2, "the diamonds take all took");
        helper.assertValueEqual(refunds.count(team), 0, "what waits after take all");
        helper.succeed();
    }

    private static RefundMenu open(GameTestHelper helper, Player player) {
        var menu = RefundMenu.provider(helper.getLevel(), helper.absolutePos(LAB)).createMenu(0, player.getInventory(), player);
        helper.assertTrue(menu instanceof RefundMenu, "the lab's refund opened " + menu);
        player.containerMenu = menu;
        return (RefundMenu) menu;
    }

    /** A still mob of {@code type} at the blast, of the Assault's gate {@code gate}, or of none. */
    private static Mob mob(GameTestHelper helper, EntityType<? extends Mob> type, UUID gate) {
        Mob mob = type.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        mob.setPos(helper.absoluteVec(Vec3.atBottomCenterOf(BLAST.above(2))));
        mob.setNoAi(true);
        if (gate != null) {
            Assault.markOwner(mob, gate);
        }
        helper.getLevel().addFreshEntity(mob);
        return mob;
    }

    /** A creeper's blast at {@link #BLAST}, as {@code source}'s own, which a mob's breaks blocks. */
    private static void explode(GameTestHelper helper, Mob source, float radius) {
        Vec3 at = helper.absoluteVec(Vec3.atCenterOf(BLAST));
        helper.getLevel().explode(source, at.x, at.y, at.z, radius, Level.ExplosionInteraction.MOB);
        source.discard();
    }

    private static List<ItemEntity> items(GameTestHelper helper) {
        AABB bounds = helper.getBounds();
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds);
    }

    private static void expectHeld(GameTestHelper helper, List<ItemStack> held, Item item, String what) {
        helper.assertTrue(held.stream().anyMatch(stack -> stack.is(item)), "the refund misses " + what + ": " + held);
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
