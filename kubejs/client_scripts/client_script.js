// priority: 0

console.info('Hello, World! (You will see this line every time client resources reload)')

JEIEvents.hideItems(event => {
	// Hide items in JEI here
  ['1', '2', '3'].forEach(num => {	
		event.hide(`projecte:relay_mk${num}`);
		event.hide(`projecte:collector_mk${num}`);	
	});

  ['basic', 'dark', 'red', 'magenta', 'pink', 'purple', 'violet',
  'blue', 'cyan', 'green', 'lime', 'yellow', 'orange', 'white', 'fading']
  .forEach(num => {	
		event.hide(`projectexpansion:${num}_power_flower`);
	});
})

JEIEvents.subtypes(event => {
  // event.useNBTKey('minecraft:stone_sword', 'AugmentData')
})

JEIEvents.addItems(event => {
  // event.add(Item.of('minecraft:stone_sword', '{AugmentData:{Type:"Dynamo",DynamoEnergy:1.43d}}'));
})