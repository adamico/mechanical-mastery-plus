ServerEvents.recipes(event => {
	let cube_packaging = (output, input, cooldown) => {
		event.recipes.thermal.press([output], [input]).energy(cooldown * 20);
	};
	
	let bottling = (output, input) => {
		event.recipes.thermal.bottler(output, input).energy(800);
		event.recipes.createFilling(output, input);
	};
	
  event.shapeless('kubejs:cube1_packaged', ['kubejs:cube1', 'kubejs:cube1', 'kubejs:cube1', 'kubejs:cube1']);
	
  cube_packaging('kubejs:cube1_packaged', 'kubejs:cube1', 600);
	cube_packaging('kubejs:cube2_packaged', 'kubejs:cube2', 400);
	cube_packaging('kubejs:cube3_packaged', 'kubejs:cube3', 200);
	cube_packaging('kubejs:cube4_packaged', 'kubejs:cube4', 100);
	
	event.shapeless('kubejs:cube1', ['#forge:gears/gold', Item.of('#forge:rods/copper', 2), '#forge:gears/iron']);
	event.recipes.createMixing('kubejs:cube2', ['kubejs:cube1', '#forge:gears/steel', Item.of('#forge:rods/brass', 2), '#forge:gears/invar']);
	event.recipes.createMixing('kubejs:cube3', ['kubejs:cube2', 'minecraft:fire_charge', 'thermal:lightning_charge', 'thermal:ice_charge', 'thermal:earth_charge', 'mekanism:basic_control_circuit']).heated();
	event.recipes.createMixing('kubejs:cube4_inert', ['kubejs:cube3', 'kubejs:fission_pellet', 'projecte:mobius_fuel', 'mekanism:elite_control_circuit']).superheated();

	event.recipes.createMixing(Item.of('extendedcrafting:luminessence', 4), ['minecraft:gunpowder', 'minecraft:redstone', Fluid.of('thermal:glowstone', 500)]).heated();
	event.recipes.createMixing(Fluid.of('createchromaticreturn:refined_mixture', 250), [Item.of('minecraft:smooth_quartz', 8), Item.of('minecraft:glowstone', 8), Item.of('projecte:mobius_fuel', 4), Item.of('mekanism:hdpe_sheet', 4),Fluid.of('minecraft:water', 1000)]).superheated();  
  event.recipes.createMixing(Item.of('createchromaticreturn:chromatic_compound', 2), [Item.of('createchromaticreturn:glowing_ingot', 2), Item.of('create:polished_rose_quartz', 2), Item.of('create:powdered_obsidian', 2), Item.of('create:andesite_alloy', 2)]).superheated();
  event.recipes.createMixing('createchromaticreturn:refined_radiance', ['createchromaticreturn:chromatic_compound', Fluid.of('createchromaticreturn:refined_mixture', 100)]).superheated();
	event.recipes.thermal.bottler('kubejs:cube4', [Fluid.of('thermal:refined_fuel', 1000), 'kubejs:cube4_inert']).energy(400000);
	
	bottling('kubejs:blaze_effigy', [Fluid.of('minecraft:lava', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:blizz_effigy', [Fluid.of('thermal:ender', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:blitz_effigy', [Fluid.of('thermal:glowstone', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:basalz_effigy', [Fluid.of('thermal:redstone', 1000), 'kubejs:dormant_effigy']);

	event.shapeless('create:blaze_burner', ['create:empty_blaze_burner', 'kubejs:blaze_effigy']);

  event.shaped('kubejs:dormant_effigy', [
		' C ',
		'III',
		' I '
		], {
			I: 'minecraft:calcite',
			C: 'projecte:soul_stone'
	});

	event.remove({id: 'projecte:soul_stone'});
	event.shaped('projecte:soul_stone', [
		'GGG',
		'LEL',
		'GGG'
		], {
			G: 'minecraft:glowstone_dust',
			L: 'minecraft:lapis_lazuli',
			E: 'minecraft:redstone'
	});

  event.shaped('kubejs:incomplete_time_augment', [
    'DRD',
    'EGE',
    'DAD'
    ], {
      D: 'extendedcrafting:black_iron_ingot',
      R: 'thermal:upgrade_augment_3',
      E: 'extendedcrafting:ultimate_catalyst',
      G: 'thermal:enderium_gear',
      A: 'kubejs:cube4'
    }
  );
  	
	event.shaped('minecraft:skeleton_skull', [
		'GGG',
		'GEG',
		'GGG'
		], {
			G: 'minecraft:bone_block',
			E: 'projecte:body_stone'
	});

  event.recipes.thermal.pulverizer(['minecraft:blaze_rod', Item.of('minecraft:blaze_rod').withChance(0.25)], 'kubejs:blaze_effigy').energy(2000);

  ['blizz', 'blitz', 'basalz'].forEach(prefix => {
    event.recipes.thermal.pulverizer([`thermal:${prefix}_rod`, Item.of(`thermal:${prefix}_rod`).withChance(0.25)], `kubejs:${prefix}_effigy`).energy(2000);   
  })

	event.shaped('functionalstorage:creative_vending_upgrade', [
		'CAC',
		'ABA',
		'CAC'
		], {
			A: 'projectexpansion:final_power_flower',
			B: 'functionalstorage:netherite_upgrade',
			C: 'projectexpansion:final_star'
		}
	);
	
	event.shaped('projectexpansion:final_power_flower', [
		'CAE',
		'ABA',
		'FGH'
		], {
			A: 'projectexpansion:final_star',
			B: 'extendedcrafting:ultimate_singularity',
			C: 'mekanism:creative_energy_cube',
			E: 'create:creative_motor',
			F: 'refinedstorage:creative_storage_disk',
			G: 'mekanism:creative_fluid_tank',
			H: 'mekanism:creative_chemical_tank'
		}
	);

  event.recipes.createSequencedAssembly([
		'thermal:machine_efficiency_creative_augment'
		], 'projectexpansion:final_star', [
		event.recipes.createDeploying('kubejs:incomplete_creative_upgrade', ['kubejs:incomplete_creative_upgrade', 'kubejs:time_augment']),
		event.recipes.createFilling('kubejs:incomplete_creative_upgrade', ['kubejs:incomplete_creative_upgrade', Fluid.of('industrialforegoing:ether_gas', 1000)]),
		]).transitionalItem('kubejs:incomplete_creative_upgrade').loops(10);

	event.recipes.createSequencedAssembly([
		'create:creative_blaze_cake'
		], 'projectexpansion:final_star_shard', [
		event.recipes.createDeploying('kubejs:incomplete_creative_blaze_cake', ['kubejs:incomplete_creative_blaze_cake', 'create:blaze_cake']),
		event.recipes.createDeploying('kubejs:incomplete_creative_blaze_cake', ['kubejs:incomplete_creative_blaze_cake', 'create:blaze_cake']),
		event.recipes.createFilling('kubejs:incomplete_creative_blaze_cake', ['kubejs:incomplete_creative_blaze_cake', Fluid.of('minecraft:lava', 1000)]),
		event.recipes.createFilling('kubejs:incomplete_creative_blaze_cake', ['kubejs:incomplete_creative_blaze_cake', Fluid.of('thermal:ender', 1000)]),
		event.recipes.createFilling('kubejs:incomplete_creative_blaze_cake', ['kubejs:incomplete_creative_blaze_cake', Fluid.of('thermal:redstone', 1000)]),
		event.recipes.createFilling('kubejs:incomplete_creative_blaze_cake', ['kubejs:incomplete_creative_blaze_cake', Fluid.of('thermal:glowstone', 1000)]),
		]).transitionalItem('kubejs:incomplete_creative_blaze_cake').loops(100);

	event.recipes.createSequencedAssembly([
		'projectexpansion:final_star_shard' // have this item be a guaranteed output
		], 'minecraft:nether_star', [ 
		// the transitional item set by transitionalItem()
		// is the item that will be used during the recipe as the item that the input is using to transition to the output.
		event.recipes.createDeploying('kubejs:incomplete_final_shard', ['kubejs:incomplete_final_shard', 'createchromaticreturn:refined_radiance']), 
		event.recipes.createDeploying('kubejs:incomplete_final_shard', ['kubejs:incomplete_final_shard', 'powah:crystal_nitro']), 
		event.recipes.createDeploying('kubejs:incomplete_final_shard', ['kubejs:incomplete_final_shard', 'create:precision_mechanism']), 
		event.recipes.createDeploying('kubejs:incomplete_final_shard', ['kubejs:incomplete_final_shard', 'kubejs:cube1']), 
		event.recipes.createDeploying('kubejs:incomplete_final_shard', ['kubejs:incomplete_final_shard', 'kubejs:cube2']), 
		event.recipes.createDeploying('kubejs:incomplete_final_shard', ['kubejs:incomplete_final_shard', 'kubejs:cube3']), 
		event.recipes.createDeploying('kubejs:incomplete_final_shard', ['kubejs:incomplete_final_shard', 'kubejs:cube4'])
		]).transitionalItem('kubejs:incomplete_final_shard').loops(20); // set the transitional item and the loops (amount of repetitions)
	
	event.recipes.createMechanicalCrafting('projectexpansion:final_star', [
	  ' ABABA ',
	  'ABCCCBA',
	  'BCDDDCB',
	  'ACDEDCA',
	  'BCDDDCB',
	  'ABCCCBA',
	  ' ABABA '
	], {
	  A: 'projectexpansion:final_star_shard',
	  B: 'mekanism:pellet_antimatter',
	  C: 'projecte:dark_matter',
	  D: 'projecte:aeternalis_fuel_block',
	  E: 'extendedcrafting:ultimate_singularity'
	});

  event.recipes.thermal.bottler(Item.of('kubejs:time_augment',
    '{AugmentData:{Type:"Machine",MachinePower:1d,MachineEnergy:0.75d,RFXfer:20d}}'), 
    [Fluid.of('industrialforegoing:ether_gas', 500), 'kubejs:incomplete_time_augment']);

  event.recipes.thermal.crucible(Fluid.of('thermal:crude_oil', 125), 'kubejs:oil_clump').energy(400);

  event.recipes.thermal.pulverizer_catalyst('thermal:basalz_powder').primaryMod(1.25).secondaryMod(3.0).energyMod(0.75).minChance(0.0).useChance(0.5);
  event.recipes.thermal.pulverizer_catalyst('thermal:earth_charge').primaryMod(1.5).secondaryMod(4.0).energyMod(0.5).minChance(0.0).useChance(0.375);
  event.recipes.thermal.pulverizer_catalyst('kubejs:basilic_reagent').primaryMod(2.0).secondaryMod(5.0).energyMod(0.25).minChance(0.0).useChance(0.25);
  event.recipes.thermal.pulverizer_catalyst('kubejs:enriched_basilic_reagent').primaryMod(2.5).secondaryMod(5.0).energyMod(0.25).minChance(0.0).useChance(0.1);

  event.remove({id: 'thermal:machines/crucible/crucible_glowstone_dust'});
  event.recipes.thermal.crucible(Fluid.of('thermal:glowstone', 250), '#forge:dusts/glowstone').energy(1200);
  
  event.remove({id: 'thermal:machines/crucible/crucible_glowstone_block'});
  event.recipes.thermal.crucible(Fluid.of('thermal:glowstone', 1000), '#forge:storage_blocks/glowstone').energy(4800);
  
  event.remove({id: 'thermal:machines/crucible/crucible_redstone_block'});
  event.recipes.thermal.crucible(Fluid.of('thermal:redstone', 1000), '#forge:storage_blocks/redstone').energy(4800);
  
  event.remove({id: 'thermal:machines/crucible/crucible_ender_pearl'});
  event.recipes.thermal.crucible(Fluid.of('thermal:ender', 250), '#forge:ender_pearls').energy(1200);
  
  event.remove({id: 'thermal:machines/crucible/crucible_cobblestone_to_lava'});
  event.recipes.thermal.crucible(Fluid.of('minecraft:lava', 250), '#forge:cobblestone').energy(3000);
  
  event.remove({id: 'thermal:machines/crucible/crucible_netherrack_to_lava'});
  event.recipes.thermal.crucible(Fluid.of('minecraft:lava', 500), '#forge:netherrack').energy(1500);

  event.recipes.thermal.bottler(Item.of('kubejs:time_augment', '{AugmentData:{Type:"Machine",MachinePower:1d,MachineEnergy:0.75d,RFXfer:20d}}'), [Fluid.of('industrialforegoing:ether_gas', 500), 'kubejs:incomplete_time_augment']);

  event.recipes.thermal.pulverizer(['minecraft:dirt', Item.of('minecraft:red_mushroom').withChance(0.25), Item.of('minecraft:brown_mushroom').withChance(0.25)], 'minecraft:podzol').energy(200);
  event.recipes.thermal.press('fluxnetworks:flux_dust', ['kubejs:coated_redstone', 'fluxnetworks:flux_block']).energy(200);

  event.recipes.thermal.press('fluxnetworks:flux_dust', ['kubejs:coated_redstone', 'fluxnetworks:flux_block']).energy(200);

  event.replaceInput({id: 'thermal:dynamo_stirling'}, '#forge:gears/iron', 'ae2:vibration_chamber');

});