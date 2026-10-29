# Changelog

Written for the pack and its players: what Mechanical Mastery Reborn adds, or changes. A published version never changes: a fix is the next patch (`docs/agents/releases.md`).

## Unreleased

- Decomposing by hand: a right-click with an essence decomposes one into its mix, straight into the inventory, then rests it for 8 seconds (#166).
- The Recycler, a multiblock built block by block with no screen and no power: a tier-1 core in the middle of a 3x3, and any mix of hand drives, inputs, outputs and casings around it, with at least one drive, input and output. Each right-click on a hand drive turns it, at most once every 5 ticks, and drives add up; eight turns decompose one essence. Inputs take essence by hand, from a hopper above or from anything beside them. Each output shows three results on a shelf, taken by a right-click or by a hopper below, filled clockwise from the front-left place (#166).
- Turning a hand drive is heard and seen: a click that rises in pitch as the decompose goes, with sparks; the crafter's sound and a puff when it decomposes; a dull click and smoke when the Recycler can't decompose, so the turn is lost (#166).
- The Recycler's parts are full blocks in white space plating, from Malcolm Riley's unused textures (CC BY 4.0). On top and bottom, the drives' rails turn to the Recycler's outer edges, so the whole build is framed. An input's front and an output's niche face the player who places them (#166).
- The `mechanicalmasteryreborn:decomposing` recipe type, which the pack writes, with a KubeJS schema; EMI shows its recipes, and Jade shows a Recycler's turns, what it holds and why it is stopped, on its core and on each of its drives (#166).
- Each decompose is recorded for the team, by tier and by way: on a Recycler, for the team whose Skyblock Builder island it stands on, or by hand (#166).
- C.O.D., dormant: its one slot holds a stack of Advanced Mechanical Essence and refills the moment it is emptied, by hand or by automation. It makes nothing else (#164).
- The essences: Basic, Regular, Improved and Advanced Mechanical Essence, which are in the Works tab, and Galvanic, Warding, Vital and Aureate Essence and Quintessence, registered under their ids but not obtainable yet (#164).
