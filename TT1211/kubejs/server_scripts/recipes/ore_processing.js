ServerEvents.recipes(event => {
	const allMetals = [
		{ name: 'iron',            ingot: 'iron_ingot',      mod: 'minecraft',                oreTag: "#minecraft:iron_ores",    rawOre: "raw_iron" },
		{ name: 'copper',          ingot: 'copper_ingot',    mod: 'minecraft',                oreTag: "#minecraft:copper_ores",  rawOre: "raw_copper" },
    { name: 'tin',             ingot: 'tin_ingot',       mod: 'modern_industrialization', oreTag: "#c:ores/tin",             rawOre: "raw_tin" },
    { name: 'lead',            ingot: 'lead_ingot',      mod: 'modern_industrialization', oreTag: "#c:ores/lead",            rawOre: "raw_lead" },
    { name: 'silver',          ingot: 'silver_ingot',    mod: 'occultism',                oreTag: "#c:ores/silver",          rawOre: "raw_silver" },
		{ name: 'antimony',        ingot: 'antimony_ingot',  mod: 'modern_industrialization', oreTag: "#c:ores/antimony",        rawOre: "raw_antimony" },
		{ name: 'zinc',            ingot: 'zinc_ingot',      mod: 'railcraft',                oreTag: "#c:ores/zinc",            rawOre: "zinc_raw" },
    { name: 'gold',            ingot: 'gold_ingot',      mod: 'minecraft',                oreTag: "#minecraft:gold_ores",    rawOre: "raw_gold" },
		{ name: 'osmium',          ingot: 'ingot_osmium',    mod: 'mekanism',                 oreTag: "#c:ores/osmium",          rawOre: "raw_osmium" },
		{ name: 'nickel',          ingot: 'nickel_ingot',    mod: 'modern_industrialization', oreTag: "#c:ores/nickel",          rawOre: "raw_nickel" },
		{ name: 'iesnium',         ingot: 'iesnium_ingot',   mod: 'occultism',                oreTag: "#c:ores/iesnium",         rawOre: "raw_iesnium" },
		{ name: 'uranium',         ingot: 'uranium_ingot',   mod: 'modern_industrialization', oreTag: "#c:ores/uranium",         rawOre: "raw_uranium" },
    { name: 'tungsten',        ingot: 'tungsten_ingot',  mod: 'modern_industrialization', oreTag: "#c:ores/tungsten",        rawOre: "raw_tungsten" },
    { name: 'iridium',         ingot: 'iridium_ingot',   mod: 'modern_industrialization', oreTag: "#c:ores/iridium",         rawOre: "raw_iridium" },
    { name: 'platinum',        ingot: 'platinum_ingot',  mod: 'modern_industrialization', oreTag: "#c:ores/platinum",        rawOre: "raw_platinum" },
    { name: 'diamond',         ingot: 'diamond',         mod: 'minecraft',                oreTag: "#c:ores/diamond",         rawOre: "diamond" },
    { name: 'emerald',         ingot: 'emerald',         mod: 'minecraft',                oreTag: "#c:ores/emerald",         rawOre: "emerald"},
    { name: 'lapis_lazuli',    ingot: 'lapis_lazuli',    mod: 'minecraft',                oreTag: "#c:ores/lapis",           rawOre: "lapis_lazuli" },
    { name: 'redstone',        ingot: 'redstone',        mod: 'minecraft',                oreTag: "#c:ores/redstone",        rawOre: "redstone" },
    { name: 'quartz',          ingot: 'quartz',          mod: 'modern_industrialization', oreTag: "#c:ores/quartz",          rawOre: "quartz" },
    { name: 'coal',            ingot: 'coal',            mod: 'minecraft',                oreTag: "#minecraft:coal_ores",    rawOre: "coal" },
    { name: 'netherite_scrap', ingot: 'netherite_scrap', mod: 'minecraft',                oreTag: "#c:ores/netherite_scrap", rawOre: "netherite_scrap" }
	];

  const gems = ['diamond', 'emerald', 'lapis_lazuli', 'redstone', 'quartz', 'coal', 'netherite_scrap'];
  // theurgy salt to add

  let cleanupRecipes = (metals) => {
    for (const metal of allMetals) {
      let { name, mod, oreTag } = metal;
      let rawOre = `${mod}:${metal.rawOre}`;
      console.log("Cleaning up " + name + " metal");
      ['', 'mechanical_'].forEach(recipe => {
				event.remove({ type: `integrateddynamics:${recipe}squeezer`, output: `#c:raw_materials/${name}` });
			});
			event.remove({ input: oreTag });
			// event.remove({ input: `${mod}:${name}_ore` });

      if (gems.includes(name)) { continue; }

			['smelting', 'blasting'].forEach(recipe => {
				event.remove({ type: `minecraft:${recipe}`, input: `${mod}:${name}_ore` });  
				event.remove({ type: `minecraft:${recipe}`, input: rawOre });
			});
		};
  }

  ['', 'mechanical_'].forEach(recipe => {
    for (const gem of gems) {
      event.remove({ type: `integrateddynamics:${recipe}squeezer`, output: gem });
    };
  });
  event.remove({ input: "#c:ores/netherite_scrap" });

  cleanupRecipes(allMetals);
  
  for (const metal of allMetals) {
    let { name, mod } = metal;
    console.log("processing metal " + name);
    if (gems.includes(name) || name === 'quartz') { continue; }

    let nugget = (mod === 'mekanism') ? `${mod}:nugget_${name}` : `${mod}:${name}_nugget`;
    nugget = (name === 'copper') ? 'modern_industrialization:copper_nugget' : nugget;
    let rawOre = `${mod}:${metal.rawOre}`;
    event.smelting(`3x ${nugget}`, rawOre).xp(0.35);
    event.blasting(`3x ${nugget}`, rawOre).xp(0.35);
  }

  // custom ore processor
  for (const metal of allMetals) {
    let { name, mod, oreTag } = metal;
    let rawOre = `${mod}:${metal.rawOre}`;
    event.recipes.modern_industrialization.ore_processor(4, 120)
      .itemIn(oreTag)
      .itemOut(`3x ${rawOre}`)
      .itemOut(`2x ${rawOre}`, 0.3)
      .itemOut(rawOre, 0.1)
      .fluidIn('kubejs:tt_fuel', 1000)
      .fluidOut('kubejs:tt_spent_fuel', 500)
  };
});