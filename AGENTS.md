## Layout

A monorepo of modpacks, one folder per pack line:

- `MMP1.19.2/`: Mechanical Mastery Plus on Forge 1.19.2. Live; bugfix releases only.
- `MMR26.1.2/`: Mechanical Mastery Reborn on NeoForge 26.1.2. In development (#163).
- `MMP1.20.1/`, `TT1211/`: unreleased experiments. Read them for ideas, never change or release them.

A task works inside one pack line's folder. Its own blocks and machines live in Reborn's companion mod, in its own repo in the 5thlayer org, not here.

## Agent skills

### Issue tracker

Issues are tracked in GitHub Issues on adamico/mechanical-mastery-plus, using the `gh` CLI. Every issue carries the label of its pack line. See `docs/agents/issue-tracker.md`.

### Triage labels

The five default triage roles, except that needs-info uses the existing `needs more info` label. See `docs/agents/triage-labels.md`.

### Domain docs

Multi-context: a shared `GLOSSARY.md` and `docs/adr/` at the root, and a `GLOSSARY.md` (and `docs/adr/` where it has decisions) per pack line, listed in `GLOSSARY-MAP.md`. See `docs/agents/domain.md`.
