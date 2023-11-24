ServerEvents.recipes(event => {
	let millAndCrushRecipes = (output, input) => {
		[{name: 'createMilling', time: 250}, {name: 'createCrushing', time: 125}].forEach(recipe => {
			event.recipes[recipe.name](output, input).processingTime(recipe.time);
		});
	}

	event.remove({id: 'create:milling/coal'});
	event.remove({id: 'create:milling/sandstone'});
	event.remove({id: 'create:milling/gravel'});
	event.remove({id: 'create:crushing/gravel'});
	event.remove({id: 'create:milling/cobblestone'});
	event.remove({id: 'create:crushing/cobblestone'});

	millAndCrushRecipes(['minecraft:gravel'], 'minecraft:cobblestone');
	millAndCrushRecipes([Item.of('minecraft:sand'), Item.of('minecraft:flint').withChance(0.1), Item.of('minecraft:clay_ball').withChance(0.05)], 'minecraft:gravel');
	millAndCrushRecipes([Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal'), Item.of('mekanism:dust_coal')], 'minecraft:coal');
	
	event.recipes.createHaunting('minecraft:egg', 'create:dough');

	event.recipes.createMixing(Fluid.of('cofh_core:experience', 250), [Item.of('create:experience_nugget', 3)]).superheated();

	let alloysRecipes = [
		{ output: '#forge:dusts/steel', output_count: 3, inputs: [Item.of('#forge:dusts/iron', 2), Item.of("#forge:dusts/coal")]},
		{ output: '#forge:dusts/brass', output_count: 2, inputs: [Item.of('#forge:dusts/copper'), Item.of('#forge:dusts/zinc')]},
		{ output: '#forge:dusts/invar', output_count: 3, inputs: [Item.of('#forge:dusts/iron', 2), Item.of('#forge:dusts/nickel')]}
	];

	alloysRecipes.forEach(alloysRecipe => {  
		event.recipes.createMixing(Item.of(alloysRecipe.output, alloysRecipe.output_count), alloysRecipe.inputs).processingTime(60);
	});

	event.recipes.createMixing(Item.of('#forge:ingots/steel', 3), [Item.of('#forge:ingots/iron', 3), Item.of("#minecraft:coals", 3)]).heated().processingTime(120);

	event.blasting('minecraft:glowstone_dust', 'create:cinder_flour');
	event.recipes.createHaunting('minecraft:ender_pearl', Item.of('create:cinder_flour', 8));
	
	event.recipes.createMechanicalExtruderExtruding(Item.of('minecraft:andesite'),
		[Fluid.of('minecraft:water'),Fluid.of('minecraft:lava')]).withCatalyst('supplementaries:flint_block').requiredBonks(4);

	event.recipes.createMechanicalExtruderExtruding(Item.of('minecraft:bedrock'),
		[Fluid.of('mekanismgenerators:fusion_fuel'), Fluid.of('integrateddynamics:liquid_chorus')]).withCatalyst('minecraft:respawn_anchor').requiredBonks(10);
	event.remove({id: 'create:crushing/prismarine_crystals'});

	event.replaceInput({id: 'create_jetpack:jetpack'}, 'minecraft:elytra', 'kubejs:cube3');

	event.remove({id: "createaddition:mixing/bioethanol"});

	event.remove({id: "createaddition:liquid_burning/bioethanol"});
	event.remove({id: "createaddition:liquid_burning/biofuel"});
	event.remove({id: "createaddition:liquid_burning/compat/thermal/refined_fuel"});

	event.custom({
		type: 'createaddition:liquid_burning',
		input: {
			fluid: "industrialforegoing:biofuel",
			amount: 1000
		},
		burnTime: 1000,
		superheated: true
	});
	event.custom({
		type: 'createaddition:liquid_burning',
		input: {
			fluid: "thermal:refined_fuel",
			amount: 1000
		},
		burnTime: 1000,
		superheated: true
	});
});