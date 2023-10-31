ServerEvents.recipes(event => {
	event.custom({
		type: "powah:energizing",
		ingredients: [
		  {
			item: "minecraft:iron_block"
		  },
		  {
			item: "minecraft:gold_block"
		  }
		],
		energy: 90000,
		result: {
		  item: "powah:energized_steel_block",
		  count: 2
		}
	});

	event.custom({
		type: "powah:energizing",
		ingredients: [
		  {
			item: "minecraft:diamond_block"
		  }
		],
		energy: 2700000,
		result: {
		  item: "powah:niotic_crystal_block",
		  count: 1
		}
	});

	event.custom({
		type: "powah:energizing",
		ingredients: [
		  {
			item: "minecraft:emerald_block"
		  }
		],
		energy: 9000000,
		result: {
		  item: "powah:spirited_crystal_block",
		  count: 1
		}
	});
  event.replaceInput({output: 'powah:dielectric_paste'}, '#minecraft:coals', 'thermal:tar');
	event.replaceInput({id: 'powah:crafting/dielectric_rod'}, 'minecraft:iron_bars', 'minecraft:iron_nugget');
	event.replaceInput({id: 'powah:crafting/dielectric_rod_h'}, 'minecraft:iron_bars', 'minecraft:iron_nugget');
});