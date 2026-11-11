# Mechanical Mastery Reborn (26.1.2)

The 26.1.2 pack line, the sequel to Mechanical Mastery Plus, on NeoForge. Shared terms are in the root `GLOSSARY.md`.

**Mechanical Essence**:
On this line the loop runs backwards. The world has been reduced to essence: each one holds the idea of every other resource, which has to be decomposed out of it.

**C.O.D.**:
The line's adversary, a sentient AI that nothing but Quintessence can shut down. It makes essence of the highest tier not yet blocked, and nothing else: Advanced first, then one tier lower each time a challenge is won, until all four are blocked, when it makes nothing more until its last stand. When its tier drops, what it still holds stays for the team to take, and the new tier spills out until there is room. Dormant, it makes it at a steady pace a little faster than decomposing by hand, and only until it holds a stack, so it never runs dry and never piles up. A team's first decompose on a Recycler (decomposing by hand never does) wakes it, and from then on it makes essence much faster, steadily between challenges and ever faster, without end, while one is played. Between challenges, being awake is only more essence for the team: C.O.D. is an adversary only while a challenge is played.

**Suite**:
The 5thlayer -works mods (Groundworks, Beltworks, Pipeworks, Wireworks, Craftworks, Fieldworks, Voidworks, Gearworks), which the pack consumes as released jars, as any player would. What the pack needs from them it scripts itself; a change goes to the mod only for a bug or a workaround worth sharing.

**Showcase**:
The FactoryWorks Showcase, the suite's own sandbox pack. A separate pack that this line borrows a base of mods from, and never depends on.

**Companion mod**:
Mechanical Mastery Reborn's own mod, which holds what the pack can't script: C.O.D., the essences, the Recycler, the lab and each team's challenge state. The pack consumes it as a released jar, like the suite. It is published as "Mechanical Mastery Reborn Core", since the pack holds the name "Mechanical Mastery Reborn"; that is its name, not a term.
_Avoid_: pack mod, core mod, Reborn Core

