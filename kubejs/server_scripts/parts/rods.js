ServerEvents.recipes(event => {
  console.log('[AMMONIUM@KUBEJS]: rods & sticks recipes...');  
  
  const rodMaterials = ['iron', 'brass', 'copper', 'gold'];

  rodMaterials.forEach(material => {
    event.custom({
      type: "ae2:inscriber",
      ingredients: {
        middle: {
          tag: `forge:plates/${material}`
        }
      },
      mode: 'inscribe',
      result: {
        item: `createaddition:${material}_rod`
      }
    });

    event.recipes.thermal.press([Item.of(`createaddition:${material}_rod`, 2)], [`#forge:ingots/${material}`, 'kubejs:press_rod_die']).energy(600);
  });
  
	['bronze', 'steel'].forEach(metal => {
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