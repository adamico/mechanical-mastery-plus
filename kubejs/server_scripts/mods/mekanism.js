ServerEvents.recipes(event => {
  event.remove({id: 'mekanism:configurator'});
	event.shaped('mekanism:configurator', [
		' I ',
		' G '
		], {
			I: '#forge:ingots/iron',
			G: '#forge:rods/steel'
	});
  
	event.shaped(Item.of('mekanism:creative_energy_cube', '{mekData:{EnergyContainers:[{Container:0b,stored:"18446744073709551615.9999"}]}}'), [
		' A ',
		'DBC',
		' E '
		], {
			A: 'projectexpansion:final_star',
			B: Item.of('mekanism:ultimate_energy_cube', '{mekData:{EnergyContainers:[{Container:0b,stored:"256000000"}]}}'),
			C: 'powah:ender_gate_nitro',
			D: 'powah:ender_cell_nitro',
			E: 'kubejs:enriched_black_essence'
		}
	);
	
	event.shaped('mekanism:creative_fluid_tank', [
		' A ',
		'DBD',
		' E '
		], {
			A: 'projectexpansion:final_star',
			B: 'mekanism:ultimate_fluid_tank',
			D: 'industrialforegoing:ether_gas_bucket',
			E: 'kubejs:enriched_black_essence'
		}
	);
	
	event.shaped('mekanism:creative_chemical_tank', [
		'DAD',
		'DBD',
		'DAD'
		], {
			A: 'projectexpansion:final_star_shard',
			B: 'mekanism:ultimate_chemical_tank',
			D: 'mekanism:pellet_antimatter',
		}
	);

  event.shapeless('mekanism:creative_fluid_tank','mekanism:creative_fluid_tank');
	event.shapeless('mekanism:creative_chemical_tank','mekanism:creative_chemical_tank');

  event.replaceInput({id: 'mekanism:transmitter/logistical_transporter/basic'}, 'mekanism:basic_control_circuit', 'prettypipes:pipe');
	event.replaceInput({id: 'mekanism:solar_neutron_activator'}, 'mekanism:hdpe_sheet', 'kubejs:black_hdpe_sheet');
	event.replaceInput({id: 'mekanism:sps_casing'}, 'mekanism:hdpe_sheet', 'kubejs:black_hdpe_sheet');
});