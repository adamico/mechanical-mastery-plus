# One essence on show: an item display turning over a smooth stone pedestal at ~x ~ ~z.
$setblock ~$(x) ~ ~$(z) minecraft:smooth_stone
$execute align xyz run summon minecraft:item_display ~$(x).5 ~1.5 ~$(z).5 {item:{id:"mechanicalmasteryreborn:$(essence)",count:1},billboard:"vertical"}
