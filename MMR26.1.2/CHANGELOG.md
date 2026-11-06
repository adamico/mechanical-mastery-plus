# Changelog

## Unreleased

- A server leaves out the client-only mods: rendering, sound, tooltip, camera and menu mods, and the libraries only they need.
- Craftworks 0.11.3: a new server's first start no longer logs a `NullPointerException` from Craftworks' config (5thlayer/craftworks#84).
- Fieldworks 0.7.2: Jade shows an ore block's remaining amount and its requirement line on a dedicated server too (5thlayer/fieldworks#56).
- Gearworks 0.2.3: the suit's FE reading can be pushed in from its corner by the `energyHud` client config's `offsetX` and `offsetY`, to clear another mod's HUD text (5thlayer/gearworks#25).
