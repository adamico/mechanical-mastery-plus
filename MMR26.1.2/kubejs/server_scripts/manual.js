// priority: 0

// Every player gets the Manual, as the line's glossary names it, once (#220): on their first join, and never
// again, so a Manual put in a chest isn't doubled. A lost one isn't replaced: GuideME's G hotkey opens a page
// from any tooltip without it, and `/guidemec open mechanicalmasteryreborn:manual` opens the whole Manual.
//
// Who has had one is kept in the server's data, by UUID, rather than the player's, so dying doesn't lose it. It is
// kept only when GuideME is loaded, so a join while it is missing gives the Manual on a later one. KubeJS's
// runCommandSilent returns nothing, so whether the give can work is read from the loaded mods, not the command (#221).
PlayerEvents.loggedIn(event => {
  const player = event.player
  const given = `mechanicalmasteryreborn:manual_given/${player.uuid}`
  const data = event.server.persistentData
  if (data.contains(given)) return
  if (!Platform.isLoaded('guideme')) return
  event.server.runCommandSilent(`guideme give ${player.username} mechanicalmasteryreborn:manual`)
  data.putBoolean(given, true)
})
