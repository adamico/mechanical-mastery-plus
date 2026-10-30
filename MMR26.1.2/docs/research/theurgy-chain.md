# The energy challenge's Chain in Theurgy 1.116.1 (#209)

Research for the energy challenge's Chain: the path from raw and refined items to the Galvanic Essence, through Theurgy's own recipes, with nothing powered by FE. `theurgy-chain.json` holds the same content as data.

## The rule

A challenge's mods take only raw and refined items. Recycling is no part of the Chain. Every step is one of Theurgy's own recipes, unchanged, except the last one, which the pack adds to make the Galvanic Essence from Theurgy's products ([ADR 0006](../adr/0006-a-challenges-mods-take-only-raw-and-refined-items.md)). Under the energy handicap, a step may use Theurgy's heat and Mercury Flux but no FE.

Sources, all primary:

- **Jar**: `theurgy-26.1.2-neoforge-1.116.1.jar`, the pack's copy, unzipped and read as data. `R/` below means `data/theurgy/recipe/` inside it. `lang` means `assets/theurgy/lang/en_us.json`, which holds the Hermetica book's text.
- **Source**: klikli-dev/theurgy at commit `498b72e0c66ec4d3a07c605ccdb9ef6ac00d2e8e` (release 26.1.2-1.116.1). `GH/` means `https://github.com/klikli-dev/theurgy/blob/498b72e0c66ec4d3a07c605ccdb9ef6ac00d2e8e/src/main/java/com/klikli_dev/theurgy/`.

**Nothing in Theurgy needs FE.** Its heat comes from burning fuel or from Mercury Flux, and Mercury Flux is a capability of its own (`GH/registry/CapabilityRegistry.java`). The only FE nearby is Craftworks' Assembler, which no chain uses.

## 1. Catalogue

### Main apparatus

| Apparatus | In → out (time) | Driver | FE | Automation | Source |
|---|---|---|---|---|---|
| Sal Ammoniac Accumulator on a Tank | 1000 mB water → 100 mB Sal Ammoniac (100 t) | none | no | Water in, solvent out of the tank | `R/accumulation/sal_ammoniac_from_water.json`; `GH/content/apparatus/salammoniacaccumulator/SalAmmoniacAccumulatorCraftingBehaviour.java` |
| Liquefaction Cauldron | item + 10–15 mB Sal Ammoniac → its sulfur. Ingot or dust 1, raw ore 3, ore 5, redstone ore 9 (100 t) | heat | no | Belt or pipe | `R/liquefaction/alchemical_sulfur_{copper,iron,gold,redstone}_from_*.json` |
| Mercury Distiller | item → Mercury Shards: 2 logs, 10 cobblestone or 8 planks → 1; copper or iron ingot, raw copper or iron, redstone, coal → 1; gold ingot or raw gold → 5 (100 t) | heat | no | Belt or pipe | `R/distillation/*.json`; `data/theurgy/tags/item/*/mercury/*.json` |
| Mercury Catalyst | shard → 800 flux at 20 a tick. Holds 50,000, pushes 2 a tick per side | none | no | Shards in. The emitters sit on it | `R/catalysation/mercury_flux_from_mercury_shard.json`; `GH/content/apparatus/mercurycatalyst/MercuryCatalystBlockEntity.java` |
| Calcination Oven | cobblestone → 1 Strata Salt; 5 Strata → 1 Mineral Salt; ingot → 2 Mineral (100 t) | heat | no | Belt or pipe | `R/calcination/alchemical_salt_{strata_from_cobblestone,mineral_from_strata_salt,mineral_from_ingots}.json` |
| Incubator + Salt, Sulfur and Mercury Vessels | 1 sulfur + 1 Mineral Salt + 1 shard → the ingot or dust (100 t) | heat | no | One pipe per vessel. The vessels take only salts, sulfurs or mercuries, one of each per craft | `R/incubation/ingots_*_from_alchemical_sulfur_*.json`; `GH/content/recipe/IncubationRecipe.java` |
| Fermentation Vat | sulfur + plank or log + water (125 / 250 / 500 mB by tier) → its type's niter (200 t) | none | no | Piped while open, works closed. A redstone signal on its back closes the lid, and losing it opens it | `R/fermentation/alchemical_niter_metals_*_using_{planks,logs}.json`; `GH/content/behaviour/redstone/VatRedstoneChangeOpenCloseLidBehaviour.java` |
| Digestion Vat | up a tier: 4 niters + 1 Purified Gold + Sal Ammoniac → 1 (400 t). Down a tier: 1 → 4, no gold (200 t). Gold ingot + any salt + 1000 mB water → 20 Purified Gold (400 t) | none | no | As the Fermentation Vat. Three input slots, each a different item | `R/digestion/*.json`; `GH/content/apparatus/digestionvat/DigestionStorageBehaviour.java` |
| Reformation Array (Sulfuric Flux Emitter + Source, Target, Result Pedestals) | source sulfurs or niters + flux → more of the target sulfur, which isn't consumed (100 t) | Mercury Flux | no | Pipe into the Source Pedestals and out of the Result Pedestal. By hand, once: link the pedestals and place the target. One target per array. The emitter holds 1000 flux and takes a recipe's whole cost at once | `R/reformation/*.json`; `GH/content/apparatus/reformationarray/{ReformationArrayCraftingBehaviour,SulfuricFluxEmitterBlockEntity}.java`; `lang` `book.theurgy.the_hermetica.apparatus.target_pedestal.intro.text` |

