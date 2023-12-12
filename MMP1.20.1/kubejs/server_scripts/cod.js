ServerEvents.unloaded(event => {
  if (!event.server.persistentData.essencesCounter) event.server.persistentData.essencesCounter = {}
  event.server.persistentData.essencesCounter = global.essencesCounter
})

ServerEvents.loaded(event => {
  global.essencesCounter = event.server.persistentData.essencesCounter
})

//Gives players the timer's persistent data to initialize the timer on each player who logs in.
PlayerEvents.loggedIn(event => {
  const { player } = event
  let pData = player.persistentData
  if (!pData.timer) event.player.persistentData.timer = 0
})

//Sets a timer on a 20 tick interval
PlayerEvents.tick(event => {
  const { player } = event
  let pData = player.persistentData

  pData.timer = (++pData.timer) % 20
  if (pData.timer != 0) return //event here if you wanted to ignore the lower event block
})

PlayerEvents.tick(event => {
  const { player } = event
  let pData = player.persistentData

  if (pData.timer == 0) {
    let cube4_produced = global.essencesCounter['cube4'].produced
    let cube4_processed = global.essencesCounter['cube4'].processed
    let cube4 = { cube4: { type: 'item', item: 'kubejs:cube4', x: 20, y: 20 } }
    let cube4_count = { cube4_count: { type: 'text', text: cube4_produced.toString() + "/" + cube4_processed.toString() + "=0", x: 30, y: 15, scale: 1.5 } }

    player.paint(cube4)
    player.paint(cube4_count)
  }
})
