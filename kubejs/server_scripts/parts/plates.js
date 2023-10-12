ServerEvents.recipes(event => {
  console.log('[AMMONIUM@KUBEJS]: plate recipes...');

  let plateMaterials = [
    { name: 'gold', mod: 'thermal', inputType: 'ingots', output: 'thermal:gold_plate' },
    { name: 'iron', mod: 'thermal', inputType: 'ingots', output: 'thermal:iron_plate' },
    { name: 'nickel', mod: 'thermal', inputType: 'ingots', output: 'thermal:nickel_plate' },
    { name: 'silver', mod: 'thermal', inputType: 'ingots', output: 'thermal:silver_plate' },
    { name: 'lead', mod: 'thermal', inputType: 'ingots', output: 'thermal:lead_plate' },
    { name: 'tin', mod: 'thermal', inputType: 'ingots', output: 'thermal:tin_plate' },
    { name: 'copper', mod: 'thermal', inputType: 'ingots', output: 'thermal:copper_plate' },
    { name: 'diamond', mod: 'kubejs', inputType: 'gems', output: 'kubejs:diamond_plate' },
    { name: 'constantan', mod: 'thermal', inputType: 'ingots', output: 'thermal:constantan_plate' },
    { name: 'invar', mod: 'thermal', inputType: 'ingots', output: 'thermal:invar_plate' }
  ];

  plateMaterials.forEach(material => {
    event.custom({
      type: 'ae2:inscriber',
      ingredients: {
        middle: {
          tag: `forge:${material.inputType}/${material.name}`
        }
      },
      mode: 'inscribe',
      result: {
        item: material.output
      }
    })
  });

  event.remove({id: 'thermal:press_gear_die'});
  event.shaped('thermal:press_gear_die', [
    ' I ',
    'IGI',
    ' I '
    ], {
      I: '#forge:plates/invar',
      G: '#forge:gears/gold'
  });
});