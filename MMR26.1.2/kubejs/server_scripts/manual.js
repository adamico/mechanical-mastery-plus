// priority: 0

// Every player gets the Manual, as the line's glossary names it, once (#220): on their first join, and never
// again, so a Manual put in a chest isn't doubled. A lost one isn't replaced: GuideME's G hotkey opens a page
// from any tooltip without it, and `/guidemec open mechanicalmasteryreborn:manual` opens the whole Manual.
//
// Who has had one is kept in the server's data, by UUID, rather than the player's, so dying doesn't lose it. It is
// kept only once the give succeeds, so a join while GuideME is missing gives the Manual on a later one.
PlayerEvents.loggedIn(event => {
  const player = event.player
  const given = `mechanicalmasteryreborn:manual_given/${player.uuid}`
  const data = event.server.persistentData
  if (data.contains(given)) return
  if (event.server.runCommandSilent(`guideme give ${player.username} mechanicalmasteryreborn:manual`) > 0) {
    data.putBoolean(given, true)
  }
})
