ServerEvents.recipes(event => {
  // remove thermal:flax_block because apart storage purposes is not a functional block
  event.remove([{ output: 'thermal:flax_block' }, { input: 'thermal:flax_block' }])

  // replace thermal:flax/flax_seeds with supplementaries:flax/flax_seeds
  event.replaceInput({}, 'thermal:flax_seeds', 'supplementaries:flax_seeds')
  event.replaceInput({}, 'thermal:flax', 'supplementaries:flax')
  event.replaceOutput({}, 'thermal:flax', 'supplementaries:flax')
  event.replaceOutput({}, 'thermal:flax_seeds', 'supplementaries:flax_seeds')

  // obtain string from any #forge:crops/flax
  event.remove({ id: 'supplementaries:strings' })
  event.shapeless('minecraft:string', '#forge:crops/flax').id('string_from_flax')
})
