# C.O.D. with the lab beside it, as a new island has them, and Advanced Mechanical Essence spilling onto the
# pile on its top, as an awake C.O.D. leaves it. The pile is laid here, since the scene wakes no team: it
# never despawns, and the clear takes it away.
function mechanicalmasteryreborn:gallery/clear
setblock ~14 ~ ~-6 mechanicalmasteryreborn:cod
setblock ~16 ~ ~-6 mechanicalmasteryreborn:lab
execute align xyz run summon minecraft:item ~14.5 ~1.1 ~-5.5 {Item:{id:"mechanicalmasteryreborn:advanced_mechanical_essence",count:32},Age:-32768s}
execute align xyz run summon minecraft:item ~14.3 ~1.1 ~-5.7 {Item:{id:"mechanicalmasteryreborn:advanced_mechanical_essence",count:16},Age:-32768s}
execute align xyz run summon minecraft:item ~14.7 ~1.1 ~-5.3 {Item:{id:"mechanicalmasteryreborn:advanced_mechanical_essence",count:16},Age:-32768s}
function mechanicalmasteryreborn:gallery/sign {scene:"cod"}
