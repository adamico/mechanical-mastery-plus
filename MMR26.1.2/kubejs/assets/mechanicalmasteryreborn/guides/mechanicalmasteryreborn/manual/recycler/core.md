---
navigation:
  title: Core
  parent: recycler/index.md
  icon: recycler_core_1
  position: 0
item_ids:
  - mechanicalmasteryreborn:recycler_core_1
---

# Core

<BlockImage id="recycler_core_1" scale="4" p:facing="north" />

The heart of a [Recycler](index.md). It stands in the middle of one edge of the Recycler's 3x3, its front facing
out, and it faces you as you place it, so place it from where you want its front: the rest of the 3x3 lies behind
it.

It holds the essence, or the item to recycle, that its [inputs](input.md) take in, and the work under way. Its
tier sets how fast the Recycler runs: how many turns of its drives a decompose or a recycle takes. Every core
returns the same **yield**, the share of a mix a decompose returns, as decomposing by hand does. A
Recycler with too few [outputs](output.md) for every kind still runs, returning only the kinds that fit, picked at
random, and the rest are lost.

Right-click it with an empty hand to take back the whole stack it holds. That gives up the work under way. Nothing
else can take it out.

There are four tiers. Tier 1 is the first core: 8 turns a decompose. Each higher core is made from the one below,
and takes fewer turns:

- [Tier 2](core_2.md): 6 turns.
- [Tier 3](core_3.md): 4 turns.
- [Tier 4](core_4.md): 2 turns.

To upgrade, break the core and place the new one where it stood, facing the same way. Breaking it drops what it
held and gives up the work under way. The core's tooltip shows the turns the server plays with.
