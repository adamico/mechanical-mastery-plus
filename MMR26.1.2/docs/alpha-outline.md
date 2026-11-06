# The alpha's copy outlines and CurseForge checklist

Facts for the maintainer to write the alpha's copy from (#290): no sentence here is copy. Each bullet names its
source in brackets, so it can be checked before it is written up. Delete this file once #291 ships: by then the
copy and the page hold what matters, and this goes stale.

Sources: [G] Reborn's `GLOSSARY.md`, [SG] the root `GLOSSARY.md`, [ADR n] an ADR, [P] `docs/pitch.md`, [M]
`MODS.md`, [R] Reborn's `README.md`, #n a ticket.

## 1. The pack's CurseForge description

It is the player section at the top of Reborn's `README.md`, above "What is here", so the repo and the page say
the same thing (#273). The README's note that the companion mod isn't on CurseForge goes with #275.

### What the pack is

- Mechanical Mastery Reborn, the sequel to Mechanical Mastery Plus, a new pack and not another version of it
  [ADR 0004, G].
- Minecraft 26.1.2 on NeoForge [`pack.toml`].
- A skyblock: each team starts on its own island in a void world, beside C.O.D., the lab and a tree, and earns
  everything after through the pack's progression [SG Skyblock, R].
- Built on the 5thlayer -works suite (Craftworks, Beltworks, Pipeworks, Wireworks, Fieldworks, Groundworks,
  Voidworks, Gearworks) instead of Create, which has no 26.1.2 build [ADR 0002, M].
- Its own blocks and machines (C.O.D., the essences, the Recycler, the lab, the challenges) are in its companion
  mod, Mechanical Mastery Reborn Core, which installs with the pack [ADR 0003, G Companion mod, #275].

### The backwards loop

- The world has been reduced to Mechanical Essence: each essence holds the idea of every other resource, which
  has to be decomposed out of it [G Mechanical Essence].
- The four tiers, Advanced (magenta), Improved (yellow), Regular (blue) and Basic (red), are played from the top
  down: Advanced first and easiest, Basic last and hardest [SG Mechanical Essence, G Decompose].
- The higher the tier, the more processed what it decomposes into: Advanced into whole machines and refined
  intermediates, Basic into raw resources and ores. A lower tier's outputs need more stages, so the factory built
  for one tier becomes the last stages of the next and grows upstream instead of being replaced [G Decompose, G Band].
- From the first essence, whole machines: steam turbines, transformers, accumulators, harvesters, assemblers,
  pumps, anvils, blast furnaces. Each one is a handful of the suite's best kit, and opens automation routes from
  the very beginning [P, #209].
- Every machine is a choice: keep it and build with it, or recycle it for a low chance of its ingredients back.
  Kept whole it is worth far more [P].
- The Recycler, after Factorio's: a 3x3 multiblock built block by block from a core, drives, inputs, outputs and
  casings, floors stacked upward as its core's tier allows. It decomposes essence into its mix, and recycles any
  crafted item one band down, which is also how surplus is thrown away [G Recycler, G Recycle].
- A Recycler short on outputs returns a random part of a mix, so more outputs return more, and a small Recycler
  is a cost, never a wall [P, #216].
- Recycling undoes crafting, never smelting, so it stops at refined [ADR 0008].
- The start is by hand: decomposing by hand, then a Recycler turned by hand drives, which cost food, then FE
  drives [G Drive, G Recycler].
- The Shop (AdminShop) is a second way to resources and the skyblock's store for saplings, water, lava, seeds and
  farm animals; until Basic, the only source of ores. Money was the heart of Plus and stays a big part of Reborn
  [G Shop, M].

### C.O.D. and Inflation

- C.O.D., the Conscious Overproduction Device whose making ended Plus, is Reborn's adversary: a sentient AI that
  nothing but Quintessence can shut down [SG C.O.D., G C.O.D.].
- It makes essence of the highest tier not yet blocked, and nothing else. Dormant, it makes it slowly and only up
  to a stack; a team's first decompose on a Recycler wakes it [G C.O.D.].
- Between challenges, being awake is only more essence. While a challenge is played it makes ever faster, without
  end, and Inflation, the excess it has built over what the team has decomposed, climbs whenever decomposing falls
  behind. At its cap the challenge resets [G C.O.D., G Inflation].

### The four challenges

- Four challenges, each bound to its own mods, played in any order against whichever tier C.O.D. makes, harder
  the lower the tier. Started and researched at the lab, followed through EMI, with no quest book [G Challenge].
- Each one's chain makes a Transmuted Essence. Putting its quota into its Port on C.O.D. wins the challenge and
  blocks the tier C.O.D. is making for good, and C.O.D. drops to the tier below [G Transmuted Essence, G Port].
- Each has a handicap, only while it is played [G Handicap]:
  - **Energy** (Theurgy's alchemy, the Galvanic Essence): the team's generators give it no energy; FE banked
    ahead still flows, so banking it is a strategy [ADR 0005, ADR 0009, M].
  - **Protection** (Gateways to Eternity, the Warding Essence): an Assault, waves of mobs from a gate on one of
    four platforms around the island, over unbreakable bridges, growing harder until the quota is filled. Its
    mobs alone drop the Rift Shards the chain needs. Defences fed by the factory hold the bridges, and what the
    mobs break is refunded at the lab [ADR 0010, ADR 0015, G Assault, G Defence].
  - **Life** (Neo Vitae's blood magic, the Vital Essence): hunger drains faster and the same food feeds less each
    time it is eaten, so a team needs a factory that feeds it a varied diet [ADR 0011, G Diet].
  - **Money** (AdminShop, the Aureate Essence): money is set aside while the challenge is played, and less of it
    comes in [G Handicap, M].
- A challenge's chain stays locked until the team starts it, so nothing of it can be built ahead [ADR 0007].
- A team can stop a challenge once it has reset, to come back better prepared [ADR 0013, G Stop].

### The last chapter

- Once all four are won, the Nether and the End open to the team [ADR 0012].
- C.O.D. makes again, Nullessence this time, in its last stand. The team makes Quintessence and puts it into
  C.O.D. to pull ahead [G Last stand, G Lead].
- Two ways to Quintessence: Voidworks' Quartessence, climbed in a Mutation Chamber with motes from the Nether and
  the End; or the Hermetic Lab, which turns Nullessence into Quintessence only while the team bears handicaps it
  chooses, faster the more it bears [G Quintessence, G Hermetic Lab, #262, #263].
- A race that can't be lost: C.O.D. paces itself a little behind the team, up to a ceiling. When the team's Lead
  reaches the margin, C.O.D. shuts down for good and stands dark on the island, and the pack is won [ADR 0014].

### Team islands and servers

- An island per team; a player with no team gets a team, and an island, of their own [R, M Skyblock Builder].
- Each island has its own C.O.D., its own challenges and Inflation; a Recycler counts for the team whose island it
  stands on [G C.O.D., G Port, #273].
- Each release has a server pack on CurseForge, a child of the client file, which leaves out the client-only mods
  [SG Server pack, ADR 0001, R].

### Help in game

- No wiki: the Manual, a GuideME book every player is given once, tells how to build and work the companion
  mod's blocks, with 3D scenes, and how the challenges and the last chapter are played. Holding its key over a
  block or essence opens its page [G Manual, M GuideME, the Manual's index].
- EMI shows what every item takes and what every essence decomposes into; locked recipes stay visible in it
  [M EMI, the Manual's index].

### Alpha and known gaps

- An alpha: playable to its end, versions 0.0.x, its page and files saying "alpha" [SG Alpha, #273].
- Known gaps, as of 2026-11-06 (#291 checks them again before publishing):
  - The Electric Defence is the only Defence, and no one has yet played it against the Assault's waves; what the
    protection challenge's Defences need next is open [#248].
  - Balance is first-pass: many amounts, as the Defence's FE and reach and the last stand's winning Lead, are first
    guesses to tune in play [#207, `ServerConfig`, `defences.js`].
  - EMI is an unofficial port, since EMI's own project has no NeoForge 26.1.2 file [M EMI].
- Feedback and bugs go to the GitHub issues [#273].

### The AI-help line

- One line on the page, per #273. The fact behind it is in section 5.

### Links

- GitHub issues, the Mechanical Mastery Discord (section 6).

## 2. The r/feedthebeast post

The maintainer reads r/feedthebeast's rules and flair before posting: they couldn't be fetched (#273).

- A one-line hook from the pitch: the backwards loop, whole machines from the first essence [P].
- The video, about 45 to 60 seconds, uploaded natively [#289, #273].
- What the pack is, in a few facts from section 1: sequel to Plus, 26.1.2 NeoForge skyblock on the -works suite,
  C.O.D. as the adversary, four challenges in any order, team islands.
- Alpha, with the known gaps.
- The CurseForge link, and feedback through the GitHub issues.
- How it was made, for the post or ready for the comments: section 5 in full.

## 3. The Mechanical Mastery Discord announcement

For Plus's players [#273].

- What carries over from Plus: the skyblock start [SG Skyblock]; the four Mechanical Essences in their colours,
  their art the maintainer's own from Plus [NOTICE]; C.O.D., whose making ended Plus, now the adversary from the
  first island [SG C.O.D.]; money and the Shop, Plus's heart [G Shop].
- What's new: Minecraft 26.1.2 on NeoForge; the -works suite in Create's place [ADR 0002]; the loop runs
  backwards, from essence down into resources [G Mechanical Essence]; the Recycler; the four challenges with
  their handicaps and the Assault; the last stand; an island and a C.O.D. per team; the in-game Manual, no quest
  book.
- A new pack, not an update: Plus stays on 1.19.2 for bugfixes [ADR 0004, AGENTS.md].
- Alpha, known gaps, the CurseForge link.

## 4. The line on Mechanical Mastery Plus's page

Pasted by hand on Plus's CurseForge page; no file of the Plus line changes [#290].

- Reborn is Plus's sequel, a new pack on Minecraft 26.1.2 NeoForge [ADR 0004].
- Alpha [SG Alpha].
- Reborn's CurseForge link (once #291 makes the project).

## 5. How the pack was made

The fact behind the AI-help line: one line on CurseForge, all of it in the Reddit outline [#290].

- The maintainer sets the direction, decides every design question and checks every change in game.
- Agents do the development, through a written workflow (the skills in `.agents/skills/`):
  - **Grilling**: an agent questions the maintainer, a round of questions at a time with a recommendation each,
    until the design is settled; the agreed terms go into the glossaries, the hard-to-reverse decisions into
    ADRs [`grilling`, `domain-modeling`].
  - **Spec and tickets**: the design becomes a spec issue, as #273, split into tickets an agent can take alone,
    labelled `ready-for-agent`, or `ready-for-human` for what only the maintainer can do [`to-spec`,
    `to-tickets`, `triage`].
  - **Implementation**: agents work a ticket test-first, with unit and game tests, and open a pull request
    [`tdd`, `pr`].
  - **Review**: each change is reviewed against the repo's documented standards and against its ticket [`code-review`].
  - The maintainer merges, and plays what changed in game.
- Agents write the code, scripts, configs and docs. All player-facing copy is the maintainer's [#273].
- The art:
  - the CurseForge covers, icons and the video's cards are drawn procedurally, by scripts agents wrote, with a
    fixed seed, from the mods' own credited textures [`publish/make-cover.py`, `mod/publish/make-cover.py`, #274];
  - the blocks wear Malcolm Riley's unused-textures (CC-BY-4.0), some modified by agents, each picked by the
    maintainer from previews [mod NOTICE, #301, #256];
  - the essences are the maintainer's own art from Plus [mod NOTICE];
  - the Rift Shard, Charged Rift and Null Shard are drawn by a script [#310].

## 6. CurseForge field checklist

The pack's project, made by hand in #291. A field marked _check on the form_ couldn't be confirmed without the
maintainer's login.

CurseForge's pages, read 2026-11-06: [CS] [Creating and Submitting a
Project](https://support.curseforge.com/en/support/solutions/articles/9000197241-project-submission-guidelines),
[CT] [Project Submission Guide and
Tips](https://support.curseforge.com/en/support/solutions/articles/9000199552-project-submission-guide-and-tips),
[CM] [Moderation Policies](https://support.curseforge.com/en/support/solutions/articles/9000197279-moderation-policies),
[Plus] [Plus's live page](https://www.curseforge.com/minecraft/modpacks/mechanical-mastery-plus).

Before the pack's project: the companion mod's project (#275) is approved and its file listed in the pack's
manifest, since moderation takes time and a profile from the pack's zip needs it for C.O.D. [#273, #275].

### The project

- [ ] **Game and class**: Minecraft, Modpacks [CS, CT].
- [ ] **Name**: "Mechanical Mastery Reborn" [`pack.toml`, ADR 0004]. Unique, English, no version or category
      name in it [CS, CT].
- [ ] **Summary**: the maintainer's one line, saying what the pack does rather than who it is for; no length
      limit found, _check on the form_ [CT, CM].
- [ ] **Description**: the README's player section (section 1), in the Markdown editor [CT]. Discord and other
      outside links go at the bottom [CM, CT]. It says what the pack adds in its own words [CM].
- [ ] **Main category**: Skyblock [#273]. Its most distinctive one [CS].
- [ ] **Additional categories**: Tech, Multiplayer [#273]. Up to four are allowed [CS, CT]; Plus's page has
      Tech, Skyblock and Quests [Plus]. Reborn has no quest book, so not Quests [G Challenge].
- [ ] **Logo**: `publish/reborn-icon.png`, 512×512 [#274]. CurseForge asks for 400×400 PNG, larger downscaled
      [CT, CM]; the suite's 512×512 icons were accepted [#273]. Not webp [CM].
- [ ] **Cover**: `publish/reborn-cover.png`, 1280×640 [#274]. No cover field is described in CurseForge's
      pages; _check on the form_.
- [ ] **License**: the pack has no licence of its own in the repo (the companion mod is MIT, with CC-BY-4.0 art);
      Plus's page shows CC BY-NC 3.0 [Plus, mod `LICENSE`]. The maintainer's choice; _check on the form_ for the
      list.
- [ ] **Slug**: `mechanical-mastery-reborn` [`tools/release.toml`]. The pages don't say how it is set; Plus's
      follows its name [Plus]. _Check on the form_.
- [ ] **Allow comments**: the maintainer's choice; if off, the description names another channel [CT].
- [ ] **Experimental**: off. An experimental project doesn't sync with the CurseForge app [CT].
- [ ] **Credits**: Malcolm Riley's unused-textures, CC-BY-4.0, which the blocks and the companion mod's cover
      wear: CC-BY asks for attribution [mod NOTICE].
- [ ] **AI-help line**: in the description (section 5). CurseForge's only AI rule is a disclaimer on AI-made
      showcase images that may misrepresent the content; the gallery is screenshots of the pack, so it doesn't
      apply, and the line is a choice [CM, #273].
- [ ] **Known gaps**: in the description (section 1), checked again before publishing.

### After it is made

- [ ] **Links**: Issues tracker, external: GitHub issues; Source: the GitHub repo [CT, Plus]. There is no
      Discord link field, so the Mechanical Mastery Discord (`https://discord.gg/PMhDEZge2W`, root `README.md`)
      goes at the bottom of the description [CT, CM]. No wiki: the Manual takes its place.
- [ ] **Gallery**: added from the Images tab, each image with a title and a description; no size rule found
      [CS, CT]. The captions are the maintainer's. Candidates, one per Pack gallery scene, taken once each is
      built [#273]:
      island (#279), decompose line (#280), power (#281), mall (#282), money (#283), energy (#284), protection
      (#285), life (#286), last chapter (#287), Voidworks (#288); and the cover.
- [ ] **Release config**: the project's id in `tools/release.toml`'s `curseforge-project` [#291, `tools/release.toml`].

### The 0.0.1 file

Uploaded by `tools/release.py publish`, not by hand [R].

- [ ] **Release type**: Release, not Alpha: a project needs a Release file to sync to the app [CS, SG Alpha].
- [ ] **Display name**: says "alpha" [SG Alpha, R].
- [ ] **Changelog**: required for every file [CM]; the release's section of `CHANGELOG.md` [R].
- [ ] **Game version and loader**: 26.1.2, NeoForge 26.1.2.126 [`pack.toml`]. Where the loader is set for a pack
      isn't described; _check on the form_ (Plus's shows Forge [Plus]).
- [ ] **Server pack**: an additional file of the client file, as CurseForge requires [CM, SG Server pack].
- [ ] **Shows in the CurseForge app** once approved [#291].
