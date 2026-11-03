# A free automated drive

Reborn has no Recycler drive that turns on its own for nothing, such as one turned by a redstone signal. Every drive that runs without a player pays for its turns: the FE drive spends FE from its own store.

## Why this is out of scope

A drive turned by redstone would cost nothing to run. One cheap vanilla clock beside it would turn a Recycler at a hand drive's pace forever, and two problems follow:

- **The hand drive's cost wouldn't matter.** A hand drive costs the player turning it food (#222), because it is how a team starts, or gets by when short of everything else, not how it runs (`MMR26.1.2/GLOSSARY.md`: Drive). A free drive at the same pace would replace it as soon as a team has a few redstone dust.
- **The energy challenge would be trivial.** The handicap cuts a team's generators, and banking FE ahead is the strategy for playing through it (MMR26.1.2 ADR 0009): a team that didn't bank must get by on hand drives. A drive needing neither food nor FE would let every team run its Recycler through the challenge at no cost, so banking FE would no longer matter.

Choosing one turn per pulse over turning while powered doesn't change this, because a redstone clock makes pulses for free.

## Prior requests

- #185: "The redstone drive"
