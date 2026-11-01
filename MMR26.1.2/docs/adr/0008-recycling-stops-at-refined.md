# Recycling stops at refined, and ores come from the Shop

Recycling undoes only crafting, grid crafting and Craftworks' assembling, never smelting, blasting, smoking or a campfire. It stops at refined: a refined item, an ingot, planks, glass or stone, can't be recycled, and the Recycler refuses it. A raw item, which nothing crafts, only recycles into less of itself, which is how a team throws raw surplus away. Until Basic, whose mix holds them, ores and raw ores come only from the Shop; the world gives the rest of raw, logs and cobblestone.

We could have let the Recycler run smelting backwards, as it first did, so that an ingot recycled into raw ore and a team could rebuild from raw resources out of any essence. That made the Shop a side path and left a recycled ingot worth more as ore, a free step back up Theurgy's raw-ore route. Stopping at refined keeps recycling to the crafted bands it exists for, and makes selling surplus and buying ores the money loop that Mechanical Mastery Plus was built on.

## Consequences

- A team's surplus refined items can't be thrown away on a Recycler; they go to the Shop.
- Theurgy's routes are counted in ingots; one that takes raw ore needs the Shop.
- The Recycler's code changes: smelting no longer recycles anything, and a refined item is refused.
- What is refined is what Regular's mix is drawn from, and what it is cut or smelted from is what Basic's must give: refusing a refined item strands nothing past those tiers. A material cut from one raw kind alone, as sandstone, paper or bone meal, is refined; a thing worn or with a state of its own, as armour or a furnace, never is.
- Raw surplus, ores among them, can still be thrown away on a Recycler, and so can anything made only by a recipe the Recycler doesn't read.
