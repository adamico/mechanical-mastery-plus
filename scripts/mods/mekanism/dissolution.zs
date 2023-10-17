import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;
import mods.mekanism.api.ingredient.ItemStackIngredient;
import crafttweaker.api.tag.manager.ITagManager;

//Adds a Dissolution Recipe that uses 100 mB of Sulfuric Acid (1 mB per tick) to convert Salt into 10 mB of Hydrogen Chloride.

// <recipetype:mekanism:dissolution>.addRecipe(name as string, itemInput as ItemStackIngredient, gasInput as GasStackIngredient, output as ICrTChemicalStack)

//<recipetype:mekanism:dissolution>.addRecipe("salt_to_hydrogen_chloride", ItemStackIngredient.from(<item:mekanism:salt>), GasStackIngredient.from(<gas:mekanism:sulfuric_acid>), <gas:mekanism:hydrogen_chloride> * 10);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:dissolution>.addRecipe("salt_to_hydrogen_chloride", <item:mekanism:salt>, GasStackIngredient.from(<gas:mekanism:sulfuric_acid>), <gas:mekanism:hydrogen_chloride> * 10);
// <recipetype:mekanism:dissolution>.addRecipe("salt_to_hydrogen_chloride", ItemStackIngredient.from(<item:mekanism:salt>), <gas:mekanism:sulfuric_acid>, <gas:mekanism:hydrogen_chloride> * 10);
// <recipetype:mekanism:dissolution>.addRecipe("salt_to_hydrogen_chloride", <item:mekanism:salt>, <gas:mekanism:sulfuric_acid>, <gas:mekanism:hydrogen_chloride> * 10);


/*
 * Removes two Dissolution Recipes:
 * 1) The recipe for producing Hydrofluoric Acid from Fluorite.
 * 2) The recipe for producing Dirty Lead Slurry from Lead Ore.
*/

// <recipetype:mekanism:dissolution>.removeByName(name as string)

var metals = ["iron", "gold", "copper", "osmium","uranium", "tin", "lead"] as string[];
var thermal_metals = ["silver", "nickel", "zinc"] as string[];

for metal in metals {
  <tagmanager:mekanism/slurry>.tag(<resource:mekanism:clean/${metal}>).add(<slurry:mekanism:clean_${metal}>);
  <tagmanager:mekanism/slurry>.tag(<resource:mekanism:dirty/${metal}>).add(<slurry:mekanism:dirty_${metal}>);
  <recipetype:mekanism:dissolution>.removeByName("mekanism:processing/"+ metal + "/slurry/dirty/from_raw_ore");
  <recipetype:mekanism:dissolution>.addRecipe("processing/" + metal + "/slurry/dirty/from_raw_ore", <tagManager:items>.tag(<resource:forge:raw_materials/${metal}>), <gas:mekanism:sulfuric_acid>, <slurry:mekanism:dirty_${metal}> * 600);
}

for metal in thermal_metals {
  <recipetype:mekanism:dissolution>.removeByName("jaopca:mekanism.raw_material_to_dirty_slurry."+ metal);
  <recipetype:mekanism:dissolution>.addRecipe("processing/" + metal + "/slurry/dirty/from_raw_ore", <tagManager:items>.tag(<resource:forge:raw_materials/${metal}>), <gas:mekanism:sulfuric_acid>, <slurry:jaopca:mekanism_dirty.${metal}> * 600);
}