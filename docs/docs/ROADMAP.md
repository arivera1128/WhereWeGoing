# Capability roadmap

Version 0.2 · Working design · September 26, 2026

**MVP:** core validation scope. **Planned:** design for now, implement later. **Future:** extension point. These are capability classifications, not dates or a claim that work is complete. Where earlier conversation left release timing ambiguous, this document records it rather than making a new product decision.

## Capability register

| Capability | Classification | Boundary / dependency |
|---|---|---|
| Android/Kotlin/Compose client | MVP | Existing prototype reported; inspect actual code before planning changes. |
| Tonight decision, winner and alternatives, explanations | MVP | Approximately 30-second UX aspiration. |
| Basic user/household profile and explicit preferences | MVP | Lightweight onboarding; no mandatory account decision yet. |
| Geographic candidate filtering and basic ranking | MVP | Scope/time/eligibility resolution before ranking; formula TBD. |
| Restaurant, Location, Deal, DealLocation | MVP | Logical model review before physical schema. |
| Basic evidence, curated deals, manual lifecycle and history | MVP | Dedicated workflow portal and automation not required. |
| Real-data sandbox plus synthetic stress cases | MVP | Real locations; labeled test offers; smaller verified deal set. |
| Environment isolation | MVP foundation | Dev, QA/staging and prod kept distinct; provisioning sequence TBD. |
| Identity/profile/activity separation | MVP design seam | Anonymous/account choice open; Google sign-in later possibility. |
| Basic deal appeal, intent and next-visit check-in | MVP | One-tap check-in records tried-and-worked, tried-and-failed, or not-used; it does not independently prove a restaurant visit. Local implementation is sufficient initially. |
| Event contracts and correlation seams | MVP design seam | Retry, deduplication and attribution details remain open under Q-06. |
| Richer or automated feedback collection | Planned | GPS triggers, push reminders and other automated follow-up are outside the tester-ready MVP. |
| Behavioral learning and richer confidence | Planned | Preserve explicit preferences; requires meaningful evidence/events. |
| User deal submissions/community verification | Planned | Validation required; lightweight MVP subset TBD. |
| Explore/map and navigation | Planned; launch inclusion TBD | Provider and interaction design open. |
| Internal ops/admin interface | Planned | Deal workflow, restaurants, users, feedback, roles/permissions. |
| Consumer account sign-in and user administration | Planned; timing TBD | Decide identity and account migration first. |
| Automated discovery/enrichment/monitoring | Planned | Candidates/evidence only; sources, cost and cadence TBD. |
| Automated validation/approval | Future | Requires explicit rules, traceability and reliability evaluation. |
| Geographic caching and broader market ingestion | Future | Optimize based on measured need. |
| iOS client or cross-platform client strategy | Future | Shared backend seam now; framework/timing TBD. |
| Predictive personalization, dining modes | Future | Builds on explicit and behavioral learning. |
| Personal rewards, reminders, savings dashboard | Future direction; manual subset timing open | Early rewards-first sketches are not confirmed launch scope. |
| Opt-in email parsing/deep rewards integrations | Future | Technical feasibility and consent design unresolved. |
| Permission-based proximity and follow-up notifications | Future for proximity; notification timing TBD | Do not equate proximity with verified visit/redemption. |
| Merchant management, claims, targeting and lead analytics | Future | Consumer value first; truthful funnel semantics. |
| Sponsored placements/monetization | Future | No MVP paid ranking; relevance and disclosure principles. |
| Entertainment, groceries and other categories | Future | Explicitly outside food/restaurants MVP. |

## Proposed delivery sequence

This sequence is a planning aid, not authorization to implement unresolved features.

### 1. Review the baseline and prototype

Read this pack; inventory actual Android features and data. Resolve contradictions and select a bounded next task. Confirm the MVP acceptance plan, initial market and feedback/event subset. Exit evidence: reviewed gap list and recorded decisions, not an assumed implementation state.

### 2. Validate the logical model

Pressure-test chain/independent restaurants, participating locations, local evidence, recurring/expiring deals and eligibility. Resolve blocking applicability and trust rules. Assign logical data-domain ownership; named people/teams remain TBD. Exit evidence: reviewed scenarios and updated model/decision log.

### 3. Review access patterns and physical design

Define nearby queries, ranking inputs, details, event writes and operations queues. Review backend selection, indexes, denormalization, permissions, environment isolation and external-data ingestion rules. Exit evidence: reviewed physical design; no direct ERD-to-Firestore conversion.

### 4. Build the real-data sandbox

Seed a bounded set of real restaurant/location records through a permitted provider process. Create clearly labeled synthetic edge cases plus genuinely source-verified deals. Candidate counts discussed: 100–300 locations, 500–2,000 test deals, and a smaller genuine set; exact targets TBD. Exit evidence: useful-scale data, repeatable tests and separation from prod.

### 5. Connect and evaluate the core consumer experience

Replace hard-coded supply with reviewed data access. Exercise household/preferences, nearby selection, applicable offers and explanations. Confirm updates can appear without changing app offer constants. Measure decision time with an agreed method and inspect incorrect/no-result cases. Exit evidence: demonstrated core loop and documented limitations.

### 6. Extend only as prioritized

Add the selected feedback/verification subset, improve learning and operations, then consider automation and wider markets. Merchant, rewards and additional clients follow separate prioritization. No launch date, staffing plan or growth quota has been established.

## Review checklist

MVP review should establish that offers are real and relevant, unknowns remain visible, family and solo profiles differ appropriately, synthetic data stays isolated, meaningful history is retained, and business concepts remain portable beyond Android. Exact performance targets, confidence thresholds and primary success metric remain open in [DECISIONS](DECISIONS.md).
