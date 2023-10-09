StartupEvents.registry('block', (event) => {
	event.create('carbonized_netherrack')
	.displayName('Carbonized Netherrack')
	.material('stone')
	.hardness(1)
	.resistance(1).requiresTool(true)
	.tagBlock('mineable/pickaxe')
	.tagBlock('minecraft:needs_iron_tool')
	.tagBlock('forge:mineable/paxel')
	.tagBlock('projecte:mineable/morningstar')
	.tagBlock('forge:netherrack')
	.tagBlock('infiniburn_overworld')
});