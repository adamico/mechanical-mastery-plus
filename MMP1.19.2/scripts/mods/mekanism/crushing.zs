import stdlib.List;
import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Crushing Recipe to crush Brick Blocks into four Bricks.

// <recipetype:mekanism:crushing>.addRecipe(name as string, input as ItemStackIngredient, output as IItemStack)

//<recipetype:mekanism:crushing>.addRecipe("crush_bricks", ItemStackIngredient.from(<item:minecraft:bricks>), <item:minecraft:brick> * 4);
//An alternate implementation of the above recipe are shown commented below. This implementation makes use of implicit casting to allow easier calling:
// <recipetype:mekanism:crushing>.addRecipe("crush_bricks", <item:minecraft:bricks>, <item:minecraft:brick> * 4);


//Removes the Crushing Recipe that produces String from Wool.

// <recipetype:mekanism:crushing>.removeByName(name as string)

//<recipetype:mekanism:crushing>.removeByName("mekanism:crushing/wool_to_string");

<recipetype:mekanism:crushing>.removeByName("mekanism:crushing/flint_to_gunpowder");

var mek_metals = ["iron", "gold", "osmium", "copper", "tin", "lead", "uranium"] as string[];
var jaopca_metals = ["silver", "nickel", "zinc"] as string[];

for metal in mek_metals {
  <recipetype:mekanism:crushing>.addRecipe("mekanism_processing/" + metal + "/dirty_dust/from_crushed_raw", ItemStackIngredient.from(<item:create:crushed_raw_${metal}>), <item:mekanism:dirty_dust_${metal}>);
}

for metal in jaopca_metals {
  <recipetype:mekanism:crushing>.addRecipe("mekanism_processing/" + metal + "/dirty_dust/from_crushed_raw", ItemStackIngredient.from(<item:create:crushed_raw_${metal}>), <item:jaopca:mekanism_dirty_dusts.${metal}>);
}

<recipetype:mekanism:crushing>.addRecipe("crush_netherrack", <item:minecraft:netherrack>, <item:create:cinder_flour> * 2);
<recipetype:mekanism:crushing>.addRecipe("crush_cube4", <item:kubejs:cube4>, <item:kubejs:cube4_dust> *2);