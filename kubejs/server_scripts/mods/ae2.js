ServerEvents.recipes(event => {
  const all_metals = global.all_metals;

  event.recipes.createSequencedAssembly([
    'ae2:silicon_press'
  ], 'minecraft:iron_block', [
    event.recipes.createDeploying('kubejs:incomplete_press', ['kubejs:incomplete_press', '#forge:silicon']),
    event.recipes.createPressing('kubejs:incomplete_press', 'kubejs:incomplete_press')
  ]).transitionalItem('kubejs:incomplete_press').loops(10);
 
	event.shaped('ae2:sky_stone_block', [
		'AAA',
		'ABA',
		'AAA'
		], {
			A: 'minecraft:polished_deepslate',
			B: 'kubejs:cube3'
	});
 
  event.recipes.createSequencedAssembly([
    'ae2:logic_processor_press'
  ], 'minecraft:iron_block', [
    event.recipes.createDeploying('kubejs:incomplete_press', ['kubejs:incomplete_press', 'minecraft:gold_ingot']),
    event.recipes.createPressing('kubejs:incomplete_press', 'kubejs:incomplete_press')
  ]).transitionalItem('kubejs:incomplete_press').loops(10);
  
  event.recipes.createSequencedAssembly([
    'ae2:engineering_processor_press'
  ], 'minecraft:iron_block', [
    event.recipes.createDeploying('kubejs:incomplete_press', ['kubejs:incomplete_press', 'minecraft:diamond']),
    event.recipes.createPressing('kubejs:incomplete_press', 'kubejs:incomplete_press')
  ]).transitionalItem('kubejs:incomplete_press').loops(10);
  
  event.recipes.createSequencedAssembly([
    'ae2:calculation_processor_press'
  ], 'minecraft:iron_block', [
    event.recipes.createDeploying('kubejs:incomplete_press', ['kubejs:incomplete_press', 'ae2:certus_quartz_crystal']),
    event.recipes.createPressing('kubejs:incomplete_press', 'kubejs:incomplete_press')
  ]).transitionalItem('kubejs:incomplete_press').loops(10);

  event.recipes.createSplashing([Item.of('ae2:certus_quartz_crystal').withChance(0.75), Item.of('ae2:charged_certus_quartz_crystal').withChance(0.25)], 'ae2:sky_dust')
	event.remove({id: 'create:compat/ae2/milling/sky_stone_block'});
	event.recipes.createMilling(['ae2:sky_dust', 'ae2:sky_stone_block'], 'ae2:sky_stone_block').processingTime(200);
  	
	event.replaceInput({input: 'ae2:silicon'}, 'ae2:silicon', '#forge/silicon');
	event.replaceInput({id: 'ae2:network/blocks/inscribers'}, 'minecraft:sticky_piston', 'minecraft:piston');
	event.replaceInput({output: 'ae2:vibration_chamber'}, "ae2:energy_acceptor", "kubejs:cube1_packaged");

  ['iron', 'copper', 'gold', 'tin', 'nickel'].forEach(metal => {
    let result = `thermal:${metal}_dust`;
    event.custom({
      type: 'ae2:inscriber',
      ingredients: {
        middle: {
          tag: `forge:raw_materials/${metal}`
        }
      },
      mode: 'inscribe',
      result: {
        item: result,
      count: 2
      }
    })
  });
});
