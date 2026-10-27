# Mechanical Mastery

Minecraft modpacks, kept here as a packwiz manifest per Minecraft version, and released to players on CurseForge and to servers as a server pack: Mechanical Mastery Plus on 1.19.2, and its sequel Mechanical Mastery Reborn on 26.1.2.

## The pack

**Pack line**:
The pack on one Minecraft version, such as 1.19.2 or 26.1.2. Each line has a pack identity, which lines may share or not: Mechanical Mastery Plus on 1.19.2, Mechanical Mastery Reborn on 26.1.2. Each releases on its own, with its own tags.
_Avoid_: port, edition

**Skyblock**:
How every Mechanical Mastery line starts: a lone island in a void world, with every resource after it earned through the pack's progression. It defines the pack, so it is kept on every line.

**Spine**:
The mods whose machines the pack's tiers are built on. On 1.19.2 it is Create, Thermal, Mekanism and Compact Crafting; on 26.1.2 it is the suite.

**Suite**:
The 5thlayer -works mods (Groundworks, Beltworks, Pipeworks, Wireworks, Craftworks, Fieldworks, Voidworks, Gearworks), which the pack consumes as released jars, as any player would. What the pack needs from them it scripts itself; a change goes to the mod only for a bug or a workaround worth sharing.

**Showcase**:
The FactoryWorks Showcase, the suite's own sandbox pack. A separate pack that the 26.1.2 line borrows a base of mods from, and never depends on.

**Companion mod**:
Mechanical Mastery Reborn's own mod, which holds its own blocks and machines: the lab, C.O.D. and the decomposing machine. The pack consumes it as a released jar, like the suite.
_Avoid_: pack mod, core mod

**Mechanical Essence**:
The pack's own tiered product: Basic (red), Regular (blue), Improved (yellow) and Advanced (magenta). On 1.19.2 the player makes essences and sells them to the shop for the next tier's resources. On 26.1.2 the loop runs backwards: the player starts with endless Advanced essence from C.O.D. and decomposes it down the tiers.
_Avoid_: cube

**Mechanical Crystal**:
An essence pressed into a crystal, one per tier. It is the form the 1.19.2 shop buys.

**C.O.D.**:
The Conscious Overproduction Device: the industrial machine abomination whose making ends the 1.19.2 line. It has piled up endless Advanced essence, inflating the economy until basic resources are scarce, and has become a sentient AI that cannot be shut down. It is the 26.1.2 line's adversary.

**Decompose**:
To break a Mechanical Essence down into the intermediates it is made of, on the suite's machines, as Factorio's recycler does. It is how resources enter the 26.1.2 line.
_Avoid_: recycle, break down (Voidworks' word for its own item breakdown)

**Challenge**:
One of four questlines, each tied to one mod, started at a special block and done in any order. Finishing it makes a new essence that blocks C.O.D.'s production. The handicap the player chose for it stays until the pack ends.
_Avoid_: chapter (a quest-book page), stage

**Handicap**:
What a challenge costs the team that chose it, kept for the rest of the pack: protection (more, stronger and special mobs), life (less max health), energy (generator blocks disabled) or money (less money).
_Avoid_: threat, debuff

**Lab**:
The special block where a team chooses a handicap to start a challenge, and feeds science items to research what the challenge unlocks.

**Research**:
Consuming science items in the lab to unlock a challenge's recipes for the whole team.
_Avoid_: stage, unlock (as a noun)

**Shop**:
Where essences and goods are traded for money. It drives the 1.19.2 line; on 26.1.2 it is a side path, and the money handicap is what makes it matter.

## Releasing

**Release**:
One version of a pack, fixed by its tag: the manifest, changelog section and client zip that version shipped with. A published release never changes.
_Avoid_: build, deploy

**Cut**:
Making a release: its changelog section, version bump, tag and client zip, all local. Nothing public happens.
_Avoid_: build, release (as a verb)

**Publish**:
Sending a cut release to its sites. Public and final for each site it reaches.
_Avoid_: upload, deploy

**Pack identity**:
The facts that name a pack wherever it is released: its name, slug, tag prefix, CurseForge project and GitHub repo.

**Client zip**:
The CurseForge-format zip a release exports from the manifest, which players install. It can't be rebuilt byte for byte, so the one exported at release time is the release's.
_Avoid_: modpack zip, export

**Server pack**:
The server files of a release, pointed at that release's client zip on CurseForge's CDN and ignoring its client-only mods, which servers install from. Uploaded to CurseForge as a child of the client zip.
_Avoid_: server zip, server files (for the uploaded pack)

**Client-only mod**:
A mod the manifest marks `side = "client"` in its `.pw.toml`, which only players install. That field is the only record of it: the server pack's ignore list is made from it, never kept by hand.
_Avoid_: client mod list, ignore list (for the record)

**Site**:
A place a release is published to: CurseForge (client zip), CurseForge's server pack, or GitHub's release.

**Release record**:
What a release learns while it is published, such as the client zip's CurseForge file id, kept beside the client zip so a retry can use it.
