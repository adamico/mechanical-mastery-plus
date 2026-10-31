// priority: 0

// What decomposing gives, by hand or on a Recycler (#166): each essence's one fixed mix at full amounts, in the
// order a Recycler puts it on its outputs, of which a decompose returns its yield (#184: a quarter on a tier-1
// core and by hand, each part left over the chance of one more), and the Recycler's parts, made from wood and stone
// alone so that a team builds its first Recycler from the island. The amounts and the parts' costs are placeholders,
// which #207's balance pass tunes.
// What other items recycle into the mod derives from their recipes; a mechanicalmasteryreborn:recycling recipe
// here would override one.
ServerEvents.recipes(event => {
  // Advanced's mix, M5 (#209): machines of the suite and vanilla that recycle down into the copper, iron, gold and
  // redstone Theurgy takes, and into wood and stone. Twenty kinds, more than a tier-1 Recycler's outputs hold, so
  // it returns a random choice of them (#216).
  event.recipes.mechanicalmasteryreborn.decomposing([
    '2x wireworks:steam_turbine',
    'wireworks:transformer',
    'wireworks:steam_engine',
    'wireworks:accumulator',
    'fieldworks:harvester_2',
    'craftworks:assembler_2',
    'beltworks:splitter',
    'pipeworks:pump',
    'pipeworks:outlet',
    'wireworks:transmission_pole',
    'gearworks:kinetic_upgrade',
    'gearworks:metabolic_upgrade',
    'voidworks:void_crucible',
    'minecraft:hopper',
    'minecraft:comparator',
    'minecraft:anvil',
    'minecraft:blast_furnace',
    'pipez:improved_upgrade',
    'theurgy:liquefaction_cauldron',
    '2x minecraft:copper_chest'
  ], '1x mechanicalmasteryreborn:advanced_mechanical_essence')
    .id('mechanicalmasteryreborn:decomposing/advanced_mechanical_essence')

  // A whole Recycler takes a core and eight more parts, at least one of them a drive, one an input and one an output.
  const part = (result, ingredients) => event.recipes.craftworks.assembling(result, ingredients)
    .id(`mechanicalmasteryreborn:assembling/${result.split(':')[1]}`)
  part('mechanicalmasteryreborn:recycler_core_1', ['4x minecraft:smooth_stone', '4x #minecraft:planks'])
  part('mechanicalmasteryreborn:hand_drive', ['2x minecraft:stick', '2x #minecraft:planks', 'minecraft:cobblestone'])
  part('mechanicalmasteryreborn:recycler_input', ['minecraft:chest', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_output', ['minecraft:chest', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_casing', ['4x #minecraft:planks'])
})
