import mods.mekanism.api.ingredient.ChemicalStackIngredient.InfusionStackIngredient;
import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Metallurgic Infusing Recipe that uses 10 mB of Fungi Infuse Type to convert any Oak Planks into Crimson Planks.

// <recipetype:mekanism:metallurgic_infusing>.addRecipe(name as string, itemInput as ItemStackIngredient, chemicalInput as InfusionStackIngredient, output as IItemStack)

// <recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", ItemStackIngredient.from(<item:minecraft:oak_planks>), InfusionStackIngredient.from(<infuse_type:mekanism:fungi> * 10), <item:minecraft:crimson_planks>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", <item:minecraft:oak_planks>, InfusionStackIngredient.from(<infuse_type:mekanism:fungi> * 10), <item:minecraft:crimson_planks>);
// <recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", ItemStackIngredient.from(<item:minecraft:oak_planks>), <infuse_type:mekanism:fungi> * 10, <item:minecraft:crimson_planks>);
// <recipetype:mekanism:metallurgic_infusing>.addRecipe("infuse_planks", <item:minecraft:oak_planks>, <infuse_type:mekanism:fungi> * 10, <item:minecraft:crimson_planks>);


//Removes the Metallurgic Infusing Recipe that allows creating Dirt from Sand.

// <recipetype:mekanism:metallurgic_infusing>.removeByName(name as string)

//<recipetype:mekanism:metallurgic_infusing>.removeByName("mekanism:metallurgic_infusing/sand_to_dirt");

<recipetype:mekanism:metallurgic_infusing>.addRecipe("black_infuse_iron", <item:minecraft:iron_ingot>, <infuse_type:crafttweaker:darkness> * 10, <item:extendedcrafting:black_iron_ingot>);
<recipetype:mekanism:metallurgic_infusing>.addRecipe("coat_redstone", <item:minecraft:redstone>, <infuse_type:mekanism:refined_obsidian> * 40, <item:kubejs:coated_redstone>);
<recipetype:mekanism:metallurgic_infusing>.addRecipe("black_infuse_basilic_reagent", <item:kubejs:basilic_reagent>, <infuse_type:crafttweaker:darkness> * 40, <item:kubejs:enriched_basilic_reagent>);
<recipetype:mekanism:metallurgic_infusing>.addRecipe("black_infuse_skull", <item:minecraft:skeleton_skull>, <infuse_type:crafttweaker:darkness> * 40, <item:minecraft:wither_skeleton_skull>);