// priority: 100

StartupEvents.registry('item', event => {
  // Register new items here
  // event.create('example_item').displayName('Example Item')

	event.create('cube1').displayName('Basic Mechanical Essence');
	event.create('cube1_packaged').displayName('Basic Mechanical Crystal');
	event.create('cube2').displayName('Regular Mechanical Essence');
	event.create('cube2_packaged').displayName('Regular Mechanical Crystal');
	event.create('cube3').displayName('Improved Mechanical Essence');
	event.create('cube3_packaged').displayName('Improved Mechanical Crystal');
	event.create('cube4').displayName('Advanced Mechanical Essence');
	event.create('cube4_packaged').displayName('Advanced Mechanical Crystal');
	event.create('cube4_inert').displayName('Inert Advanced Mechanical Essence').tooltip('So close, yet so far...');

	event.create('press_rod_die').displayName('Rod Die');
	event.create('fission_pellet').displayName('Fissile fuel pellet');
	event.create('black_hdpe_sheet').displayName('Black HDPE Sheet');

	event.create('blaze_effigy').displayName('Blaze effigy').tooltip('Right click to summon a Blaze');
	event.create('blizz_effigy').displayName('Blizz effigy').tooltip('Right click to summon a Blizz');
	event.create('blitz_effigy').displayName('Blitz effigy').tooltip('Right click to summon a Blitz');
	event.create('basalz_effigy').displayName('Basalz effigy').tooltip('Right click to summon a Basalz');
	event.create('dormant_effigy').displayName('Dormant effigy').tooltip('The power to create life, just not fully awoken');

	event.create('steel_gear').displayName('Steel Gear');
	event.create('steel_rod').displayName('Steel Rod');
	event.create('diamond_plate').displayName('Diamond Plate');

	event.create('black_essence').displayName('Essence of Darkness');
	event.create('enriched_black_essence').displayName('Enriched Essence of Darkness');

	event.create('basilic_reagent').displayName('Basilic Reagent');
	event.create('enriched_basilic_reagent').displayName('Enriched Basilic Reagent');

	event.create('incomplete_final_shard').displayName('Incomplete Final Star Shard');

	event.create('incomplete_time_augment').displayName('Time Destabilizer Components').texture('kubejs:item/basic_package');
	event.create('time_augment').displayName('Localized time destabilizer').tooltip('Enables machine compatibility with watch' +
	' of flowing time. Best used alongside resonant integral components')

	event.create('incomplete_creative_blaze_cake').displayName('Incomplete Creative Blaze Cake');
	event.create('incomplete_creative_upgrade').displayName('Incomplete Creative Augment');
  
	event.create('coated_redstone').displayName('Obsidian-Coated Redstone');
  
  // industrial foregoing items
	event.create('incomplete_simple_frame').displayName('Incomplete Simple Machine Frame').texture('kubejs:item/basic_package');
	event.create('incomplete_advanced_frame').displayName('Incomplete Advanced Machine Frame').texture('kubejs:item/basic_package');
	event.create('incomplete_supreme_frame').displayName('Incomplete Supreme Machine Frame').texture('kubejs:item/basic_package');
	event.create('incomplete_speed_1').displayName('Incomplete Speed 1 Addon').texture('kubejs:item/basic_package');
	event.create('incomplete_speed_2').displayName('Incomplete Speed 2 Addon').texture('kubejs:item/basic_package');
	event.create('incomplete_eff_1').displayName('Incomplete Efficiency 1 Addon').texture('kubejs:item/basic_package');
	event.create('incomplete_eff_2').displayName('Incomplete Efficiency 2 Addon').texture('kubejs:item/basic_package');
	event.create('incomplete_proc_1').displayName('Incomplete Processing 1 Addon').texture('kubejs:item/basic_package');
	event.create('incomplete_proc_2').displayName('Incomplete Processing 2 Addon').texture('kubejs:item/basic_package');
	event.create('incomplete_range_2').displayName('Incomplete Range 2 Addon').texture('kubejs:item/basic_package');

	event.create('incomplete_press').displayName('Incomplete Inscriber Press').texture('kubejs:item/incomplete_press');
});