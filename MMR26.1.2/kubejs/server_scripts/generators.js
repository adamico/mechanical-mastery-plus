// priority: 0

// The generators the energy handicap stops (#202): while a team plays the energy challenge, each of these on its
// island gives it no energy. The companion mod ships the tag empty and never names a suite mod. Only generators a
// network draws from belong here, as all of Wireworks' are: one that pushes its energy out would go on (MMR26.1.2
// ADR 0005).
ServerEvents.tags('block', event => {
  event.add('mechanicalmasteryreborn:generators', '#wireworks:generators')
})
