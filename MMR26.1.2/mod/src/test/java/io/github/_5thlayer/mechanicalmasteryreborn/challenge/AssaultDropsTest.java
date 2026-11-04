// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/**
 * The Assault's roster and its Rift Shards (#251), in the pack this mod's folder sits in: the mobs each step adds, each
 * with its own {@code assault/<mob>} loot table whose Rift Shard pool gives the harder mobs more, and no other loot
 * table giving a Rift Shard. No Gateways is on the game tests' runs, and the pack's data isn't either, so these read
 * the files.
 */
class AssaultDropsTest {

    private static final Path DATA = Path.of("../kubejs/data");
    private static final Path PACK = DATA.resolve("mechanicalmasteryreborn");
    private static final Path ASSAULT = PACK.resolve("gateways/gateways/assault.json");
    private static final Path HOME = PACK.resolve("gateways/gateways/home.json");
    private static final Path TABLES = PACK.resolve("loot_table/assault");
    private static final String SHARD = "mechanicalmasteryreborn:rift_shard";
    private static final String TABLE = "mechanicalmasteryreborn:assault/";

    /**
     * The mobs whose vanilla tables give a drop only to a player's kill, so whose Assault tables copy those tables' pools
     * without that condition, and the drops each copy keeps that the Chain, or going deep, is for.
     */
    private static final Map<String, List<String>> INLINED = Map.of(
            "zombie", List.of("minecraft:rotten_flesh", "minecraft:iron_ingot"),
            "husk", List.of("minecraft:rotten_flesh", "minecraft:iron_ingot"),
            "spider", List.of("minecraft:string", "minecraft:spider_eye"),
            "vindicator", List.of("minecraft:emerald"),
            "blaze", List.of("minecraft:blaze_rod"),
            "breeze", List.of("minecraft:breeze_rod"),
            "evoker", List.of("minecraft:totem_of_undying", "minecraft:emerald"));

    /** What a mob's Rift Shard pool gives: a chance of between {@code min} and {@code max} shards. */
    private record Drop(double chance, int min, int max) {
    }

    @Test
    void eachStepAddsItsMobs() throws IOException {
        Map<Integer, List<String>> steps = new TreeMap<>();
        for (JsonElement element : read(ASSAULT).getAsJsonArray("modifiers")) {
            JsonObject modifier = element.getAsJsonObject();
            JsonObject mode = modifier.getAsJsonObject("application_mode");
            if (mode.get("type").getAsString().equals("gateways:after_every_n_waves") && modifier.has("entities")) {
                steps.computeIfAbsent(mode.get("waves").getAsInt(), waves -> new ArrayList<>()).addAll(mobs(modifier));
            }
        }
        assertEquals(Map.of(
                5, List.of("zombie", "skeleton"),
                10, List.of("husk", "spider", "slime"),
                15, List.of("vindicator", "creeper", "witch"),
                20, List.of("blaze", "enderman"),
                25, List.of("breeze"),
                30, List.of("evoker")), steps);
    }

    @Test
    void everyRosterMobHasItsOwnAssaultTable() throws IOException {
        for (JsonElement entity : entities()) {
            String mob = name(entity.getAsJsonObject());
            List<String> tables = new ArrayList<>();
            for (JsonElement modifier : entity.getAsJsonObject().getAsJsonArray("modifiers")) {
                if (modifier.getAsJsonObject().get("type").getAsString().equals("gateways:loot_table")) {
                    tables.add(modifier.getAsJsonObject().get("loot_table").getAsString());
                }
            }
            assertEquals(List.of(TABLE + mob), tables, "the Assault's " + mob + "'s loot tables");
            assertTrue(Files.exists(TABLES.resolve(mob + ".json")), "no assault/" + mob + " loot table");
        }
    }

    @Test
    void eachMobsTableGivesItsVanillaDropsAndItsShards() throws IOException {
        Map<String, Drop> expected = new LinkedHashMap<>();
        for (String mob : List.of("zombie", "husk", "skeleton", "spider", "slime")) {
            expected.put(mob, new Drop(0.2, 1, 1));
        }
        expected.put("creeper", new Drop(0.35, 1, 1));
        for (String mob : List.of("witch", "vindicator", "blaze", "enderman")) {
            expected.put(mob, new Drop(1, 1, 2));
        }
        expected.put("breeze", new Drop(1, 2, 2));
        expected.put("evoker", new Drop(1, 3, 3));
        Set<String> roster = new TreeSet<>();
        for (JsonElement entity : entities()) {
            roster.add(name(entity.getAsJsonObject()));
        }
        assertEquals(new TreeSet<>(expected.keySet()), roster, "the Assault's roster");
        assertEquals(roster, tableNames(), "the assault/* loot tables");
        for (var mob : expected.entrySet()) {
            JsonObject table = read(TABLES.resolve(mob.getKey() + ".json"));
            List<JsonObject> pools = table.getAsJsonArray("pools").asList().stream().map(JsonElement::getAsJsonObject).toList();
            boolean vanilla = pools.stream().anyMatch(pool -> pool.toString().contains("\"value\":\"minecraft:entities/" + mob.getKey() + "\""));
            for (String drop : INLINED.getOrDefault(mob.getKey(), List.of())) {
                assertTrue(pools.stream().anyMatch(pool -> pool.toString().contains("\"name\":\"" + drop + "\"")),
                        mob.getKey() + "'s table leaves out its " + drop);
            }
            assertTrue(vanilla != INLINED.containsKey(mob.getKey()), mob.getKey() + "'s table neither names nor copies its vanilla drops, or both");
            List<Drop> shards = pools.stream().filter(pool -> pool.toString().contains(SHARD)).map(AssaultDropsTest::drop).toList();
            assertEquals(List.of(mob.getValue()), shards, mob.getKey() + "'s Rift Shards");
        }
    }

