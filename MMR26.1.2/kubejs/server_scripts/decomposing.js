// priority: 0

// What decomposing gives, by hand or on a Recycler (#166): each essence's one fixed mix at full amounts, in the
// order a Recycler puts it on its outputs, of which a decompose returns its yield (#184: a quarter on a tier-1
// core and by hand, each part left over the chance of one more), and the Recycler's parts, made from wood and stone
// alone so that a team builds its first Recycler from the island. The amounts, one of each kind, and the parts' costs
// are #207's first balance set: the amounts shape the build economy, not the win, so they stay at one until a
// playtest names a shortage, and cobblestone stays the scarcest resource by design.
// What other items recycle into the mod derives from their recipes; a mechanicalmasteryreborn:recycling recipe
// here would override one.
ServerEvents.recipes(event => {
  // Advanced's mix (#209): intermediate-2 and deeper items across the pack's mods (excluding decorative blocks,
  // tools/armor, non-64-stackable items, and the excluded items in docs/research/advanced-mix-candidates.md).
  // One of each kind; a Recycler with fewer output slots than the mix returns a random choice of what fits (#216).
  event.recipes.mechanicalmasteryreborn.decomposing([
    // Suite: Beltworks, Craftworks, Fieldworks, Gearworks, Pipeworks, Voidworks, Wireworks
    'beltworks:loader',
    'beltworks:feeder',
    'beltworks:splitter',
    'craftworks:refiner',
    'craftworks:assembler_1',
    'fieldworks:harvester_1',
    'gearworks:gear_bench',
    'pipeworks:pump',
    'pipeworks:outlet',
    'voidworks:void_crucible',
    'wireworks:small_pole',
    'wireworks:boiler',
    'wireworks:steam_engine',
    // Vanilla
    'minecraft:anvil',
    'minecraft:blast_furnace',
    'minecraft:smoker',
    'minecraft:hopper',
    'minecraft:copper_chest',
    'minecraft:lectern',
    'minecraft:crafter',
    'minecraft:dispenser',
    'minecraft:sticky_piston',
    'minecraft:comparator',
    'minecraft:repeater',
    'minecraft:daylight_detector',
    'minecraft:target',
    'minecraft:lever',
    'minecraft:rail',
    'minecraft:powered_rail',
    'minecraft:creaking_heart',
    'minecraft:map',
    // Theurgy
    'theurgy:liquefaction_cauldron',
    'theurgy:calcination_oven',
    'theurgy:distiller',
    'theurgy:pyromantic_brazier',
    'theurgy:sal_ammoniac_accumulator',
    'theurgy:sal_ammoniac_tank',
    'theurgy:incubator',
    'theurgy:incubator_mercury_vessel',
    'theurgy:incubator_salt_vessel',
    'theurgy:incubator_sulfur_vessel',
    'theurgy:fermentation_vat',
    'theurgy:digestion_vat',
    'theurgy:mercury_catalyst',
    'theurgy:mercury_capacitor',
    'theurgy:caloric_flux_emitter',
    'theurgy:mercury_flux_emitter',
    'theurgy:sulfuric_flux_emitter',
    'theurgy:logistics_fluid_inserter',
    'theurgy:logistics_fluid_extractor',
    'theurgy:logistics_mercury_flux_connector',
    // Neo Vitae
    'neovitae:ara_vitae',
    'neovitae:hellfire_forge',
    'mechanicalmasteryreborn:recycler_core_1',
    'mechanicalmasteryreborn:hand_drive',
    'mechanicalmasteryreborn:recycler_input',
    'mechanicalmasteryreborn:recycler_output',
  ], '1x mechanicalmasteryreborn:advanced_mechanical_essence')
    .id('mechanicalmasteryreborn:decomposing/advanced_mechanical_essence')

  // A whole Recycler takes a core and eight more parts, at least one of them a drive, one an input and one an output.
  const part = (result, ingredients) => event.recipes.craftworks.assembling(result, ingredients)
    .id(`mechanicalmasteryreborn:assembling/${result.split(':')[1]}`)
  part('mechanicalmasteryreborn:recycler_core_1', ['4x minecraft:smooth_stone', '4x #minecraft:planks'])
  part('mechanicalmasteryreborn:hand_drive', ['2x minecraft:stick', '2x #minecraft:planks', 'minecraft:cobblestone'])
  part('mechanicalmasteryreborn:fe_drive', ['mechanicalmasteryreborn:hand_drive', '4x minecraft:redstone', '2x minecraft:iron_ingot'])
  part('mechanicalmasteryreborn:recycler_input', ['minecraft:chest', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_output', ['minecraft:chest', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_casing', ['4x #minecraft:planks'])
})
