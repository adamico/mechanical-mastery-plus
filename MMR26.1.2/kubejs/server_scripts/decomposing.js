// priority: 0

ServerEvents.recipes(event => {
  const ADVANCED_MIX = [
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
    'neovitae:ara_vitae',
    'neovitae:hellfire_forge',
    'mechanicalmasteryreborn:recycler_core_1',
    'mechanicalmasteryreborn:hand_drive',
    'mechanicalmasteryreborn:recycler_input',
    'mechanicalmasteryreborn:recycler_output',
  ]
  event.recipes.mechanicalmasteryreborn.decomposing(ADVANCED_MIX, '1x mechanicalmasteryreborn:advanced_mechanical_essence')
    .id('mechanicalmasteryreborn:decomposing/advanced_mechanical_essence')

  const NULLESSENCE_MIX = ADVANCED_MIX
  event.recipes.mechanicalmasteryreborn.decomposing(NULLESSENCE_MIX, '1x mechanicalmasteryreborn:nullessence')
    .id('mechanicalmasteryreborn:decomposing/nullessence')

  const part = (result, ingredients) => event.recipes.craftworks.assembling(result, ingredients)
    .id(`mechanicalmasteryreborn:assembling/${result.split(':')[1]}`)
  part('mechanicalmasteryreborn:recycler_core_1', ['4x minecraft:smooth_stone', '4x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_core_2', ['mechanicalmasteryreborn:recycler_core_1', '4x minecraft:iron_ingot', '4x minecraft:smooth_stone'])
  part('mechanicalmasteryreborn:recycler_core_3', ['mechanicalmasteryreborn:recycler_core_2', '4x minecraft:piston', '4x minecraft:iron_bars'])
  part('mechanicalmasteryreborn:recycler_core_4', ['mechanicalmasteryreborn:recycler_core_3', '2x minecraft:hopper', '2x minecraft:blast_furnace'])
  part('mechanicalmasteryreborn:hand_drive', ['2x minecraft:stick', '2x #minecraft:planks', 'minecraft:cobblestone'])
  part('mechanicalmasteryreborn:fe_drive', ['mechanicalmasteryreborn:hand_drive', '4x minecraft:redstone', '2x minecraft:iron_ingot'])
  part('mechanicalmasteryreborn:recycler_input', ['minecraft:chest', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_output', ['minecraft:chest', '2x #minecraft:planks'])
  part('mechanicalmasteryreborn:recycler_casing', ['4x #minecraft:planks'])
})
