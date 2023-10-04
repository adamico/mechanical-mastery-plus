ServerEvents.recipes(event => {
	event.remove({id: 'refinedstorage:controller'});
	event.shaped('refinedstorage:controller', [
		'EAE',
		'SQS',
		'EME'
		], {
			E: 'refinedstorage:quartz_enriched_iron',
			A: 'refinedstorage:advanced_processor',
			S: '#forge:silicon',
			Q: 'refinedstorage:machine_casing',
			M: 'kubejs:cube3'
	});

    event.shaped('refinedstorage:creative_storage_disk', [
		' A ',
		'DBD',
		' E '
		], {
			A: 'projectexpansion:final_star',
			B: 'extrastorage:disk_16384k',
			D: 'refinedstorage:controller',
			E: 'kubejs:enriched_black_essence'
		}
	);

	event.remove({id: 'creativecrafter:creative_crafter'});
	event.shaped('creativecrafter:creative_crafter', [
		'ABA',
		'CDC',
		'ABA'
		], {
			A: 'minecraft:netherite_block',
			B: 'minecraft:nether_star',
			C: 'minecraft:dragon_head',
			D: 'extrastorage:netherite_crafter'
	});

  event.replaceInput({input: 'refinedstorage:silicon'}, 'refinedstorage:silicon', '#forge/silicon');
});
