# The 26.1.2 line's blocks and machines live in a companion mod

The 26.1.2 line's own blocks and machines live in a companion mod from the first release: the lab, C.O.D., the Recycler and the per-team handicaps. KubeJS keeps only recipes and balance. The companion mod starts from the libworks template, so it gets CI and game tests, and it is released on CurseForge like any mod the pack consumes. It lives in this repo, in the line's folder, not in a repo of its own: Reborn is planned and tracked here, and the pack and its mod change together. So it can't take the template as a whole repo, as the suite's mods do: its Gradle build sits in a subfolder, its CI runs only on changes there, and its release tags are named so they never clash with the pack's.

KubeJS 8 can make ticking machines with inventories and FE, and starting there would have been quicker. We chose a mod anyway because the loop rests on these machines: per-team state, the race against C.O.D. and the lab's choices are much easier to test in Java game tests than in scripts. Moving them out of KubeJS later would also have meant rewriting them while players' worlds already held them.

Amended for #166: the decomposing machine is the Recycler, a tiered machine from a hand-cranked tier up, much faster than decomposing by hand. Reborn is a modpack first, so the companion mod ships the machine and no recipes: what each essence and item decomposes into is the pack's KubeJS.

Amended for #184: what an item recycles into is derived by the companion mod, from the recipes that make it run backwards, since writing one for every item would never keep up with the pack's mods. The pack writes only overrides, as `recycling` recipes. What essences decompose into is still the pack's alone.
