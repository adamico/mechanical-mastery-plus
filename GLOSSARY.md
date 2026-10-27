# Mechanical Mastery

Minecraft modpacks, kept here as one packwiz manifest per pack line, released to players on CurseForge and to servers as a server pack. This glossary holds what every line shares; each line's own terms are in its folder (`GLOSSARY-MAP.md`).

## The pack

**Pack line**:
The pack on one Minecraft version, kept in its own folder: Mechanical Mastery Plus on 1.19.2 (`MMP1.19.2/`), Mechanical Mastery Reborn on 26.1.2 (`MMR26.1.2/`). Each line has a pack identity, which lines may share or not, and releases on its own, with its own tags.
_Avoid_: port, edition

**Skyblock**:
How every Mechanical Mastery line starts: a lone island in a void world, with every resource after it earned through the pack's progression. It defines the pack, so it is kept on every line.

**Spine**:
The mods whose machines a line's tiers are built on. Each line names its own.

**Mechanical Essence**:
The pack's own tiered product: Basic (red), Regular (blue), Improved (yellow) and Advanced (magenta). Each line runs its own loop through them.
_Avoid_: cube

**C.O.D.**:
The Conscious Overproduction Device: the industrial machine abomination whose making ends Mechanical Mastery Plus, and which makes Mechanical Essences without limit.

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
