import mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient;
import mods.mekanism.api.ingredient.ItemStackIngredient;

//Adds a Purifying Recipe that uses 200 mB of Oxygen (1 mB per tick) Basalt into Polished Basalt.

// <recipetype:mekanism:purifying>.addRecipe(name as string, itemInput as ItemStackIngredient, chemicalInput as GasStackIngredient, output as IItemStack)

// <recipetype:mekanism:purifying>.addRecipe("purify_basalt", ItemStackIngredient.from(<item:minecraft:basalt>), GasStackIngredient.from(<gas:mekanism:oxygen>), <item:minecraft:polished_basalt>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:purifying>.addRecipe("purify_basalt", <item:minecraft:basalt>, GasStackIngredient.from(<gas:mekanism:oxygen>), <item:minecraft:polished_basalt>);
// <recipetype:mekanism:purifying>.addRecipe("purify_basalt", ItemStackIngredient.from(<item:minecraft:basalt>), <gas:mekanism:oxygen>, <item:minecraft:polished_basalt>);
// <recipetype:mekanism:purifying>.addRecipe("purify_basalt", <item:minecraft:basalt>, <gas:mekanism:oxygen>, <item:minecraft:polished_basalt>);


//Removes the Purifying Recipe that creates Gold Clumps from Gold Ore.

// <recipetype:mekanism:purifying>.removeByName(name as string)

//<recipetype:mekanism:purifying>.removeByName("mekanism:processing/gold/clump/from_ore");

<recipetype:mekanism:purifying>.addRecipe('purify_earth_charge', <item:thermal:earth_charge>, <gas:mekanism:sulfuric_acid>, <item:kubejs:basilic_reagent>);
<recipetype:mekanism:purifying>.addRecipe('purifying/fission_pellet', <tag:items:forge:dusts/fluorite>, <gas:mekanism:fissile_fuel>, <item:kubejs:fission_pellet>);

<recipetype:mekanism:purifying>.addRecipe("dragon_breath_bottling", <item:minecraft:glass_bottle> * 16, <gas:crafttweaker:dragon_breath>, <item:minecraft:dragon_breath> * 16);