# The Recycler on FE: the same parts, an FE drive in each back corner, each built with its store full, so the
# Recycler turns on its own until the stores are spent, and its core, placed last. The scene brings no
# generator: a cable from one fills the drives again.
function mechanicalmasteryreborn:gallery/clear
function mechanicalmasteryreborn:gallery/recycler_parts
setblock ~13 ~1 ~-8 mechanicalmasteryreborn:fe_drive{store:{energy:600}}
setblock ~15 ~1 ~-8 mechanicalmasteryreborn:fe_drive{store:{energy:600}}
setblock ~14 ~1 ~-6 mechanicalmasteryreborn:recycler_core_1[facing=south]
function mechanicalmasteryreborn:gallery/sign {scene:"recycler_fe"}
