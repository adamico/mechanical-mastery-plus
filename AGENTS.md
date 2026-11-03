## Layout

A monorepo of modpacks, one folder per pack line:

- `MMP1.19.2/`: Mechanical Mastery Plus on Forge 1.19.2. Live; bugfix releases only.
- `MMR26.1.2/`: Mechanical Mastery Reborn on NeoForge 26.1.2. In development (#163).
- `MMP1.20.1/`, `TT1211/`: unreleased experiments. Read them for ideas, never change or release them.

A task works inside one pack line's folder. Reborn's companion mod lives inside its line's folder too, in `MMR26.1.2/mod/`: a Gradle build of its own, started from the libworks template and kept out of the pack by `.packwizignore`.

## Reborn's pack index

`MMR26.1.2/index.toml` and `MMR26.1.2/pack.toml` are refreshed on `main` only: a branch never commits them. `MMR26.1.2/scripts/launch.py` refreshes them for its own run and puts them back, so an in-game check sees a branch's new files without a refresh. After merging a branch that adds, removes or changes pack files, run `packwiz refresh` in `MMR26.1.2/` on `main` and commit both files; a merge that conflicts on them takes `main`'s and refreshes.

## Agent skills

### Issue tracker

Issues are tracked in GitHub Issues on adamico/mechanical-mastery-plus, using the `gh` CLI. Every issue carries the label of its pack line. See `docs/agents/issue-tracker.md`.

### Triage labels

The five default triage roles, except that needs-info uses the existing `needs more info` label. See `docs/agents/triage-labels.md`.

### Domain docs

Multi-context: a shared `GLOSSARY.md` and `docs/adr/` at the root, and a `GLOSSARY.md` (and `docs/adr/` where it has decisions) per pack line, listed in `GLOSSARY-MAP.md`. See `docs/agents/domain.md`.
