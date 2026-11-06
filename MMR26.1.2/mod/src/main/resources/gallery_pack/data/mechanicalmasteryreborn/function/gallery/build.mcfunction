# The Gallery: a working scene of each of the companion mod's own features, the first where it is run and
# each further one 24 blocks east (libworks ADR 0010, #278). Each clears its own plot first. The scenes hold
# the mod's blocks and vanilla's only, and set no team state: built on a team's island, its Recyclers, C.O.D.
# and Ports are that team's, so C.O.D. awake and spilling, or a challenge played or won, are seen there.
function mechanicalmasteryreborn:gallery/recycler
execute positioned ~24 ~ ~ run function mechanicalmasteryreborn:gallery/recycler_fe
execute positioned ~48 ~ ~ run function mechanicalmasteryreborn:gallery/cod
execute positioned ~72 ~ ~ run function mechanicalmasteryreborn:gallery/ports
execute positioned ~96 ~ ~ run function mechanicalmasteryreborn:gallery/essences
execute positioned ~120 ~ ~ run function mechanicalmasteryreborn:gallery/defence
