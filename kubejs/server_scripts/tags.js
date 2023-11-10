// Listen to item tag event

var hideInEMI = (event, output) => {
  event.add('c:hidden_from_recipe_viewers', output);
}

var bottomless = (event, fluidOrTag) => {
  event.remove('create:no_infinite_draining', fluidOrTag);
  event.add('create:bottomless/allow', fluidOrTag);
}

ServerEvents.tags('item', event => {
  // Get the #forge:cobblestone tag collection and add Diamond Ore to it
  // event.add('forge:cobblestone', 'minecraft:diamond_ore')

  // Get the #forge:cobblestone tag collection and remove Mossy Cobblestone from it
  // event.remove('forge:cobblestone', 'minecraft:mossy_cobblestone')

  // Get #forge:ingots/copper tag and remove all entries from it
  // event.removeAll('forge:ingots/copper')

  // Required for FTB Quests to check item NBT
  // event.add('itemfilters:check_nbt', 'some_item:that_has_nbt_types')

  // You can create new tags the same way you add to existing, just give it a name
  // event.add('forge:completely_new_tag', 'minecraft:clay_ball')

  // It supports adding tags to tags as well. Just prefix the second tag with #
  // event.add('c:stones', '#forge:stone')

  // Removes all tags from this entry
  // event.removeAllTagsFrom('minecraft:stick')

  // Add all items from the forge:stone tag to the c:stone tag, unless the id contains diorite
  // const stones = event.get('forge:stone').getObjectIds()
  // const blacklist = Ingredient.of(/.*diorite.*/)
  // stones.forEach(stone => {
  //   if (!blacklist.test(stone)) event.add('c:stone', stone)
  // })

  event.add('thermal:crafting/dies', 'kubejs:press_rod_die');
  event.add('thermal:crafting/dies', 'fluxnetworks:flux_block');
  event.add('forge:plates/diamond', 'kubejs:diamond_plate');
  event.add('forge:plates', 'kubejs:diamond_plate');
  event.add('forge:slimeballs', 'kubejs:oil_clump'); //TODO: remove this?
  event.add('forge:gears/steel', 'kubejs:steel_gear');

  event.add('forge:gears', 'kubejs:steel_gear');

  ['steel'].forEach(metal => {
    event.add(`forge:rods/${metal}`, `kubejs:${metal}_rod`);
    event.add('forge:rods', `kubejs:${metal}_rod`);
  })

  event.add('forge:storage_blocks/steel', 'industrialforegoing:steel_block');

  event.add('forge:heads', 'supplementaries:enderman_head');

  ['create:brass', 'thermal:bronze', 'thermal:constantan', 'thermal:electrum', 'thermal:enderium',
    'thermal:invar', 'thermal:lumium', 'thermal:signalum', 'steel'].forEach(alloy => {
      ['ingot', 'block'].forEach(type => {
        let materialName = (alloy == 'steel') ? `mekanism:${type}_${alloy}` : `${alloy}_${type}`;
        event.add('forge:alloys', materialName);
      });
    });

  ['enderium', 'lumium', 'signalum'].forEach(alloy => {
    ['ingot', 'block'].forEach(type => {
      event.add('forge:alloys/special', `thermal:${alloy}_${type}`);
    });
    hideInEMI(event, `thermal:${alloy}_dust`);
  });

  [
    'darkutils:charm_portal',
    'thermal:ender_pearl_dust',
    'createchromaticreturn:shadow_essence_bucket',
    'createchromaticreturn:refined_mixture_bucket',
    'mekanism:creative_fluid_tank'
  ].forEach(item => {
    hideInEMI(event, item)
  });

  ['carrot', 'apple', 'potato', 'beetroot', 'onion', 'tomato'].forEach(item => {
    ['quark', 'farmersdelight'].forEach(mod => {
      let itemName = `${mod}:${item}_crate`;
      hideInEMI(event, itemName);
    })
  });
});

ServerEvents.tags('block', event => {
  event.add('blockrunner:very_quick_blocks', '#engineersdecor:plain_concretes');
  event.add('forge:budding', '#forge:budding_blocks');
});

ServerEvents.tags('fluid', event => {
  [
    'industrialforegoing:essence',
    'mekanismgenerators:fusion_fuel',
    '#forge:chocolate',
    'kubejs:light_essence',
    'kubejs:darkness_essence'
  ].forEach(fluidOrTag => {
    bottomless(event, fluidOrTag);
  });

  [
    'createchromaticreturn:refined_mixture',
    'createchromaticreturn:shadow_essence'
  ].forEach(fluid => {
    hideInEMI(event, fluid)
  });
});