# Decisions and open questions

Version 0.2 · Consolidation date: September 26, 2026

These records summarize the September 2026 discussion and current explicit request. The consolidation date is not a claim that each decision was originally made that day. “Established” records agreed direction; it does not imply implementation. New material decisions should include date, status, context, rationale, consequences and affected documents. Preserve superseded records.

## Established decisions

| ID | Decision and rationale | Consequences / affected documents |
|---|---|---|
| D-01 | Food/restaurants MVP; answer “Where should we eat tonight?” in ~30 seconds. Focus on decision fatigue and cost. | PRODUCT, ROADMAP: other deal categories excluded; measurement details remain open. |
| D-02 | Personalized decision engine, one winner plus alternatives and explanations. Relevance exceeds deal volume. | PRODUCT, DATA_MODEL: basic household/user profiles and explicit preferences; learning grows later. |
| D-03 | Android/Kotlin/Jetpack Compose initially; platform-agnostic backend direction. | ARCHITECTURE: preserve shared logic/data seams for iOS; exact execution split TBD. |
| D-04 | Restaurant 1:N Location, including independent restaurants. A separate Brand hierarchy is unnecessary now. | DATA_MODEL: business concept separated from physical outlet. |
| D-05 | Deal separate from Location, with scope and DealLocation applicability. | DATA_MODEL, DEAL_LIFECYCLE: do not duplicate one offer for every outlet; inheritance policy remains open. |
| D-06 | Source-agnostic discovery creates evidence/candidates, not truth. | ARCHITECTURE, DEAL_LIFECYCLE: user, crawler, AI and merchant inputs must follow validation. |
| D-07 | Discovery/Evidence → Candidate → Enrichment → Validation → Publication → Monitoring → Retirement. | DEAL_LIFECYCLE: process engine distinct from state storage; manual operations are valid initially. |
| D-08 | Preserve evidence/history and distinguish scope, publication, applicability and confidence. | DATA_MODEL, DEAL_LIFECYCLE: append observations/corrections; no universal confirmation from one local report. |
| D-09 | Recommendation, intent, visit, deal confirmation/verification and feedback are distinct events. | DATA_MODEL: preserve outcomes; intent is not a visit and household size is not actual diners. Capture timing remains Q-06. |
| D-10 | Geographic filtering bounds recommendations, not the master model. | ARCHITECTURE, DATA_MODEL: nearby candidates first; no nationwide retrieval for each request. |
| D-11 | Real restaurant/location data plus synthetic test offers and smaller genuine deal set. | ARCHITECTURE, ROADMAP: real-data sandbox replaces hard-coded milestone; counts/provider TBD. |
| D-12 | Isolate dev, QA/staging and prod data/environments. | ARCHITECTURE: current request makes three-environment separation explicit; promote code, not test data. |
| D-13 | Internal operations is a platform area spanning deals, restaurants, users, permissions and future merchants. | ARCHITECTURE, ROADMAP: design coherently; full interface need not ship now. |
| D-14 | Identity/authentication separate from profiles and activity. | ARCHITECTURE, DATA_MODEL: accounts and Google sign-in are not assumed MVP requirements. |
| D-15 | Design the seams now, not all the features now; classify MVP/Planned/Future. | All documents: preserve extension points without implementing the entire vision. |
| D-16 | Review logical model, scenarios and access patterns before physical storage design. | DATA_MODEL, ARCHITECTURE: Firestore is a candidate; no direct conversion into collections. |
| D-17 | Consumer value first, with future merchant lead-generation analytics. | PRODUCT, DATA_MODEL: retain truthful funnel meanings; merchant product outside MVP. |
| D-18 | No MVP paid ranking; future sponsorship must remain relevant and disclosed. | PRODUCT, ROADMAP: monetization details remain open. |
| D-19 | AI-assisted development should preserve owner understanding. | AGENTS, ARCHITECTURE: explain choices, alternatives and important behavior in plain language. |
| D-20 | The tester-ready MVP uses separate deal-appeal, intent, visit and deal-outcome signals. “I’ll try this deal” records intent. The next app visit asks whether the user visited; “Not yet” can return for at most three later visits with a dismiss option. A reported visit separately asks whether the deal worked. GPS triggers, push reminders and native navigation are outside this scope. | PRODUCT, ROADMAP, DATA_MODEL: implement a lightweight local check-in without treating intent as a visit, deal failure as restaurant dislike, or missing feedback as a negative response. |