**Decompose**:
To break a Mechanical Essence down into its mix, on a Recycler or, much more slowly, by hand, as Factorio's scrap is. It is how resources enter this line. The higher the tier, the more processed what it decomposes into: Advanced into the most refined intermediates, Basic into raw resources. A lower tier's outputs need more stages to become what the tier above yielded, so the factory built for one tier becomes the last stages of the next one's and grows upstream rather than being replaced. Each lower tier is harder and longer to decompose, with more loops and more mods: Advanced, the first, is the easiest, and Basic, the last, the hardest.
_Avoid_: break down (Voidworks' word for its own item breakdown)

**Recycle**:
To break any item that isn't an essence down one band, on a Recycler only, for part of what made it, as Factorio's recycler does. It undoes only the crafting that made an item, never smelting, so it stops at refined: a refined item can't be recycled, and a raw one only recycles into less of itself. Nor does it undo what a player can uncraft for free: a block of ingots can't be recycled, and is uncrafted instead. Anything cooked counts as refined, food included, and what an ore drops, raw as it is, can't be recycled either. Where a recipe takes any of several items, recycling returns the first its tag lists. What the pack says an item recycles into always holds, refined or not. It is how a team gets below the band its essence decomposes into, and how it throws surplus away. Recycling never wakes C.O.D. and never counts against Overproduction.
_Avoid_: decompose (for an item that isn't an essence), scrap, void

**Recycler**:
The companion mod's machine, after Factorio's, and much faster than decomposing by hand. It decomposes an essence into its mix, and recycles any item one band down for part of what made it, so a team can always rebuild from refined resources and recycling is also how surplus is thrown away. It is a multiblock built block by block: a core in the middle of one edge of a 3x3, its front facing out, and any mix of drives, inputs, outputs and casings (neutral parts that only fill a place) in the other eight places, the centre among them, with at least one drive, input and output. Above that first floor it may have more, stacked directly upward, each a full 3x3 of any parts, as many floors in all as its core's tier, the minimums counted across them all. Its floors count upward from the core's to the first that isn't full: that floor and any above it, like any above what its core's tier allows, are no part of it. Its frame wraps the floors that are as one block, and a block two Recyclers would both claim belongs to neither. Its outputs are filled floor by floor upward, each floor clockwise from the front-left place, as seen facing the core's front, ending at the centre and, above the core's floor, the place over the core. Every Recycler processes anything; its core's tier decides how well. Each output holds three kinds of item; when a decompose or recycle gives more kinds than its outputs hold, it returns as many as fit, picked at random, and the rest are lost, so more outputs return more of a mix.
_Avoid_: starter machine, decomposer, crank recycler

**Core**:
The block in the middle of a Recycler's front edge, facing out, which holds its work and whose tier sets how fast it runs and how many floors its Recycler may have. There are four tiers; every core yields the same. A core of the next tier is crafted from the one below, and only once the team has won as many challenges as the tier it replaces, in any order; it takes the place of the old one in the same frame. A player takes back what it holds by hand, giving up the work under way; nothing else can.

**Drive**:
A part of a Recycler that turns it: by a player's right-click, FE or other means. A Recycler's drives add up; a hand-worked drive never turns faster than a player, and every turn that counts tires the player turning it, as sprinting does, so it costs food: it is how a team starts, or gets by when short of everything else, not how it runs. Decomposing by hand costs no food. An FE drive spends FE from its own store, so under the energy handicap it runs on what is stored until that is spent.
_Avoid_: crank (what a hand-worked drive is turned by), handle

**Mix**:
The full amounts an essence breaks down into: several items of its tier's band, in fixed amounts. A decompose returns its yield of them, so each essence's mix holds on average, not on every decompose, and only on a Recycler with room for every kind. A mix may hold any number of kinds, in any amounts. Below Advanced, a tier's mix holds what the mixes above it are made from, so a team's factory grows upstream; what a chain needs beyond that comes from the Shop or the world. Basic's is the first to hold raw ores; no mix holds an ore block. The mixes are closed: whatever an item of a mix recycles into, some mix holds. Each essence has one mix.
_Avoid_: output, loot

**Yield**:
The share of a mix, or of what made an item, that one decompose or recycle returns, each part of an item left over as a chance of one. It is the same on every core and by hand.
_Avoid_: fraction, return rate, efficiency

**Band**:
How processed an item is, from the top: intermediate 2, intermediate 1, refined, raw. A refined item is a material only smelted from raw, however many times, or cut from one raw kind alone: ingots, planks, glass, stone, smooth stone, sandstone, paper and bone meal. A thing worn or with a state of its own, as armour or a furnace, is never refined, however it is made. A raw item is one nothing crafts: what the world gives, as logs, cobblestone, ores, raw ores, what an ore drops, as redstone and quartz, and mob drops. An intermediate 1 is the first crafted thing, and recycles completely into refined and raw items in one step, as a piston or a stick does; an intermediate 2 is anything that takes two steps or more, as an Assembler made from a Crafter does, or a Hopper from a Chest. Wood and stone count as much as metal. Each essence tier decomposes mostly into one band, Advanced into intermediate 2 down to Basic into raw.

**Overproduction**:
The excess C.O.D. has built up since the current challenge began: essences it has made minus essences the team has decomposed, of the tier it is making. Only while a challenge is played does C.O.D. make them ever faster, with no ceiling, so it climbs whenever decomposing falls behind; voiding or storing essence doesn't lower it. When it reaches its cap before the challenge is finished, the challenge resets, and C.O.D. falls back to the pace it woke at.
_Avoid_: Inflation (the money handicap's price rises), glut, overflow, timer

**Transmuted Essence**:
One of the four new essences, each made at the end of one challenge by that challenge's own way of processing essence. Once a team has put its challenge's quota of them into its Port on C.O.D., the tier C.O.D. is making is blocked for good, and C.O.D. drops to the tier below; a reset loses what was put in toward the quota, and makes every one made before it stale: each is marked with the attempt it was made in and a Port takes only the current attempt's. Stale matters to a Port alone: the Shop buys a stale one, and a Quartessence takes it, as any other. A won chain goes on making them, and the Shop buys every one, for more than what made it but less than twice that, so a won chain never pays for the next challenge outright, and sells none. They are Galvanic (energy), Warding (protection), Vital (life) and Aureate (money). Each belongs to its challenge, not to a tier.
_Avoid_: new essence

**Extraction**:
Breaking a Transmuted Essence other than the Galvanic down on a machine of the energy chain into one raw form of mercury, salt or sulfur, its own for each essence. A stale one is extracted as any other. It feeds the chain's lowest stages, never past one, and is a trade against selling the essence, not a must.
_Avoid_: decomposing (the Recycler's), recycling

**Galvanic Charge**:
One of three items the energy chain packs on the way to the Galvanic Essence: mercury, salt and sulfur each into its own, salt and sulfur only as Reformed Salt and Reformed Sulfur. Packing them is the chain's, so it stays locked until the team starts energy, and mercury saved up ahead still waits for the handicap.
_Avoid_: cell, battery

**Reformed Salt**:
The salt a Salt Charge packs, made only on the Reformation Array's pedestals, paid in mercury flux, from any salt the Distiller, Calcination or Extraction gives, so reformation can't be skipped.
_Avoid_: mineral salt (Theurgy's, which only sits on the target pedestal)

**Reformed Sulfur**:
The sulfur a Sulfur Charge packs, made only on the Reformation Array's pedestals, paid in mercury flux, from any sulfur the Distiller, Calcination or Extraction gives, so reformation can't be skipped.
_Avoid_: redstone sulfur (Theurgy's, which only sits on the target pedestal)

**Galvanic Core**:
What the Incubator makes from one Galvanic Charge of each kind; several make a Galvanic Essence.
_Avoid_: Core (a Recycler's)

**Quintessence**:
The final essence, made in the last stand. Its Voidworks way assembles one of each Transmuted Essence into a Quartessence and climbs that, in a Mutation Chamber, by a steep Rung paid in motes from the Nether and the End; a second way, with no Voidworks, makes it from Nullessence only while the team bears handicaps it chooses, harder than in their challenges, and the faster the more it bears. Put into C.O.D. itself during its last stand, by hand or any automation, each one cancels a fixed amount of what C.O.D. makes, and only Quintessence moves the Lead.

**Quartessence**:
The four Transmuted Essences assembled into one, one of each, which a Mutation Chamber, never a Void Crucible, climbs into Quintessence. Its recipe and that Rung stay locked until the last stand starts; Voidworks itself never is.

**Nullessence**:
What C.O.D. makes in its last stand, the opposite of Quintessence. A team decomposes it on the Recycler into a mix drawn from all four tiers', which the four chains run on, or turns it into Quintessence by a second way of its own; whatever becomes of it, C.O.D. has made it, and it counts against the Lead.
_Avoid_: antithesis, anti-essence

**Hermetic Lab**:
The team's lab, awakened once the team starts its last stand: it stands changed and opens a menu of its own, and turns Nullessence, and nothing else, into Quintessence, the second way, with no Voidworks. Each handicap a team takes on there runs for a fixed spell that can't be ended early, at its challenge's peak from the start, and opens a supply line fed by the very thing the handicap takes, as it is taken: the team's generators' output, with banked FE cut (energy); Essentia Vitae from hearts given at the Hermetic Lab itself, under the hungriest diet (life); Null Shards from that spell's Assault, at its hardest steps (protection); and money drained from what the team earns during the spell, with its money set aside and the Shop's prices up (money). So nothing saved up feeds a line. The Hermetic Lab works only while a line is fed, and the more lines are, the faster it works and the less Nullessence each Quintessence takes; only all four take it ahead of C.O.D. on its own. A line counts only while its handicap is in effect, so a paused Assault counts for nothing while its spell runs on.
_Avoid_: Athanor (Neo Vitae's own block), furnace, purifier

**Last stand**:
What a team plays once it has won all four challenges, for good: C.O.D. making again, Nullessence this time, from when the team starts it at the lab. The Nether and the End open to the team, and it makes Quintessence; Quintessence's chain stays locked until the last stand starts, as a challenge's does. It has no handicap and can't be lost. It speeds up, a little behind, as the team's Quintessence nears its pace, up to a ceiling, and slows to a set floor when the team stalls. It ends, and the pack is won, when the team's Lead reaches the margin: C.O.D. shuts down for good and stands dark on the island.
_Avoid_: last chapter, endgame, final challenge, fifth challenge

**Lead**:
How far the team's Quintessence is ahead of C.O.D. in its last stand: what the team's Quintessence has cancelled minus the Nullessence C.O.D. has made since the last stand started, kept, voided or processed alike. Decomposing doesn't move it. It never falls below minus the margin, and the team wins the pack when it reaches the margin.
_Avoid_: score, Overproduction (which is a challenge's)

**Challenge**:
One of four goals, each bound to a fixed mod or set of mods, started at the lab and followed through EMI, as Factorio's progression is, with no quest book. On every tier, the challenge reshapes how a team climbs back up from what it decomposed, through its own mods. The team picks which challenge to take on against each tier C.O.D. makes, so the four are played in any order. Each challenge can be beaten against any tier, harder the lower the tier. It is won by putting its quota of Transmuted Essence into its Port on C.O.D., which blocks the tier C.O.D. is making; a won challenge can't be played again. Its mods keep their own recipes. Its chain stays locked for a team until it starts the challenge, so nothing of it can be built ahead, and its machines, however they are had, do no work until then, though their crafting is never locked, and stays unlocked from then on, past a win or a reset, but not past a stop. Money's chain is the Shop's Offers its Permit opens, held the same way. What its mods offer outside the chain, as the Shop, is never locked, and their items can still come from a mix, to be recycled.
_Avoid_: questline, chapter, stage

**Stop**:
A team leaving the challenge it plays at the lab, to take on another or come back to it better prepared. It is offered only once the challenge has reset for the team, and from then on from the start of every attempt of it, and made when every member online has asked for it. It ends the handicap and Overproduction, loses what was put toward the quota, and locks the challenge's chain again until the team starts it anew, though a reset keeps it; the stopped challenge, or any other not won, can start at once.
_Avoid_: abandon, quit, forfeit

**Quota**:
How many of its Transmuted Essence a challenge takes to win, against the tier C.O.D. is making. It grows with each challenge won, faster than the better core a win unlocks speeds a team up, so each challenge takes longer than the last.
_Avoid_: target, goal

**Port**:
One of the four blocks of the island, one for each Transmuted Essence, set on C.O.D.'s four corners in an x, as part of it and as unbreakable, and counting for the team whose island it stands on. A Port opens only while its challenge is played, and then takes its own Transmuted Essence, and no other, from a hand or any automation, using each one up toward the quota and never taking more than it. Once its challenge is won it shuts for good and shows it, so the island shows how far the team has got.
_Avoid_: input (a Recycler's part), socket, slot

**Chain**:
The path a challenge's mods take a team up to the challenge's Transmuted Essence, from whatever their own recipes take, or, for a challenge whose mod has no processing of its own, the path the pack writes on the suite's machines. What an essence decomposes into is recycled or crafted into that, and that recycling is no part of the challenge.
_Avoid_: processing chain, path

**Handicap**:
What a challenge costs the team while it plays it, and only then: protection (an Assault on its island), life (hunger that drains faster, and a food feeding a player less each time it is already in their Diet, the foods they have eaten lately, forgotten when the challenge ends), energy (its generators give it no energy, though FE it banked ahead is still spent, and its chain's heat comes from FE alone) or money (less money). A team's money can't be stockpiled ahead: it is set aside while the challenge is played, and given back when it ends. Its FE can, and banking it is part of the energy challenge's strategy; so can ordinary mob drops, but not the drop only an Assault gives, and so can food, though under faster hunger only a varied farm keeps up.
_Avoid_: threat, debuff

**Assault**:
The protection challenge's handicap: waves of mobs the companion mod sends at a playing team's island from one gate at a time, on one of four platforms it builds in the void around the island, with unbreakable bridges to it. The waves grow harder, with a breather between each step, at which the gate moves to another side at random, and never stop until the quota is filled or Overproduction reaches its cap; a gate that fails opens again. Its mobs hunt the team's players and drop Rift Shards, explosions damage the team's island while it runs, claimed or not, what its mobs break is the team's refund when it ends, and it pauses while no one is on the island.
_Avoid_: raid, siege, invasion

**Home gate**:
A smaller gate a team opens on its own island with a pearl, whenever it likes, once it has won the protection challenge: no recipe makes the pearl, and the Shop sells it from the win on. It gives the Assault's drop, but less of it than the Assault, so a won protection chain goes on making Warding Essence.
_Avoid_: practice gate, farm gate

**Defence**:
A block that fights the Assault's mobs on its own, fed by the factory with FE or with what its lines make, and never for nothing. Defences are free from the start, as the team's gear is, so growing them ahead is part of the protection challenge's strategy.
_Avoid_: turret, tower

**Null Shard**:
The drop only the mobs of an Assault taken on at the Hermetic Lab give, which its protection line uses up; any automation may bring it there.
_Avoid_: Rift Shard (the challenge's own drop)

**Rift Shard**:
The drop only an Assault's mobs and a home gate give, the harder mobs of its deeper waves giving more of it, which the protection challenge's Chain takes with the Assault's ordinary drops and oil up to the Warding Essence. A team keeps what it hasn't used past a win or a reset.
_Avoid_: ward shard, gate drop

**Coin**:
What the money challenge's Chain mints from an ingot of copper, iron or gold, melted with lava and cast in a mold, and what an Aureate Essence is made from, coins of all three metals. A team short of a metal exchanges coins up for it: copper coins for an iron one, iron coins for a gold one. The Shop never buys a coin, so a team's gold is either sold or minted, never both.
_Avoid_: currency, money (the Account's), token

**Generator**:
A block the pack names as making energy, as Wireworks' engines, turbines and solar panels are. It belongs to the team whose island it stands on, and while that team plays the energy challenge it gives the team no energy, saying why, and goes on for every other team.
_Avoid_: power source

**Lab**:
The special block where a team chooses a handicap to start a challenge, asks for a stop, takes its refund, swaps a dirt for an emergency sapling when it has none, at most once a day. Once the team starts its last stand it awakens as the Hermetic Lab.

**Refund**:
What a team's lab holds for it: everything its Assaults' mobs broke on its island, given back when each Assault ends rather than put back in its place, each block as a careful mining by hand would give it. It waits there until a member takes it.
_Avoid_: repair, restore, compensation

**Manual**:
The pack's in-game book on how to build and work the companion mod's blocks, with 3D scenes, and on how its challenges and its last stand are played, which every player is given once. It holds neither lore nor recipes, which are EMI's: a chain is told as its steps' names, never their amounts.
_Avoid_: guide, quest book, wiki

**Shop**:
Where goods are traded for money: a second way to resources beside decomposing, and the skyblock's store for what no essence gives, as saplings, water and lava. A team buys what it lacks, and sells only the few goods the Shop takes, made things as metal blocks rather than whatever it has too much of; a fluid that only the Shop gives is never sold back. Whatever the Shop takes, it pays more for than what made it cost, at full price; the share the money handicap pays may leave a loss. It never buys what the first essence's mix holds, so a team's first money takes a step after decomposing; from the first win on, what a mix holds may be sold. Until Basic, whose mix holds them, it is the only source of ores, and it is always the source of a team's first food: seeds and passive farm animals. Money was the heart of Mechanical Mastery Plus and stays a big part of its sequel. It is never locked, and the money handicap is what makes it costly.

**Permit**:
AdminShop's named unlock, held by a team's Account, which opens Offers gated by it. A Sell Permit opens Offers the Shop buys from the team, a Buy Permit Offers it sells to the team; one never opens both. The Shop sells none: a team holds only the two it is given, the money challenge's, which opens every Offer of its chain and is held while the chain is unlocked, from a start to a stop, and the protection win's, for good.
_Avoid_: Trade Permit, license, tier

**Gallery**:
A datapack in the companion mod's jar, off until a player turns it on, that builds working scenes. There are two: the mod's own builds a scene of each of the companion mod's features from its blocks alone, each with a sign naming it, as every suite mod's does (libworks ADR 0010); the pack's builds whole lines of the pack's mods working together, one per stretch of the pack's story, for its video and gallery images, and needs the whole pack to load.
_Avoid_: demo pack, showcase pack, pack gallery, gallery pack
