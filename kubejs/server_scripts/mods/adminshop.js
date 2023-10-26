ServerEvents.recipes(event => {
  let createPermit = (tier, itemsToBuy, itemsToSell) => {
    let toBuyStrings = [];
    itemsToBuy.map(item => {
      toBuyStrings.push("'[{\"text\":\"Buy: " + item + "\",\"italic\":false}]'"); 
    });
    let toBuy = toBuyStrings.join(',');

    let toSellStrings = [];
    itemsToSell.map(item => {
      toSellStrings.push("'[{\"text\":\"Sell: " + item + "\",\"italic\":false}]'");
    });
    let toSell = toSellStrings.join(',');

    let lore = `Lore:[${toBuy},${toSell}]`;
    let name = "Name:'[{\"text\":\"Tier " + tier + " Trade Permit\",\"color\":\"magenta\",\"italic\":false}]'";
    let key = "key:"+ tier;
    let nbt = `{display:{${lore},${name}},${key}}`;
    let permitItem = Item.of('adminshop:permit', nbt);

    event.shaped(permitItem, [
      "PPP",
      "PEP",
      "PPP"
    ], {
      P: "minecraft:paper",
      E: `kubejs:cube${tier}`
    }).id(`adminshop:permit_tier_${tier}`);
  }

  let tier1BItems = ['Raw Zinc', 'Kelp', 'Tiny Dry Rubber', 'Bone', 'Lapis Ore', 'Gobber Ore', 'Apatite Ore', 'Cobbled Deepslate'];
  let tier1SItems = ['Regular Mechanical Crystal'];
  createPermit(1, tier1BItems, tier1SItems);

  let tier2BItems = ['Crude Oil', 'Oil Sand', 'Raw Osmium', 'Diamond Ore', 'Niter Ore', 'Sulfur Ore', 'Ancient Debris', 'Nether Gobber Ore', 'Basic Create Items'];
  let tier2SItems = ['Improved Mechanical Crystal'];
  createPermit(2, tier2BItems, tier2SItems);

  let tier3BItems = ['Emerald Ore', 'Powah Resources', 'Mekanism Resources', 'Start a village!', 'Get passive mobs!'];
  let tier3SItems = ['Advanced Mechanical Crystal', "Liquid Pink Slime", "Liquid Meat"];
  createPermit(3, tier3BItems, tier3SItems);

  let tier4BItems = ['Enchanting Bottle', 'Rubberwood Sapling', 'End Gobber Ore', 'Many liquids', 'Many spawn eggs', 'Flux Networks Resources'];
  let tier4SItems = ['Liquid Experience', 'Refined Fuel', 'Lava'];
  createPermit(4, tier4BItems, tier4SItems);

  event.replaceInput({id: 'adminshop:shop'}, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
  event.replaceInput({id: 'adminshop:buyer_1'}, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
  event.replaceInput({id: 'adminshop:buyer_2'}, 'minecraft:barrel', 'kubejs:cube2_packaged');
  event.replaceInput({id: 'adminshop:buyer_3'}, 'minecraft:barrel', 'kubejs:cube3_packaged');
  event.replaceInput({id: 'adminshop:seller'}, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
  event.replaceInput({id: 'adminshop:fluid_buyer'}, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
  event.replaceInput({id: 'adminshop:fluid_seller'}, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
});