### Heat and flux

| Block | What it does | FE | Source |
|---|---|---|---|
| Pyromantic Brazier | Heats the apparatus above it by burning furnace fuel. Fuel goes in by belt or pipe. | no | `GH/content/apparatus/pyromanticbrazier/PyromanticBrazierBlockEntity.java`; `lang` `...pyromantic_brazier.intro.text` |
| Caloric Flux Emitter | Sits on a Catalyst and heats one linked apparatus up to 8 blocks away. It burns 100 flux per 120 ticks for as long as it is fed. | no | `GH/content/apparatus/caloricfluxemitter/CaloricFluxEmitterBlockEntity.java`; `lang` `...caloric_flux_emitter.usage.text` |
| Mercury Capacitor | A flux store, ten times a Catalyst's. | no | `lang` `...mercury_capacitor.intro.text` |

### How Theurgy moves between materials

The metals sit in tiers (`data/theurgy/tags/item/alchemical_sulfurs/metals/*.json`): copper is abundant, iron common and gold rare. Redstone is a common "other mineral" (`.../other_minerals/common.json`).

- **Within a tier**: a niter reforms into any sulfur of its type and tier.
- **Up a tier**: digestion takes 4 niters and 1 Purified Gold for 1. This is Exaltation (`lang` `...digestion_vat.intro.text`).
- **Down a tier**: digestion gives 4 for 1 and needs no gold.
- **Back to an item**: incubation.

A raw ore liquefies to 3 sulfurs, and each sulfur incubates back to an ingot, so Theurgy triples ore by itself.

Theurgy's own research (`data/theurgy/modonomicon/research/the_hermetica/*.json`) only tracks which book entries a player has read. It locks no recipe.

## 2. Candidate Chains

All three end at the same proposed final step (section 3): **16 Copper Sulfur + 4 Iron Sulfur + 2 Gold Sulfur + 1000 Mercury Flux**, on an array whose target holds a Redstone Sulfur. They differ in which metal goes in and how it reaches the three sulfurs.

The figures below rest on these assumptions:

- One apparatus of each kind, at recipe times, steady state, with seeds already placed.
- Recipe times are fixed and heat doesn't speed them up. Heat is on or off.
- Flux for heat is counted only while an apparatus works.
- Raw means logs, cobblestone, raw ores and ores. Refined means ingots, dusts and planks.

### A. Spagyric breakdown: the metals as they are

