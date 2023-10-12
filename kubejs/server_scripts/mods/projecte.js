ServerEvents.recipes(event => {
  event.replaceInput({id: 'projecte:high_covalence_dust'}, 'minecraft:diamond', 'minecraft:gold_ingot');
	event.replaceInput({id: 'projecte:condenser_mk1'}, 'minecraft:obsidian', 'minecraft:gold_ingot');
	event.replaceInput({id: 'projecte:condenser_mk1'}, 'minecraft:diamond', 'projecte:high_covalence_dust');
	event.replaceInput({output: 'projecte:swiftwolf_rending_gale'}, 'projecte:iron_band', 'minecraft:nether_star');

	event.replaceInput({id: 'projectexpansion:emc_link/basic'}, 'projecte:condenser_mk1', 'minecraft:barrel');
	event.replaceInput({id: 'projectexpansion:emc_link/basic'}, 'projecte:transmutation_tablet', '#forge:plates/invar');
	event.replaceInput({id: 'projectexpansion:emc_link/dark'}, 'projecte:dark_matter', 'kubejs:cube1');
	event.replaceInput({id: 'projectexpansion:emc_link/red'}, 'projecte:red_matter', 'kubejs:cube2');
	event.replaceInput({id: 'projectexpansion:emc_link/magenta'}, 'projectexpansion:magenta_matter', 'kubejs:cube3');
	event.replaceInput({id: 'projectexpansion:emc_link/pink'}, 'projectexpansion:pink_matter', 'kubejs:cube4');
	event.replaceInput({output: 'projecte:philosophers_stone'}, 'minecraft:diamond', 'minecraft:emerald');
	event.replaceInput({id: 'projectexpansion:transmutation_interface'}, 'projectexpansion:final_star_shard', 'kubejs:cube3');
	event.replaceInput({id: 'projectexpansion:transmutation_interface'}, 'projectexpansion:final_emc_link', 'kubejs:cube2');
	event.replaceInput({id: 'projecte:transmutation_table'}, 'projecte:philosophers_stone', 'minecraft:crafting_table');
	event.replaceInput({id: 'projecte:transmutation_table'}, 'minecraft:obsidian', '#forge:plates/iron');
	event.replaceInput({id: 'projecte:transmutation_tablet'}, 'projecte:dark_matter_block', '#forge:plates/invar');

	event.replaceInput({id: 'projecte:dark_matter'}, 'minecraft:diamond_block', 'kubejs:cube4');

  event.remove({id: '/projectexpansion:power_flower/'});
	event.remove({id: '/projectexpansion:collector/'});
	event.remove({id: '/projectexpansion:relay/'});
  event.remove({id: 'projectexpansion:star/final_star_shard'});
	event.remove({id: 'projectexpansion:star/final_star'});
  event.remove({id: 'projecte:conversions/alchemical_coal_to_coal'});
  
  ['1', '2', '3'].forEach(num => {	
		event.remove({id: `projecte:relay_mk${num}`});
		event.remove({id: `projecte:collector_mk${num}`});	
	});
});