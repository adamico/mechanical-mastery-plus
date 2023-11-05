ServerEvents.recipes(event => {
	//functions
	let bottling = (output, input) => {
		event.recipes.createFilling(output, input);
	};
	
	//tier0

	event.replaceInput({id: 'create:crafting/appliances/slime_ball'}, 'create:dough', '#forge:dough');

	event.replaceInput({output: 'integrateddynamics:squeezer'}, 'minecraft:stick', 'createaddition:iron_rod');

	['squeezer', 'mechanical_squeezer'].forEach(recipe => {
		event.remove({id: `integrateddynamics:${recipe}/convenience/minecraft_dye_white_bone`});
		event.custom({
			type: `integrateddynamics:${recipe}`,
			item: {
				item: "minecraft:bone"
			},
			result: {
				items: [
					{
						item: {
							item: "minecraft:bone_meal",
							count: 3
						}
					},
					{
						item: {
							item: "minecraft:bone_meal",
							count: 1
						},
						chance: 0.5
					},
					{
						"item": {
							item: "minecraft:white_dye",
							count: 2
						},
						chance: 0.5
					}
				]	
			},
			duration: 10
		});

		event.remove({id: `integrateddynamics:${recipe}/ore/flint`});
		event.custom({
			type: `integrateddynamics:${recipe}`,
			item: "minecraft:gravel",
			result: {
				items: [
					{
						item: "minecraft:sand"
					},
					{
						item: "minecraft:flint",
						chance: 0.5
					}
				]
			},
			duration: 30
		});

		event.custom({
			type: `integrateddynamics:${recipe}`,
			item: {
				tag: "minecraft:coals"
			},
			result: {
				items: [
					{
						"item": {
							item: "minecraft:black_dye",
							count: 2
						}
					}
				]	
			},
			duration: 10
		});
	});

	//tier1
  event.shapeless('kubejs:cube1_packaged', ['kubejs:cube1', 'kubejs:cube1', 'kubejs:cube1', 'kubejs:cube1']);
	event.recipes.thermal.press('kubejs:cube1_packaged', 'kubejs:cube1').energy(600 * 20);
	event.shapeless('kubejs:cube1', ['#forge:gears/gold', Item.of('#forge:rods/copper', 2), '#forge:gears/iron']);

  event.replaceInput({id: 'thermal:dynamo_stirling'}, '#forge:gears/iron', 'ae2:vibration_chamber');

	//tier2
	event.recipes.createCompacting('kubejs:cube2_packaged', 'kubejs:cube2');
	event.recipes.createMixing('kubejs:cube2', ['kubejs:cube1', '#forge:gears/steel', Item.of('#forge:dusts/quartz', 2), Item.of('#forge:rods/brass', 2), '#forge:gears/invar']);

	//tier3
	event.recipes.createMixing('kubejs:cube3', ['kubejs:cube2', 'minecraft:fire_charge', 'thermal:lightning_charge', 'thermal:ice_charge', 'thermal:earth_charge', 'mekanism:basic_control_circuit']).heated();

	bottling('kubejs:blaze_effigy', [Fluid.of('minecraft:lava', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:blizz_effigy', [Fluid.of('thermal:ender', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:blitz_effigy', [Fluid.of('thermal:glowstone', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:basalz_effigy', [Fluid.of('thermal:redstone', 1000), 'kubejs:dormant_effigy']);

	event.shapeless('create:blaze_burner', ['create:empty_blaze_burner', 'kubejs:blaze_effigy']);

	event.recipes.createMixing(Item.of('minecraft:gunpowder', 4), [Item.of('#minecraft:coals', 2), ['thermal:niter', 'thermal:niter_dust'], ['thermal:sulfur', 'thermal:sulfur_dust']]).heated();

	event.remove({id: 'createchromaticreturn:creative_cake_recipe'});
	event.recipes.createSequencedAssembly([
		Item.of('create:creative_blaze_cake').withChance(60.0),
		Item.of('createchromaticreturn:creative_flour').withChance(30.0),
		Item.of('minecraft:dragon_head').withChance(6.0)
		], "kubejs:dragon_dna", [
		event.recipes.create.deploying('kubejs:incomplete_creative_blaze_cake', ['kubejs:incomplete_creative_blaze_cake', 'createchromaticreturn:creative_flour']),
		event.recipes.create.deploying('kubejs:incomplete_creative_blaze_cake', ['kubejs:incomplete_creative_blaze_cake', 'minecraft:dragon_head']),
		event.recipes.create.filling('kubejs:incomplete_creative_blaze_cake', [Fluid.of('mekanismgenerators:fusion_fuel', 1000), 'kubejs:incomplete_creative_blaze_cake']),
		event.recipes.create.filling('kubejs:incomplete_creative_blaze_cake', [Fluid.of('thermal:ender', 1000),'kubejs:incomplete_creative_blaze_cake']),
		event.recipes.create.filling('kubejs:incomplete_creative_blaze_cake', [Fluid.of('thermal:redstone', 1000),'kubejs:incomplete_creative_blaze_cake']),
		event.recipes.create.filling('kubejs:incomplete_creative_blaze_cake', [Fluid.of('thermal:glowstone', 1000),'kubejs:incomplete_creative_blaze_cake']),
		]).transitionalItem('kubejs:incomplete_creative_blaze_cake').loops(100);

	event.shaped('minecraft:skeleton_skull', [
		'BBB',
		'DCD',
		'BBB'
		], {
			B: 'minecraft:bone_block',
			D: 'minecraft:black_dye',
			C: 'kubejs:cube1'
	});

  event.shaped('kubejs:dormant_effigy', [
		' S ',
		'BBB',
		' B '
		], {
			B: 'minecraft:bone_block',
			S: 'minecraft:skeleton_skull'
	});

  event.recipes.thermal.pulverizer(['minecraft:blaze_rod', Item.of('minecraft:blaze_rod').withChance(0.25)], 'kubejs:blaze_effigy').energy(2000);

  ['blizz', 'blitz', 'basalz'].forEach(prefix => {
    event.recipes.thermal.pulverizer([`thermal:${prefix}_rod`, Item.of(`thermal:${prefix}_rod`).withChance(0.25)], `kubejs:${prefix}_effigy`).energy(2000);   
  })

  event.remove({id: 'thermal:machines/crucible/crucible_glowstone_dust'});
  event.recipes.thermal.crucible(Fluid.of('thermal:glowstone', 100), '#forge:ingots/lumium').energy(600);
  
  event.remove({id: 'thermal:machines/crucible/crucible_glowstone_block'});
  event.recipes.thermal.crucible(Fluid.of('thermal:glowstone', 1000), '#forge:storage_blocks/lumium').energy(3600);
  
	event.remove({id: 'thermal:machines/crucible/crucible_redstone_dust'});
	event.recipes.thermal.crucible(Fluid.of('thermal:redstone', 100), '#forge:ingots/signalum').energy(600);

  event.remove({id: 'thermal:machines/crucible/crucible_redstone_block'});
  event.recipes.thermal.crucible(Fluid.of('thermal:redstone', 1000), '#forge:storage_blocks/signalum').energy(3600);
  
  event.remove({id: 'thermal:machines/crucible/crucible_ender_pearl'});
  event.recipes.thermal.crucible(Fluid.of('thermal:ender', 100), '#forge:ingots/enderium').energy(600);
	event.recipes.thermal.crucible(Fluid.of('thermal:ender', 1000), '#forge:storage_blocks/enderium').energy(3600);

  event.remove({id: 'thermal:machines/crucible/crucible_cobblestone_to_lava'});
  event.recipes.thermal.crucible(Fluid.of('minecraft:lava', 50), '#forge:cobblestone').energy(300);
  
  event.remove({id: 'thermal:machines/crucible/crucible_netherrack_to_lava'});
  event.recipes.thermal.crucible(Fluid.of('minecraft:lava', 1000), 'minecraft:netherrack').energy(3600);

	//tier4
	event.recipes.createMixing('kubejs:cube4_inert', [Fluid.of('thermal:refined_fuel', 1000), 'kubejs:cube3', 'kubejs:fission_pellet', 'minecraft:experience_bottle', 'mekanism:elite_control_circuit']).superheated();
	event.recipes.createCrushing(Item.of('kubejs:cube4_dust', 2), 'kubejs:cube4');

	event.remove({id: 'thermal:machines/centrifuge/centrifuge_oil_sand'});
	event.remove({id: 'thermal:machines/centrifuge/centrifuge_oil_red_sand'});

	event.custom({
		type: 'thermal:centrifuge',
		ingredient: { item: 'thermal:oil_sand'},
		result: [ 
			{
				item: "minecraft:sand",
				chance: 0.75,
				locked: true
			},
			{
				item: "thermal:bitumen",
				chance: 1.5
			},
			{
				item: "thermal:tar",
				chance: 1.0
			},
			{
				fluid: "thermal:crude_oil",
				amount: 125
			}
		],
		energy: 40000
	});
	
	event.remove({id: 'mekanism:control_circuit/elite'});
	event.replaceInput({output: 'mekanism:combiner'}, 'mekanism:elite_control_circuit', 'kubejs:cube3');

	event.recipes.thermal.press('fluxnetworks:flux_dust', ['kubejs:coated_redstone', 'fluxnetworks:flux_block']).energy(200);

	//tier5

	['provider', 'cell'].forEach(type => {
		event.remove({id: 'createchromaticreturn:basic_induction_' + type});
	});

  event.recipes.thermal.chiller(Item.of('kubejs:darkness_essence', 4), Fluid.of('kubejs:darkness_essence')).energy(20000);

	event.remove({output: 'createchromaticreturn:multiplite_ingot'})
	event.recipes.createMixing(Item.of('createchromaticreturn:multiplite_ingot'), [Item.of('createchromaticreturn:refined_radiance', 16), Item.of('supplementaries:enderman_head', 8), Item.of('minecraft:shulker_shell', 8)]).superheated();

	event.recipes.shapeless('mob_grinding_utils:fluid_xp_bucket', ['industrialforegoing:essence_bucket']);
	event.recipes.shapeless('industrialforegoing:essence_bucket', ['mob_grinding_utils:fluid_xp_bucket']);

	let interThermal = 'kubejs:incomplete_creative_upgrade';
  event.recipes.create.sequenced_assembly([
		Item.of('thermal:machine_efficiency_creative_augment').withChance(30.0),
		Item.of('mekanism:pellet_antimatter').withChance(2.0),
		Item.of('thermal:upgrade_augment_3').withChance(16.0)
	], 'thermal:upgrade_augment_3', [
		event.recipes.create.deploying(interThermal, [interThermal, 'mekanism:pellet_antimatter']),
		event.recipes.create.filling(interThermal, [interThermal, Fluid.of('mekanismgenerators:fusion_fuel', 1000)]),
		event.recipes.create.pressing(interThermal, [interThermal])
	]).transitionalItem(interThermal).loops(10);

	event.recipes.create.sequenced_assembly([
		Item.of('thermal:machine_catalyst_creative_augment').withChance(30.0),
		Item.of('mekanism:pellet_antimatter').withChance(2.0),
		Item.of('thermal:machine_catalyst_augment').withChance(16.0)
	], 'thermal:machine_catalyst_augment', [
		event.recipes.createDeploying(interThermal, [interThermal, 'mekanism:pellet_antimatter']),
		event.recipes.createFilling(interThermal, [interThermal, Fluid.of('industrialforegoing:pink_slime', 1000)]),
		event.recipes.create.pressing(interThermal, [interThermal])
	]).transitionalItem(interThermal).loops(10);

	let interBG = 'kubejs:incomplete_creative_paste_container';
	event.recipes.create.sequenced_assembly([
		Item.of('buildinggadgets:construction_paste_container_creative').withChance(30.0),
		Item.of('mekanism:pellet_antimatter').withChance(2.0),
		Item.of('buildinggadgets:construction_paste_container_t3').withChance(16.0)
	], 'buildinggadgets:construction_paste_container_t3', [
		event.recipes.createDeploying(interBG, [interBG, 'mekanism:pellet_antimatter']),
		event.recipes.createFilling(interBG, [interBG, Fluid.of('cofh_core:honey', 1000)]),
		event.recipes.create.pressing(interBG, [interBG])
	]).transitionalItem(interBG).loops(10);

	event.recipes.createSequencedAssembly([
		'kubejs:dragon_dna'
	], 'minecraft:nether_star', [
			event.recipes.createDeploying('kubejs:incomplete_dragon_dna', ['kubejs:incomplete_dragon_dna', 'minecraft:shulker_shell']), 
			event.recipes.createDeploying('kubejs:incomplete_dragon_dna', ['kubejs:incomplete_dragon_dna', 'minecraft:end_crystal']), 
			event.recipes.createDeploying('kubejs:incomplete_dragon_dna', ['kubejs:incomplete_dragon_dna', 'powah:crystal_nitro']),
			event.recipes.createDeploying('kubejs:incomplete_dragon_dna', ['kubejs:incomplete_dragon_dna', 'kubejs:cube1']), 
			event.recipes.createDeploying('kubejs:incomplete_dragon_dna', ['kubejs:incomplete_dragon_dna', 'kubejs:cube2']),
			event.recipes.createDeploying('kubejs:incomplete_dragon_dna', ['kubejs:incomplete_dragon_dna', 'kubejs:cube3']), 
			event.recipes.createDeploying('kubejs:incomplete_dragon_dna', ['kubejs:incomplete_dragon_dna', 'kubejs:cube4'])
	]).transitionalItem('kubejs:incomplete_dragon_dna').loops(20);
	
	event.remove({id: 'thermal:machines/press/press_vine_to_latex'});
	event.remove({id: 'thermal:machines/press/press_dandelion_to_latex'});

	event.recipes.createMechanicalCrafting('minecraft:dragon_head', [
	  ' R R ',
	  ' R R ',
	  ' SSS ',
	  'SASAS',
	  'SCSCS',
	  'SBBBS',
	  ' SSS '
	], {
	  B: 'minecraft:dragon_breath',
	  A: 'mekanism:pellet_antimatter',
	  R: 'thermal:cured_rubber_block',
	  C: 'createchromaticreturn:bedrock_shard',
		S: 'minecraft:sculk',
	});

	event.remove({id: 'mekanism:control_circuit/ultimate'});
	event.remove({id: 'mekanism:chemical_washer'});
	event.shaped('mekanism:chemical_washer', [
		'CTC',
		'USU',
		'CKC'
	], {
		C: 'kubejs:cube4',
		K: 'mekanism:basic_chemical_tank',
		T: 'mekanism:basic_fluid_tank',
		U: 'mekanism:ultimate_control_circuit',
		S: 'mekanism:steel_casing'
	});

	event.remove({id: 'mekanism:chemical_crystallizer'});
	event.shaped('mekanism:chemical_crystallizer', [
		'CFC',
		'USU',
		'CFC'
	], {
		C: 'kubejs:cube4',
		F: 'mekanism:fluorite_gem',
		U: 'mekanism:ultimate_control_circuit',
		S: 'mekanism:steel_casing'
	});
	
	event.shaped('minecraft:elytra', [
		' G ',
		'MGM',
		'M M'
	], {
		G: 'gobber2:gobber2_ingot_end',
		M: 'minecraft:phantom_membrane'
	});

	event.remove({id: 'thermal:machines/pulverizer/pulverizer_catalyst_basalz_powder'});
  event.recipes.thermal.pulverizer_catalyst('thermal:basalz_powder').primaryMod(1.25).secondaryMod(3.0).energyMod(0.75).minChance(0.0).useChance(0.5);
  event.recipes.thermal.pulverizer_catalyst('thermal:earth_charge').primaryMod(1.5).secondaryMod(4.0).energyMod(0.5).minChance(0.0).useChance(0.375);
  event.recipes.thermal.pulverizer_catalyst('kubejs:basilic_reagent').primaryMod(2.0).secondaryMod(5.0).energyMod(0.25).minChance(0.0).useChance(0.25);
  event.recipes.thermal.pulverizer_catalyst('kubejs:enriched_basilic_reagent').primaryMod(2.5).secondaryMod(5.0).energyMod(0.25).minChance(0.0).useChance(0.1);
});