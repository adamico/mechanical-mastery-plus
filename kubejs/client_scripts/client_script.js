// priority: 0

console.info('Hello, World! (You will see this line every time client resources reload)')

JEIEvents.hideItems(event => {
  ['iron', 'gold', 'diamond'].forEach(material => {
    event.hide(`industrialforegoing:${material}_gear`);
  });
})

JEIEvents.removeCategories(event => {
  //console.log(event.getCategoryIds()) //log a list of all category ids to logs/kubejs/client.txt
})

JEIEvents.subtypes(event => {
  // event.useNBTKey('minecraft:stone_sword', 'AugmentData')
})

JEIEvents.addItems(event => {
  // event.add(Item.of('minecraft:stone_sword', '{AugmentData:{Type:"Dynamo",DynamoEnergy:1.43d}}'));
})