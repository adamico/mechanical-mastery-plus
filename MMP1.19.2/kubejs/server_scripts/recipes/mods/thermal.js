ServerEvents.recipes(event => {
  const smeltingRecipes = [
    { output: 'minecraft:calcite', outputCount: 1, inputs: ['minecraft:stone', 'minecraft:bone_meal'], energy: 800 },
    { output: 'create:brass_ingot', outputCount: 2, inputs: [{ value: [{ tag: 'forge:ingots/copper' }] }, { value: [{ tag: 'forge:ingots/zinc' }] }], energy: 2400 },
    { output: 'minecraft:amethyst_shard', outputCount: 1, inputs: [{ value: [{ item: 'ae2:charged_certus_quartz_crystal' }] }, { value: [{ item: 'kubejs:cube3' }] }], energy: 24000 },
    { output: 'thermal:enderium_block', outputCount: 2, inputs: [{ value: [{ tag: 'forge:storage_blocks/diamond' }] }, { value: [{ tag: 'forge:storage_blocks/lead' }], count: 3 }, { value: [{ tag: 'forge:storage_blocks/ender_pearl' }], count: 2 }], energy: 144000 },
    { output: 'thermal:lumium_block', outputCount: 4, inputs: [{ value: [{ tag: 'forge:storage_blocks/silver' }] }, { value: [{ tag: 'forge:storage_blocks/tin' }], count: 3 }, { value: [{ item: 'minecraft:glowstone' }], count: 4 }], energy: 108000 },
    { output: 'thermal:signalum_block', outputCount: 4, inputs: [{ value: [{ tag: 'forge:storage_blocks/silver' }] }, { value: [{ tag: 'forge:storage_blocks/copper' }], count: 3 }, { value: [{ item: 'minecraft:redstone_block' }], count: 4 }], energy: 108000 },
    { output: 'thermal:constantan_block', outputCount: 2, inputs: [{ value: [{ tag: 'forge:storage_blocks/nickel' }] }, { value: [{ tag: 'forge:storage_blocks/copper' }] }], energy: 28800 },
    { output: 'thermal:invar_block', outputCount: 3, inputs: [{ value: [{ tag: 'forge:storage_blocks/nickel' }] }, { value: [{ tag: 'forge:storage_blocks/iron' }] }], energy: 43200 },
    { output: 'thermal:electrum_block', outputCount: 2, inputs: [{ value: [{ tag: 'forge:storage_blocks/silver' }] }, { value: [{ tag: 'forge:storage_blocks/gold' }] }], energy: 28800 },
    { output: 'thermal:bronze_block', outputCount: 4, inputs: [{ value: [{ tag: 'forge:storage_blocks/tin' }] }, { value: [{ tag: 'forge:storage_blocks/copper' }], count: 3 }], energy: 57600 },
    { output: 'create:brass_block', outputCount: 2, inputs: [{ value: [{ tag: 'forge:storage_blocks/zinc' }] }, { value: [{ tag: 'forge:storage_blocks/copper' }] }], energy: 21600 },
    { output: 'bigreactors:graphite_ingot', outputCount: 1, inputs: ["thermal:coal_coke", { value: [{ tag: "forge:silicon" }] }], energy: 3000 },
    { output: 'bigreactors:graphite_block', outputCount: 1, inputs: ["thermal:coal_coke_block", { value: [{ tag: "forge:silicon" }], count: 9 }], energy: 18000 }
  ];

  smeltingRecipes.forEach(recipe => {
    let count = recipe.outputCount || 1;
    event.custom({
      type: "thermal:smelter",
      ingredients: recipe.inputs,
      result: [
        {
          item: recipe.output,
          count: count
        }
      ],
      energy: recipe.energy
    });
  });

  ['enderium', 'lumium', 'signalum'].forEach(alloy => {
    event.remove({ id: `thermal:fire_charge/${alloy}_glass_2` });
    event.remove({ id: `thermal:machines/smelter/smelter_glass_${alloy}` });
    event.recipes.thermal.smelter(Item.of(`thermal:${alloy}_glass`, 2), [Item.of('thermal:obsidian_glass', 2), `thermal:${alloy}_ingot`]).energy(4800);
    event.shapeless(`thermal:${alloy}_glass`, [Item.of('thermal:obsidian_glass', 2), 'minecraft:fire_charge', `thermal:${alloy}_ingot`]);
  });

  event.replaceInput({ id: 'thermal:machine_smelter' }, 'thermal:rf_coil', 'kubejs:cube1');
  event.replaceInput({ id: 'thermal:augments/machine_speed_augment' }, 'thermal:rf_coil', 'kubejs:cube2');
  event.replaceInput({ id: 'thermal:machine_crucible' }, 'thermal:rf_coil', 'kubejs:cube2');
  event.replaceInput({ id: 'thermal:machine_chiller' }, 'thermal:rf_coil', 'kubejs:cube2');

  event.remove({ id: 'thermal:machine_frame' });
  event.shaped('thermal:machine_frame', [
    'ICI',
    'CGC',
    'ICI'
  ], {
    I: 'minecraft:iron_ingot',
    C: 'minecraft:cobblestone',
    G: '#forge:gears/tin'
  });

  event.remove({ id: 'thermal:machine_pulverizer' });
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

  event.remove({ id: 'thermal:augments/upgrade_augment_2' });
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

  event.remove({ id: 'thermal:augments/upgrade_augment_3' });
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

  event.replaceInput({ id: 'thermal:augments/upgrade_augment_1' }, '#forge:gears/gold', 'kubejs:cube1');
  event.replaceInput({ id: 'thermal:machine_pyrolyzer' }, 'minecraft:blaze_rod', '#forge:ingots/steel');

  event.remove({ id: 'thermal:machines/pyrolyzer/pyrolyzer_coal' });
  event.remove({ id: 'thermal:gunpowder_4' });

  event.remove({ id: 'thermal:press_gear_die' });
  event.shaped('thermal:press_gear_die', [
    ' I ',
    'IGI',
    ' I '
  ], {
    I: '#forge:plates/invar',
    G: '#forge:gears/gold'
  });

  event.recipes.thermal.crucible(Fluid.of('kubejs:molten_glass', 1000), 'minecraft:sand').energy(8000);
  event.recipes.thermal.chiller(Item.of('minecraft:glass', 1), Fluid.of('kubejs:molten_glass', 1000)).energy(8000);


  ['enderium', 'lumium', 'signalum'].forEach(material => {
    event.remove({ id: `thermal:${material}_dust_4` });
  });

  event.remove({ id: 'thermal:earth_charge/ender_pearl_dust_from_ender_pearl' });

  ['andesite', 'granite', 'diorite'].forEach(stone => {
    event.remove({ id: 'thermal:machines/pulverizer/pulverizer_' + stone });
  });

  var recipes = [
    { input: 'quark:golden_carrot_crate', energy: 380400 },
    { input: 'quark:golden_apple_crate', energy: 512000 },
    { input: 'farmersdelight:cabbage_crate', energy: 32000 }
  ];

  recipes.forEach(recipe => {
    event.recipes.thermal.gourmand_fuel(recipe.input).energy(recipe.energy);
  });
});
