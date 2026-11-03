// priority: 0

// The Defences (#236, MMR26.1.2 ADR 0010): free from the start, locked by no challenge or chapter, so a team can
// grow its defences ahead of the protection challenge. The FE defence's cost is #207's first guess: an early-game
// crossbow and iron, with redstone for the FE it runs on.
ServerEvents.recipes(event => {
  event.recipes.craftworks.assembling('mechanicalmasteryreborn:fe_defence',
    ['minecraft:crossbow', '4x minecraft:iron_ingot', '4x minecraft:redstone'])
    .id('mechanicalmasteryreborn:assembling/fe_defence')
})
