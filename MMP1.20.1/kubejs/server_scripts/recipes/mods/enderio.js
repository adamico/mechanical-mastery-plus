ServerEvents.recipes(event => {
  event.recipes.enderio.alloy_smelting(
    'enderio:dark_steel_ingot',
    ['minecraft:iron_ingot',  '2x #minecraft:coals', 'minecraft:obsidian']
  );

  event.recipes.enderio.sag_milling(
    [
      Item.of('minecraft:iron_ingot').withChance(0.75),
      Item.of('minecraft:gold_ingot').withChance(0.75),
      Item.of('create:zinc_ingot').withChance(0.5),
    ],
    'kubejs:cube4'
  ).energy(4000);
});
