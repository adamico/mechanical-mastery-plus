# The Electric Defence on a stone post, built with its store full, so it shoots the first hostile mob that
# comes in reach until the store is spent. The scene brings no generator: a cable from one fills it again.
function mechanicalmasteryreborn:gallery/clear
setblock ~14 ~ ~-6 minecraft:stone
setblock ~14 ~1 ~-6 mechanicalmasteryreborn:fe_defence{store:{energy:2000}}
function mechanicalmasteryreborn:gallery/sign {scene:"defence"}
