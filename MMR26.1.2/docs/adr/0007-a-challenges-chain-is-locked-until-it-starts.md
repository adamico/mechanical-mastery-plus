# A challenge's chain is locked until the team starts it

A challenge's chain stays locked for a team until the team starts that challenge at the lab, and stays unlocked from then on, past a win or a reset, though a stop locks it again (ADR 0013). Locking a chain means three rules, the same for every challenge:

1. Before the team starts the challenge, its chain does no processing. The challenge's mods' machines do no work on the team's island, and the pack's own recipes for the chain, such as the Galvanic and Warding Essences', can't run on whatever machine makes them, a suite machine too.
2. The challenge's mods' machines can always be crafted. What is locked is their work, never their crafting.
3. EMI marks every locked recipe, naming the challenge that unlocks it: each recipe made on a locked machine, those EMI shows through JEI's plugins included, and each of the chain's own recipes. The mark goes once the team has started the challenge.

Locking is per recipe and per machine, not per mod: what a challenge's mods offer outside the chain, as AdminShop's Shop, is never locked, and a locked mod's items can still come from an essence's mix, to be recycled, as Neo Vitae's Hellfire Forge comes from Advanced's.

Two cases fall outside the rules. Protection's Home Gate Pearl is never craftable: the Shop sells it once the team has won protection. Money's chain is no machine's work but the Shop's Offers, gated on a Permit the team is given when it starts the challenge and loses when it stops it (ADR 0013, #296): the chain's Sell Offers and the Offers of the Permits the team buys. A Permit bought is the team's own, as a built machine is, and the Offers it opens stay open. A team's money is set aside while the money challenge is played (glossary **Handicap**), so the chain is paid only with money earned during it.

We could have left a challenge's mods free from the start, as Theurgy and Neo Vitae were in 0.0.1. A team could then build a whole chain while no handicap applies and C.O.D. makes no Inflation, and starting the challenge would only switch a finished factory on. The handicap and the Inflation clock are the challenge, so the chain has to run under them.

We first locked a chain's machines by locking their crafting, but many of them come from Advanced's mix anyway, so locking their crafting kept nothing back. Freeing their crafting then freed their processing with it, since the processing lock read its machines from the same list (#246). So each chain's machines, whose processing is locked, are a list of their own, apart from any crafting lock.

## Consequences

- Each chain's machines are the pack's item tag `mechanicalmasteryreborn:chain_machines/<handicap>`, and the companion mod stops each one's work by its mod's own hook. Energy's are Theurgy's processing machines: the Sal Ammoniac Accumulator, the Liquefaction Cauldron, the Fermentation and Digestion Vats, the Calcination Oven, the Distiller, the Mercury Catalyst, the Incubator, and the Reformation Array's Sulfuric Flux Emitter. Life's are Neo Vitae's Ara Vitae and Hellfire Forge. Protection and money have none.
- Each chain's own recipes are the ones its Chapters stage file names, which Chapters keeps from the crafting grid and the pack's Craftworks lock from the Assemblers. A recipe that runs only on a locked machine, as the Galvanic Essence's incubation, is held by the machine too.
- A reset keeps the chain unlocked: the factory a failed attempt built is what it earned, and a second attempt starts with it.
- A chain unlocked by one challenge stays open for the team's later challenges.
- Gearworks is no challenge's mod, since locking it would lock the suite's own gear; it stays free, as the gear that protection makes worth having.
- A lock stops a chain being run ahead, not a stock being saved up.
