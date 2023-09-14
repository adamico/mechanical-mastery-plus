// priority: 0

ServerEvents.recipes(event => {
	let all_metals = ['iron', 'gold', 'osmium', 'copper', 'tin', 'lead', 'uranium', 'aluminum', 'silver', 'nickel', 'zinc'];
	let vanilla_metals = ['iron', 'copper', 'gold'];
	let mekanism_metals = ['iron', 'gold', 'osmium', 'copper', 'tin', 'lead', 'uranium'];
	let thermal_metals = ['tin', 'lead', 'silver', 'nickel'];
	let create_metals = ['iron', 'gold', 'copper', 'zinc', 'osmium', 'silver', 'tin', 'lead', 'aluminum', 'uranium', 'nickel'];
	let jaopca_metals = ['aluminum', 'silver', 'nickel', 'zinc']
  
  let process_metals = (metals) => {
    metals.forEach(metal => {
      event.recipes.createCrushing([Item.of(`create:crushed_raw_${metal}`), Item.of(`create:crushed_raw_${metal}`).withChance(0.3)], Item.of(`#mekanism:clumps/${metal}`));
      event.recipes.mekanismCrushing(Item.of(`#mekanism:dirty_dusts/${metal}`, 2), `create:crushed_raw_${metal}`);
      event.recipes.thermal.pulverizer(Item.of(`#forge:dusts/${metal}`).withChance(1.2), Item.of(`#mekanism:dirty_dusts/${metal}`)).energy(4000);
		});
  };
  
  process_metals(all_metals);
  event.remove([{input: '#mekanism:clumps'}, {output: '#mekanism:dirty_dusts'}]);
  
	create_metals.forEach((metal) => {
		event.remove({id: `create:splashing/crushed_${metal}_ore`});
		event.remove({id: `create:splashing/mekanism/crushed_${metal}_ore`});
		event.remove({id: `create:splashing/thermal/crushed_${metal}_ore`});
		event.remove({id: `create:splashing/immersiveengineering/crushed_${metal}_ore`});
	});
	
	event.recipes.thermal.pulverizer([Item.of('#forge:dusts/aluminum').withChance(1.25), Item.of('mekanism:dust_iron').withChance(0.1)], '#forge:raw_materials/aluminum').energy(4000);
	event.recipes.mekanismEnriching(Item.of('thermal:silver_dust', 4), Item.of('thermal:raw_silver', 3));

	event.remove({id: 'mekanism:processing/bronze/ingot/from_dust_blasting'});
	event.remove({id: 'mekanism:processing/bronze/ingot/from_dust_smelting'});
});