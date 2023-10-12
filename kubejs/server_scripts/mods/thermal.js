ServerEvents.recipes(event => {
  console.log('[AMMONIUM@KUBEJS]: thermal recipes...');  

  const smeltingRecipes = [
    { output: '#forge:ingots/steel', outputCount: 3, inputs: [Item.of('#forge:ingots/iron', 3), Item.of('#minecraft:coals', 3)], energy: 12000 },
    { output: 'minecraft:calcite', outputCount: 1, inputs: ['minecraft:stone', 'minecraft:bone_meal'], energy: 800 },
    { output: 'create:brass_ingot', outputCount: 2, inputs: ['#forge:ingots/copper', '#forge:ingots/zinc'], energy: 2400 },
    { output: 'minecraft:amethyst_shard', outputCount: 1, inputs: ['ae2:charged_certus_quartz_crystal', 'kubejs:cube'], energy: 24000 },
    { output: 'thermal:enderium_block', outputCount: 2, inputs: ['#forge:storage_blocks/diamond', Item.of('#forge:storage_blocks/lead', 3), Item.of('#forge:storage_blocks/ender_pearl', 2)], energy: 144000 },
    { output: 'thermal:lumium_block', outputCount: 4, inputs: ['#forge:storage_blocks/silver', Item.of('#forge:storage_blocks/tin', 3), Item.of('#forge:storage_blocks/glowstone', 2)], energy: 108000 },
    { output: 'thermal:signalum_block', outputCount: 4, inputs: ['#forge:storage_blocks/silver', Item.of('#forge:storage_blocks/copper', 3), Item.of('#forge:storage_blocks/redstone', 4)], energy: 108000 },
    { output: 'thermal:constantan_block', outputCount: 2, inputs: ['#forge:storage_blocks/nickel', '#forge:storage_blocks/copper'], energy: 28800 },
    { output: 'thermal:invar_block', outputCount: 3, inputs: ['#forge:storage_blocks/nickel', Item.of('#forge:storage_blocks/iron')], energy: 43200 },
    { output: 'thermal:electrum_block', outputCount: 2, inputs: ['#forge:storage_blocks/silver', '#forge:storage_blocks/gold'], energy: 28800 },
    { output: 'thermal:bronze_block', outputCount: 4, inputs: ['#forge:storage_blocks/tin', Item.of('#forge:storage_blocks/copper', 3)], energy: 57600 },
    { output: 'create:brass_block', outputCount: 2, inputs: ['#forge:storage_blocks/zinc', '#forge:storage_blocks/copper'], energy: 21600 }
  ];

  smeltingRecipes.forEach(recipe => {
    event.recipes.thermal.smelter(Item.of(recipe.output, recipe.outputCount), recipe.inputs).energy(recipe.energy);
  });

  event.replaceInput({id: 'thermal:machine_smelter'}, 'thermal:rf_coil', 'kubejs:cube2');
  event.replaceInput({id: 'thermal:augments/machine_speed_augment'}, 'thermal:rf_coil', 'kubejs:cube2');
  event.replaceInput({id: 'thermal:machine_crucible'}, 'thermal:rf_coil', 'kubejs:cube2');

  event.remove({id: 'thermal:machine_frame'});
  event.shaped('thermal:machine_frame', [
    'ICI',
    'CGC',
    'ICI'
  ], {
    I: 'minecraft:iron_ingot',
    C: 'minecraft:cobblestone',
    G: '#forge:gears/tin'
  })
  
  event.remove({id: 'thermal:machine_pulverizer'});
  event.shaped('thermal:machine_pulverizer', [
    ' P ',
    'RFR',
    'GCG'
  ], {
    P: 'minecraft:piston',
    R: 'minecraft:redstone',
    F: 'thermal:machine_frame',
    G: '#forge:gears/copper',
    C: 'thermal:rf_coil'
  });
  
  event.remove({id: 'thermal:augments/upgrade_augment_2'});
  event.shaped('thermal:upgrade_augment_2', [
    'EAE',
    'SQS',
    'EME'
    ], {
      E: '#forge:ingots/electrum',
      A: 'thermal:upgrade_augment_1',
      S: '#forge:gears/signalum',
      Q: '#forge:gems/quartz',
      M: 'kubejs:cube2'
  });
  
  event.remove({id: 'thermal:augments/upgrade_augment_3'});
  event.shaped('thermal:upgrade_augment_3', [
    'EAE',
    'SQS',
    'EME'
    ], {
      E: '#forge:ingots/enderium',
      A: 'thermal:upgrade_augment_2',
      S: '#forge:gears/lumium',
      Q: '#thermal:glass/hardened',
      M: 'kubejs:cube3'
  });

  event.recipes.thermal.pulverizer(['minecraft:dirt', Item.of('minecraft:red_mushroom').withChance(0.25), Item.of('minecraft:brown_mushroom').withChance(0.25)], 'minecraft:podzol').energy(200);

  event.replaceInput({id: 'thermal:augments/upgrade_augment_1'}, '#forge:gears/gold', 'kubejs:cube1');
  event.replaceInput({id: 'thermal:machine_pyrolyzer'}, 'minecraft:blaze_rod', '#forge:ingots/steel');

  event.remove({id: 'thermal:machines/pyrolyzer/pyrolyzer_coal'});
  event.remove({id: 'thermal:gunpowder_4'});
})