# A challenge's mods keep their own recipes

A challenge's mods, Theurgy for the energy challenge, AdminShop for the money challenge and the mods of the challenges to come, keep their recipes as the mod defines them. The pack rewrites none of them; the one recipe it adds to a challenge's mods is the last step, which makes the challenge's Transmuted Essence from that mod's own products. Theurgy's recipes happen to take only raw and refined items, and AdminShop's machines take intermediates as a barrel and a hopper: either way, a challenge's Chain starts from whatever its mods' own recipes take.

We could have scripted recipes that take Advanced's intermediates (a piston, a copper bulb) straight into Theurgy, as the first research for Advanced's mix proposed. That made each challenge's start a set of pack recipes, tied to whatever an essence's mix held. Keeping the challenge's mods on their own recipes lets a mix change without touching any challenge, and plays each mod as its players know it.

Gateways to Eternity, the protection challenge's mod, is a mod whose content is data: it ships a few sample gates and their pearls' recipes, and a pack is meant to write its own. For it, keeping the mod as its players know it means keeping its mechanics, its waves, rewards and failures, as Gateways has them. The pack empties its sample gates and their recipes, and writes the Assault's gates and the home gate's pearl (ADR 0010).

A mod of this kind has no processing of its own, so there is no chain of its recipes to end with one step. Where a challenge's mod has no processing, the pack writes its Chain on the suite's machines: the protection challenge's runs on Craftworks' Assemblers and Fieldworks' Harvester, from the Assault's drops, Fieldworks' crude, tar and magma slurry, obsidian and Wireworks' plastic and steam, each made by its own mod's rules, up to the Warding Essence. The pack still rewrites none of those mods' recipes; it only adds the Chain's own.

This ADR first read "A challenge's mods take only raw and refined items", which described Theurgy's recipes as though it were the rule; the decision was always to leave the mods' recipes alone.

## Consequences

- What an essence decomposes into has to be brought, by recycling or crafting, to what its challenge's Chain takes.
- Recycling is no part of a Chain.
- A Chain the pack writes on the suite's machines takes nothing a mod makes only one way by another route: Fieldworks' tar comes only from crude meeting lava, so the protection Chain takes it so.
