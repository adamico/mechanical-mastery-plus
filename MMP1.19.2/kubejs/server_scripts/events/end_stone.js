EntityEvents.death('enderman', event => {
  const { entity, server, level } = event
  let radius = 3

  entity.setHealth(1)
  let pos = entity.blockPosition()
  let aPos = new BlockPos(pos.x - radius, pos.y - 1, pos.z - radius)
  let bPos = new BlockPos(pos.x + radius, pos.y + 1, pos.z + radius)
  // Utils.server.tell("pos = " + pos)
  // Utils.server.tell("aPos = " + aPos)
  // Utils.server.tell("bPos = " + bPos)
  entity.remove("killed")
  blocksBetweenCorners(level, aPos, bPos, 'minecraft:stone').forEach(block => {
    if (event.source.actual.type !== "minecraft:endermite") return
    if (Math.random() <= 0.25) {
      server.runCommandSilent(`execute in ${event.level.dimension} run particle minecraft:dust 1 1 1 1 ${block.x} ${block.y + 1} ${block.z} 0 0 0 0 10 normal`);
      server.runCommandSilent(`playsound minecraft:entity.enderman.teleport block @p ${block.x} ${block.y} ${block.z} 0.4 0.7`);
      block.set('minecraft:end_stone')
    }
  })

  event.cancel()

  /**
   * iterates over a 3D grid of blocks and returns an array of blocks with id == matchBlockId.
   *
   * @param { Internal.Level } level        - level
   * @param { BlockPos }       aPos         - 1st corner position object
   * @param { BlockPos }       bPos         - 2nd corner position object
   * @param { string }         matchBlockId - block id to match
  */
  function blocksBetweenCorners(level, aPos, bPos, matchBlockId) {
    let { x: x1, y: y1, z: z1 } = aPos;
    let { x: x2, y: y2, z: z2 } = bPos;

    // initialize cursor position
    let x = x1;
    let y = y1;
    let z = z1;

    // Log the initial position to the console
    // console.log(`x=${x}, y=${y}, z=${z}`);
    // console.log(`x1=${x1}, y2=${y1}, z1=${z1}`);
    // console.log(`x2=${x2}, y2=${y2}, z2=${z2}`);

    // Create an empty array to store encountered blocks
    let box = [];

    while (x <= x2) {
      while (y <= y2) {
        while (z <= z2) {
          let block = level.getBlock(x, y, z);
          // console.log("block = " + block)
          if (block.id == matchBlockId) box.push(block);
          z++;
        }
        y++;
        z = z1;
      }
      x++;
      y = y1;
    }
    // console.log("box = " + box)
    return box;
  }
})
