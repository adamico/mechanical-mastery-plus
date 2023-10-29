ServerEvents.recipes(event => {
	const all_metals = [
		{name: 'iron', mod: 'minecraft', extraDust: Item.of('#forge:dusts/nickel').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_iron').withChance(0.3)},
		{name: 'gold', mod: 'minecraft', extraDust: Item.of('#forge:dusts/copper').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_gold').withChance(0.3)},
		{name: 'copper', mod: 'minecraft', extraDust: Item.of('#forge:dusts/gold').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_copper').withChance(0.3)},
		{name: 'zinc', mod: 'create', extraDust: Item.of('#forge:dusts/tin').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_zinc').withChance(0.3)},
		{name: 'osmium', mod: 'mekanism', extraDust: Item.of('#forge:dusts/silver').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_osmium').withChance(0.3)},
		{name: 'silver', mod: 'thermal', extraDust: Item.of('#forge:dusts/osmium').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_silver').withChance(0.3)},
		{name: 'tin', mod: 'thermal', extraDust: Item.of('#forge:dusts/zinc').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_tin').withChance(0.3)},
		{name: 'lead', mod: 'thermal', extraDust: Item.of('#forge:dusts/uranium').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_lead').withChance(0.3)},
		{name: 'uranium', mod: 'mekanism', extraDust: Item.of('#forge:dusts/lead').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_uranium').withChance(0.3)},
		{name: 'nickel', mod: 'thermal', extraDust: Item.of('#forge:dusts/iron').withChance(0.05), extraCrushed: Item.of('create:crushed_raw_nickel').withChance(0.3)}
	];

  let process_metals = (metals) => {
    metals.forEach(metal => {
			[{name: 'smelting', time: 200}, {name: 'blasting', time: 100}].forEach((recipe) => {
				event[recipe.name](Item.of(`#forge:nuggets/${metal.name}`, 6), Item.of(`#forge:raw_materials/${metal.name}`)).xp(0.6).cookingTime(recipe.time);
			});
			event.recipes.createMilling([Item.of(`create:crushed_raw_${metal.name}`), metal.extraCrushed], Item.of(`#forge:raw_materials/${metal.name}`)).processingTime(200);			
			event.recipes.createCrushing([Item.of(`create:crushed_raw_${metal.name}`), metal.extraCrushed], Item.of(`#forge:raw_materials/${metal.name}`)).processingTime(100);
			event.recipes.createMilling([Item.of(`create:crushed_raw_${metal.name}`), metal.extraCrushed], Item.of(`#mekanism:clumps/${metal.name}`)).processingTime(200);			
			event.recipes.createCrushing([Item.of(`create:crushed_raw_${metal.name}`), metal.extraCrushed], Item.of(`#mekanism:clumps/${metal.name}`)).processingTime(100);	
			event.recipes.thermal.pulverizer([Item.of(`#forge:dusts/${metal.name}`).withChance(1.2), metal.extraDust], Item.of(`#mekanism:dirty_dusts/${metal.name}`)).energy(3000);
			event.recipes.thermal.furnace(Item.of(`#forge:ingots/${metal.name}`), `#forge:dusts/${metal.name}`).xp(0.2);
			event.recipes.createSplashing([Item.of(`#forge:nuggets/${metal.name}`, 9), Item.of(`#forge:nuggets/${metal.name}`, 2).withChance(0.1)], `create:crushed_raw_${metal.name}`);
		});
  };

	['raw_materials', 'storage_blocks'].forEach(inputType => {		
		['create:crushing', 'blasting', 'quark:tweaks/smelting', 'thermal:smelter'].forEach(recipeType => {
			event.remove({type: recipeType, input: '#forge:'+ inputType});
		});
	});
	
	['ingots', 'storage_blocks'].forEach(type => {
		event.remove({type: 'smelting', output: '#forge:' + type});
		event.remove({type: 'thermal:smelter', output: '#forge:' + type, not: [{output: '#forge:alloys'}, {output: '#forge:alloys/special'}]});
	});

	['enderium', 'lumium', 'signalum'].forEach(alloy => {
		event.remove({input: '#forge:dusts/' + alloy, output: '#forge:ingots/' + alloy});
	});

	event.remove({type: 'create:splashing', input: '#create:crushed_raw_materials'})
	event.remove({type: 'mekanism:crushing', output: '#forge:dusts'});

	process_metals(all_metals);
	
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
      count: 1
      }
    })
	});

	['iron', 'copper', 'gold'].forEach(metal => {
		event.custom({
			"type": "lychee:block_interacting",
			"item_in": [
				{
					"item": "kubejs:ore_hammer"
				},
				{
					"item": "raw_" + metal
				}
			],
			"block_in": "stone",
			"post": [
				{
					"type": "drop_item",
					"item": "thermal:" + metal + "_dust",
				},
				{
					"type": "damage_item"
				},
				{
					"type": "add_item_cooldown",
					"s": 3
				}
			]
		});
  });

	event.shaped('kubejs:ore_hammer', [
		' Sb',
		' rS',
		'r  '
	], {
		r: '#forge:rods/wooden',
		S: '#forge:stone',
		b: 'minecraft:oak_button'
	});

	event.replaceInput({id: 'gobber2:gobber2_ingot'}, 'minecraft:diamond', 'kubejs:cube2');
	event.replaceInput({id: 'gobber2:gobber2_ingot_nether'}, 'minecraft:netherite_scrap', 'kubejs:cube3');
	event.replaceInput({id: 'gobber2:gobber2_ingot_end'}, 'minecraft:chorus_flower', 'kubejs:cube4');
});