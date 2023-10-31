// priority: 100

console.info('Hello, World! (You will see this line every time client resources reload)');
const $VanillaTypes = Java.loadClass('mezz.jei.api.constants.VanillaTypes');

JEIEvents.hideItems(event => {
  ['enderium', 'lumium', 'signalum'].forEach(alloy => {
    event.hide(`thermal:${alloy}_dust`);
  });

  event.hide('adminshop:permit');
  global.jeiRuntime.ingredientManager.addIngredientsAtRuntime($VanillaTypes.ITEM_STACK, [
    Item.of('adminshop:permit', "{display:{Lore:['[{\"text\":\"Buy: Raw Zinc\",\"italic\":false}]','[{\"text\":\"Buy: Kelp\",\"italic\":false}]','[{\"text\":\"Buy: Tiny Dry Rubber\",\"italic\":false}]','[{\"text\":\"Buy: Bone\",\"italic\":false}]','[{\"text\":\"Buy: Lapis Ore\",\"italic\":false}]','[{\"text\":\"Buy: Gobber Ore\",\"italic\":false}]','[{\"text\":\"Buy: Apatite Ore\",\"italic\":false}]','[{\"text\":\"Buy: Cobbled Deepslate\",\"italic\":false}]','[{\"text\":\"Sell: Improved Mechanical Crystal\",\"italic\":false}]'],Name:'[{\"text\":\"[T1] Trade Permit\",\"color\":\"magenta\",\"italic\":false}]'},key:1}").itemStack,
    Item.of('adminshop:permit', "{display:{Lore:['[{\"text\":\"Buy: Crude Oil\",\"italic\":false}]','[{\"text\":\"Buy: Oil Sand\",\"italic\":false}]','[{\"text\":\"Buy: Raw Osmium\",\"italic\":false}]','[{\"text\":\"Buy: Diamond Ore\",\"italic\":false}]','[{\"text\":\"Buy: Niter Ore\",\"italic\":false}]','[{\"text\":\"Buy: Sulfur Ore\",\"italic\":false}]','[{\"text\":\"Buy: Ancient Debris\",\"italic\":false}]','[{\"text\":\"Buy: Nether Gobber Ore\",\"italic\":false}]','[{\"text\":\"Buy: Basic Create Items\",\"italic\":false}]','[{\"text\":\"Sell: Advanced Mechanical Crystal\",\"italic\":false}]'],Name:'[{\"text\":\"[T2] Trade Permit\",\"color\":\"magenta\",\"italic\":false}]'},key:2}").itemStack,
    Item.of('adminshop:permit', "{display:{Lore:['[{\"text\":\"Buy: Emerald Ore\",\"italic\":false}]','[{\"text\":\"Buy: Powah Resources\",\"italic\":false}]','[{\"text\":\"Buy: Mekanism Resources\",\"italic\":false}]','[{\"text\":\"Buy: Start a village!\",\"italic\":false}]','[{\"text\":\"Buy: Get passive mobs!\",\"italic\":false}]','[{\"text\":\"Sell: Advanced Mechanical Crystal\",\"italic\":false}]','[{\"text\":\"Sell: Liquid Pink Slime\",\"italic\":false}]','[{\"text\":\"Sell: Liquid Meat\",\"italic\":false}]'],Name:'[{\"text\":\"[T3] Trade Permit\",\"color\":\"magenta\",\"italic\":false}]'},key:3}").itemStack,
    Item.of('adminshop:permit', "{display:{Lore:['[{\"text\":\"Buy: Enchanting Bottle\",\"italic\":false}]','[{\"text\":\"Buy: Rubberwood Sapling\",\"italic\":false}]','[{\"text\":\"Buy: End Gobber Ore\",\"italic\":false}]','[{\"text\":\"Buy: Many liquids\",\"italic\":false}]','[{\"text\":\"Buy: Many spawn eggs\",\"italic\":false}]','[{\"text\":\"Buy: Flux Networks Resources\",\"italic\":false}]','[{\"text\":\"Sell: Liquid Experience\",\"italic\":false}]','[{\"text\":\"Sell: Refined Fuel\",\"italic\":false}]','[{\"text\":\"Sell: Lava\",\"italic\":false}]'],Name:'[{\"text\":\"[T4] Trade Permit\",\"color\":\"magenta\",\"italic\":false}]'},key:4}").itemStack,
    Item.of('adminshop:permit', "{display:{Lore:['[{\"text\":\"Buy: Nothing\",\"italic\":false}]'],Name:'[{\"text\":\"[SPECIAL] I need money Trade Permit\",\"color\":\"magenta\",\"italic\":false}]'},key:5}").itemStack
  ])
})

JEIEvents.subtypes(event => {
  event.useNBTKey('adminshop:permit', 'display');
})