## Superseded or unapproved sketches

- **Brand → Restaurant → Location:** superseded by D-04 after explicit discussion of why a Brand entity was unnecessary.
- **Hard-coded deals as the next milestone:** useful for the first prototype, superseded by the real-data sandbox direction. Do not restart the prototype from scratch without inspecting it.
- **Firestore collection snippets:** illustrative earlier drafts, not approved physical design. D-16 governs implementation.
- **Rewards-first two-tab app, savings counters and reminders:** early ideas; later direction centers on a restaurant recommendation engine. Exact manual rewards scope remains open.
- **ALL_LOCATIONS automatically inherited unless excluded:** proposed optimization, not settled policy. Later discussion explicitly reopened inheritance versus location confirmation.
- **Capture every event immediately versus Planned event features:** superseded for the tester-ready UI by D-20. Richer automation and the technical event contract remain later work.
- **Specific offer examples, seed counts, seven-mile radii, result counts and notification delays:** illustrative suggestions, not universal constants or verified current facts.

## Open decision register

Owner for all questions: product owner with implementation review; no named team assignments have been established.

| ID | Open question | Needed before / relevant docs |
|---|---|---|
| Q-01 | Backend/storage selection, hosting, API contracts, physical layout, geographic indexes and justified denormalization? | Database implementation; ARCHITECTURE, DATA_MODEL. |
| Q-02 | Scope vocabulary, regional boundaries, missing-mapping semantics, inheritance and conflict precedence? | Applicability implementation; DATA_MODEL. |
| Q-03 | Publication minimum evidence; recommending unknown locations; confidence formula, freshness/decay and recheck cadence? | Consumer trust rules and lifecycle gates; DEAL_LIFECYCLE. |
| Q-04 | Eligibility representation and unknown handling; time zones, overnight recurrence and expiration semantics; savings calculation? | Filtering/ranking and presentation; PRODUCT, DATA_MODEL. |
| Q-05 | Restaurant provider, allowed storage/caching, ingestion budget and refresh; exact market and seed counts; curated deal sourcing? | Real-data ingestion; ARCHITECTURE, ROADMAP. |
| Q-06 | D-20 resolves the tester-ready UI subset. How are event identity, correlation, attribution, retries and deduplication defined before shared persistence and analytics? | Event implementation and analytics claims; PRODUCT, DATA_MODEL, ROADMAP. |
| Q-07 | Anonymous versus authenticated MVP users; account migration; household ownership/membership and required profile fields? | Persistent user design; ARCHITECTURE, DATA_MODEL. |
| Q-08 | Ranking formula, fit gates versus soft preferences, tie-breakers, behavioral learning and explicit/learned conflict handling? | Recommendation implementation; PRODUCT. |
| Q-09 | Operational roles, action permissions, approval requirements, merge/version/reactivation semantics and minimum tool scope? | Operational workflow implementation; ARCHITECTURE, DEAL_LIFECYCLE. |
| Q-10 | Privacy, location precision/consent, event/evidence retention, deletion/redaction and future merchant aggregation policy? | Production data collection; ARCHITECTURE, DATA_MODEL. |
| Q-11 | Exact onboarding and no-result experience; explore/map/navigation launch scope and providers; notifications and manual rewards scope? | Related UI implementation; PRODUCT, ROADMAP. |
| Q-12 | MVP success metric and how to measure the ~30-second aspiration; performance targets and acceptance criteria? | MVP evaluation; PRODUCT, ROADMAP. |
| Q-13 | Product name, monetization, merchant claims/ownership, analytics attribution, rewards feasibility and iOS approach? | Relevant later milestone; PRODUCT, ROADMAP. |
| Q-14 | Environment provisioning sequence, release controls, domain ownership, audit storage and recovery approach? | Shared/backend operational rollout; ARCHITECTURE, ROADMAP. |

## New decision template

```text
ID / title:
Date:
Status: Proposed | Accepted | Superseded
Context / question:
Decision:
Rationale and alternatives:
Consequences / scope classification:
Affected documents and implementation:
Replaces / replaced by:
```

## Change log

- **0.2 — September 26, 2026:** consolidated earlier BRD/logical-model drafts and later lifecycle, evidence, environment, internal-tool and identity discussion into this coherent pack. Added explicit provenance, scope uncertainty and decision tracking. No application code or infrastructure changed.
