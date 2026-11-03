---
navigation:
  title: The Recycler
  icon: recycler_core_1
  position: 1
---

# The Recycler

The Recycler [decomposes](../decomposing.md) essence and recycles what crafting made, much faster than decomposing
by hand. You build it block by block, from parts made on a Craftworks Assembler.

<GameScene zoom={4} interactive={true}>
  <Block id="recycler_core_1" x="1" z="0" p:facing="north" />
  <Block id="recycler_output" x="2" z="0" p:facing="north" p:place="north_east" />
  <Block id="recycler_output" x="0" z="0" p:facing="north" p:place="north_west" />
  <Block id="recycler_input" x="1" z="1" p:place="centre" />
  <Block id="hand_drive" x="2" z="2" p:place="south_east" />
  <Block id="recycler_casing" x="0" z="1" p:place="west" />
  <Block id="recycler_casing" x="0" z="2" p:place="south_west" />
  <Block id="recycler_casing" x="1" z="2" p:place="south" />
  <Block id="recycler_casing" x="2" z="1" p:place="east" />
  <BlockAnnotation x="1" z="0" color="#ffd84a">**Core**, facing out: it holds the work, and its tier sets how fast the Recycler runs.</BlockAnnotation>
  <BlockAnnotation x="2" z="0" color="#4ad8ff">**Output**: the first, read before the other.</BlockAnnotation>
  <BlockAnnotation x="0" z="0" color="#4ad8ff">**Output**: the second. Each output holds three kinds of item.</BlockAnnotation>
  <BlockAnnotation x="1" z="1" color="#7cff4a">**Input**: essence, or an item to recycle, goes in here.</BlockAnnotation>
  <BlockAnnotation x="2" z="2" color="#ff7c4a">**Hand drive**: right-click it to turn the Recycler.</BlockAnnotation>
  <BlockAnnotationTemplate id="recycler_casing">
    <BlockAnnotation color="#aaaaaa">**Casing**: fills a place, and does nothing else.</BlockAnnotation>
  </BlockAnnotationTemplate>
</GameScene>

A tier-1 Recycler with two outputs: drag to turn it, and hover over a part to see what
it does.

## Building one

A Recycler is a 3x3 of nine blocks:

- Its [core](core.md) stands in the middle of one edge, its front facing out, and the rest of the 3x3 lies
  behind it.
- The other eight places, the centre among them, take any mix of [drives](hand_drive.md),
  [inputs](input.md), [outputs](output.md) and [casings](casing.md), with at least one drive, one input and
  one output.

Each place must belong to that core alone: two Recyclers can't share a part. A Recycler with a place empty, or
holding any other block, is incomplete and does nothing. Jade says what it lacks.

## Floors

A Recycler may have more floors, stacked straight up from its core's, up to as many floors in all as its core's
tier: a tier-1 core has one, a tier-4 core up to four. Each floor above the core's is a full 3x3 of any parts, all
nine places filled, the one over the core among them. Its drive, input and output may stand on any floor.

Floors count upward to the first one that isn't full: that floor and any above it are no part of the Recycler,
and show no frame, while the floors below go on working. A floor above what the core's tier allows isn't part of
it either, until a higher core claims it. A place two Recyclers would both claim, across floors too, belongs to
neither. The frame wraps every floor that counts as one tall block.

To upgrade a Recycler, break its core and place one of a higher tier where it stood: the rest of the 3x3 stays as
it is, and floors already built above are claimed at once.

## Outputs

Each [output](output.md) holds three kinds of item, so a Recycler returns every kind in what it decomposes or
recycles only with enough outputs for them all. With too few it still runs, but returns as many kinds as fit, picked
at random, and the rest are lost: more outputs return more of a mix. The two above return six kinds at a time.

Its outputs are read in order, from the one at the front-left, on the left as you face the core, clockwise seen
from above, with the centre last, then floor by floor upward, each floor in the same order and ending with the
place over the core. Each kind goes on the first position already holding that item, or else on the
first empty one, so on empty outputs a mix lands in the order EMI shows it. With outputs for every kind, the Recycler
stops while any kind fits nowhere; with too few, it picks among the kinds that fit, and stops only when none does.
Jade says when it stops, until its outputs are emptied.

## Running it

Put essence, or any other item to recycle, into an [input](input.md), and turn a [drive](hand_drive.md). After
enough turns the core decomposes or recycles one, and the outputs fill. Jade, on the core, shows what it holds
and how many turns are left. More drives turn it faster, so players on more drives run a Recycler faster, and a
higher [core](core.md) needs fewer turns.
