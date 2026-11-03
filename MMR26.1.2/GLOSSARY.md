# Mechanical Mastery Reborn (26.1.2)

The 26.1.2 pack line, the sequel to Mechanical Mastery Plus, on NeoForge. Shared terms are in the root `GLOSSARY.md`.

**Spine**:
The suite.

**Mechanical Essence**:
On this line the loop runs backwards. The world has been reduced to essence: each one holds the idea of every other resource, which has to be decomposed out of it.

**C.O.D.**:
The line's adversary, a sentient AI that cannot be shut down. It makes essence of the highest tier not yet blocked, and nothing else: Advanced first, then one tier lower each time a challenge is won, until all four are blocked, when it makes nothing more. When its tier drops, what it still holds stays for the team to take, and the new tier spills out until there is room. Dormant, it makes it at a steady pace a little faster than decomposing by hand, and only until it holds a stack, so it never runs dry and never piles up. A team's first decompose on a Recycler (decomposing by hand never does) wakes it, and from then on it makes essence much faster, steadily between challenges and ever faster, without end, while one is played. Between challenges, being awake is only more essence for the team: C.O.D. is an adversary only while a challenge is played.

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
To break any item that isn't an essence down one band, on a Recycler only, for part of what made it, as Factorio's recycler does. It undoes only the crafting that made an item, never smelting, so it stops at refined: a refined item can't be recycled, and a raw one only recycles into less of itself. Nor does it undo what a player can uncraft for free: a block of ingots can't be recycled, and is uncrafted instead. Anything cooked counts as refined, food included. What the pack says an item recycles into always holds, refined or not. It is how a team gets below the band its essence decomposes into, and how it throws surplus away. Recycling never wakes C.O.D. and never counts against Inflation.
_Avoid_: decompose (for an item that isn't an essence), scrap, void

**Recycler**:
The companion mod's machine, after Factorio's, and much faster than decomposing by hand. It decomposes an essence into its mix, and recycles any item one band down for part of what made it, so a team can always rebuild from refined resources and recycling is also how surplus is thrown away. It is a multiblock built block by block: a core in the middle of one edge of a 3x3, its front facing out, and any mix of drives, inputs, outputs and casings in the other eight places, the centre among them, with at least one drive, input and output. Above that first floor it may have more, stacked directly upward, each a full 3x3 of any parts, as many floors in all as its core's tier, the minimums counted across them all. Its floors count upward from the core's to the first that isn't full: that floor and any above it, like any above what its core's tier allows, are no part of it. Its frame wraps the floors that are as one block, and a block two Recyclers would both claim belongs to neither. Its outputs are filled floor by floor upward, each floor clockwise from the front-left place, as seen facing the core's front, ending at the centre and, above the core's floor, the place over the core. Every Recycler processes anything; its core's tier decides how well. Each output holds three kinds of item; when a decompose or recycle gives more kinds than its outputs hold, it returns as many as fit, picked at random, and the rest are lost, so more outputs return more of a mix.
_Avoid_: starter machine, decomposer, crank recycler

**Core**:
The block in the middle of a Recycler's front edge, facing out, which holds its work and whose tier sets how fast it runs and how many floors its Recycler may have. There are four tiers; every core yields the same. A core of the next tier is crafted from the one below, and only once the team has won as many challenges as the tier it replaces, in any order; it takes the place of the old one in the same frame. A player takes back what it holds by hand, giving up the work under way; nothing else can.

**Drive**:
A part of a Recycler that turns it: by a player's right-click, FE or other means. A Recycler's drives add up; a hand-worked drive never turns faster than a player, and every turn that counts tires the player turning it, as sprinting does, so it costs food: it is how a team starts, or gets by when short of everything else, not how it runs. Decomposing by hand costs no food. An FE drive spends FE from its own store, so under the energy handicap it runs on what is stored until that is spent.
_Avoid_: crank (what a hand-worked drive is turned by), handle

**Casing**:
A neutral part of a Recycler that does nothing but fill a place in its 3x3.

**Mix**:
The full amounts an essence breaks down into: several items of its tier's band, in fixed amounts. A decompose returns its yield of them, so each essence's mix holds on average, not on every decompose, and only on a Recycler with room for every kind. A mix may hold any number of kinds, in any amounts. Each essence has one mix, never changed by research.
_Avoid_: output, loot

**Yield**:
The share of a mix, or of what made an item, that one decompose or recycle returns, each part of an item left over as a chance of one. It is the same on every core and by hand.
_Avoid_: fraction, return rate, efficiency

**Alternate recipe**:
An ascending recipe a challenge's research unlocks, often on one machine of the challenge's mods: a shortcut that skips a band, a step that mixes materials (copper with iron), or a more efficient step, as Satisfactory's are.

**Band**:
How processed an item is, from the top: intermediate 2, intermediate 1, refined, raw. A refined item is a material only smelted from raw, however many times, or cut from one raw kind alone: ingots, planks, glass, stone, smooth stone, sandstone, paper and bone meal. A thing worn or with a state of its own, as armour or a furnace, is never refined, however it is made. A raw item is one nothing crafts: what the world gives, as logs, cobblestone, ores, raw ores and mob drops. An intermediate 1 is the first crafted thing, and recycles completely into refined and raw items in one step, as a piston or a stick does; an intermediate 2 is anything that takes two steps or more, as an Assembler made from a Crafter does, or a Hopper from a Chest. Wood and stone count as much as metal. Each essence tier decomposes into one band, Advanced into intermediate 2 down to Basic into raw.

**Inflation**:
The excess C.O.D. has built up since the current challenge began: essences it has made minus essences the team has decomposed, of the tier it is making. Only while a challenge is played does C.O.D. make them ever faster, with no ceiling, so it climbs whenever decomposing falls behind; voiding or storing essence doesn't lower it. When it reaches its cap before the challenge is finished, the challenge resets, and C.O.D. falls back to the pace it woke at.
_Avoid_: glut, overflow, timer

**Transmuted Essence**:
One of the four new essences, each made at the end of one challenge by that challenge's own way of processing essence. Once a team has put its challenge's quota of them into its Port on C.O.D., the tier C.O.D. is making is blocked for good, and C.O.D. drops to the tier below; a reset loses what was put in toward the quota. A won chain goes on making them, and the Shop buys every one but the Aureate, which it only sells. They are Galvanic (energy), Warding (protection), Vital (life) and Aureate (money). Each belongs to its challenge, not to a tier.
_Avoid_: new essence

**Quintessence**:
The final essence, made in the last chapter from the four Transmuted Essences, which shuts C.O.D. down for good.

**Challenge**:
One of four goals, each bound to a fixed mod or set of mods, started and researched at the lab and followed through EMI, as Factorio's progression is, with no quest book. On every tier, the challenge reshapes how a team climbs back up from what it decomposed, through its own mods and alternate recipes. The team picks which challenge to take on against each tier C.O.D. makes, so the four are played in any order. Each challenge can be beaten against any tier, harder the lower the tier. It is won by putting its quota of Transmuted Essence into its Port on C.O.D., which blocks the tier C.O.D. is making; a won challenge can't be played again. Its mods keep their own recipes. Its chain stays locked for a team until it starts the challenge, so nothing of it can be built ahead, and a chain machine that comes from a mix does no work until then, and stays unlocked from then on, past a win or a reset. What its mods offer outside the chain, as the Shop, is never locked, and their items can still come from a mix, to be recycled.
_Avoid_: questline, chapter, stage

**Quota**:
How many of its Transmuted Essence a challenge takes to win, against the tier C.O.D. is making. It grows with each challenge won, faster than the better core a win unlocks speeds a team up, so each challenge takes longer than the last.
_Avoid_: target, goal

**Port**:
One of the four blocks of the island, one for each Transmuted Essence, set on C.O.D.'s four corners in an x, as part of it and as unbreakable, and counting for the team whose island it stands on. A Port opens only while its challenge is played, and then takes its own Transmuted Essence, and no other, from a hand or any automation, using each one up toward the quota and never taking more than it. Once its challenge is won it shuts for good and shows it, so the island shows how far the team has got.
_Avoid_: input (a Recycler's part), socket, slot

**Chain**:
The path a challenge's mods take a team up to the challenge's Transmuted Essence, from whatever their own recipes take. What an essence decomposes into is recycled or crafted into that, and that recycling is no part of the challenge.
_Avoid_: processing chain, path

**Handicap**:
What a challenge costs the team while it plays it, and only then: protection (an Assault on its island), life (less max health), energy (its generators give it no energy, though FE it banked ahead is still spent) or money (less money). A team's money can't be stockpiled ahead: it is set aside while the challenge is played, and given back when it ends. Its FE can, and banking it is part of the energy challenge's strategy; so can ordinary mob drops, but not the drop only an Assault gives.
_Avoid_: threat, debuff

**Assault**:
The protection challenge's handicap: waves of mobs the companion mod sends at a playing team's island from gates on platforms it builds in the void, with unbreakable bridges to the island. The waves grow harder, with a breather between each step, and never stop until the quota is filled or Inflation reaches its cap; a gate that fails opens again. Its mobs hunt the team's players and drop Rift Shards, explosions damage the team's island while it runs, claimed or not, and it pauses while no one is on the island.
_Avoid_: raid, siege, invasion

**Home gate**:
A smaller gate a team opens on its own island with a pearl, whenever it likes, once it has started the protection challenge. It gives the Assault's drop, but less of it than the Assault, so a won protection chain goes on making Warding Essence.
_Avoid_: practice gate, farm gate

**Defence**:
A block that fights the Assault's mobs on its own, fed by the factory with FE or with what its lines make, and never for nothing. Defences are free from the start, as the team's gear is, so growing them ahead is part of the protection challenge's strategy.
_Avoid_: turret, tower

**Rift Shard**:
The drop only an Assault's mobs and a home gate give, which the Warding Essence is made from with ordinary drops and something recycled from the loop. A team keeps what it hasn't used past a win or a reset.
_Avoid_: ward shard, gate drop

**Generator**:
A block the pack names as making energy, as Wireworks' engines, turbines and solar panels are. It belongs to the team whose island it stands on, and while that team plays the energy challenge it gives the team no energy, saying why, and goes on for every other team.
_Avoid_: power source

**Lab**:
The special block where a team chooses a handicap to start a challenge, and feeds science items to research what the challenge unlocks.

**Manual**:
The pack's in-game book on how to build and work the companion mod's blocks, with 3D scenes, which every player is given once. It holds neither lore nor recipes, which are EMI's.
_Avoid_: guide, quest book, wiki

**Research**:
Consuming science items in the lab to unlock a challenge's alternate recipes for the whole team.
_Avoid_: stage, unlock (as a noun)

**Shop**:
Where goods are traded for money: a second way to resources beside decomposing, and the skyblock's store for what no essence gives, as saplings, water and lava. A team buys what it lacks, and sells only the few goods the Shop takes, made things as metal blocks rather than whatever it has too much of; a fluid that only the Shop gives is never sold back. Until Basic, whose mix holds them, it is the only source of ores, and it is always the source of a team's first food: seeds and passive farm animals. Money was the heart of Mechanical Mastery Plus and stays a big part of its sequel. It is never locked, and the money handicap is what makes it costly.

**Permit**:
AdminShop's named unlock, held by a team's Account, which opens Offers gated by it. A Sell Permit opens Offers the Shop buys from the team, a Buy Permit Offers it sells to the team; one never opens both. A team buys its Permits in the Shop, all but the one it is given when it starts the money challenge, and keeps them for good.
_Avoid_: Trade Permit, license, tier
