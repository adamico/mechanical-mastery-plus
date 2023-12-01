StartupEvents.registry('block', (event) => {
  event.create('cube4_spawner').soundType('netherite_block').blockEntity(blockEntity => {
    blockEntity.inventory(9, 4, 'kubejs:cube4')
    blockEntity.rightClickOpensInventory()
    blockEntity.clientTick(20, 0, entity => {
      let x = entity.x + 0.5
      let y = entity.y + 1.5
      entityEvent.level.addParticle('minecraft:campfire_cosy_smoke', true, x, y, 0, 0.3, 0)
    })
    //TODO: create a function to parameterize spawn frequency and item spawned
    blockEntity.serverTick(20, 0, entity => {
      entity.inventory.insertItem('kubejs:cube4', false)
    })
  })
});
