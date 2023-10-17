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
			console.log('Processing metal: ' + metal.name);
			[{name: 'smelting', time: 200}, {name: 'blasting', time: 100}].forEach((recipe) => {
				event.remove({id: `${metal.mod}:${metal.name}_ingot_from_${recipe.name}_raw_${metal.name}`});
				event[recipe.name](Item.of(`#forge:nuggets/${metal.name}`, 6), Item.of(`#forge:raw_materials/${metal.name}`)).xp(0.6).cookingTime(recipe.time);
			});

			event.remove({id: `create:crushing/raw_${metal.name}`});
			event.remove({id: `create:crushing/raw_${metal.name}_block`});
			event.recipes.createMilling([Item.of(`create:crushed_raw_${metal.name}`), metal.extraCrushed], Item.of(`#forge:raw_materials/${metal.name}`)).processingTime(200)			
			event.recipes.createCrushing([Item.of(`create:crushed_raw_${metal.name}`), metal.extraCrushed], Item.of(`#forge:raw_materials/${metal.name}`)).processingTime(100);
     	event.recipes.thermal.pulverizer([Item.of(`#forge:dusts/${metal.name}`).withChance(1.2), metal.extraDust], Item.of(`#mekanism:dirty_dusts/${metal.name}`)).energy(3000);
			event.recipes.thermal.pulverizer([Item.of(`#forge:dusts/${metal.name}`).withChance(1.2), metal.extraDust], Item.of(`create:crushed_raw_${metal.name}`)).energy(3000);
		
			event.remove({id: `create:splashing/${metal.mod}/crushed_raw_${metal.ore}`});
			event.recipes.createSplashing([Item.of(`#forge:nuggets/${metal.ore}`, 9), Item.of(`#forge:dusts/${metal.ore}`).withChance(0.5)], `create:crushed_raw_${metal.ore}`)
			
			event.remove({id: `mekanism:processing/${metal.name}/dust/from_raw_ore`});
			event.remove({id: `mekanism:processing/${metal.name}/dust/from_raw_block`});
			event.remove({id: "jaopca:mekanism.raw_material_to_dust." + metal.name});
			event.remove({id: "jaopca:mekanism.raw_storage_block_to_dust." + metal.name});
		});
  };
	 
  process_metals(all_metals);

	[
		{input: 'iron', output: 'redstone'}, 
		{input: 'gold', output: 'quartz'}, 
		{input: 'copper', output: 'clay_ball'}, 
		{input: 'zinc', output: 'gunpowder'}
	].forEach(metal => {
		event.replaceOutput({id: `create:splashing/crushed_raw_${metal.input}`}, `minecraft:${metal.output}`, `#forge:dusts/${metal.input}`)
	});
});