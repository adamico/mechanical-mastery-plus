// priority: 0

// A player with no Skyblock Builder team gets a team of one, with an island of its own, rather than
// Skyblock Builder's shared spawn. Skyblock Builder puts such a player into the spawn team as they
// log in, so this waits a tick for that to land first.
//
// `skyblock create` refuses a player who already has a team, so it is safe on every login. The team
// takes the player's name; if a team of that name is taken (a teammate's old team, say), the second
// command makes one with a random name instead, and is refused when the first succeeded.
PlayerEvents.loggedIn(event => {
  const name = event.player.username
  event.server.scheduleInTicks(1, () => {
    event.server.runCommandSilent(`execute as ${name} run skyblock create "${name}"`)
    event.server.runCommandSilent(`execute as ${name} run skyblock create`)
  })
})
