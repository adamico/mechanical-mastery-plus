ServerEvents.recipes(event => {
  event.remove({id: 'clickmachine:auto_clicker'});
	event.shaped('clickmachine:auto_clicker', [
		'DDD',
		'DKD',
		'DRD'
	], {
		D: 'minecraft:diorite',
		K: 'kubejs:cube1',
		R: '#forge:storage_blocks/redstone'
	});

	event.remove({id: 'trashcans:item_trash_can'});
	event.shapeless('trashcans:item_trash_can', ['thermal:device_nullifier', '#forge:chests']);
	event.remove({id: 'trashcans:liquid_trash_can'});
	event.shapeless('trashcans:liquid_trash_can', ['thermal:device_nullifier', 'minecraft:bucket']);
	event.remove({id: 'trashcans:energy_trash_can'});
	event.shaped('trashcans:energy_trash_can', [
		'ABA',
		'CDC',
		'ABA'
		], {
			A: 'mekanism:induction_casing',
			B: 'mekanism:ultimate_induction_cell',
			C: 'mekanism:ultimate_induction_provider',
			D: 'mekanism:induction_port'
	});

	event.remove({id: 'cookingforblockheads:sink'});
	event.shaped('cookingforblockheads:sink', [
		'III',
		'CWC',
		'AAA'
		], {
			A: 'minecraft:terracotta',
			C: 'kubejs:cube4',
			W: 'sfm:water_tank',
			I: 'minecraft:iron_ingot'
		}
	);

	event.replaceInput({id: 'mob_grinding_utils:recipe_fan'}, '#forge:ingots/iron', '#forge:plates/iron');	
	event.replaceInput({id: 'mob_grinding_utils:recipe_spikes'}, 'minecraft:iron_sword', '#forge:rods/iron');
	event.replaceInput({id: 'pipez:gas_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:energy_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:fluid_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:item_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');

	event.replaceInput({output: 'hyperbox:hyperbox'}, 'minecraft:crying_obsidian', 'kubejs:cube3');

	event.replaceInput({id: 'functionalstorage:linking_tool'}, 'minecraft:diamond', 'minecraft:light_blue_dye');
	event.replaceInput({id: 'functionalstorage:configuration_tool'}, 'minecraft:emerald', 'minecraft:lime_dye');

	event.remove({id: 'gobber2:gobber2_ring_void'});
	event.remove({id: 'gobber2:gobber2_ring_vision'});
	event.remove({id: 'gobber2:gobber2_ring_sunshine'});
	event.remove({id: 'gobber2:gobber2_ring_explorer'});
	event.remove({id: 'gobber2:gobber2_medallion_hero'});
	event.remove({id: 'gobber2:gobber2_medallion_dolphin'});
	
	
	
});