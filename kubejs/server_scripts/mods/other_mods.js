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
		'ABA',
		'CDC',
		'ABA'
		], {
			A: 'minecraft:terracotta',
			B: 'kubejs:cube4',
			C: 'mekanism:ultimate_fluid_tank',
			D: 'projecte:evertide_amulet'
		}
	);

	event.replaceInput({id: 'mob_grinding_utils:recipe_fan'}, '#forge:ingots/iron', '#forge:plates/iron');	
	event.replaceInput({id: 'mob_grinding_utils:recipe_spikes'}, 'minecraft:iron_sword', '#forge:rods/iron');
	event.replaceInput({id: 'pipez:gas_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:energy_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:fluid_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:item_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');

	event.replaceInput({output: 'angelring:angel_ring'}, 'angelring:diamond_ring', 'kubejs:cube4');

	event.replaceInput({output: 'hyperbox:hyperbox'}, 'minecraft:crying_obsidian', 'kubejs:cube3');

	event.remove({output: 'farmingforblockheads:market'});
	event.shaped('farmingforblockheads:market', [
		'PWP',
		'LCL',
		'LLL'
		], {
			P: 'minecraft:planks',
			L: 'minecraft:oak_log',
			C: 'kubejs:cube3',
			W: 'minecraft:red_wool'
		}
	);	
});