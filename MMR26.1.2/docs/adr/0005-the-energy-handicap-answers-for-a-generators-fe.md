# The energy handicap answers for a generator's FE ahead of its own mod

Under the energy handicap, the companion mod stops a team's generators by answering NeoForge's energy capability for them first. It registers its provider ahead of every other mod's, on every block. For a block in its `generators` tag, standing on the island of a team that plays the energy challenge, it returns a face that gives no FE. For anything else it returns nothing, and the block's own mod answers as before. The suite's networks draw a generator's FE through that capability every tick, and NeoForge returns the first provider that answers, so the generator gives its team nothing. It fills its own buffer and idles, and the FE it banked is drawn again when the challenge ends. The pack fills the tag with `#wireworks:generators`, so the mod never names a suite mod.

We could have asked the suite for a hook, a check every generator makes before it burns. That would stop the burning itself, but it means a suite change for the pack's sake and a release train, and it ties the companion mod to a suite mod at compile time. The glossary's Suite says a change goes to the mod only for a bug or a workaround worth sharing. Intercepting the generators' ticks would have named Wireworks' classes.

## Consequences

- This only stops a generator whose FE is drawn from it. A generator that pushes its FE into its neighbours goes on, so the pack tags only generators that are drawn from.
- The face changes when a challenge starts or ends, and nothing tells NeoForge's capability caches. The suite asks afresh every tick, but a mod that keeps a `BlockCapabilityCache` on a generator keeps its old answer until the block changes.
- A stopped generator still burns fuel for the few seconds its buffer takes to fill.
- The generator's own mod still reports itself as a generator with a full buffer. The companion mod's Jade line says why it is stopped.
