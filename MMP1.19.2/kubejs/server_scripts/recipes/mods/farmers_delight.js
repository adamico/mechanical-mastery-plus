ServerEvents.recipes(event => {
  event.custom({
    type: 'farmersdelight:cutting',
    ingredients: [
      { item: 'minecraft:dripstone_block' }
    ],
    tool: { item: 'kubejs:ore_hammer' },
    result: [
      { item: 'minecraft:pointed_dripstone'}
    ]
  });

  event.remove({id: 'farmersdelight:carrot_from_crate'});
});