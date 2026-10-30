# Mechanical Mastery Reborn (26.1.2)

The 26.1.2 pack line, the sequel to Mechanical Mastery Plus, on NeoForge. Shared terms are in the root `GLOSSARY.md`.

**Spine**:
The suite.

**Mechanical Essence**:
On this line the loop runs backwards. The world has been reduced to essence: each one holds the idea of every other resource, which has to be decomposed out of it.

**C.O.D.**:
The line's adversary, a sentient AI that cannot be shut down. It makes essence of the highest tier not yet blocked, and nothing else: Advanced first, then one tier lower each time a challenge is won, until all four are blocked. Dormant, it makes it at a steady pace a little faster than decomposing by hand, and only until it holds a stack, so it never runs dry and never piles up. A team's first decompose on a Recycler (decomposing by hand never does) wakes it, and from then on it makes essence much faster, steadily between challenges and ever faster, without end, while one is played. Between challenges, being awake is only more essence for the team: C.O.D. is an adversary only while a challenge is played.

**Suite**:
The 5thlayer -works mods (Groundworks, Beltworks, Pipeworks, Wireworks, Craftworks, Fieldworks, Voidworks, Gearworks), which the pack consumes as released jars, as any player would. What the pack needs from them it scripts itself; a change goes to the mod only for a bug or a workaround worth sharing.

**Showcase**:
The FactoryWorks Showcase, the suite's own sandbox pack. A separate pack that this line borrows a base of mods from, and never depends on.

**Companion mod**:
Mechanical Mastery Reborn's own mod, which holds what the pack can't script: C.O.D., the essences, the Recycler, the lab and each team's challenge state. The pack consumes it as a released jar, like the suite.
_Avoid_: pack mod, core mod

**Decompose**:
To break a Mechanical Essence down into its mix, on a Recycler or, much more slowly, by hand, as Factorio's scrap is. It is how resources enter this line. The higher the tier, the more processed what it decomposes into: Advanced into the most refined intermediates, Basic into raw resources. A lower tier's outputs need more stages to become what the tier above yielded, so the factory built for one tier becomes the last stages of the next one's and grows upstream rather than being replaced. Each lower tier is harder and longer to decompose, with more loops and more mods: Advanced, the first, is the easiest, and Basic, the last, the hardest.
_Avoid_: break down (Voidworks' word for its own item breakdown)

**Recycle**:
To break any item that isn't an essence down one band, on a Recycler only, for part of what made it, as Factorio's recycler does. It is how a team gets below the band its essence decomposes into, and how it throws surplus away. Recycling never wakes C.O.D. and never counts against Inflation.
_Avoid_: decompose (for an item that isn't an essence), scrap, void

**Recycler**:
The companion mod's machine, after Factorio's, and much faster than decomposing by hand. It decomposes an essence into its mix, and recycles any item one band down for part of what made it, so a team can always rebuild from raw resources and recycling is also how surplus is thrown away. It is a multiblock built block by block: a core in the middle of one edge of a 3x3, its front facing out, and any mix of drives, inputs, outputs and casings in the other eight places, the centre among them, with at least one drive, input and output. Every Recycler processes anything; its core's tier decides how well.
_Avoid_: starter machine, decomposer, crank recycler

**Core**:
The block in the middle of a Recycler's front edge, facing out, which holds its work and whose tier sets how fast it runs and how well: its yield, and how many it processes at once. A player takes back what it holds by hand, giving up the work under way; nothing else can.

**Drive**:
A part of a Recycler that turns it: by a player's right-click, a redstone signal, FE, a pack mod's magic or other means. A Recycler's drives add up; a hand-worked drive never turns faster than a player, and FE drives stop under the energy handicap.
_Avoid_: crank (what a hand-worked drive is turned by), handle

**Casing**:
A neutral part of a Recycler that does nothing but fill a place in its 3x3.

**Mix**:
The full amounts an essence breaks down into: several items of its tier's band, in fixed amounts. A decompose returns its yield of them, so each essence's mix holds on average, not on every decompose. Each essence has one mix, never changed by research.
_Avoid_: output, loot

**Yield**:
The share of a mix, or of what made an item, that one decompose or recycle returns, each part of an item left over as a chance of one. The core's tier sets it; by hand is always the lowest core's.
_Avoid_: fraction, return rate, efficiency

**Alternate recipe**:
An ascending recipe a challenge's research unlocks, often on one machine of the challenge's mods: a shortcut that skips a band, a step that mixes materials (copper with iron), or a more efficient step, as Satisfactory's are.

**Band**:
How processed an item is, from the top: intermediate 2, intermediate 1, refined, raw. Each essence tier decomposes into one band, Advanced into intermediate 2 down to Basic into raw.

**Inflation**:
The excess C.O.D. has built up since the current challenge began: essences it has made minus essences the team has decomposed. Only while a challenge is played does C.O.D. make them ever faster, with no ceiling, so it climbs whenever decomposing falls behind; voiding or storing essence doesn't lower it. When it reaches its cap before the challenge is finished, the challenge resets, and C.O.D. falls back to the pace it woke at.
_Avoid_: glut, overflow, timer

**Transmuted Essence**:
One of the four new essences, each made at the end of one challenge by that challenge's own way of processing essence. Put into C.O.D., it blocks the tier C.O.D. is making for good, and C.O.D. drops to the tier below. They are Galvanic (energy), Warding (protection), Vital (life) and Aureate (money). Each belongs to its challenge, not to a tier.
_Avoid_: new essence

**Quintessence**:
The final essence, made in the last chapter from the four Transmuted Essences, which shuts C.O.D. down for good.

**Challenge**:
One of four questlines, each bound to a fixed mod or set of mods, started at the lab. On every tier, the challenge reshapes how a team climbs back up from what it decomposed, through its own mods and alternate recipes. The team picks which challenge to take on against each tier C.O.D. makes, so the four are played in any order. Each challenge can be beaten against any tier, harder the lower the tier. Finishing it makes a Transmuted Essence that blocks the tier C.O.D. is making. Its mods take only raw and refined items, never intermediates. Its chain stays locked for a team until it starts the challenge, so nothing of it can be built ahead, and stays unlocked from then on, past a win or a reset. What its mods offer outside the chain, as the Shop, is never locked, and their items can still come from a mix, to be recycled.
_Avoid_: chapter (a quest-book page), stage

**Chain**:
The path a challenge's mods take a team from raw and refined items up to the challenge's Transmuted Essence. It starts where recycling leaves off: the Recycler brings what an essence decomposes into down to those bands, and that recycling is no part of the challenge.
_Avoid_: processing chain, path

**Handicap**:
What a challenge costs the team while it plays it, and only then: protection (more, stronger and special mobs), life (less max health), energy (its generators give it no energy, and its FE drives stop) or money (less money).
_Avoid_: threat, debuff

**Generator**:
A block the pack names as making energy, as Wireworks' engines, turbines and solar panels are. It belongs to the team whose island it stands on, and while that team plays the energy challenge it gives the team no energy, saying why, and goes on for every other team.
_Avoid_: power source

**Lab**:
The special block where a team chooses a handicap to start a challenge, and feeds science items to research what the challenge unlocks.

**Research**:
Consuming science items in the lab to unlock a challenge's alternate recipes for the whole team.
_Avoid_: stage, unlock (as a noun)

**Shop**:
Where essences and goods are traded for money. On this line it is a side path, and the money handicap is what makes it matter.
