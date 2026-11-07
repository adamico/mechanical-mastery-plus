# Changelog

## Unreleased

## 0.0.4

- Groundworks 0.7.1: a sheet holding a Blueprint is told from paper: it is named Blueprint, glints, and its tooltip says Copy or Cut, how many blocks it holds and the box's size, and "Pasting air" when set to; a blank sheet with a Corner stored says where. A sheet stamped by 0.7.0 catches up the first time it is held (5thlayer/groundworks#87).

### Mods

- Updated Groundworks: `groundworks-0.7.0.jar` → `groundworks-0.7.1.jar`

## 0.0.3

- The manual's Overproduction section tells the threat as falling behind C.O.D., not as its essence doing harm, so keeping or voiding essence is no danger; the danger is the reset, which loses what the Port has taken and makes the attempt's Transmuted Essence stale. The C.O.D. page says the same in a line (#321).
- The money challenge mints copper, iron and gold on the Assembler, locked until the team starts the challenge as every chain is, and kept past a reset but not a stop: an ingot melts with lava into molten metal, which carries the lava's rock as dross, the molten metal is cast in a Mold, and the cast is cooled with water and knocked out into that metal's Coin and Burnt Sand, which holds the dross. Burnt Sand washes back into sand, and sand and clay with water make the next Mold. Every step takes a fluid, so the Personal Assembler plans none of it (#320).
- The Shop prices lava as a consumable, $5 a bucket in place of $1000, and no longer buys it back, since only the Shop gives it. The Galvanic, Warding and Vital Essences sell for more than what their chains spend making them and under twice that, in place of $2500 each: $4.50, $1500 and $340. The Shop no longer buys any fluid: Essentia Vitae, liquified experience, sal ammoniac and superheated steam go, so a chain's fluids go into its essence rather than being sold. So a won chain's extras are a profit at full price and a loss at the money handicap's half (#319).
- The Aureate Essence is minted, not bought: coins of all three metals are sealed into one on the Assembler, and copper and iron coins exchange up, two copper for an iron and three iron for a gold, at a loss against their metal. An Aureate sealed during the money challenge carries its attempt, so a reset makes it stale. The Shop no longer sells the Aureate and buys it as it buys the other three, for $50 under the money Permit, and never buys a coin. The Manual's money page tells the mint's steps (#323).
- Craftworks 0.11.5: the CurseForge app no longer installs official EMI's 1.21.1 jar beside the pack's EMI port (5thlayer/craftworks#69), and a world no longer fails to load when a mod's item throws as Craftworks asks whether it is a filled bucket.

### Mods

- Updated kc00l's Craftworks: `craftworks-0.11.3.jar` → `craftworks-0.11.5.jar`

## 0.0.2

- The pack has a title screen of its own: its island and essences behind the pack's title, the menu's buttons in its colours, the pack's version, a link to report a bug, and its name and Quintessence on the game's window (FancyMenu).

### Mods

- Added FancyMenu (`fancymenu_neoforge_3.9.14_MC_26.1.2.jar`)
- Added Konkrete (`konkrete_neoforge_1.10.1_MC_26.1.1.jar`)
- Added Melody (`melody_neoforge_1.0.16_MC_26.1.1.jar`)

## 0.0.1

- A server leaves out the client-only mods: rendering, sound, tooltip, camera and menu mods, and the libraries only they need.
- Craftworks 0.11.3: a new server's first start no longer logs a `NullPointerException` from Craftworks' config (5thlayer/craftworks#84).
- Voidworks 0.2.2: its Jade plugin loads on a dedicated server, so a player joining one sees Voidworks' Jade lines; 0.2.1 logged "Failed to load plugin from Voidworks" there (5thlayer/voidworks#137, found in #277).
- Fieldworks 0.7.2: Jade shows an ore block's remaining amount and its requirement line on a dedicated server too (5thlayer/fieldworks#56).
- Gearworks 0.2.3: the suit's FE reading can be pushed in from its corner by the `energyHud` client config's `offsetX` and `offsetY`, to clear another mod's HUD text (5thlayer/gearworks#25).
