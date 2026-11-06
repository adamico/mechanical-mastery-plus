# The four Ports in a row, in the challenges' order: Galvanic, Warding, Vital, Aureate, each in its essence's
# colour. A Port shows its team's play of its challenge, dark until played, lit while played and framed once
# won, so here, with no team state set, each stands closed; on a team's island it shows that team's.
function mechanicalmasteryreborn:gallery/clear
setblock ~8 ~ ~-6 mechanicalmasteryreborn:galvanic_port
setblock ~12 ~ ~-6 mechanicalmasteryreborn:warding_port
setblock ~16 ~ ~-6 mechanicalmasteryreborn:vital_port
setblock ~20 ~ ~-6 mechanicalmasteryreborn:aureate_port
function mechanicalmasteryreborn:gallery/sign {scene:"ports"}
