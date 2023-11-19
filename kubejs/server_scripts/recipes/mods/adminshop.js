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

  let tier2BItems = ['Crude Oil', 'Oil Sand', 'Raw Osmium', 'Diamond Ore', 'Niter Ore', 'Sulfur Ore', 'Ancient Debris', 'Nether Gobber Ore', 'Basic Create Items'];
  let tier2SItems = ['Improved Mechanical Crystal'];
  createPermit(2, tier2BItems, tier2SItems, 'kubejs:cube2');

  let tier3BItems = ['Emerald Ore', 'Chorus Flower', 'Powah Resources', 'Nuclear ores', 'Mekanism Resources', 'Start a village!', 'Get passive mobs!'];
  let tier3SItems = ['Advanced Mechanical Crystal', "Liquid Pink Slime", "Liquid Meat"];
  createPermit(3, tier3BItems, tier3SItems, 'kubejs:cube3');

  let tier4BItems = ['Enchanting Bottle', 'Rubberwood Sapling', 'More nuclear ores', 'End Gobber Ore', 'Many liquids', 'Many spawn eggs', 'Flux Networks Resources'];
  let tier4SItems = ['Liquid Experience', 'Refined Fuel', 'Lava'];
  createPermit(4, tier4BItems, tier4SItems, 'kubejs:cube4');

  let tier5BItems = ['Nothing'];
  let tier5SItems = ['Block of Iron', 'Block of Gold', 'Block of Copper'];
  let tier5name = '"text":"[SPECIAL] I need money Trade Permit"';
  createPermit(5, tier5BItems, tier5SItems, 'pipez:universal_pipe', tier5name);

  event.replaceInput({ id: 'adminshop:shop' }, 'minecraft:ender_chest', 'ironchest:crystal_chest');
  event.replaceInput({ id: 'adminshop:buyer_1' }, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
  event.replaceInput({ id: 'adminshop:buyer_2' }, 'minecraft:barrel', 'kubejs:cube2_packaged');
  event.replaceInput({ id: 'adminshop:buyer_3' }, 'minecraft:barrel', 'kubejs:cube3_packaged');
  event.replaceInput({ id: 'adminshop:seller' }, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
  event.replaceInput({ id: 'adminshop:fluid_buyer' }, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
  event.replaceInput({ id: 'adminshop:fluid_seller' }, 'minecraft:ender_chest', 'kubejs:cube1_packaged');
});