
PlayerEvents.loggedIn(event => {
  const { player } = event
  let pData = player.persistentData
  //Gives players the timer's persistent data to initialize the timer on each player who logs in.
  // if (!pData.timer) event.player.persistentData.timer = 0
})


PlayerEvents.tick(event => {
  const { player } = event
  let pData = player.persistentData
  //Sets a timer on a 20 tick interval
  // pData.timer = (++pData.timer) % 20
  // if (pData.timer != 0) return //event here if you wanted to ignore the lower event block
})

PlayerEvents.tick(event => {
  const { player, level } = event
  // const { x, y, z } = global.essence4producerCoords
  let pData = player.persistentData

  // if (pData.timer == 0) {
  //   let cube4_produced = 0
  //   let cube4_processed = 0
  //   if (level.getBlock(x, y, z).entity != undefined) {
  //     let essence4producerData = level.getBlock(x, y, z).persistentData['cube4']
  //     cube4_produced = essence4producerData.produced
  //   }

  //   if (level.getBlock(x, y + 2, z).entity != undefined) {
  //     let essence4processorData = level.getBlock(x, y + 2, z).entity.persistentData['cube4']
  //     cube4_processed = essence4processorData.processed
  //   }

  //   let cube4_delta = cube4_produced - cube4_processed
  //   let cube4 = { cube4: { type: 'item', item: 'kubejs:cube4', x: 20, y: 20 } }
  //   let cube4_count = { cube4_count: { type: 'text', text: cube4_produced.toString() + "/" + cube4_processed.toString() + "=" + cube4_delta.toString(), x: 30, y: 15, scale: 1.5 } }
  //   //
  //   player.paint(cube4)
  //   player.paint(cube4_count)
  // }
})
