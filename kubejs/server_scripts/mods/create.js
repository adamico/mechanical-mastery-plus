ServerEvents.recipes(event => {
	event.recipes.createMilling([Item.of('minecraft:sand'), Item.of('thermal:sulfur').withChance(0.8)], ['minecraft:sandstone']).processingTime(250);
	event.recipes.createMilling([Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal').withChance(0.25)], 'minecraft:coal').processingTime(250);
	event.recipes.createMilling([Item.of('#forge:dusts/zinc'), Item.of('#forge:dusts/zinc')], Item.of('#forge:raw_materials/zinc')).processingTime(250);
	event.recipes.createCrushing([Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal')], 'minecraft:coal').processingTime(250);
	
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

	event.remove({id: 'createchromaticreturn:motor_recipe'});
	event.recipes.createMechanicalCrafting('create:creative_motor', [
    "MNYNM",
    "NNRNN",
    "NRSRN",
    "NNRNN",
    "MNCNM"
	], {
			N: "minecraft:netherite_block",
			R: "createchromaticreturn:refined_mechanism",
			M: "createchromaticreturn:multiplite_ingot",
			S: 'projectexpansion:final_star',
			Y: "create:rotation_speed_controller",
			C: "create:shaft"
    }
	);

	event.remove({id: 'createchromaticreturn:cf_to_gp'});
	event.remove({id: 'createchromaticreturn:gp_to_bp'});
	event.remove({id: 'createchromaticreturn:cf_to_gs'});
	event.blasting('minecraft:glowstone_dust', 'create:cinder_flour');
	event.recipes.createHaunting('minecraft:ender_pearl', Item.of('create:cinder_flour', 8));
	
	event.remove({id: 'createchromaticreturn:carbon_powder_recipe'});
	event.recipes.createCrushing([Item.of('projecte:dark_matter').withChance(0.5), Item.of('createchromaticreturn:carbon_powder', 8).withChance(0.5)], 'projecte:dark_matter_block');

	[
		{input: 'iron', output: 'redstone'}, 
		{input: 'gold', output: 'quartz'}, 
		{input: 'copper', output: 'clay_ball'}, 
		{input: 'zinc', output: 'gunpowder'}
	].forEach(metal => {
		event.replaceOutput({id: `create:splashing/crushed_raw_${metal.input}`}, `minecraft:${metal.output}`, `#forge:dusts/${metal.input}`)
	});

	[
		{mod: 'thermal', ore: 'nickel'},
		{mod: 'thermal', ore: 'silver'},
		{mod: 'mekanism', ore: 'osmium'},
		{mod: 'mekanism', ore: 'tin'},
		{mod: 'mekanism', ore: 'lead'},
		{mod: 'mekanism', ore: 'uranium'}
	].forEach(metal => {
		event.remove({id: `create:splashing/${metal.mod}/crushed_raw_${metal.ore}`});
		event.recipes.createSplashing([Item.of(`#forge:nuggets/${metal.ore}`, 9), Item.of(`#forge:dusts/${metal.ore}`).withChance(0.5)], `create:crushed_raw_${metal.ore}`)
	});

	event.remove({id: 'createchromaticreturn:refined_mixture_recipe'});
	event.recipes.createMixing(Fluid.of('createchromaticreturn:refined_mixture', 250), [Item.of('minecraft:smooth_quartz', 8), Item.of('minecraft:glowstone', 8), Item.of('projecte:mobius_fuel', 4), Item.of('mekanism:hdpe_sheet', 4),Fluid.of('minecraft:water', 1000)]).superheated();  
  
	event.remove({id: 'createchromaticreturn:chromatic_compound_recipe'});
	event.recipes.createMixing(Item.of('createchromaticreturn:chromatic_compound', 2), [Item.of('createchromaticreturn:glowing_ingot', 2), Item.of('create:polished_rose_quartz', 2), Item.of('#forge:dusts/obsidian', 2), Item.of('create:andesite_alloy', 2)]).superheated();

	event.recipes.createMechanicalExtruderExtruding(Item.of('minecraft:andesite'),
		[Fluid.of('minecraft:water'),Fluid.of('minecraft:lava')]).withCatalyst('supplementaries:flint_block').requiredBonks(4);

	event.recipes.createMechanicalExtruderExtruding(Item.of('minecraft:bedrock'),
		[Fluid.of('mekanismgenerators:fusion_fuel'), Fluid.of('integrateddynamics:liquid_chorus')]).withCatalyst('minecraft:respawn_anchor').requiredBonks(10);
	event.remove({id: 'create:crushing/prismarine_crystals'});
});