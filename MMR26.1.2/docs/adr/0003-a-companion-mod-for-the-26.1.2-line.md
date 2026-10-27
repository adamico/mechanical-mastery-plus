# The 26.1.2 line's blocks and machines live in a companion mod

The 26.1.2 line's own blocks and machines live in a companion mod from the first release: the lab, C.O.D., the decomposing machine and the per-team handicaps. KubeJS keeps only recipes and balance. The companion mod starts from the libworks template, so it gets CI and game tests, and it is released on CurseForge like any mod the pack consumes.

KubeJS 8 can make ticking machines with inventories and FE, and starting there would have been quicker. We chose a mod anyway because the loop rests on these machines: per-team state, the race against C.O.D. and the lab's choices are much easier to test in Java game tests than in scripts. Moving them out of KubeJS later would also have meant rewriting them while players' worlds already held them.
