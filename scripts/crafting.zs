import crafttweaker.api.item.IItemStack;

craftingTable.addShaped("creative_energy_cube", <item:mekanism:creative_energy_cube>.withTag({ mekData: {EnergyContainers: [{Container: 0, stored: "18446744073709551615"}]}}),
  [
    [<item:minecraft:air>, <item:minecraft:dragon_head>, <item:minecraft:air>],
    [<item:powah:energy_cell_nitro>.withTag({powah_tile_data: {energy_stored_main_energy: 2000000000 as long}}), <item:mekanism:ultimate_energy_cube>.withTag({mekData: {EnergyContainers: [{Container: 0, stored: "256000000"}]}}), <item:powah:ender_gate_nitro>],
    [<item:minecraft:air>, <item:kubejs:enriched_darkness_essence>, <item:minecraft:air>]
  ]);