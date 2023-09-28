// priority: 0

ServerEvents.recipes(event => {
	console.log('[AMMONIUM@KUBEJS]: Adding custom recipes...');
	
	// REPLACING RECIPES
	event.remove({id: 'thermal:machine_frame'})
	event.shaped('thermal:machine_frame', [
		'ICI',
		'CGC',
		'ICI'
	], {
		I: 'minecraft:iron_ingot',
		C: 'minecraft:cobblestone',
		G: '#forge:gears/tin'
	})
	
	event.remove({id: 'thermal:machine_pulverizer'});
	event.shaped('thermal:machine_pulverizer', [
		' P ',
		'RFR',
		'GCG'
	], {
		P: 'minecraft:piston',
		R: 'minecraft:redstone',
		F: 'thermal:machine_frame',
		G: '#forge:gears/copper',
		C: 'thermal:rf_coil'
	});
	
	event.remove({id: 'thermal:press_gear_die'});
	event.shaped('thermal:press_gear_die', [
		' I ',
		'IGI',
		' I '
		], {
			I: '#forge:plates/invar',
			G: '#forge:gears/gold'
	});
	
	event.remove({id: 'mekanism:configurator'});
	event.shaped('mekanism:configurator', [
		' I ',
		' G '
		], {
			I: '#forge:ingots/iron',
			G: '#forge:rods/steel'
	});
	
	event.remove({id: 'thermal:augments/upgrade_augment_2'});
	event.shaped('thermal:upgrade_augment_2', [
		'EAE',
		'SQS',
		'EME'
		], {
			E: '#forge:ingots/electrum',
			A: 'thermal:upgrade_augment_1',
			S: '#forge:gears/signalum',
			Q: '#forge:gems/quartz',
			M: 'kubejs:cube2'
	});
	
	event.remove({id: 'thermal:augments/upgrade_augment_3'});
	event.shaped('thermal:upgrade_augment_3', [
		'EAE',
		'SQS',
		'EME'
		], {
			E: '#forge:ingots/enderium',
			A: 'thermal:upgrade_augment_2',
			S: '#forge:gears/lumium',
			Q: '#thermal:glass/hardened',
			M: 'kubejs:cube3'
	});
	
	event.remove({id: 'refinedstorage:controller'});
	event.shaped('refinedstorage:controller', [
		'EAE',
		'SQS',
		'EME'
		], {
			E: 'refinedstorage:quartz_enriched_iron',
			A: 'refinedstorage:advanced_processor',
			S: '#forge:silicon',
			Q: 'refinedstorage:machine_casing',
			M: 'kubejs:cube3'
	});
	
	event.remove({id: 'clickmachine:auto_clicker'});
	event.shaped('clickmachine:auto_clicker', [
		'DDD',
		'DKD',
		'DRD'
	], {
		D: 'minecraft:diorite',
		K: 'kubejs:cube1',
		R: '#forge:storage_blocks/redstone'
	});

	event.shaped('fluxnetworks:flux_block', [
		'ABA',
		'BCB',
		'ABA'
	], {
		A: '#forge:dusts/obsidian',
		B: '#forge:storage_blocks/redstone',
		C: 'kubejs:cube2'
	});

	event.shaped('ae2:sky_stone_block', [
		'AAA',
		'ABA',
		'AAA'
		], {
			A: 'minecraft:polished_deepslate',
			B: 'kubejs:cube3'
	});

	event.remove({id: 'trashcans:item_trash_can'});
	event.shapeless('trashcans:item_trash_can', ['thermal:device_nullifier', '#forge:chests']);
	event.remove({id: 'trashcans:liquid_trash_can'});
	event.shapeless('trashcans:liquid_trash_can', ['thermal:device_nullifier', 'minecraft:bucket']);
	event.remove({id: 'trashcans:energy_trash_can'});
	event.shaped('trashcans:energy_trash_can', [
		'ABA',
		'CDC',
		'ABA'
		], {
			A: 'mekanism:induction_casing',
			B: 'mekanism:ultimate_induction_cell',
			C: 'mekanism:ultimate_induction_provider',
			D: 'mekanism:induction_port'
	});

	event.remove({id: 'creativecrafter:creative_crafter'});
	event.shaped('creativecrafter:creative_crafter', [
		'ABA',
		'CDC',
		'ABA'
		], {
			A: 'minecraft:netherite_block',
			B: 'minecraft:nether_star',
			C: 'minecraft:dragon_head',
			D: 'extrastorage:netherite_crafter'
	});

	event.remove({id: 'cookingforblockheads:sink'});
	event.shaped('cookingforblockheads:sink', [
		'ABA',
		'CDC',
		'ABA'
		], {
			A: 'minecraft:terracotta',
			B: 'kubejs:cube4',
			C: 'mekanism:ultimate_fluid_tank',
			D: 'projecte:evertide_amulet'
		}
	);
	
	event.recipes.createSplashing([Item.of('ae2:certus_quartz_crystal').withChance(0.75), Item.of('ae2:charged_certus_quartz_crystal').withChance(0.25)], 'ae2:sky_dust')
	event.remove({id: 'create:compat/ae2/milling/sky_stone_block'});
	event.recipes.createMilling(['ae2:sky_dust', 'ae2:sky_stone_block'], 'ae2:sky_stone_block');

	event.remove({id: 'create:milling/sandstone'});
	event.recipes.createMilling([Item.of('minecraft:sand'), Item.of('immersiveengineering:dust_saltpeter').withChance(0.8)], 'minecraft:sandstone');
	
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
	
	event.replaceInput({id: 'mob_grinding_utils:recipe_fan'}, '#forge:ingots/iron', '#forge:plates/iron');	
	event.replaceInput({id: 'mob_grinding_utils:recipe_spikes'}, 'minecraft:iron_sword', '#forge:rods/iron');
	event.replaceInput({id: 'powah:crafting/dielectric_paste_2'}, 'minecraft:blaze_powder', 'minecraft:redstone');
	event.replaceInput({id: 'powah:crafting/dielectric_rod'}, 'minecraft:iron_bars', 'minecraft:iron_nugget');
	event.replaceInput({id: 'powah:crafting/dielectric_rod_h'}, 'minecraft:iron_bars', 'minecraft:iron_nugget');
	event.replaceInput({id: 'prettypipes:blank_module'}, 'minecraft:quartz', 'minecraft:iron_nugget');
	event.replaceInput({id: 'projecte:high_covalence_dust'}, 'minecraft:diamond', 'minecraft:gold_ingot');
	event.replaceInput({id: 'projecte:condenser_mk1'}, 'minecraft:obsidian', 'minecraft:gold_ingot');
	event.replaceInput({id: 'projecte:condenser_mk1'}, 'minecraft:diamond', 'projecte:high_covalence_dust');
	event.replaceInput({id: 'extendedcrafting:redstone_ingot'}, 'minecraft:iron_ingot', '#forge:ingots/lead');
	event.replaceInput({id: 'immersiveengineering:crafting/blastbrick'}, 'minecraft:magma_block', 'create:andesite_alloy');
	event.replaceInput({id: 'immersiveengineering:crafting/cokebrick'}, '#forge:sandstone', 'create:andesite_alloy');
	event.replaceInput({id: 'thermal:augments/upgrade_augment_1'}, '#forge:gears/gold', 'kubejs:cube1');
	event.replaceInput({id: 'thermal:machine_pyrolyzer'}, 'minecraft:blaze_rod', '#forge:ingots/steel');
	event.replaceInput({id: 'mekanism:transmitter/logistical_transporter/basic'}, 'mekanism:basic_control_circuit', 'prettypipes:pipe');
	event.replaceInput({id: 'prettypipes:round_robin_sorting_modifier'}, 'minecraft:arrow', 'minecraft:flint');
	event.replaceInput({id: 'projecte:transmutation_table'}, 'projecte:philosophers_stone', 'minecraft:crafting_table');
	event.replaceInput({id: 'projecte:transmutation_table'}, 'minecraft:obsidian', '#forge:plates/iron');
	event.replaceInput({id: 'projecte:transmutation_tablet'}, 'projecte:dark_matter_block', '#forge:plates/invar');
	event.replaceInput({id: 'projectexpansion:transmutation_interface'}, 'projectexpansion:final_star_shard', 'kubejs:cube3');
	event.replaceInput({id: 'projectexpansion:transmutation_interface'}, 'projectexpansion:final_emc_link', 'kubejs:cube2');
	event.replaceInput({id: 'projectexpansion:emc_link/basic'}, 'projecte:condenser_mk1', 'minecraft:barrel');
	event.replaceInput({id: 'projectexpansion:emc_link/basic'}, 'projecte:transmutation_tablet', '#forge:plates/invar');
	event.replaceInput({id: 'projectexpansion:emc_link/dark'}, 'projecte:dark_matter', 'kubejs:cube1');
	event.replaceInput({id: 'projectexpansion:emc_link/red'}, 'projecte:red_matter', 'kubejs:cube2');
	event.replaceInput({id: 'projectexpansion:emc_link/magenta'}, 'projectexpansion:magenta_matter', 'kubejs:cube3');
	event.replaceInput({id: 'projectexpansion:emc_link/pink'}, 'projectexpansion:pink_matter', 'kubejs:cube4');
	event.replaceInput({id: 'mekanism:solar_neutron_activator'}, 'mekanism:hdpe_sheet', 'kubejs:black_hdpe_sheet');
	event.replaceInput({id: 'mekanism:sps_casing'}, 'mekanism:hdpe_sheet', 'kubejs:black_hdpe_sheet');
	event.replaceInput({id: 'immersiveengineering:crafting/hammer'}, 'minecraft:string', 'minecraft:iron_nugget');
	event.replaceInput({id: 'projectexpansion:infinite_steak'}, 'minecraft:cooked_beef', 'minecraft:wheat');
	event.replaceInput({id: 'projecte:body_stone'}, 'projecte:red_matter', 'projecte:mobius_fuel');
	event.replaceInput({id: 'rftoolsutility:crafter1'}, 'minecraft:crafting_table', 'thermal:machine_crafter');
	event.replaceInput({id: 'pipez:gas_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:energy_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:fluid_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'pipez:item_pipe'}, 'minecraft:redstone', 'minecraft:glowstone_dust');
	event.replaceInput({id: 'industrialforegoing:mob_imprisonment_tool'}, 'minecraft:ghast_tear', 'projecte:dark_matter');
	event.replaceInput({id: 'industrialforegoing:stasis_chamber'}, 'minecraft:ghast_tear', 'projecte:dark_matter');
	event.replaceInput({id: 'itemcollectors:basic_collector'}, 'minecraft:ender_pearl', 'thermal:device_collector');

	event.replaceOutput({id: 'immersiveengineering:smelting/ingot_steel_from_dust'}, 'immersiveengineering:ingot_steel', 'mekanism:ingot_steel');
	event.replaceOutput({id: 'immersiveengineering:smelting/ingot_steel_from_dust_from_blasting'}, 'immersiveengineering:ingot_steel', 'mekanism:ingot_steel');
	event.replaceOutput({id: 'immersiveengineering:arcfurnace/steel'}, 'immersiveengineering:ingot_steel', 'mekanism:ingot_steel');
	event.replaceOutput({id: 'immersiveengineering:arcfurnace/dust_steel'}, 'immersiveengineering:ingot_steel', 'mekanism:ingot_steel');
	
	['gold', 'iron', 'electrum', 'constantan', 'nickel', 'silver', 'lead', 'copper'].forEach((metal) => {
		event.replaceOutput({id: `immersiveengineering:crafting/plate_${metal}_hammering`}, `immersiveengineering:plate_${metal}`, `thermal:${metal}_plate`);
		event.replaceOutput({id: `immersiveengineering:metalpress/plate_${metal}`}, `immersiveengineering:plate_${metal}`, `thermal:${metal}_plate`);
	});
	
	let cube_packaging = (output, input, cooldown) => {
		event.recipes.thermal.press([output], [input]).energy(cooldown * 20);
	}
	
	let bottling = (output, input) => {
		event.recipes.thermal.bottler(output, input).energy(800);
		event.recipes.createFilling(output, input);
	}
	

  let resetNBT = (output, input, prefix, suffix) => {
    event.shapeless(output, [input]);
  }
  
  [
    {suffix: 'module',
      list: ['stack_size', 'low_extraction', 'medium_extraction', 'high_extraction',
        'low_filter', 'medium_filter', 'high_filter',
        'low_crafting', 'medium_crafting', 'high_crafting',
        'low_retrieval', 'medium_retrieval', 'high_retrieval'
      ]
    },
    {suffix: 'modifier',
      list: ['filter_increase', 'tag_filter']
    }
  ].forEach(moduleListObj => {
    let prefix = 'prettypipes';
    let suffix = moduleListObj.suffix;
    moduleListObj.list.forEach(module => {
      let name = `${prefix}:${module}_${suffix}`;
      console.log('resetting NBT for ' + name);
      resetNBT(name, name);
    })
  });
	
	// ADDING RECIPES
	event.shaped('kubejs:press_rod_die', [
		' I ',
		'ISI',
		' I '
		], {
			I: '#forge:plates/invar',
			S: '#forge:rods/gold'
	});
	
	event.shaped('kubejs:dormant_effigy', [
		' C ',
		'III',
		' I '
		], {
			I: 'minecraft:calcite',
			C: 'projecte:soul_stone'
	});

	event.shaped('projecte:soul_stone', [
		'GGG',
		'LEL',
		'GGG'
		], {
			G: 'minecraft:glowstone_dust',
			L: 'minecraft:lapis_lazuli',
			E: 'minecraft:redstone'
	});
	
	event.shaped('minecraft:skeleton_skull', [
		'GGG',
		'GEG',
		'GGG'
		], {
			G: 'minecraft:bone_block',
			E: 'projecte:body_stone'
	});
	
	event.remove({id: 'immersiveengineering:crafting/stick_iron'});
	event.shaped('immersiveengineering:stick_iron', [
		'I',
		'I'
		], {
			I: '#forge:ingots/iron'
	});
	
	event.remove({id: 'immersiveengineering:crafting/stick_steel'});
	event.shaped('immersiveengineering:stick_steel', [
		'I',
		'I'
		], {
			I: '#forge:ingots/steel'
	});
	
	event.remove({id: 'immersiveengineering:crafting/stick_aluminum'});
	event.shaped('immersiveengineering:stick_aluminum', [
		'I',
		'I'
		], {
			I: '#forge:ingots/aluminum'
	});
	
	['gold', 'copper'].forEach((metal) => {
		event.shaped(`createaddition:${metal}_rod`, [
		'I',
		'I'
		], {
			I: `#forge:ingots/${metal}`
		});
	});
	
	event.shaped('kubejs:bronze_rod', [
		'I',
		'I'
		], {
			I: '#forge:ingots/bronze'
	});
	
	event.shaped('kubejs:diamond_rod', [
		'I',
		'I'
		], {
			I: '#forge:gems/diamond'
	});
	
	event.shaped('kubejs:aluminum_gear', [
		' A ',
		'AIA',
		' A '
		], {
			I: '#forge:nuggets/iron',
			A: '#forge:ingots/aluminum'
		});
		
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
	
	event.shaped('create:creative_motor', [
		' A ',
		'DBD',
		' C '
		], {
			A: 'create:steam_engine',
			B: 'projectexpansion:final_star',
			C: 'create:rotation_speed_controller',
			D: 'create:brass_casing'
		}
	);
	
	event.shaped(Item.of('mekanism:creative_energy_cube', '{mekData:{EnergyContainers:[{Container:0b,stored:"18446744073709551615.9999"}]}}'), [
		' A ',
		'DBC',
		' E '
		], {
			A: 'projectexpansion:final_star',
			B: Item.of('mekanism:ultimate_energy_cube', '{mekData:{EnergyContainers:[{Container:0b,stored:"256000000"}]}}'),
			C: 'powah:ender_gate_nitro',
			D: 'powah:ender_cell_nitro',
			E: 'kubejs:enriched_black_essence'
		}
	);
	
	event.shaped('mekanism:creative_fluid_tank', [
		' A ',
		'DBD',
		' E '
		], {
			A: 'projectexpansion:final_star',
			B: 'mekanism:ultimate_fluid_tank',
			D: 'industrialforegoing:ether_gas_bucket',
			E: 'kubejs:enriched_black_essence'
		}
	);
	
	event.shaped('mekanism:creative_chemical_tank', [
		'DAD',
		'DBD',
		'DAD'
		], {
			A: 'projectexpansion:final_star_shard',
			B: 'mekanism:ultimate_chemical_tank',
			D: 'mekanism:pellet_antimatter',
		}
	);
	
	event.shaped('refinedstorage:creative_storage_disk', [
		' A ',
		'DBD',
		' E '
		], {
			A: 'projectexpansion:final_star',
			B: 'extrastorage:disk_16384k',
			D: 'refinedstorage:controller',
			E: 'kubejs:enriched_black_essence'
		}
	);

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

	event.remove({id: 'rftoolsutility:crafter2'});
	event.shaped('rftoolsutility:crafter2', [
		' A ',
		'BCB',
		' A '
		], {
			A: 'minecraft:redstone_torch',
			B: 'thermal:machine_crafter',
			C: 'rftoolsutility:crafter1'
		}
	)
	event.remove({id: 'rftoolsutility:crafter3'});
	event.shaped('rftoolsutility:crafter3', [
		' A ',
		'BCB',
		' A '
		], {
			A: 'minecraft:redstone_torch',
			B: 'thermal:machine_crafter',
			C: 'rftoolsutility:crafter2'
		}
	);

	event.remove({id: 'prettypipes:low_speed_module'});
	event.shaped('prettypipes:low_speed_module', [
		' A ',
		'ABA',
		' A '
		], {
			A: 'minecraft:redstone',
			B: 'prettypipes:blank_module'
		}
	);

	event.remove({id: 'prettypipes:medium_speed_module'});
	event.shaped('prettypipes:medium_speed_module', [
		'CAC',
		'ABA',
		'CAC'
		], {
			A: 'minecraft:redstone',
			B: 'prettypipes:low_speed_module',
			C: 'minecraft:iron_ingot'
		}
	);

	event.remove({id: 'prettypipes:high_speed_module'});
	event.shaped('prettypipes:high_speed_module', [
		'CAC',
		'ABA',
		'CAC'
		], {
			A: 'minecraft:redstone',
			B: 'prettypipes:medium_speed_module',
			C: 'minecraft:gold_ingot'
		}
	);

	event.recipes.thermal.pulverizer(['minecraft:dirt', Item.of('minecraft:red_mushroom').withChance(0.25), Item.of('minecraft:brown_mushroom').withChance(0.25)], 'minecraft:podzol').energy(200);
	event.recipes.thermal.press('fluxnetworks:flux_dust', ['kubejs:coated_redstone', 'fluxnetworks:flux_block']).energy(200);

	event.recipes.thermal.bottler(Item.of('kubejs:time_augment', 
	'{AugmentData:{Type:"Machine",MachinePower:1d,MachineEnergy:0.75d,RFXfer:20d}}'),
	[Fluid.of('industrialforegoing:ether_gas', 500), 'kubejs:incomplete_time_augment']);
	
	cube_packaging('kubejs:cube1_packaged', 'kubejs:cube1', 600);
	cube_packaging('kubejs:cube2_packaged', 'kubejs:cube2', 400);
	cube_packaging('kubejs:cube3_packaged', 'kubejs:cube3', 200);
	cube_packaging('kubejs:cube4_packaged', 'kubejs:cube4', 100);
	
	event.recipes.createMixing('kubejs:cube2', ['kubejs:cube1', '#forge:gears/steel', Item.of('#forge:rods/bronze', 2), '#forge:gears/invar']);
	event.recipes.createMixing('kubejs:cube3', ['kubejs:cube2', 'minecraft:fire_charge', 'thermal:lightning_charge', 'thermal:ice_charge', 'thermal:earth_charge', 
	'mekanism:basic_control_circuit']).heated();
	event.recipes.createMixing('kubejs:cube4_inert', ['kubejs:cube3', 'kubejs:fission_pellet', 'projecte:mobius_fuel', 'mekanism:elite_control_circuit']).superheated();
	event.recipes.createMixing(Item.of('extendedcrafting:luminessence', 4), ['minecraft:gunpowder', 'minecraft:redstone', Fluid.of('thermal:glowstone', 500)]).heated();
  event.recipes.createMixing(Fluid.of('createchromaticreturn:refined_mixture', 250),
    [Item.of('minecraft:smooth_quartz', 8), Item.of('minecraft:glowstone', 8),
    Item.of('projecte:mobius_fuel', 4), Item.of('mekanism:hdpe_sheet', 4),Fluid.of('minecraft:water', 1000)]).superheated();

  
  event.recipes.createMixing(Item.of('createchromaticreturn:chromatic_compound', 2),
    [Item.of('createchromaticreturn:glowing_ingot', 2), Item.of('create:polished_rose_quartz', 2), Item.of('create:powdered_obsidian', 2), 
    Item.of('create:andesite_alloy', 2)]).superheated();
  event.recipes.createMixing('createchromaticreturn:refined_radiance', ['createchromaticreturn:chromatic_compound', Fluid.of('createchromaticreturn:refined_mixture', 100)]).superheated();
	event.recipes.thermal.bottler('kubejs:cube4', [Fluid.of('thermal:refined_fuel', 1000), 'kubejs:cube4_inert']).energy(400000);
	
	bottling('immersiveengineering:treated_wood_horizontal', [Fluid.of('immersiveengineering:creosote', 125), '#minecraft:planks']);
	bottling('kubejs:blaze_effigy', [Fluid.of('minecraft:lava', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:blizz_effigy', [Fluid.of('thermal:ender', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:blitz_effigy', [Fluid.of('thermal:glowstone', 1000), 'kubejs:dormant_effigy']);
	bottling('kubejs:basalz_effigy', [Fluid.of('thermal:redstone', 1000), 'kubejs:dormant_effigy']);
	
	event.recipes.thermal.smelter('mekanism:ingot_steel', ['#forge:ingots/iron', 'immersiveengineering:coal_coke']).energy(4000);
	event.recipes.thermal.smelter('minecraft:calcite', ['minecraft:stone', 'minecraft:bone_meal']).energy(800);
	event.recipes.thermal.smelter('kubejs:black_hdpe_sheet', ['mekanism:hdpe_sheet', 'extendedcrafting:black_iron_ingot']).energy(2000);
	event.recipes.thermal.smelter(Item.of('create:brass_ingot',2 ), ['#forge:ingots/copper', '#forge:ingots/zinc']).energy(2400);
	event.recipes.thermal.smelter('minecraft:amethyst_shard', ['ae2:charged_certus_quartz_crystal', 'kubejs:cube3']).energy(24000);
	event.recipes.thermal.sawmill('minecraft:blaze_rod', 'kubejs:blaze_effigy').energy(2000);
	event.recipes.thermal.sawmill('thermal:blizz_rod', 'kubejs:blizz_effigy').energy(2000);
	event.recipes.thermal.sawmill('thermal:blitz_rod', 'kubejs:blitz_effigy').energy(2000);
	event.recipes.thermal.sawmill('thermal:basalz_rod', 'kubejs:basalz_effigy').energy(2000);
	
	event.shapeless('mekanism:ingot_steel','immersiveengineering:ingot_steel');
	event.shapeless(Item.of('minecraft:andesite', 2), [Item.of('minecraft:cobblestone', 2), Item.of('projecte:high_covalence_dust', 2)]);
	event.shapeless(Item.of('minecraft:andesite', 8), [Item.of('minecraft:cobblestone', 8), Item.of('minecraft:quartz', 1)]);
	event.shapeless('kubejs:cube1', ['#forge:gears/aluminum', Item.of('#forge:rods/copper', 2), '#forge:gears/iron']);
	event.shapeless('kubejs:diamond_plate', ['#forge:gems/diamond', '#forge:gems/diamond', Item.of('immersiveengineering:hammer')])
	.damageIngredient(Item.of('immersiveengineering:hammer'), 1);
	event.shapeless('create:blaze_burner', ['create:empty_blaze_burner', 'kubejs:blaze_effigy']);
	event.shapeless('mekanism:creative_fluid_tank','mekanism:creative_fluid_tank');
	event.shapeless('mekanism:creative_chemical_tank','mekanism:creative_chemical_tank');
	event.shapeless('minecraft:elytra', ['immersiveengineering:glider', 'kubejs:black_hdpe_sheet']);

	event.recipes.createPressing('thermal:invar_plate', 'thermal:invar_ingot');
  event.recipes.createPressing('thermal:constantan_plate', 'thermal:constantan_ingot');
  event.recipes.createPressing('thermal:electrum_plate', 'thermal:electrum_ingot');
  event.recipes.createPressing('thermal:signalum_plate', 'thermal:signalum_ingot');
  event.recipes.createPressing('thermal:lumium_plate', 'thermal:lumium_ingot');
	event.recipes.createPressing('thermal:enderium_plate', 'thermal:enderium_ingot');

	event.recipes.thermal.smelter(Item.of('create:brass_block', 2), ['#forge:storage_blocks/zinc', '#forge:storage_blocks/copper']).energy(21600);
	event.recipes.thermal.smelter(Item.of('thermal:enderium_block', 2), ['#forge:storage_blocks/diamond', Item.of('#forge:storage_blocks/lead', 3), Item.of('#forge:storage_blocks/ender_pearl', 2)]).energy(144000);
	event.recipes.thermal.smelter(Item.of('thermal:lumium_block', 4), ['#forge:storage_blocks/silver', Item.of('#forge:storage_blocks/tin', 3), Item.of('#forge:storage_blocks/glowstone', 2)]).energy(108000);
	event.recipes.thermal.smelter(Item.of('thermal:signalum_block', 4), ['#forge:storage_blocks/silver', Item.of('#forge:storage_blocks/copper', 3), Item.of('#forge:storage_blocks/redstone', 4)]).energy(108000);
	event.recipes.thermal.smelter(Item.of('thermal:constantan_block', 2), ['#forge:storage_blocks/nickel', '#forge:storage_blocks/copper']).energy(28800);
	event.recipes.thermal.smelter(Item.of('thermal:invar_block', 3), ['#forge:storage_blocks/nickel', Item.of('#forge:storage_blocks/iron')]).energy(43200);
	event.recipes.thermal.smelter(Item.of('thermal:electrum_block', 2), ['#forge:storage_blocks/silver', '#forge:storage_blocks/gold']).energy(28800);
	event.recipes.thermal.smelter(Item.of('thermal:bronze_block', 4), ['#forge:storage_blocks/tin', Item.of('#forge:storage_blocks/copper', 3)]).energy(57600);

	event.recipes.thermal.press(['kubejs:steel_gear'], [Item.of('#forge:ingots/steel', 4), 'thermal:press_gear_die']).energy(2400);
	event.recipes.thermal.press(['thermal:diamond_gear'], [Item.of('#forge:gems/diamond', 4), 'thermal:press_gear_die']).energy(2400);
	event.recipes.thermal.press(['kubejs:aluminum_gear'], [Item.of('#forge:ingots/aluminum', 4), 'thermal:press_gear_die']).energy(2400);
	event.recipes.thermal.press([Item.of('kubejs:bronze_rod', 2)], ['#forge:ingots/bronze', 'kubejs:press_rod_die']).energy(600);
	event.recipes.thermal.press([Item.of('createaddition:copper_rod', 2)], ['#forge:ingots/copper', 'kubejs:press_rod_die']).energy(600);
	event.recipes.thermal.press([Item.of('createaddition:gold_rod', 2)], ['#forge:ingots/gold', 'kubejs:press_rod_die']).energy(600);
	event.recipes.thermal.press('kubejs:diamond_rod', ['#forge:gems/diamond', 'kubejs:press_rod_die']).energy(600);
	event.recipes.thermal.press('kubejs:diamond_plate', Item.of('#forge:gems/diamond', 2)).energy(600);
	event.recipes.thermal.press([Item.of('immersiveengineering:stick_iron', 2)], ['#forge:ingots/iron', 'kubejs:press_rod_die']).energy(600);
	event.recipes.thermal.press([Item.of('immersiveengineering:stick_steel', 2)], ['#forge:ingots/steel', 'kubejs:press_rod_die']).energy(600);
	event.recipes.thermal.press([Item.of('immersiveengineering:stick_aluminum', 2)], ['#forge:ingots/aluminum', 'kubejs:press_rod_die']).energy(600);
	
	event.recipes.thermal.crucible(Fluid.of('thermal:crude_oil', 125), 'kubejs:oil_clump').energy(400);

	event.smelting('minecraft:leather', 'immersiveengineering:hemp_fabric');
	event.recipes.createHaunting('minecraft:egg', 'create:dough');

	// end game recipes

	event.recipes.createSequencedAssembly([
		'ae2:silicon_press'
		], 'minecraft:iron_block', [
		event.recipes.createDeploying('kubejs:incomplete_press', ['kubejs:incomplete_press', '#forge:silicon']),
		event.recipes.createPressing('kubejs:incomplete_press', 'kubejs:incomplete_press')
		]).transitionalItem('kubejs:incomplete_press').loops(10);
	
	event.recipes.createSequencedAssembly([
		'ae2:logic_processor_press'
		], 'minecraft:iron_block', [
		event.recipes.createDeploying('kubejs:incomplete_press', ['kubejs:incomplete_press', 'minecraft:gold_ingot']),
		event.recipes.createPressing('kubejs:incomplete_press', 'kubejs:incomplete_press')
		]).transitionalItem('kubejs:incomplete_press').loops(10);

	event.recipes.createSequencedAssembly([
		'ae2:engineering_processor_press'
		], 'minecraft:iron_block', [
		event.recipes.createDeploying('kubejs:incomplete_press', ['kubejs:incomplete_press', 'minecraft:diamond']),
		event.recipes.createPressing('kubejs:incomplete_press', 'kubejs:incomplete_press')
		]).transitionalItem('kubejs:incomplete_press').loops(10);

	event.recipes.createSequencedAssembly([
		'ae2:calculation_processor_press'
		], 'minecraft:iron_block', [
		event.recipes.createDeploying('kubejs:incomplete_press', ['kubejs:incomplete_press', 'ae2:certus_quartz_crystal']),
		event.recipes.createPressing('kubejs:incomplete_press', 'kubejs:incomplete_press')
		]).transitionalItem('kubejs:incomplete_press').loops(10);
	
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
	
	// custom recipes
	event.custom({
		type:"createaddition:rolling",
		input: {
			tag: "forge:ingots/bronze"
		},
		result: {
			item: "kubejs:bronze_rod",
			count: 2
		}
	});
	
	event.custom({
		type:"createaddition:rolling",
		input: {
			tag: "forge:gems/diamond"
		},
		result: {
			item: "kubejs:diamond_rod",
			count: 1
		}
	});

	// custom energizing orb recipes
	event.custom({
		type: "powah:energizing",
		ingredients: [
		  {
			item: "minecraft:iron_block"
		  },
		  {
			item: "minecraft:gold_block"
		  }
		],
		energy: 90000,
		result: {
		  item: "powah:energized_steel_block",
		  count: 2
		}
	});

	event.custom({
		type: "powah:energizing",
		ingredients: [
		  {
			item: "minecraft:diamond_block"
		  }
		],
		energy: 2700000,
		result: {
		  item: "powah:niotic_crystal_block",
		  count: 1
		}
	});

	event.custom({
		type: "powah:energizing",
		ingredients: [
		  {
			item: "minecraft:emerald_block"
		  }
		],
		energy: 9000000,
		result: {
		  item: "powah:spirited_crystal_block",
		  count: 1
		}
	});

	// pulverizer catalysts 
	event.recipes.thermal.pulverizer_catalyst('thermal:basalz_powder').primaryMod(1.25).secondaryMod(3.0).energyMod(0.75).minChance(0.0).useChance(0.5);
	event.recipes.thermal.pulverizer_catalyst('thermal:earth_charge').primaryMod(1.5).secondaryMod(4.0).energyMod(0.5).minChance(0.0).useChance(0.375);
	event.recipes.thermal.pulverizer_catalyst('kubejs:basilic_reagent').primaryMod(2.0).secondaryMod(5.0).energyMod(0.25).minChance(0.0).useChance(0.25);
	event.recipes.thermal.pulverizer_catalyst('kubejs:enriched_basilic_reagent').primaryMod(2.5).secondaryMod(5.0).energyMod(0.25).minChance(0.0).useChance(0.1);
	
	// REMOVING RECIPES
	event.remove({ id: '/projectexpansion:power_flower/' });
	event.remove({ id: '/projectexpansion:collector/' });
	event.remove({ id: '/projectexpansion:relay/' });
	event.remove({id: 'projecte:soul_stone'});
	event.remove({id: 'betterendforge:gunpowder_from_sulphur'});
	event.remove({id: 'extendedcrafting:black_iron_ingot'});
	event.remove({id: 'extendedcrafting:luminessence'});
	event.remove({id: 'industrialforegoing:iron_gear'});
	event.remove({id: 'industrialforegoing:gold_gear'});
	event.remove({id: 'industrialforegoing:diamond_gear'});
	event.remove({id: 'projectexpansion:star/final_star_shard'});
	event.remove({id: 'projectexpansion:star/final_star'});
	event.remove({id: 'industrialforegoing:ore_laser_base'});
	event.remove({id: 'createchromaticreturn:motor_recipe'});
	event.remove({id: 'thermal:machines/pyrolyzer/pyrolyzer_coal'});
	event.remove({id: 'createchromaticreturn:cf_to_gp'});
	event.remove({id: 'createchromaticreturn:gp_to_bp'});
	event.remove({id: 'createchromaticreturn:cf_to_gs'});
	event.remove({id: 'createchromaticreturn:refined_radiance_recipe'});
	event.remove({id: 'createchromaticreturn:refined_mixture_recipe'});
	event.remove({id: 'createchromaticreturn:chromatic_compound_recipe'});
	event.remove({id: 'createchromaticreturn:bedrock_shard_crushing'});
	event.remove({id: 'projecte:conversions/alchemical_coal_to_coal'});

	['1', '2', '3'].forEach(num => {	
		event.remove({id: `projecte:relay_mk${num}`});
		event.remove({id: `projecte:collector_mk${num}`});	
	})

	event.remove({id: 'immersiveengineering:crafting/constantan_mix'});
	event.remove({id: 'immersiveengineering:crafting/electrum_mix'});
	
})

ServerEvents.tags('item', event => {
	// Add items to tags
	event.add('thermal:crafting/dies', 'kubejs:press_rod_die')
	event.add('thermal:crafting/dies', 'fluxnetworks:flux_block');
	event.add('forge:slimeballs', 'kubejs:oil_clump')
	event.add('forge:gears/steel', 'kubejs:steel_gear')
	event.add('forge:gears', 'kubejs:steel_gear')
	event.add('forge:rods/bronze', 'kubejs:bronze_rod')
	event.add('forge:rods', 'kubejs:bronze_rod')
	event.add('forge:rods/diamond', 'kubejs:diamond_rod')
	event.add('forge:rods', 'kubejs:diamond_rod')
	event.add('forge:plates/diamond', 'kubejs:diamond_plate')
	event.add('forge:plates', 'kubejs:diamond_plate')
	event.add('forge:gears/aluminum', 'kubejs:aluminum_gear')
	event.add('forge:gears', 'kubejs:aluminum_gear')
	event.add('forge:storage_blocks/steel', 'industrialforegoing:steel_block')

	// Remove items from tags
	event.remove('forge:gears', 'industrialforegoing:iron_gear')
	event.remove('forge:gears', 'industrialforegoing:gold_gear')
	event.remove('forge:gears', 'industrialforegoing:diamond_gear')
	event.remove('forge:gears/iron', 'industrialforegoing:iron_gear')
	event.remove('forge:gears/gold', 'industrialforegoing:gold_gear')
	event.remove('forge:gears/diamond', 'industrialforegoing:diamond_gear')
})