**Starts from** copper, iron and gold ingots (refined), or raw copper, iron and gold (raw). It also takes water (raw), logs (raw) for mercury, and one redstone dust (refined) for the template.

1. Accumulator: water → Sal Ammoniac.
2. Cauldron: 16 copper, 4 iron and 2 gold ingots → their sulfurs. On raw ores it takes 6 raw copper, 2 raw iron and 1 raw gold, and the surplus carries over.
3. Distiller: 2 logs → a shard. Catalyst: shard → 800 flux.
4. The Galvanic step.

### B. Exaltation of copper: climb from the bottom

**Starts from** raw copper (raw) as the only metal. It also takes planks (refined), water, logs and cobblestone (raw). Once, to seed it: one iron ingot, one raw gold and one redstone dust.

1. Cauldron: raw copper → 3 Copper Sulfur.
2. Fermentation Vat: Copper Sulfur + plank + 125 mB water → Abundant Metals Niter (`R/fermentation/alchemical_niter_metals_abundant_using_planks.json`).
3. Digestion Vat: 4 Abundant + Purified Gold + 10 mB Sal Ammoniac → 1 Common (`R/digestion/alchemical_niter_metals_common_from_abundant.json`).
4. Reformation (Iron target): Common Niter + 100 flux → Iron Sulfur (`R/reformation/alchemical_sulfur_iron_from_alchemical_niter_metals_common.json`).
5. Digestion: 4 Common + Purified Gold + 15 mB Sal Ammoniac → 1 Rare (`.../alchemical_niter_metals_rare_from_common.json`). Then Reformation (Gold target): Rare + 150 flux → Gold Sulfur (`R/reformation/alchemical_sulfur_gold_from_alchemical_niter_metals_rare.json`).
6. The gold loop:
   - Calcination: cobblestone → Strata Salt, then 5 Strata → Mineral Salt.
   - Incubator: Gold Sulfur + Mineral Salt + shard → gold ingot (`R/incubation/ingots_gold_from_alchemical_sulfur_gold.json`).
   - Digestion: ingot + salt + 1000 mB water → 20 Purified Gold (`R/digestion/purified_gold.json`).
7. The Galvanic step.

### C. Transmutation of iron: down for copper, up for gold

**Starts from** iron ingots (refined), or raw iron (raw), as the only metal. It also takes planks (refined), water and cobblestone (raw), and cobblestone is its mercury. Once, to seed it: one copper ingot, one raw gold and one redstone dust.

1. Cauldron: iron ingot → Iron Sulfur. Four of them go straight to the Galvanic step.
2. Fermentation: Iron Sulfur + plank + 250 mB water → Common Metals Niter (`R/fermentation/alchemical_niter_metals_common_using_planks.json`).
3. Copper, by digesting down: 1 Common + 10 mB Sal Ammoniac → 4 Abundant (`R/digestion/alchemical_niter_metals_abundant_from_common.json`). Then Reformation (Copper target): Abundant + 50 flux → Copper Sulfur (`R/reformation/alchemical_sulfur_copper_from_alchemical_niter_metals_abundant.json`). Four iron make the sixteen copper.
4. Gold, by digesting up: as in B, step 5.
5. The gold loop, as in B. Mercury comes from 10 cobblestone per shard (`R/distillation/cobblestones.json`).
6. The Galvanic step.

### Comparison

