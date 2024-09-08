ServerEvents.recipes(event => {
  event.replaceInput({id: 'integrateddynamics:crafting/squeezer'}, 'minecraft:iron_block', 'minecraft:iron_trapdoor');

	['squeezer', 'mechanical_squeezer'].forEach(recipe => {
		event.remove({id: `integrateddynamics:${recipe}/convenience/minecraft_dye_white_bone`});
		
		event.custom({
			type: `integrateddynamics:${recipe}`,
			item: {
				item: 'kubejs:cube4'
			},
			result: {
				items: [
					{
						item: {
							item: 'minecraft:iron_nugget',
							count: 2
						},
						chance: 0.75
					},
					{
						item: {
							item: 'minecraft:gold_nugget',
							count: 1
						},
						chance: 0.75
					},
					{
						'item': {
							item: 'create:copper_nugget',
							count: 4
						},
						chance: 0.75
					}
				]	
			},
			duration: 10
		});

		event.custom({
			type: `integrateddynamics:${recipe}`,
			item: {
				item: 'minecraft:bone'
			},
			result: {
				items: [
					{
						item: {
							item: 'minecraft:bone_meal',
							count: 3
						}
					},
					{
						item: {
							item: 'minecraft:bone_meal',
							count: 1
						},
						chance: 0.5
					},
					{
						'item': {
							item: 'minecraft:white_dye',
							count: 2
						},
						chance: 0.5
					}
				]	
			},
			duration: 10
		});

		event.remove({id: `integrateddynamics:${recipe}/ore/flint`});
		event.custom({
			type: `integrateddynamics:${recipe}`,
			item: 'minecraft:gravel',
			result: {
				items: [
					{
						item: 'minecraft:sand'
					},
					{
						item: 'minecraft:flint',
						chance: 0.5
					}
				]
			},
			duration: 30
		});

		event.custom({
			type: `integrateddynamics:${recipe}`,
			item: {
				tag: 'minecraft:coals'
			},
			result: {
				items: [
					{
						'item': {
							item: 'minecraft:black_dye',
							count: 2
						}
					}
				]	
			},
			duration: 10
		});
	});
})