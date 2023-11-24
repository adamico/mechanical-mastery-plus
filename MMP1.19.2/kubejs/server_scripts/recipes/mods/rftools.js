ServerEvents.recipes(event => {
  event.replaceInput({id: 'rftoolsutility:crafter1'}, 'minecraft:crafting_table', 'thermal:machine_crafter');

	event.remove({id: 'rftoolsutility:crafter2'});
	event.shaped('rftoolsutility:crafter2', [
		' A ',
		'BCB',
		' A '
		], {
			A: 'minecraft:redstone_torch',
			B: 'thermal:machine_crafter',
			C: 'rftoolsutility:crafter1'
		}
	);

	event.remove({id: 'rftoolsutility:crafter3'});
	event.shaped('rftoolsutility:crafter3', [
		' A ',
		'BCB',
		' A '
		], {
			A: 'minecraft:redstone_torch',
			B: 'thermal:machine_crafter',
			C: 'rftoolsutility:crafter2'
		}
	);
});