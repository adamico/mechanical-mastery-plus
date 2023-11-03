ServerEvents.blockLootTables(event => {
  event.modifyBlock(/^minecraft:.*grass$/, table => {
    table.addPool(pool => {
      pool.addItem(Item.of('supplementaries:flax_seeds')).randomChance(0.25);
    })
  });
});