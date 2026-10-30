// priority: 0

// What decomposing gives, by hand or on a Recycler (#166): each essence's one fixed mix at full amounts, in the
// order a Recycler puts it on its outputs, of which a decompose returns its yield (#184: a quarter on a tier-1
// core and by hand, each part left over the chance of one more), and the Recycler's parts, made from what
// Advanced decomposes into and the island tree's planks. All placeholders, which #168's balance pass replaces.
// What other items recycle into the mod derives from their recipes; a mechanicalmasteryreborn:recycling recipe
// here would override one.
ServerEvents.recipes(event => {
  // An empty bucket, not water and lava: the island has a source of each, and the quest book's emergency items
  // a bucket of each, as Mechanical Mastery Plus did.
  event.recipes.mechanicalmasteryreborn.decomposing([
    'minecraft:piston',
    'minecraft:hopper',
    'minecraft:comparator',
    'minecraft:bucket'
  ], '1x mechanicalmasteryreborn:advanced_mechanical_essence')
    .id('mechanicalmasteryreborn:decomposing/advanced_mechanical_essence')

  // A whole Recycler takes a core, a drive, an input, two outputs for Advanced's four results and four
  // casings: two pistons, three hoppers and a comparator, about twelve decomposes by hand at a quarter.
  const part = (result, ingredients) => event.recipes.craftworks.assembling(result, ingredients)
    .id(`mechanicalmasteryreborn:assembling/${result.split(':')[1]}`)
  part('mechanicalmasteryreborn:recycler_core_1', ['minecraft:piston', 'minecraft:comparator', '4x #minecraft:planks'])
  part('mechanicalmasteryreborn:hand_drive', ['minecraft:piston', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_input', ['minecraft:hopper', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_output', ['minecraft:hopper', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_casing', ['4x #minecraft:planks'])
})