    /** The Electric Defence kills with no player, and its kills drop the same as a player's (#251). */
    @Test
    void noDropNeedsAPlayersKill() throws IOException {
        for (String mob : tableNames()) {
            assertFalse(Files.readString(TABLES.resolve(mob + ".json")).contains("minecraft:killed_by_player"),
                    mob + "'s table gives a drop only to a player's kill");
        }
    }

    /** Ghasts level the island from afar, endermites and silverfish drop nothing, and phantoms fly over the bridges. */
    @Test
    void leavesOutTheMobsThatDontFit() throws IOException {
        for (JsonElement entity : entities()) {
            String mob = name(entity.getAsJsonObject());
            assertFalse(Set.of("ghast", "endermite", "silverfish", "phantom").contains(mob), "the Assault sends a " + mob);
        }
    }

    @Test
    void noOtherLootTableGivesARiftShard() throws IOException {
        try (Stream<Path> files = Files.walk(DATA)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                boolean lootTable = DATA.relativize(file).toString().replace('\\', '/').matches("[^/]+/loot_tables?/.*");
                if (lootTable && !file.startsWith(TABLES)) {
                    assertFalse(Files.readString(file).contains(SHARD), file + " gives a Rift Shard");
                }
            }
        }
    }

    /** One shard a wave, and one more for the whole gate: fewer than the Assault gives (#251). */
    @Test
    void homeGateGivesOneShardAWaveAndOneAtItsEnd() throws IOException {
        JsonObject home = read(HOME);
        for (JsonElement wave : home.getAsJsonArray("waves")) {
            assertEquals(1, shards(wave.getAsJsonObject().getAsJsonArray("rewards").asList()), "a home gate wave's Rift Shards");
        }
        assertEquals(1, shards(home.getAsJsonArray("rewards").asList()), "a home gate's end reward");
    }

    private static int shards(List<JsonElement> rewards) {
        return rewards.stream().map(JsonElement::getAsJsonObject)
                .filter(reward -> reward.get("type").getAsString().equals("gateways:stack"))
                .map(reward -> reward.getAsJsonObject("stack"))
                .filter(stack -> stack.get("id").getAsString().equals(SHARD))
                .mapToInt(stack -> stack.has("count") ? stack.get("count").getAsInt() : 1).sum();
    }

    /** A Rift Shard pool: one roll of one shard entry, under at most a random chance, counted by at most a set_count. */
    private static Drop drop(JsonObject pool) {
        assertEquals(1, pool.get("rolls").getAsInt(), "a Rift Shard pool's rolls");
        double chance = 1;
        if (pool.has("conditions")) {
            List<JsonElement> conditions = pool.getAsJsonArray("conditions").asList();
            assertEquals(1, conditions.size(), "a Rift Shard pool's conditions");
            JsonObject condition = conditions.getFirst().getAsJsonObject();
            assertEquals("minecraft:random_chance", condition.get("condition").getAsString(), "a Rift Shard pool's condition");
            chance = condition.get("chance").getAsDouble();
        }
        List<JsonElement> entries = pool.getAsJsonArray("entries").asList();
        assertEquals(1, entries.size(), "a Rift Shard pool's entries");
        JsonObject entry = entries.getFirst().getAsJsonObject();
        assertEquals(SHARD, entry.get("name").getAsString());
        int min = 1;
        int max = 1;
        if (entry.has("functions")) {
            for (JsonElement function : entry.getAsJsonArray("functions")) {
                assertEquals("minecraft:set_count", function.getAsJsonObject().get("function").getAsString());
                JsonElement count = function.getAsJsonObject().get("count");
                if (count.isJsonPrimitive()) {
                    min = max = count.getAsInt();
                } else {
                    JsonObject uniform = count.getAsJsonObject();
                    assertEquals("minecraft:uniform", uniform.get("type").getAsString());
                    min = uniform.get("min").getAsInt();
                    max = uniform.get("max").getAsInt();
                }
            }
        }
        return new Drop(chance, min, max);
    }

    /** Every entity the Assault sends, in its base wave and its steps. */
    private static List<JsonElement> entities() throws IOException {
        JsonObject gate = read(ASSAULT);
        List<JsonElement> entities = new ArrayList<>(gate.getAsJsonObject("base_wave").getAsJsonArray("entities").asList());
        for (JsonElement modifier : gate.getAsJsonArray("modifiers")) {
            if (modifier.getAsJsonObject().has("entities")) {
                entities.addAll(modifier.getAsJsonObject().getAsJsonArray("entities").asList());
            }
        }
        return entities;
    }

    private static List<String> mobs(JsonObject modifier) {
        return modifier.getAsJsonArray("entities").asList().stream().map(entity -> name(entity.getAsJsonObject())).toList();
    }

    private static String name(JsonObject entity) {
        return entity.get("entity").getAsString().replace("minecraft:", "");
    }

    private static Set<String> tableNames() throws IOException {
        try (Stream<Path> files = Files.list(TABLES)) {
            return files.map(path -> path.getFileName().toString().replace(".json", "")).collect(TreeSet::new, Set::add, Set::addAll);
        }
    }

    private static JsonObject read(Path file) throws IOException {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }
}
