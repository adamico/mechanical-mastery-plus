# Changelog

## Unreleased

- The pack has a title screen of its own: its island and essences behind the pack's title, the menu's buttons in its colours, the pack's version, a link to report a bug, and its name and Quintessence on the game's window (FancyMenu).

## 0.0.1

- A server leaves out the client-only mods: rendering, sound, tooltip, camera and menu mods, and the libraries only they need.
- Craftworks 0.11.3: a new server's first start no longer logs a `NullPointerException` from Craftworks' config (5thlayer/craftworks#84).
- Voidworks 0.2.2: its Jade plugin loads on a dedicated server, so a player joining one sees Voidworks' Jade lines; 0.2.1 logged "Failed to load plugin from Voidworks" there (5thlayer/voidworks#137, found in #277).
- Fieldworks 0.7.2: Jade shows an ore block's remaining amount and its requirement line on a dedicated server too (5thlayer/fieldworks#56).
- Gearworks 0.2.3: the suit's FE reading can be pushed in from its corner by the `energyHud` client config's `offsetX` and `offsetY`, to clear another mod's HUD text (5thlayer/gearworks#25).
