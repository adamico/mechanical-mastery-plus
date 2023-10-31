ServerEvents.blockLootTables(event => {
  event.modifyBlock(/^minecraft:.*grass$/, table => {
    table.addPool(pool => {
      pool.addCondition({
          "condition": "minecraft:match_tool",
          "predicate": {
            "tag": "farmersdelight:straw_harvesters"
          }
        });
      pool.addItem(Item.of('lititup:switchgrass_fiber')).randomChance(0.5);
    });
  });
});