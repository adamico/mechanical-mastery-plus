ServerEvents.recipes(event => {
  //functions
  let bigreactors_fluidizersolidmixing = (ing1, ing1Count, ing2, ing2Count, result, resultAmount) => {
    event.custom({
      "type": "bigreactors:fluidizersolidmixing",
      "ingredient1": {
        "ingredient": {
          "item": ing1
        },
        "count": ing1Count
      },
      "ingredient2": {
        "ingredient": {
          "item": ing2
        },
        "count": ing2Count
      },
      "result": {
        "fluid": result,
        "count": resultAmount
      }
    })
    event.custom({
      "type": "bigreactors:fluidizersolidmixing",
      "ingredient1": {
        "ingredient": {
          "item": ing2
        },
        "count": ing2Count
      },
      "ingredient2": {
        "ingredient": {
          "item": ing1
        },
        "count": ing1Count
      },
      "result": {
        "fluid": result,
        "count": resultAmount
      }
    })
  }

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

  event.remove({ id: 'bigreactors:misc/book/erguide' })

  event.remove({ id: 'bigreactors:fluidizer/solidmixing/verderium_2' })
  event.remove({ id: 'bigreactors:fluidizer/solidmixing/verderium_1' })
  event.remove({ id: 'bigreactors:fluidizer/solidmixing/verderium9_1' })
  event.remove({ id: 'bigreactors:fluidizer/solidmixing/verderium9_2' })
  bigreactors_fluidizersolidmixing('mekanism:ingot_uranium', 2, 'bigreactors:blutonium_ingot', 1, 'bigreactors:verderium', 2000)
  bigreactors_fluidizersolidmixing('mekanism:block_uranium', 2, 'bigreactors:blutonium_block', 1, 'bigreactors:verderium', 2000)
})
