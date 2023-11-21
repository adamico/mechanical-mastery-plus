ServerEvents.recipes(event => {
  console.log('[AMMONIUM@KUBEJS]: gears recipes...');

  ['steel'].forEach(metal => {
    event.shaped(`kubejs:${metal}_gear`, [
      ' A ',
      'AIA',
      ' A '
      ], {
        I: '#forge:nuggets/iron',
        A: `#forge:plates/${metal}`
    });
  });

  let replaceGearRecipes = (gearRecipes) => {
    gearRecipes.forEach(recipe => {
      event.replaceInput({ id: `thermal:parts/${recipe.id}_gear` }, `${recipe.inputType}/${recipe.material}`, `${recipe.outputType}/${recipe.material}`);
    });
  }

  const ieMaterials = ['gold', 'iron', 'diamond']
  ieMaterials.forEach(material => {
    event.remove({id: `industrialforegoing:${material}_gear`});
  })

  const gearRecipes = [
    { id: 'gold', inputType: '#forge:ingots', material: 'gold', outputType: '#forge:plates' },
    { id: 'iron', inputType: '#forge:ingots', material: 'iron', outputType: '#forge:plates' },
    { id: 'nickel', inputType: '#forge:ingots', material: 'nickel', outputType: '#forge:plates' },
    { id: 'silver', inputType: '#forge:ingots', material: 'silver', outputType: '#forge:plates' },
    { id: 'lead', inputType: '#forge:ingots', material: 'lead', outputType: '#forge:plates' },
    { id: 'tin', inputType: '#forge:ingots', material: 'tin', outputType: '#forge:plates' },
    { id: 'copper', inputType: '#forge:ingots', material: 'copper', outputType: '#forge:plates' },
    { id: 'constantan', inputType: '#forge:ingots', material: 'constantan', outputType: '#forge:plates' },
    { id: 'invar', inputType: '#forge:ingots', material: 'invar', outputType: '#forge:plates' },
    { id: 'diamond', inputType: '#forge:gems', material: 'diamond', outputType: '#forge:plates' }
  ];

  replaceGearRecipes(gearRecipes);

  event.recipes.thermal.press(['kubejs:steel_gear'], [Item.of('#forge:ingots/steel', 4), 'thermal:press_gear_die']).energy(2400);
  event.recipes.thermal.press(['thermal:diamond_gear'], [Item.of('#forge:gems/diamond', 4), 'thermal:press_gear_die']).energy(2400);
})