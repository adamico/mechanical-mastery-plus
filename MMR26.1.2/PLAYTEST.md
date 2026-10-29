# Playtest checklist

Pack content (the manifest, configs, KubeJS, the island, the quest book) has no automated test (#163). Before each release, play through this list in the CurseForge profile with `scripts/launch.py`, which uses the companion mod's local build: build it first with `sh ./gradlew build` in `mod/`. A ticket that adds pack content adds its checks here, under its own heading.

Start each section that says "a fresh world" from a new singleplayer world, made with the defaults.

## The island (#165)

- [ ] `packwiz refresh` in this folder leaves `pack.toml`, `index.toml` and `mods/` unchanged, and every mod in `mods/` has its role in `MODS.md`.
- [ ] `scripts/launch.py` starts the profile, and the log names the local companion mod: `mods: mechanicalmasteryreborn ... -> local <version>`.
- [ ] The new world screen picks Skyblock Builder's world type by itself.
- [ ] A fresh world puts you on the Reborn island, on the grass facing C.O.D., not on the stone spawn platform, and `/skyblock list` shows a team named after you, with you in it.
- [ ] C.O.D. holds a stack of Advanced essence. Taking all of it, by hand and by a hopper under it, fills it again at once.
- [ ] A second team gets its own island: `/skyblock manage teams create Second`, then `/skyblock visit Second` shows a second island with its own C.O.D., far from yours. (On a LAN or server, a second player joining instead gets their own team and island as you did.)
- [ ] A player who leaves their team (`/skyblock leave`) and logs in again gets a new team of one with a new island, never the shared spawn.
- [ ] A Nether portal on the island takes you to a void Nether, standing on the portal's platform, and back.
- [ ] `/execute in minecraft:the_end run tp @s 0 70 0` puts you over the End's main island in a void End, and the exit portal brings you home.
