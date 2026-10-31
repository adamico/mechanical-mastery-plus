---
navigation:
  title: The Recycler
  icon: recycler_core_1
  position: 1
---

# The Recycler

The Recycler [decomposes](../decomposing.md) essence and recycles everything else, much faster than decomposing
by hand. You build it block by block, from parts made on a Craftworks Assembler.

<GameScene zoom={4} interactive={true}>
  <Block id="recycler_core_1" x="1" z="0" p:facing="north" />
  <Block id="recycler_output" x="2" z="0" p:facing="north" />
  <Block id="recycler_output" x="0" z="0" p:facing="north" />
  <Block id="recycler_input" x="2" z="1" p:facing="east" />
  <Block id="hand_drive" x="2" z="2" p:corner="south_east" />
  <Block id="recycler_casing" x="0" z="1" />
  <Block id="recycler_casing" x="0" z="2" />
  <Block id="recycler_casing" x="1" z="2" />
  <Block id="recycler_casing" x="1" z="1" />
  <BlockAnnotation x="1" z="0" color="#ffd84a">**Core**, facing out: it holds the work, and its tier sets how well the Recycler runs.</BlockAnnotation>
  <BlockAnnotation x="2" z="0" color="#4ad8ff">**Output**: the first, so it gets the first three kinds of a mix.</BlockAnnotation>
  <BlockAnnotation x="0" z="0" color="#4ad8ff">**Output**: the second, for the fourth kind on.</BlockAnnotation>
  <BlockAnnotation x="2" z="1" color="#7cff4a">**Input**: essence, or an item to recycle, goes in here.</BlockAnnotation>
  <BlockAnnotation x="2" z="2" color="#ff7c4a">**Hand drive**: right-click it to turn the Recycler.</BlockAnnotation>
  <BlockAnnotationTemplate id="recycler_casing">
    <BlockAnnotation color="#aaaaaa">**Casing**: fills a place, and does nothing else.</BlockAnnotation>
  </BlockAnnotationTemplate>
</GameScene>

A tier-1 Recycler that decomposes Advanced Mechanical Essence: drag to turn it, and hover over a part to see what
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

A Recycler may have more floors, each a full 3x3 of parts, up to as many floors in all as its core's tier. A
tier-1 core has one.

## Outputs

Each [output](output.md) holds three kinds of item, so a Recycler needs enough outputs for every kind in what it
decomposes or recycles: two for Advanced's mix of four, as above. With too few, it doesn't run, and Jade says so.
Its outputs are filled in order, from the one at the front-left, on the left as you face the core, clockwise
seen from above, with the centre last. EMI shows a mix in that order.

## Running it

Put essence, or any other item to recycle, into an [input](input.md), and turn a [drive](hand_drive.md). After
enough turns the core decomposes or recycles one, and the outputs fill. Jade, on the core, shows what it holds
and how many turns are left. More drives turn it faster, so players on more drives run a Recycler faster.
