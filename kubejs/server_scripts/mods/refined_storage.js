ServerEvents.recipes(event => {
  event.shaped('refinedstorage:creative_storage_disk', [
		' A ',
		'DBD',
		' E '
		], {
			A: 'minecraft:dragon_head',
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
});