# Decisions and open questions

Version 0.2 · Consolidation date: September 26, 2026

These records summarize the September 2026 discussion and current explicit request. The consolidation date is not a claim that each decision was originally made that day. “Established” records agreed direction; it does not imply implementation. New material decisions should include date, status, context, rationale, consequences and affected documents. Preserve superseded records.

## Established decisions

| ID | Decision and rationale | Consequences / affected documents |
|---|---|---|
| D-01 | Food/restaurants MVP; answer “Where should we eat tonight?” in ~30 seconds. Focus on decision fatigue and cost. | PRODUCT, ROADMAP: other deal categories excluded; measurement details remain open. |
| D-02 | Personalized decision engine, one winner plus alternatives. Relevance exceeds deal volume. D-28 refines what the consumer sees and what remains internally traceable. | PRODUCT, DATA_MODEL: basic household/user profiles and explicit preferences; learning grows later. |
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
| D-21 | A weak-deal result may recommend a preferred place with a clear warning. A true no-result state must explain why no trustworthy candidate is available and offer a relevant recovery action rather than fabricate a recommendation. | PRODUCT, recommendation UI: unsupported areas direct users to update location; an exhausted candidate set directs users to review removed places. |
| D-22 | The tester-ready next-visit interaction uses one question: “Did you try this deal?” Outcomes are tried-and-worked, tried-and-failed, or no. The card can be dismissed and may return up to three times. This supersedes D-20’s two-screen visit-then-deal sequence for the tester-ready UI; it does not claim an independently verified visit. | PRODUCT, ROADMAP, Android UI: reduce interaction steps while keeping deal failure separate from restaurant preference. |
| D-23 | Alternatives on Tonight's Pick are actionable choices. Choosing one promotes it to the featured position as “Your selected deal,” moves the original recommendation into the alternatives, and gives the chosen deal the same intent and usefulness actions. The app keeps the algorithmic recommendation distinct from the user's selection. | PRODUCT, Android UI: users can act on a preferred alternative without losing the recommendation signal or entering a separate workflow. |
| D-24 | The Home dashboard summarizes meal outcomes rather than every interaction. A meal is recorded only after a check-in says the deal worked or did not work; “No,” usefulness votes and selections do not create meal history. Initial metrics are meals recorded, deals that worked and unique places visited. Estimated savings is deferred because the prototype cannot calculate actual order value reliably. | PRODUCT, DATA_MODEL, Android UI: Home becomes a personal dinner dashboard while preserving truthful event meaning and avoiding invented savings. |
| D-25 | Pressing “I'll try this deal” saves a dinner plan and returns Home. Home shows the plan with details, change and cancel actions; a replacement plan replaces the pending check-in, while cancellation creates no meal. | USER_JOURNEY, PRODUCT, Android UI: finish the decision flow promptly and make the current plan manageable from Home without treating intent as a meal. |
| D-26 | Initial onboarding uses guest access, ZIP or a nonfunctional location affordance, adult and child counts, each child's current age, a default 10-mile radius editable later, and five restaurant prompts. “Haven't tried it” is unknown rather than negative and does not exclude a place. | USER_JOURNEY, PRODUCT, Android onboarding: reduce typing, preserve eligibility detail and defer authentication/location services while retaining their design seams. Implemented in the local prototype; D-27 governs current age-based eligibility. |
| D-27 | Onboarding and the editable Profile use one `HouseholdProfile` model and repository. Child-deal eligibility uses exact saved ages; the current prototype's child offers use a 12-and-under rule, and missing ages never imply eligibility. | ARCHITECTURE, DATA_MODEL, USER_JOURNEY, Android client: prevent Profile edits from discarding onboarding detail and keep persistence replaceable. Deal-specific eligibility terms remain future structured data work. |
| D-28 | **Accepted September 27, 2026.** Recommendation scoring version 1 gives deal strength 65% and restaurant/learned food fit 35%. Removed places are excluded. A 1-star place cannot be featured; a 2-star place can be featured only as a strong-deal fallback. Untried places receive neither a penalty nor a novelty bonus. A food trait is blocked from the featured result only after at least four ratings with at least 75% negative. Three alternatives include the next two acceptable scores and, when available, a different category. Consumer cards show deal terms, verification and an editable restaurant rating rather than algorithm explanations or deal-usefulness votes. A confirmed 1-star tap immediately reruns the current ranking. Ratings 2–5 save immediately but keep the current Tonight's Pick stable until a later recommendation session. Internal score components remain available for testing. | PRODUCT, ARCHITECTURE, USER_JOURNEY, Android client: establishes a testable first scoring policy while keeping the visible decision flow concise and preventing an unexpected mid-flow shuffle. This supersedes the visible explanation and usefulness portions of D-02, D-20 and D-23. Geography, distance, structured validity and broader behavior learning remain future work. |
| D-29 | **Accepted September 27, 2026.** The app still recommends a restaurant when no active deal exists because choosing where to eat is the primary value. A future offer never boosts tonight's score or appears as tonight's deal. Home shows only the nearest eligible upcoming offer within seven days. Active uncertain offers are labeled “Possible deal” with last-checked context and a prompt to confirm with the location; they receive less score weight than verified offers. A no-deal plan and its later check-in use restaurant language rather than claiming deal use. | PRODUCT, USER_JOURNEY, recommendation engine and Android UI: separates restaurant choice, current deal confidence and future planning while giving users a reason to return. |

## Superseded or unapproved sketches

- **Brand → Restaurant → Location:** superseded by D-04 after explicit discussion of why a Brand entity was unnecessary.
- **Hard-coded deals as the next milestone:** useful for the first prototype, superseded by the real-data sandbox direction. Do not restart the prototype from scratch without inspecting it.
- **Firestore collection snippets:** illustrative earlier drafts, not approved physical design. D-16 governs implementation.
- **Rewards-first two-tab app, savings counters and reminders:** early ideas; later direction centers on a restaurant recommendation engine. Exact manual rewards scope remains open.
- **ALL_LOCATIONS automatically inherited unless excluded:** proposed optimization, not settled policy. Later discussion explicitly reopened inheritance versus location confirmation.
- **Capture every event immediately versus Planned event features:** superseded for the tester-ready UI by D-20. Richer automation and the technical event contract remain later work.
- **Two-screen visit and deal confirmation in D-20:** superseded for the tester-ready UI by D-22’s single check-in question. The broader logical event distinction remains available for later evidence sources.
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
| Q-08 | D-28 resolves the local scoring version 1 formula and basic fit gates. How should distance, urgency, variety history, behavior learning, decay and explicit-versus-learned conflicts work with production data? | Later recommendation implementation; PRODUCT. |
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