| | A Spagyric breakdown | B Exaltation of copper | C Transmutation of iron |
|---|---|---|---|
| (a) Main apparatus | 5: Accumulator, Cauldron, Distiller, Catalyst, Array | 9, all of them; 3 arrays (Iron, Gold, Redstone targets) | 9, all of them; 3 arrays (Copper, Gold, Redstone targets) |
| (b) Byproduct or loop | Weak: mercury heats its own apparatus; on raw ores the surplus sulfurs carry over | Strong gold loop: each climb costs a Purified Gold, so 0.93 of the 2.93 Gold Sulfur made goes back to an ingot | Digestion both ways: down multiplies by four for free, up costs gold. A light gold loop: 0.1 of 2.1 Gold Sulfur goes back |
| (c) Alternate recipe spots | Two metals liquefied in one step; research gating raw ore's 3 or ore's 5 | Abundant straight to rare (skips a band); 2 niters per fermentation; 40 Purified Gold per ingot; Copper Sulfur reformed straight to Iron | Iron Sulfur reformed straight to Copper (skips fermentation and digestion); common to rare without gold |
| (d) Belts and pipes | Fully. By hand, once: link the array, place the template | Fully, with a redstone loop on each vat's lid. By hand, once: three arrays and their seeds | As B |
| (e) Time per Galvanic | About 2,600 t (2.2 min): 22 liquefactions, then 400 t of reformation. About 1,300 t on raw ores | About 12,600 t (10.5 min): 63 fermentations are the bottleneck. About 3.5 min with 3 Fermentation and 2 Digestion Vats | About 2,500 t (2.1 min) steady: 12.4 fermentations. About 6 min for the first |
| (e) Raw and refined per Galvanic | 16 copper, 4 iron, 2 gold ingots (or about 5.3 raw copper, 1.3 raw iron, 0.7 raw gold); 2.3 B water; about 3 logs | About 26.3 raw copper (79 sulfurs); 63 planks; 18.7 B water; 6.5 logs; 5.6 cobblestone | About 16.4 iron ingots (or 5.5 raw iron); 12.4 planks; 5.6 B water; about 28 cobblestone |
| Heat | 2,400 heated ticks: Brazier fuel, or about 2,000 flux (5 logs) | About 2,600 t of Cauldron (26.3 raw copper), plus the Distiller, Oven and Incubator; the vats need none | 1,640 t of Cauldron, plus the Distiller and Incubator |

How the three compare:

- **A** is the cheapest to understand and the quickest, but it spends the most metal and leaves half of Theurgy unused.
- **B** uses everything and has the strongest loop. It is a grind that rewards building more vats.
- **C** uses everything and spends the fewest metals: four iron make sixteen copper. Its alchemy runs both ways.

**Building it.** No chain crafts anything while it runs. Building the apparatus beforehand takes copper, iron and gold ingots, stone, blackstone (the pedestals), a diamond (the target pedestal), quartz blocks (the Catalyst), a Sal Ammoniac Crystal (the Digestion Vat and the Sulfuric Flux Emitter), a cauldron, a barrel and a decorated pot (`R/crafting/shaped/*.json`). Those are all grid recipes, so they go on an Assembler before the challenge or on the Personal Assembler by hand.

## 3. The Galvanic step (proposal for #173)

**Reformation Array, "the dynamo"**:

- **Sources**: 16 Alchemical Sulfur (Copper), 4 Alchemical Sulfur (Iron) and 2 Alchemical Sulfur (Gold), on three Source Pedestals.
- **Target**: Alchemical Sulfur (Redstone), not consumed.
- **Cost**: 1000 Mercury Flux and 400 ticks.

The reading: a copper coil, an iron core and gold contacts, given its shape by the redstone signal and charged with the emitter's whole store of Theurgy's "pure energy".

This improves on the first research's proposal (16 Copper + 4 Gold + 1000 flux, Redstone template), which is kept as the alternative `galvanic_reformation_lean`. Adding iron puts all three of Theurgy's metal tiers in the recipe, abundant, common and rare. That gives B's climb and C's descent real work to do, and lets C start from the metal the step needs most of.

Why it works, from source:

