# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Before exploring, read these

- **`GLOSSARY.md`** at the repo root, or
- **`GLOSSARY-MAP.md`** at the repo root if it exists: it points at one `GLOSSARY.md` per context. Read each one relevant to the topic.
- **`docs/adr/`**: read ADRs that touch the area you're about to work in. In multi-context repos, also check `src/<context>/docs/adr/` for context-scoped decisions.

If any of these files don't exist, **proceed silently**. Don't flag their absence; don't suggest creating them upfront. The `/domain-modeling` skill (reached via `/grill-with-docs` and `/improve-codebase-architecture`) creates them lazily when terms or decisions actually get resolved.

## This repo

Multi-context: one context per pack line, in the line's folder, plus a shared one at the root. `GLOSSARY-MAP.md` lists them.

```
/
├── GLOSSARY-MAP.md
├── GLOSSARY.md                        ← shared: releasing, pack identity, essences, C.O.D.
├── docs/adr/                          ← decisions every line follows
├── MMP1.19.2/
│   └── GLOSSARY.md                    ← Mechanical Mastery Plus
└── MMR26.1.2/
    ├── GLOSSARY.md                    ← Mechanical Mastery Reborn
    └── docs/adr/                      ← Reborn's own decisions
```

Read the shared glossary and the glossary of the line you're working in. A term defined in both reads as the shared meaning narrowed by the line. ADR numbers are unique across the repo, wherever the ADR lives, so a new ADR takes the next number after the highest anywhere.

## File structure in general

Single-context repo (most repos):

```
/
├── GLOSSARY.md
├── docs/adr/
│   ├── 0001-event-sourced-orders.md
│   └── 0002-postgres-for-write-model.md
└── src/
```

Multi-context repo (presence of `GLOSSARY-MAP.md` at the root):

```
/
├── GLOSSARY-MAP.md
├── docs/adr/                          ← system-wide decisions
└── src/
    ├── ordering/
    │   ├── GLOSSARY.md
    │   └── docs/adr/                  ← context-specific decisions
    └── billing/
        ├── GLOSSARY.md
        └── docs/adr/
```

## Use the glossary's vocabulary

When your output names a domain concept (in an issue title, a refactor proposal, a hypothesis, a test name), use the term as defined in `GLOSSARY.md`. Don't drift to synonyms the glossary explicitly avoids.

If the concept you need isn't in the glossary yet, that's a signal: either you're inventing language the project doesn't use (reconsider) or there's a real gap (note it for `/domain-modeling`).

## Flag ADR conflicts

If your output contradicts an existing ADR, surface it explicitly rather than silently overriding:

> _Contradicts ADR-0007 (event-sourced orders), but worth reopening because…_
