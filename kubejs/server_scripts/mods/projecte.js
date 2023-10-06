ServerEvents.recipes(event => {
  event.replaceInput({id: 'projecte:high_covalence_dust'}, 'minecraft:diamond', 'minecraft:gold_ingot');
	event.replaceInput({id: 'projecte:condenser_mk1'}, 'minecraft:obsidian', 'minecraft:gold_ingot');
	event.replaceInput({id: 'projecte:condenser_mk1'}, 'minecraft:diamond', 'projecte:high_covalence_dust');
	event.replaceInput({output: 'projecte:swiftwolf_rending_gale'}, 'projecte:iron_band', '64x kubejs:cube4');

	event.replaceInput({id: 'projectexpansion:emc_link/basic'}, 'projecte:condenser_mk1', 'minecraft:barrel');
	event.replaceInput({id: 'projectexpansion:emc_link/basic'}, 'projecte:transmutation_tablet', '#forge:plates/invar');

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