- `GH/content/recipe/ReformationRecipe.java` takes counted `sources`, one `target`, any `result`, `mercuryFlux` and `time`. Its `matches()` needs each source on a different pedestal and the target to match. Leftover pedestals are allowed.
- `GH/content/apparatus/reformationarray/ReformationResultPedestalBlockEntity.java` puts no check on what the result is, so an essence can land there.
- The pedestals take only `#theurgy:alchemical_sulfurs_and_niters` (`ReformationSourcePedestalBlockEntity.java`). So the Galvanic Essence can never be its own template and can't multiply itself.
- 1000 flux is the Sulfuric Flux Emitter's whole capacity (`SulfuricFluxEmitterBlockEntity.java`, `CAPACITY = 1000`), so this is the dearest a reformation can be.
- **No collision.** No Theurgy recipe with a Redstone Sulfur target takes copper, iron or gold. Redstone itself can't be a source here: `R/reformation/alchemical_sulfur_redstone_from_alchemical_sulfurs_other_minerals_common.json` would match a Redstone Sulfur on any source pedestal first.
- Register one Galvanic reformation, not two. With leftover pedestals allowed, an array loaded for the dynamo also matches the lean recipe.

**Alternative, `galvanic_digestion`**: Digestion Vat, 16 Abundant + 4 Common + 1 Rare Metals Niter + 1000 mB Sal Ammoniac → Galvanic Essence, 1200 ticks. It is "Exaltation of the current": the three tiers fill the vat's three slots (`DigestionStorageBehaviour.java`), which accept any item some recipe uses. It has no flux and no template, though, so it loses the "charge", and chain A would have to ferment its metals to use it.

The Incubator was weighed and dropped. It takes exactly one of each input (`IncubationRecipe.java`), so it can't carry the cost.

## 4. Seeds

A Reformation Array only makes more of the sulfur already on its target. On raw and refined inputs, every seed is now one item liquefied, once per array, and never per Galvanic:

| Seed (target) | Made from | A | B | C |
|---|---|---|---|---|
| Redstone Sulfur (the Galvanic array) | 1 redstone dust, or a redstone ore for 9 | needed | needed | needed |
| Iron Sulfur | 1 iron ingot or raw iron | — | needed | — |
| Copper Sulfur | 1 copper ingot or raw copper | — | — | needed |
| Gold Sulfur, and the first Purified Gold | 1 raw gold → 3 Gold Sulfur: one seeds the target, one incubates back to an ingot for the first 20 Purified Gold | — | needed | needed |

- **A** needs only the redstone, because it reforms nothing but the final step.
- **B and C** can seed each array from one item of the chain's own kind: the starting metal, liquefied. The gold and the redstone are the exception. Neither chain's starting metal can make them before the first Purified Gold exists, so each needs one raw gold and one redstone dust from outside its per-Galvanic inputs.

## 5. Open questions

- **Gold and redstone for the seeds.** B and C need one raw gold, and every chain one redstone dust, that their per-Galvanic inputs don't hold. Does the Basic or Refined mix carry them, or a quest reward?
- **Alternate recipes and ADR 0006.** The glossary makes an alternate recipe a pack recipe on the challenge's machines. The ADR says the one recipe the pack adds to a challenge's mods is the last step. Are alternate recipes an exception to the ADR? Or should research only gate Theurgy's own richer recipes (raw ore's 3, ore's 5, the Sal Ammoniac Crystal's 1000 mB)?
- **Bands.** Logs, cobblestone, raw ores and ores count as raw here, and ingots, dusts and planks as refined. Is that the line the mixes use?
- **Pedestal stacks.** The dynamo takes 16 Copper Sulfur from one Source Pedestal, which has one slot and no limit of its own. That it holds 64 is assumed, not seen in game.
- **Heat.** A Caloric Flux Emitter burns flux the whole time it is fed, not only while a recipe runs. The heat figures count only working time.
- **Rates read from source, not seen in game.** The Catalyst's 2 flux a tick per side, the Caloric Flux Emitter's 100 per 120 ticks, and the Sulfuric Flux Emitter filling at the Catalyst's rate.
- **Belts and pipes.** Beltworks and Pipeworks into Theurgy's apparatus is expected through NeoForge's standard capabilities but untested.
- **The Catalyst and the handicap.** Should the Mercury Catalyst count as a generator under the energy handicap? Its flux isn't FE, so today it doesn't, and every chain relies on it.
