# What each mod is for

Every mod in `mods/` has a role here (#163, story 49). A content mod with no role doesn't go in, and a mod that loses its role comes out. A new mod gets a line in the same commit that adds it.

## Content

The suite, taken as released CurseForge jars:

- **Craftworks**: decomposing, on its Assemblers and Refiners, and the Personal Assembler that plans from essence.
- **Beltworks**, **Pipeworks**: carry the decomposed intermediates and fluids, so the loop runs unattended.
- **Wireworks**: power. Its generators are what the energy handicap turns off.
- **Fieldworks**: ore blocks crafted from intermediates, and the Harvester that works them.
- **Groundworks**: line placement, and the base every other suite mod needs.
- **Voidworks**: void harvested from the world and carried between dimensions, the reason the void Nether and End are reachable.
- **Gearworks**: the FE suit and lance. In the pack with the rest of the suite (ADR 0002); its challenge, protection, comes after 0.0.1.

The rest of 0.0.1:

- **Theurgy**: alchemy, the path to the Galvanic Essence.
- **Chapters**: research locks. A team's finished research is a stage, and recipes wait on it.
- **FTB Quests**: the quest book, which tells C.O.D.'s story and mirrors each research.
- **FTB Teams**, **FTB Library**, **FTB XMod Compat**: what FTB Quests needs for teams, and its bridge to the other mods.
- **Skyblock Builder**: the island start, an island per team, and the void Nether and End.
- **Default World Type**: makes Skyblock Builder's world type the one a new world picks.
- **EMI**: the recipe viewer. Locked recipes stay visible in it. EMI's own project has no file for NeoForge 26.1.2, so this is the port the Showcase plays, `emi-unofficial-port-unstable`.
- **Refined Storage**: storage.
- **KubeJS**, **Rhino**: the pack's scripts: recipes, balance, and giving a player with no team a team of one.

The companion mod, Mechanical Mastery Reborn, is in the pack once it is on CurseForge. Until then `scripts/launch.py` adds its local build.

## The Showcase's base

The FactoryWorks Showcase's performance and QoL mods, minus its content: no new blocks or items, nothing that changes what a player can make.

- Performance: **AI Improvements**, **BadOptimizations**, **Clumps**, **Connectivity**, **Entity Culling**, **FerriteCore**, **fix GPU memory leak**, **ImmediatelyFast**, **ModernFix**, **Particle Core**, **Sodium**, **spark**.
- Fixes: **AttributeFix**, **Crash Assistant**, **I'm Fast**, **Max Health Fix**, **Neruina**, **NetherPortalFix**.
- QoL: **AppleSkin**, **BetterF3**, **Block Runner**, **Bridging Mod**, **Clean Swing Through Grass**, **Colorful Hearts**, **Configured**, **Controlling**, **Default Options**, **Double Doors**, **Durability Tooltip**, **Enchantment Descriptions**, **Extreme Sound Muffler**, **FallingTree**, **Freecam**, **Inventory Essentials**, **Inventory Sorter**, **Jade**, **Leaves Be Gone**, **More Overlays Updated**, **Mouse Tweaks**, **RightClickHarvest**, **Simple Discord Rich Presence**, **Sound Physics Remastered**, **Swivel**, **Toast Control**, **TrashSlot**.

Left out of the Showcase's list, as content with no role here: Animal Feeding Trough, Charging Gadgets, Chisel Reborn, Comforts, Connected Glass, Easy Villagers, Energy Meter, Factory Blocks, FTB Chunks, FTB Filter System, FTB Industrial Contraptions, Functional Storage, GraveStone, Just Dire Things, JustHammers, Mining Gadgets, OpenBlocks Elevator, Refined Storage Curios Integration, Sophisticated Backpacks, Starter Kit and XP Tome. JEI and its addons (JER, JEP, JEED) go too, since EMI is the viewer.

## Libraries

In the pack only because a mod above needs them: Balm, Better Advanced Tooltips (KubeJS), Cloth Config, Collective, Configurable, Cupboard, Fzzy Config, GeckoLib (Theurgy), JamLib, Kotlin for Forge, LibX (Skyblock Builder), Modonomicon (Theurgy), Nanite Library, Placebo, Prickle, Puzzles Lib, Searchables, SuperMartijn642's Config Lib (Durability Tooltip) and YetAnotherConfigLib. A library no mod needs any more comes out with the mod.
