ServerEvents.recipes(event => {
  console.log('[kc00l@KUBEJS]: gears recipes...');

  const ieMaterials = ['gold', 'iron', 'diamond']
  ieMaterials.forEach(material => {
    event.remove({id: `industrialforegoing:${material}_gear`});
  })

  event.recipes.thermal.press(['kubejs:steel_gear'], [Item.of('#forge:ingots/steel', 4), 'thermal:press_gear_die']).energy(2400);
  event.recipes.thermal.press(['thermal:diamond_gear'], [Item.of('#forge:gems/diamond', 4), 'thermal:press_gear_die']).energy(2400);
})