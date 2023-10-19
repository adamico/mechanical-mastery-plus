ServerEvents.recipes(event => {
  let createPermit = (tier, itemsToBuy, itemsToSell) => {
    let colors = ["pink", "light_blue", "yellow", "magenta"];
    let color = colors[tier - 1];

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
    let name = "Name:'[{\"text\":\"Tier " + tier + ` Trade Permit","color":"${color}","italic":false}]'`;
    let key = "key:"+ tier;
    let nbt = `{display:{${lore},${name}},${key}}`;
    let permitItem = Item.of('adminshop:permit', nbt);

    console.log(nbt);

    event.shaped(permitItem, [
      "PPP",
      "PEP",
      "PPP"
    ], {
      P: "minecraft:paper",
      E: `kubejs:cube${tier}`
    });
  }

  let tier1BItems = ['Raw Zinc', 'Kelp', 'Tiny Dry Rubber', 'Bone', 'Lapis Ore'];
  let tier1SItems = ['Basic Mechanical Crystal'];
  createPermit(1, tier1BItems, tier1SItems);

  let tier2BItems = ['Crude Oil', 'Oil Sand', 'Raw Osmium', 'Diamond Ore', 'Niter Ore', 'Sulfur Ore'];
  let tier2SItems = ['Regular Mechanical Crystal'];
  createPermit(2, tier2BItems, tier2SItems);

  let tier3BItems = ['Emerald Ore', 'Raw Uranium', 'Fluorite Ore', 'Ancient Debris', 'Uraninite'];
  let tier3SItems = ['Improved Mechanical Crystal'];
  createPermit(3, tier3BItems, tier3SItems);

  let tier4BItems = ['Nothing'];
  let tier4SItems = ['Advanced Mechanical Crystal'];
  createPermit(4, tier4BItems, tier4SItems);

  event.replaceInput({id: 'adminshop:shop'}, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
});