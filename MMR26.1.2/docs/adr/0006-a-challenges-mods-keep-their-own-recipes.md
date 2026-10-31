# A challenge's mods keep their own recipes

A challenge's mods, Theurgy for the energy challenge, AdminShop for the money challenge and the mods of the challenges to come, keep their recipes as the mod defines them. The pack rewrites none of them; the one recipe it adds to a challenge's mods is the last step, which makes the challenge's Transmuted Essence from that mod's own products. Theurgy's recipes happen to take only raw and refined items, and AdminShop's machines take intermediates as a barrel and a hopper: either way, a challenge's Chain starts from whatever its mods' own recipes take.

We could have scripted recipes that take Advanced's intermediates (a piston, a copper bulb) straight into Theurgy, as the first research for #209 proposed. That made each challenge's start a set of pack recipes, tied to whatever an essence's mix held. Keeping the challenge's mods on their own recipes lets a mix change without touching any challenge, and plays each mod as its players know it.

This ADR first read "A challenge's mods take only raw and refined items", which described Theurgy's recipes as though it were the rule; the decision was always to leave the mods' recipes alone.

## Consequences

- What an essence decomposes into has to be brought, by recycling or crafting, to what its challenge's Chain takes.
- Recycling is no part of a Chain.
