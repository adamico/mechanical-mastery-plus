ServerEvents.recipes(event => {
	event.shapeless(Item.of('minecraft:andesite', 8), [Item.of('minecraft:cobblestone', 8), Item.of('minecraft:quartz', 1)]);
	event.shaped(Item.of('minecraft:chest', 4), [
		'###',
		'# #',
		'###'
	], { '#': '#minecraft:logs' });

	event.shaped('minecraft:flint', [
		' # ',
		'###',
		' # '
	], { '#': 'minecraft:gravel' });
});