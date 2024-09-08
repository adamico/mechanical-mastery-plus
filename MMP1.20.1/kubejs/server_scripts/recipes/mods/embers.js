ServerEvents.recipes(event => {
  event.replaceInput({id: 'embers:tinker_hammer'}, '#forge:ingots/lead', '#forge:ingots/copper')

  function coining(metal) {
    event.shapeless(`createdeco:${metal}_coin`, [
      `#forge:nuggets/${metal}`,
      'embers:tinker_hammer'
    ]).keepIngredient('embers:tinker_hammer')
  }

  ['copper', 'gold', 'iron'].forEach(metal => {
    coining(metal)
  })
})