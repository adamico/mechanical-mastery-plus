ServerEvents.recipes(event => {
	event.recipes.createMilling([Item.of('minecraft:sand'), Item.of('thermal:sulfur').withChance(0.8)], 'minecraft:sandstone');
	event.recipes.createMilling([Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal').withChance(0.25)], 'minecraft:coal');
	event.recipes.createMilling([Item.of('#forge/dusts/zinc'), Item.of('#forge/dusts/zinc')], Item.of('#forge:raw_ores/zinc'));
	event.recipes.createCrushing([Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal')], 'minecraft:coal');
	
	event.recipes.createHaunting('minecraft:egg', 'create:dough');

	let alloysRecipes = [
		{ output: '#forge:dusts/steel', output_count: 3, inputs: [Item.of('#forge:dusts/iron', 2), Item.of("#forge:dusts/coal")]},
		{ output: '#forge:dusts/brass', output_count: 2, inputs: [Item.of('#forge:dusts/copper'), Item.of('#forge:dusts/zinc')]},
		{ output: '#forge:dusts/invar', output_count: 3, inputs: [Item.of('#forge:dusts/iron', 2), Item.of('#forge:dusts/nickel')]}
	];

	alloysRecipes.forEach(alloysRecipe => {  
		event.recipes.createMixing(Item.of(alloysRecipe.output, alloysRecipe.output_count), alloysRecipe.inputs).processingTime(60);
	});

	event.recipes.createMixing(Item.of('#forge:ingots/steel', 3), [Item.of('#forge:ingots/iron', 3), Item.of("#minecraft:coals", 3)]).heated().processingTime(120);

	event.remove({id: 'create:milling/sandstone'});
	// event.remove({id: 'createchromaticreturn:motor_recipe'});
	// event.remove({id: 'createchromaticreturn:cf_to_gp'});
	// event.remove({id: 'createchromaticreturn:gp_to_bp'});
	// event.remove({id: 'createchromaticreturn:cf_to_gs'});
	// event.remove({id: 'createchromaticreturn:refined_radiance_recipe'});
	// event.remove({id: 'createchromaticreturn:refined_mixture_recipe'});
	// event.remove({id: 'createchromaticreturn:chromatic_compound_recipe'});
	// event.remove({id: 'createchromaticreturn:bedrock_shard_crushing'});

	event.recipes.createMechanicalExtruderExtruding(Item.of('minecraft:andesite'),
	[Fluid.of('minecraft:water'),Fluid.of('minecraft:lava')]).withCatalyst('supplementaries:flint_block').requiredBonks(4);
});