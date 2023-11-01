ServerEvents.recipes(event => {
  event.shaped('kubejs:incomplete_simple_frame', [
    'PMP',
    'N N',
    'IGI'
  ], {
    P: '#forge:plastic',
    M: 'industrialforegoing:machine_frame_pity',
    N: '#forge:ingots/nether_brick',
    I: '#forge:ingots/iron',
    G: '#forge:gears/gold'
  });
  
  event.shaped('kubejs:incomplete_advanced_frame', [
    'PMP',
    'N N',
    'IGI'
  ], {
    P: '#forge:plastic',
    M: 'industrialforegoing:machine_frame_simple',
    N: 'minecraft:netherite_scrap',
    I: '#forge:ingots/gold',
    G: '#forge:gears/diamond'
  });
  
  event.shaped('kubejs:incomplete_supreme_frame', [
    'PMP',
    'N N',
    'IGI'
  ], {
    P: '#forge:plastic',
    M: 'industrialforegoing:machine_frame_advanced',
    N: '#forge:ingots/netherite',
    I: '#forge:gems/diamond',
    G: '#forge:gears/diamond'
  });
  
  event.shapeless('kubejs:incomplete_speed_1', ['#forge:dusts/redstone', '#forge:dusts/redstone', 
  '#forge:glass_panes', '#forge:glass_panes', '#forge:gears/gold', '#forge:gears/gold', 
  'minecraft:sugar', 'minecraft:sugar']);
  
  event.shapeless('kubejs:incomplete_speed_2', ['#forge:dusts/redstone', '#forge:dusts/redstone', 
  '#forge:glass_panes', '#forge:glass_panes', '#forge:gears/diamond', '#forge:gears/diamond', 
  'minecraft:sugar', 'minecraft:sugar']);
  
  event.shapeless('kubejs:incomplete_eff_1', ['#forge:dusts/redstone', '#forge:dusts/redstone', 
  '#forge:glass_panes', '#forge:glass_panes', '#forge:gears/gold', '#forge:gears/gold', 
  '#forge:rods/blaze', '#forge:rods/blaze']);
  
  event.shapeless('kubejs:incomplete_eff_2', ['#forge:dusts/redstone', '#forge:dusts/redstone', 
  '#forge:glass_panes', '#forge:glass_panes', '#forge:gears/diamond', '#forge:gears/diamond', 
  '#forge:rods/blaze', '#forge:rods/blaze']);
  
  event.shapeless('kubejs:incomplete_proc_1', ['#forge:dusts/redstone', '#forge:dusts/redstone', 
  '#forge:glass_panes', '#forge:glass_panes', '#forge:gears/gold', '#forge:gears/gold', 
  'minecraft:furnace', 'minecraft:crafting_table']);
  
  event.shapeless('kubejs:incomplete_proc_2', ['#forge:dusts/redstone', '#forge:dusts/redstone', 
  '#forge:glass_panes', '#forge:glass_panes', '#forge:gears/diamond', '#forge:gears/diamond', 
  'minecraft:furnace', 'minecraft:crafting_table']);
  
  event.shapeless('kubejs:incomplete_range_2', ['#forge:dusts/redstone', '#forge:dusts/redstone', 
  '#forge:glass_panes', '#forge:glass_panes', '#forge:gems/lapis', '#forge:gems/lapis', 
  '#forge:gems/lapis', '#forge:gems/lapis']);
  
  
  var data = {
    recipes: [
      { input: [Fluid.of('industrialforegoing:latex', 250), 'kubejs:incomplete_simple_frame'], output: 'industrialforegoing:machine_frame_simple', energy: 300 },
      { input: [Fluid.of('industrialforegoing:pink_slime', 500), 'kubejs:incomplete_advanced_frame'], output: 'industrialforegoing:machine_frame_advanced', energy: 300 },
      { input: [Fluid.of('industrialforegoing:ether_gas', 135), 'kubejs:incomplete_supreme_frame'], output: 'industrialforegoing:machine_frame_supreme', energy: 300 },
      { input: [Fluid.of('industrialforegoing:latex', 1000), 'kubejs:incomplete_speed_1'], output: Item.of('industrialforegoing:speed_addon_1', '{TitaniumAugment:{Speed:2.0f}}'), energy: 300 },
      { input: [Fluid.of('industrialforegoing:latex', 1000), 'kubejs:incomplete_speed_2'], output: Item.of('industrialforegoing:speed_addon_2', '{TitaniumAugment:{Speed:3.0f}}'), energy: 300 },
      { input: [Fluid.of('industrialforegoing:latex', 1000), 'kubejs:incomplete_eff_1'], output: Item.of('industrialforegoing:efficiency_addon_1', '{TitaniumAugment:{Efficiency:0.9f}}'), energy: 300 },
      { input: [Fluid.of('industrialforegoing:latex', 1000), 'kubejs:incomplete_eff_2'], output: Item.of('industrialforegoing:efficiency_addon_2', '{TitaniumAugment:{Efficiency:0.8f}}'), energy: 300 },
      { input: [Fluid.of('industrialforegoing:latex', 1000), 'kubejs:incomplete_proc_1'], output: Item.of('industrialforegoing:processing_addon_1', '{TitaniumAugment:{Processing:2.0f}}'), energy: 300 },
      { input: [Fluid.of('industrialforegoing:latex', 1000), 'kubejs:incomplete_proc_2'], output: Item.of('industrialforegoing:processing_addon_2', '{TitaniumAugment:{Processing:3.0f}}'), energy: 300 },
      { input: [Fluid.of('industrialforegoing:latex', 1000), 'kubejs:incomplete_range_2'], output: Item.of('industrialforegoing:range_addon1', '{TitaniumAugment:{Range:1.0f}}'), energy: 300 }
    ]
  };
  data.recipes.forEach((recipe) => {
    event.recipes.thermal.bottler(recipe.output, recipe.input).energy(recipe.energy);
  });

  event.custom({
    type: "industrialforegoing:stonework_generate",
    lavaConsume: 200,
    lavaNeed: 1000,
    output: {
      count: 1,
      item: "minecraft:stone"
    },
    waterConsume: 200,
    waterNeed: 1000
  });

  event.replaceInput({id: 'industrialforegoing:mob_imprisonment_tool'}, 'minecraft:ghast_tear', 'minecraft:nether_star');
	event.replaceInput({id: 'industrialforegoing:stasis_chamber'}, 'minecraft:ghast_tear', 'minecraft:nether_star');

  event.remove({id: 'industrialforegoing:ore_laser_base'});
});
