ServerEvents.recipes(event => {
  event.remove({id: "enderio:alloy_smelting/dark_steel_ingot"});
  event.recipes.enderio.alloy_smelting("enderio:dark_steel_ingot", ["mekanism:ingot_steel", "minecraft:obsidian"]).energy(3200);
});
