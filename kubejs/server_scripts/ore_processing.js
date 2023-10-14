ServerEvents.recipes(event => {
	const all_metals = global.all_metals;
	let create_metals = ['iron', 'gold', 'copper', 'zinc', 'osmium', 'silver', 'tin', 'lead', 'uranium', 'nickel'];
  
  let process_metals = (metals) => {
    metals.forEach(metal => {
			event.remove({id: `create:crushing/raw_${metal}`});
			event.remove({id: `create:crushing/raw_${metal}_block`});
			event.recipes.createCrushing([Item.of(`create:crushed_raw_${metal}`), Item.of(`create:crushed_raw_${metal}`).withChance(0.5)], Item.of(`#forge:raw_materials/${metal}`)).processingTime(200);
     	event.recipes.thermal.pulverizer(Item.of(`#forge:dusts/${metal}`).withChance(1.2), Item.of(`#mekanism:dirty_dusts/${metal}`)).energy(4000);
		});
  };
  
  process_metals(all_metals);

	create_metals.forEach((metal) => {
		event.remove({id: `create:splashing/crushed_${metal}_ore`});
		event.remove({id: `create:splashing/mekanism/crushed_${metal}_ore`});
		event.remove({id: `create:splashing/thermal/crushed_${metal}_ore`});
	});
	
	event.recipes.thermal.pulverizer([Item.of('#forge:dusts/iron').withChance(1.6), Item.of('#forge:dusts/nickel').withChance(0.2)], 'create:crushed_raw_iron').energy(4000);

});