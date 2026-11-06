# A 3x3 Recycler's parts but its drives, raised a block on a stone plinth, its core at ~14 ~1 ~-6 facing the
# sign and so its centre behind it at ~14 ~1 ~-7: an input in the centre, outputs on the west and east sides
# facing outward, a casing at the back, and the corners left for the drives the scene chooses. Essence goes in
# from a chest over the input through a hopper, and each output's hopper under it sorts what it holds into a
# barrel of its own, in plain sight. The core goes last, so every part takes its place in the frame.
fill ~13 ~ ~-8 ~15 ~ ~-8 minecraft:stone
setblock ~14 ~ ~-7 minecraft:stone
setblock ~14 ~ ~-6 minecraft:stone
setblock ~14 ~1 ~-7 mechanicalmasteryreborn:recycler_input
setblock ~14 ~1 ~-8 mechanicalmasteryreborn:recycler_casing
setblock ~13 ~1 ~-6 mechanicalmasteryreborn:recycler_output[facing=west]
setblock ~13 ~1 ~-7 mechanicalmasteryreborn:recycler_output[facing=west]
setblock ~15 ~1 ~-6 mechanicalmasteryreborn:recycler_output[facing=east]
setblock ~15 ~1 ~-7 mechanicalmasteryreborn:recycler_output[facing=east]
setblock ~13 ~ ~-6 minecraft:hopper[facing=west]
setblock ~13 ~ ~-7 minecraft:hopper[facing=west]
setblock ~15 ~ ~-6 minecraft:hopper[facing=east]
setblock ~15 ~ ~-7 minecraft:hopper[facing=east]
setblock ~12 ~ ~-6 minecraft:barrel[facing=up]
setblock ~12 ~ ~-7 minecraft:barrel[facing=up]
setblock ~16 ~ ~-6 minecraft:barrel[facing=up]
setblock ~16 ~ ~-7 minecraft:barrel[facing=up]
setblock ~14 ~2 ~-7 minecraft:hopper[facing=down]
setblock ~14 ~3 ~-7 minecraft:chest[facing=south]{Items:[{Slot:0b,id:"mechanicalmasteryreborn:advanced_mechanical_essence",count:64}]}
