ServerEvents.recipes(event => {
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
			S: 'minecraft:dragon_head',
			Y: "create:rotation_speed_controller",
			C: "create:shaft"
    }
	);

	event.remove({id: 'createchromaticreturn:cf_to_gp'});
	event.remove({id: 'createchromaticreturn:gp_to_bp'});
	event.remove({id: 'createchromaticreturn:cf_to_gs'});
	
	event.remove({id: 'createchromaticreturn:carbon_powder_recipe'});
	event.recipes.createCrushing([Item.of('createchromaticreturn:bedrock_shard').withChance(0.01), Item.of('createchromaticreturn:carbon_powder', 8).withChance(0.5)], 'minecraft:gilded_blackstone').processingTime(15000);

	event.remove({id: 'createchromaticreturn:refined_mixture_recipe'});
	event.recipes.createMixing(Fluid.of('kubejs:light_essence', 1000), [Item.of('minecraft:smooth_quartz', 8), Item.of('minecraft:glowstone', 8), Item.of('mekanism:hdpe_sheet', 4), Fluid.of('minecraft:water', 1000)]).superheated();  
  
	event.remove({id: 'createchromaticreturn:chromatic_compound_recipe'});
	event.recipes.createMixing(Item.of('createchromaticreturn:chromatic_compound', 2), [Item.of('createchromaticreturn:glowing_ingot', 2), Item.of('create:polished_rose_quartz', 2), Item.of('#forge:dusts/obsidian', 2), Item.of('create:andesite_alloy', 2)]).superheated();

	event.remove({id: 'createchromaticreturn:shadow_steel_recipe'});
	event.recipes.createMixing(Item.of('createchromaticreturn:shadow_steel'), [Item.of('createchromaticreturn:chromatic_compound', 16), Fluid.of('kubejs:darkness_essence', 1000)]).superheated();

	event.remove({id: 'createchromaticreturn:refined_radiance_recipe'});
	event.recipes.createMixing(Item.of('createchromaticreturn:refined_radiance'), [Item.of('createchromaticreturn:chromatic_compound', 16), Fluid.of('kubejs:light_essence', 1000)]).superheated();
});