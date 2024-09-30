// priority:1000
ServerEvents.tags('item', event => {
  const FUEL_BLOCKS = ['minecraft:coal_block', 'mekanism:block_charcoal', 'modern_industrialization:lignite_coal_block', 'railcraft:coke_coal_block']
  FUEL_BLOCKS.forEach(fuel_block => { 
    event.add('tt_pack:fuel_blocks', fuel_block)
  });
});