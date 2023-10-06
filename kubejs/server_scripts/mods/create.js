ServerEvents.recipes(event => {
  event.remove({id: 'create:milling/sandstone'});
	event.recipes.createMilling([Item.of('minecraft:sand'), Item.of('thermal:sulfur').withChance(0.8)], 'minecraft:sandstone');

  event.recipes.createHaunting('minecraft:egg', 'create:dough');

	event.createMixing('mekanism:dust_steel', [Item.of('minecraft:iron_ingot', 3), 'minecraft:coal', 'minecraft:charcoal']).processingTime(60);
	event.remove({id: 'createchromaticreturn:motor_recipe'});
	event.remove({id: 'createchromaticreturn:cf_to_gp'});
	event.remove({id: 'createchromaticreturn:gp_to_bp'});
	event.remove({id: 'createchromaticreturn:cf_to_gs'});
	event.remove({id: 'createchromaticreturn:refined_radiance_recipe'});
	event.remove({id: 'createchromaticreturn:refined_mixture_recipe'});
	event.remove({id: 'createchromaticreturn:chromatic_compound_recipe'});
	event.remove({id: 'createchromaticreturn:bedrock_shard_crushing'});

	event.recipes.createMechanicalExtruderExtruding(Item.of('minecraft:andesite'),
	[Fluid.of('minecraft:water'),Fluid.of('minecraft:lava')]).withCatalyst('supplementaries:flint_block').requiredBonks(4);
	// create mixing for invar, steel and brass dusts with low yield
});