// priority: 100

StartupEvents.registry('item', event => {
  // Register new items here
  // event.create('example_item').displayName('Example Item')

	event.create('cube1').displayName('Basic Mechanical Essence');
	event.create('cube2').displayName('Regular Mechanical Essence');
	event.create('cube3').displayName('Improved Mechanical Essence');
	event.create('cube4').displayName('Advanced Mechanical Essence');

	event.create('darkness_essence').displayName('Essence of Darkness');
	event.create('enriched_darkness_essence').displayName('Enriched Essence of Darkness');

	event.create('incomplete_dragon_dna').displayName('Incomplete Ender Dragon DNA');
	event.create('dragon_dna').displayName('Ender Dragon DNA');

	event.create('incomplete_creative_blaze_cake').displayName('Incomplete Creative Blaze Cake');
  event.create('incomplete_creative_paste_container').displayName('Incomplete Derpyness');
	event.create('coated_redstone').displayName('Obsidian-Coated Redstone');

	event.create('ore_hammer', 'pickaxe').tier('wood').displayName('Ore Hammer').maxDamage(100)
		.tooltip('Right click a block of stone with a raw ore in off hand to crush it into dust');

	event.create('incomplete_press').displayName('Incomplete Inscriber Press').texture('kubejs:item/incomplete_press');
});