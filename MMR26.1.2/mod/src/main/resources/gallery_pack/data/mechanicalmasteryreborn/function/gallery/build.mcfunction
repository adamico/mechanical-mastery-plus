# The Gallery: a working scene of each of the companion mod's own features, the first where it is run and
# each further one 24 blocks east (libworks ADR 0010, #278). Each clears its own plot first. The scenes hold
# the mod's blocks and vanilla's only, and set no team state: C.O.D. awake, a challenge played or won, are
# seen on a real island.
function mechanicalmasteryreborn:gallery/recycler
execute positioned ~24 ~ ~ run function mechanicalmasteryreborn:gallery/recycler_fe
execute positioned ~48 ~ ~ run function mechanicalmasteryreborn:gallery/cod
execute positioned ~72 ~ ~ run function mechanicalmasteryreborn:gallery/ports
execute positioned ~96 ~ ~ run function mechanicalmasteryreborn:gallery/essences
execute positioned ~120 ~ ~ run function mechanicalmasteryreborn:gallery/defence
