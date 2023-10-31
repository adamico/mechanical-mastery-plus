ServerEvents.recipes(event => {
  event.remove({id: 'mekanism:configurator'});
	event.shaped('mekanism:configurator', [
		' I ',
		' G '
		], {
			I: '#forge:ingots/iron',
			G: '#forge:rods/steel'
	});

	event.replaceInput({id: 'mekanism:transmitter/universal_cable/basic'}, 'minecraft:redstone', 'kubejs:cube2');

  event.replaceInput({id: 'mekanism:transmitter/logistical_transporter/basic'}, 'mekanism:basic_control_circuit', 'prettypipes:pipe');
	event.replaceInput({id: 'mekanism:solar_neutron_activator'}, 'mekanism:hdpe_sheet', 'kubejs:black_hdpe_sheet');
	event.replaceInput({id: 'mekanism:sps_casing'}, 'mekanism:hdpe_sheet', 'kubejs:black_hdpe_sheet');
});