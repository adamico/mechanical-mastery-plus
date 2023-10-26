ServerEvents.recipes(event => {
  console.log('[kc00l@KUBEJS]: rods & sticks recipes...');  
  
  const rodMaterials = ['iron', 'copper', 'gold'];

  rodMaterials.forEach(material => {
    event.recipes.thermal.press([Item.of(`createaddition:${material}_rod`, 2)], [`#forge:ingots/${material}`, 'kubejs:press_rod_die']).energy(600);
  });
  
	['steel'].forEach(metal => {
		event.custom({
			type:"createaddition:rolling",
			input: {
				tag: `forge:ingots/${metal}`
			},
			result: {
				item: `kubejs:${metal}_rod`,
				count: 2
			}
		})		
	});
  
  event.shaped('kubejs:press_rod_die', [
		' I ',
		'ISI',
		' I '
		], {
			I: '#forge:plates/invar',
			S: '#forge:rods/gold'
	});
});