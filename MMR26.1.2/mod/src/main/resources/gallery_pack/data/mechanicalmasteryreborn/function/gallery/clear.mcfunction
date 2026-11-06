# Clears a scene's plot, from ~2 to ~25 east and ~-12 to the sign at ~0 south, 16 high on a stone floor.
# Entities go first (the essences' item displays), and loose items last: the fills pop the
# sign and spill what the chests, hoppers and barrels held.
execute positioned ~2 ~-1 ~-12 as @e[type=!minecraft:player,dx=23,dy=16,dz=12] run kill @s
# structure_void first: a fill that changes nothing fails the build (libworks ADR 0010).
fill ~2 ~-1 ~-12 ~25 ~15 ~ minecraft:structure_void
fill ~2 ~-1 ~-12 ~25 ~-1 ~ minecraft:stone
fill ~2 ~ ~-12 ~25 ~15 ~ minecraft:air
execute positioned ~2 ~-1 ~-12 as @e[type=minecraft:item,dx=23,dy=16,dz=12] run kill @s
