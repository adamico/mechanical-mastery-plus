ServerEvents.recipes(event => {
	const all_metals = [
		{ name: 'iron',    ingot: 'iron_ingot',    mod: 'minecraft', input_tag: "#minecraft:iron_ores" },
		{ name: 'gold',    ingot: 'gold_ingot',    mod: 'minecraft', input_tag: "#minecraft:gold_ores" },
		{ name: 'copper',  ingot: 'copper_ingot',  mod: 'minecraft', input_tag: "#minecraft:copper_ores" },
		{ name: 'zinc',    ingot: 'zinc_ingot',    mod: 'railcraft', input_tag: "#c:ores/zinc" },
		{ name: 'osmium',  ingot: 'ingot_osmium',  mod: 'mekanism',  input_tag: "#c:ores/osmium" },
		{ name: 'silver',  ingot: 'silver_ingot',  mod: 'occultism', input_tag: "#c:ores/silver" },
		{ name: 'tin',     ingot: 'ingot_tin',     mod: 'mekanism',  input_tag: "#c:ores/tin" },
		{ name: 'lead',    ingot: 'ingot_lead',    mod: 'mekanism',  input_tag: "#c:ores/lead" },
		{ name: 'uranium', ingot: 'ingot_uranium', mod: 'mekanism',  input_tag: "#c:ores/uranium" },
		{ name: 'nickel',  ingot: 'nickel_ingot',  mod: 'modern_industrialization', input_tag: "#c:ores/nickel" }
	];

  let process_metals = (metals) => {
    metals.forEach(metal => {
      console.log("Processing " + metal.name + " metal");
			['smelting', 'blasting'].forEach(recipe => {
				event.remove({ type: `minecraft:${recipe}`, input: `${metal.mod}:${metal.name}_ore`});
			});
      ['', 'mechanical_'].forEach(recipe => {
				event.remove({ type: `integrateddynamics:${recipe}squeezer`, output: `#c:raw_materials/${metal.name}`});
			});
			event.remove({ input: metal.input_tag });
			event.remove({ input: `${metal.mod}:${metal.name}_ore` });
		});
  };


// 	['raw_materials', 'storage_blocks'].forEach(inputType => {		
// 		['create:crushing', 'blasting', 'quark:tweaks/smelting', 'thermal:smelter'].forEach(recipeType => {
// 			event.remove({type: recipeType, input: '#forge:'+ inputType});
// 		});
// 	});
	
// 	['ingots', 'storage_blocks'].forEach(type => {
// 		event.remove({type: 'smelting', output: '#forge:' + type, not: [{input: 'minecraft:clay_ball'}, {input: 'minecraft:netherrack'}]});
// 		event.remove({type: 'thermal:smelter', output: '#forge:' + type, not: [{output: '#forge:alloys'}, {output: '#forge:alloys/special'}]});
// 	});

// 	['iron', 'copper', 'gold'].forEach(metal => {
// 		event.remove({type: 'blasting', output: `${metal}_block`});
// 	});
	
// 	['enderium', 'lumium', 'signalum'].forEach(alloy => {
// 		event.remove({input: '#forge:dusts/' + alloy, output: '#forge:ingots/' + alloy});
// 	});

// 	event.remove({type: 'create:splashing', input: '#create:crushed_raw_materials'})
// 	event.remove({type: 'mekanism:crushing', output: '#forge:dusts'});

process_metals(all_metals);
	
//   ['iron', 'copper', 'gold', 'tin', 'nickel'].forEach(metal => {
//     let result = `thermal:${metal}_dust`;
//     event.custom({
//       type: 'ae2:inscriber',
//       ingredients: {
//         middle: {
//           tag: `forge:raw_materials/${metal}`
//         }
//       },
//       mode: 'inscribe',
//       result: {
//         item: result,
//       count: 1
//       }
//     })
// 	});

// 	['iron', 'copper', 'gold'].forEach(metal => {
// 		event.custom({
// 			"type": "lychee:block_interacting",
// 			"item_in": [
// 				{
// 					"item": "kubejs:ore_hammer"
// 				},
// 				{
// 					"item": "raw_" + metal
// 				}
// 			],
// 			"block_in": "stone",
// 			"post": [
// 				{
// 					"type": "drop_item",
// 					"item": "thermal:" + metal + "_dust",
// 				},
// 				{
// 					"type": "damage_item"
// 				},
// 				{
// 					"type": "add_item_cooldown",
// 					"s": 3
// 				}
// 			]
// 		});
//   });

// 	event.shaped('kubejs:ore_hammer', [
// 		' Sb',
// 		' rS',
// 		'r  '
// 	], {
// 		r: '#forge:rods/wooden',
// 		S: '#forge:stone',
// 		b: 'minecraft:oak_button'
// 	});

// 	event.replaceInput({id: 'gobber2:gobber2_ingot'}, 'minecraft:diamond', 'kubejs:cube2');
// 	event.replaceInput({id: 'gobber2:gobber2_ingot_nether'}, 'minecraft:netherite_scrap', 'kubejs:cube3');
// 	event.replaceInput({id: 'gobber2:gobber2_ingot_end'}, 'minecraft:chorus_flower', 'kubejs:cube4');
});