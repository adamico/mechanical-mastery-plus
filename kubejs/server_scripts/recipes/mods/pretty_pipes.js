ServerEvents.recipes(event => {
  let resetNBT = (output, input, prefix, suffix) => {
    event.shapeless(output, [input]);
  }
  
  let prettyPipesModules = [
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
  ]
  
  prettyPipesModules.forEach(moduleListObj => {
    let prefix = 'prettypipes';
    let suffix = moduleListObj.suffix;
    moduleListObj.list.forEach(module => {
      let name = `${prefix}:${module}_${suffix}`;
      console.log('resetting NBT for ' + name);
      resetNBT(name, name);
    })
  });
  
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

  event.replaceInput({id: 'prettypipes:round_robin_sorting_modifier'}, 'minecraft:arrow', 'minecraft:flint');
  event.replaceInput({id: 'prettypipes:blank_module'}, 'minecraft:quartz', 'minecraft:iron_nugget');
});