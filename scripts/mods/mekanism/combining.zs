import mods.mekanism.api.ingredient.ItemStackIngredient;



// <recipetype:mekanism:combining>.addRecipe(name as string, mainInput as ItemStackIngredient, extraInput as ItemStackIngredient, output as IItemStack)

//<recipetype:mekanism:combining>.addRecipe("combining/bookshelf", ItemStackIngredient.from(<item:minecraft:book> * 3), ItemStackIngredient.from(<tag:items:minecraft:planks>, 6), <item:minecraft:bookshelf>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:combining>.addRecipe("combining/bookshelf", <item:minecraft:book> * 3, ItemStackIngredient.from(<tag:items:minecraft:planks>, 6), <item:minecraft:bookshelf>);
// <recipetype:mekanism:combining>.addRecipe("combining/bookshelf", ItemStackIngredient.from(<item:minecraft:book> * 3), <tag:items:minecraft:planks> * 6, <item:minecraft:bookshelf>);
// <recipetype:mekanism:combining>.addRecipe("combining/bookshelf", <item:minecraft:book> * 3, <tag:items:minecraft:planks> * 6, <item:minecraft:bookshelf>);

//<recipetype:mekanism:combining>.addRecipe("combining/dark_prismarine", ItemStackIngredient.from(<item:minecraft:prismarine_shard> * 8), ItemStackIngredient.from(<tag:items:forge:dyes/black>), <item:minecraft:dark_prismarine>);
//Alternate implementations of the above recipe are shown commented below. These implementations make use of implicit casting to allow easier calling:
// <recipetype:mekanism:combining>.addRecipe("combining/dark_prismarine", <item:minecraft:prismarine_shard> * 8, ItemStackIngredient.from(<tag:items:forge:dyes/black>), <item:minecraft:dark_prismarine>);
// <recipetype:mekanism:combining>.addRecipe("combining/dark_prismarine", ItemStackIngredient.from(<item:minecraft:prismarine_shard> * 8), <tag:items:forge:dyes/black>, <item:minecraft:dark_prismarine>);
// <recipetype:mekanism:combining>.addRecipe("combining/dark_prismarine", <item:minecraft:prismarine_shard> * 8, <tag:items:forge:dyes/black>, <item:minecraft:dark_prismarine>);

// <recipetype:mekanism:combining>.removeByName(name as string)

<recipetype:mekanism:combining>.addRecipe("combining/elite_control_circuit", <item:mekanism:advanced_control_circuit>, <item:mekanism:alloy_reinforced> * 2, <item:mekanism:elite_control_circuit>);