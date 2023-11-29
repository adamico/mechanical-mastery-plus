ServerEvents.recipes(event => {
  const createPermit = (tier, itemsToBuy, itemsToSell, centralItem, name) => {
    if (name == null) {
      name = `"text":"[T${tier}] Trade Permit"`
    }

    let toBuyStrings = [];
    itemsToBuy.map(item => {
      toBuyStrings.push(`'[{"text":"Buy: ${item}","italic":false}]'`);
    });
    let toBuy = toBuyStrings.join(',');

    let toSellStrings = [];
    itemsToSell.map(item => {
      toSellStrings.push(`'[{"text":"Sell: ${item}","italic":false}]'`);
    });
    let toSell = toSellStrings.join(',');

    let lore = `Lore:[${toBuy},${toSell}]`;
    let fullName = `Name:'[{${name},"color":"magenta","italic":false}]'`;
    let key = "key:" + tier;
    let nbt = `{display:{${lore},${fullName}},${key}}`;
    let permitItem = Item.of('adminshop:permit', nbt);

    let recipeArray = [
      "PPP",
      "PEP",
      "PPP"
    ];
    let recipeHash = { P: "minecraft:paper", E: centralItem };
    let recipeId = `adminshop:permit_tier_${tier}`;
    event.shaped(permitItem, recipeArray, recipeHash).id(recipeId);
  }

  let tier1BItems = ['Raw Zinc', 'Kelp', 'Tiny Dry Rubber', 'Latex', 'Bone', 'Lapis Ore', 'Gobber Ore', 'Apatite Ore', 'Cobbled Deepslate'];
  let tier1SItems = ['Regular Mechanical Crystal'];
  createPermit(1, tier1BItems, tier1SItems, 'kubejs:cube1');
});