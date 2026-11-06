# A scene's sign, at ~14 ~ ~ facing the visitor: its title and three lines from the lang file. Waxed, so a
# click runs its title's command instead of opening the editor: the scene again, from the plot's origin, since
# a sign's command runs at the sign's centre.
$setblock ~14 ~ ~ minecraft:oak_sign[rotation=0]{is_waxed:1b,front_text:{messages:[{translate:"mechanicalmasteryreborn.gallery.$(scene)",bold:true,click_event:{action:"run_command",command:"execute positioned ~-14 ~ ~ run function mechanicalmasteryreborn:gallery/$(scene)"}},{translate:"mechanicalmasteryreborn.gallery.$(scene).1"},{translate:"mechanicalmasteryreborn.gallery.$(scene).2"},{translate:"mechanicalmasteryreborn.gallery.$(scene).3"}]}}
