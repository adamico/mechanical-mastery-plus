ServerEvents.recipes(event => {
  event.replaceInput({id: 'extendedcrafting:redstone_ingot'}, 'minecraft:iron_ingot', '#forge:ingots/lead');
	event.remove({id: 'extendedcrafting:black_iron_ingot'});
	event.remove({id: 'extendedcrafting:luminessence'});
});