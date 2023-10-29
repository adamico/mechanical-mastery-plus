ServerEvents.recipes(event => {
	event.replaceInput({id: 'minecraft:piston'}, 'cobblestone', 'clayworks:glazed_terracotta');

	// smelting recipes
	let outputs = [
		{name: 'glass', mod: 'forge'},
		{name: 'sandstone', mod: 'forge'},
		{name: 'stone', mod: 'forge'},
		{name: 'coals', mod: 'minecraft'},
	];
	
	outputs.forEach(output => {
		event.remove({type: 'smelting', output: `#${output.mod}:${output.name}`});
		event.remove({type: 'smelting', output: '#engineersdecor:accepted_mineral_smelter_input'});
	});
})