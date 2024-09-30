ServerEvents.recipes(event => {
	const all_metals = [
		{ name: 'iron',            ingot: 'iron_ingot',      mod: 'minecraft',                input_tag: "#minecraft:iron_ores" },
		{ name: 'copper',          ingot: 'copper_ingot',    mod: 'minecraft',                input_tag: "#minecraft:copper_ores" },
    { name: 'tin',             ingot: 'ingot_tin',       mod: 'mekanism',                 input_tag: "#c:ores/tin" },
    { name: 'lead',            ingot: 'ingot_lead',      mod: 'mekanism',                 input_tag: "#c:ores/lead" },
    { name: 'silver',          ingot: 'silver_ingot',    mod: 'occultism',                input_tag: "#c:ores/silver" },
		{ name: 'antimony',        ingot: 'antimony_ingot',  mod: 'modern_industrialization', input_tag: "#c:ores/antimony" },
		{ name: 'zinc',            ingot: 'zinc_ingot',      mod: 'railcraft',                input_tag: "#c:ores/zinc" },
    { name: 'gold',            ingot: 'gold_ingot',      mod: 'minecraft',                input_tag: "#minecraft:gold_ores" },
		{ name: 'osmium',          ingot: 'ingot_osmium',    mod: 'mekanism',                 input_tag: "#c:ores/osmium" },
		{ name: 'nickel',          ingot: 'nickel_ingot',    mod: 'modern_industrialization', input_tag: "#c:ores/nickel" },
		{ name: 'iesnium',         ingot: 'iesnium_ingot',   mod: 'occultism',                input_tag: "#c:ores/iesnium" },
		{ name: 'uranium',         ingot: 'ingot_uranium',   mod: 'mekanism',                 input_tag: "#c:ores/uranium" },
    { name: 'tungsten',        ingot: 'tungsten_ingot',  mod: 'modern_industrialization', input_tag: "#c:ores/tungsten" },
    { name: 'iridium',         ingot: 'iridium_ingot',   mod: 'modern_industrialization', input_tag: "#c:ores/iridium" },
    { name: 'platinum',        ingot: 'platinum_ingot',  mod: 'modern_industrialization', input_tag: "#c:ores/platinum" },
    { name: 'diamond',         ingot: 'diamond',         mod: 'minecraft',                input_tag: "#c:ores/diamond" },
    { name: 'emerald',         ingot: 'emerald',         mod: 'minecraft',                input_tag: "#c:ores/emerald" },
    { name: 'lapis',           ingot: 'lapis',           mod: 'minecraft',                input_tag: "#c:ores/lapis" },
    { name: 'redstone',        ingot: 'redstone',        mod: 'minecraft',                input_tag: "#c:ores/redstone" },
    { name: 'nether_quartz',   ingot: 'nether_quartz',   mod: 'minecraft',                input_tag: "#c:ores/quartz" },
    { name: 'quartz',          ingot: 'quartz',          mod: 'modern_industrialization', input_tag: "#c:ores/quartz" },
    { name: 'coal',            ingot: 'coal',            mod: 'minecraft',                input_tag: "#minecraft:coal_ores" }
	];

  // theurgy salt to add

  let process_metals = (metals) => {
    metals.forEach(metal => {
      console.log("Processing " + metal.name + " metal");
			['smelting', 'blasting'].forEach(recipe => {
				event.remove({ type: `minecraft:${recipe}`, input: `${metal.mod}:${metal.name}_ore` });
			});
      ['', 'mechanical_'].forEach(recipe => {
				event.remove({ type: `integrateddynamics:${recipe}squeezer`, output: `#c:raw_materials/${metal.name}` });
			});
			event.remove({ input: metal.input_tag });
			event.remove({ input: `${metal.mod}:${metal.name}_ore` });
		});
  };

  ['smelting', 'blasting'].forEach(recipe => {
    event.remove({ type: `minecraft:${recipe}`, input: `minecraft:ancient_debris` });
  });
  ['', 'mechanical_'].forEach(recipe => {
    event.remove({ type: `integrateddynamics:${recipe}squeezer`, output: 'minecraft:netherite_scrap' });
  });
  event.remove({ input: "#c:ores/netherite_scrap" });

  process_metals(all_metals);
});