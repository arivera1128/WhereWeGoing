# Restaurant-deals app — project documentation

Version 0.2 · Working design · Consolidated September 26, 2026

This pack turns the available full text of **Discuss Android App Idea** and the current documentation request into a portable project baseline. Product name: TBD.

## Start here

Place `AGENTS.md` at the Android application's repository root and `docs/` beside it. If the repository already has instructions, merge them deliberately; do not overwrite unrelated instructions. This package is a documentation handoff, not an instruction to implement every described capability.

Read in this order:

1. [AGENTS.md](AGENTS.md) — working rules for Codex.
2. [PRODUCT.md](docs/PRODUCT.md) — product requirements and experience.
3. [ARCHITECTURE.md](docs/ARCHITECTURE.md) — system boundaries and environments.
4. [DATA_MODEL.md](docs/DATA_MODEL.md) — logical entities, relationships and access patterns.
5. [DEAL_LIFECYCLE.md](docs/DEAL_LIFECYCLE.md) — how observations become published deals.
6. [ROADMAP.md](docs/ROADMAP.md) — MVP, Planned and Future scope.
7. [DECISIONS.md](docs/DECISIONS.md) — established decisions, superseded ideas and open questions.

## How to interpret this pack

- **Established** means supported by the discussion or the current explicit request.
- **Working design / proposed** means a useful structure requiring review, not an approved implementation requirement.
- **TBD / open question** means no decision has been made.
- **MVP** is the core validation scope; **Planned** means accommodate now and implement later; **Future** is a known extension, not a commitment or deadline. The roadmap records uncertain release assignments explicitly.

The conversation reported a running Android prototype. Its source code was not present in this workspace, so this pack does not assert which features are currently implemented. No synced reference files were changed. Historical restaurant promotions and numerical analytics examples are illustrative, not verified current offers or actual results. The attached historical image was not needed to establish the eligibility requirement; its offer was not independently verified.

Later discussion supersedes the initial hard-coded prototype milestone and earlier Brand hierarchy sketches. Exact seed counts, rewards scope, event capture timing and applicability inheritance remain review items rather than silently resolved requirements.

## Suggested first task

> Read AGENTS.md and all linked project documents. Review the existing Android code against them without implementing changes. Identify contradictions, current capabilities, missing MVP foundations and decisions that block the real-data sandbox. Propose the smallest next implementation step. Do not translate the logical model directly into Firestore collections; first review access patterns and physical design.

## Provenance

Source conversation: [Discuss Android App Idea](https://chatgpt.com/c/6a9ca28d-b5ac-83ea-8ec3-2172fef20f96), September 2026; plus the current explicit documentation request. This is a curated requirements baseline, not a transcript. Historical tool setup instructions, conversational interruptions, pricing claims and unverified live offers are not carried forward as requirements.
