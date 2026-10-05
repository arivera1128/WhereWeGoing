# Capability roadmap

Version 0.2 · Working design · September 26, 2026

**MVP:** core validation scope. **Planned:** design for now, implement later. **Future:** extension point. These are capability classifications, not dates or a claim that work is complete. Where earlier conversation left release timing ambiguous, this document records it rather than making a new product decision.

## Capability register

| Capability | Classification | Boundary / dependency |
|---|---|---|
| Android/Kotlin/Compose client | MVP | Existing prototype reported; inspect actual code before planning changes. |
| Tonight decision, winner and alternatives, explanations | MVP | Approximately 30-second UX aspiration. |
| Basic user/household profile and explicit preferences | MVP | Guest onboarding; optional account required for submissions and problem reports. |
| Geographic candidate filtering and basic ranking | MVP | Scope/time/eligibility resolution before ranking; formula TBD. |
| Restaurant, Location, Deal, DealLocation | MVP | Logical model review before physical schema. |
| Basic evidence, curated deals, manual lifecycle and history | MVP | Dedicated workflow portal and automation not required. |
| Real-data sandbox plus synthetic stress cases | MVP | Real locations; labeled test offers; smaller verified deal set. |
| Environment isolation | MVP foundation | Dev, QA/staging and prod kept distinct; provisioning sequence TBD. |
| Identity/profile/activity separation | MVP design seam | Guest access accepted; stable identity and guest-to-account continuity required; provider undecided. |
| Basic deal appeal, intent and next-visit check-in | MVP | One-tap check-in records tried-and-worked, tried-and-failed, or not-used; it does not independently prove a restaurant visit. Local implementation is sufficient initially. |
| Local structured persistence | MVP foundation | Room/SQLite behind Kotlin repositories; schema export and migrations required. Shared backend and synchronization remain later decisions. |
| Event contracts and correlation seams | MVP design seam | Retry, deduplication and attribution details remain open under Q-06. |
| Richer or automated feedback collection | Planned | GPS triggers, push reminders and other automated follow-up are outside the tester-ready MVP. |
| Behavioral learning and richer confidence | Planned | Preserve explicit preferences; requires meaningful evidence/events. |
| User deal submissions and My submissions | MVP, not implemented | Signed-in text entry; missing-place path; manual review before publication; clarification and resubmission. |
| Community deal outcomes and problem reports | MVP subset | Guests can check in; only signed-in users report problems. Self-reports inform review, not automatic verification. |
| Open in maps with Copy address fallback | MVP, not implemented | Specific location details; external handoff; no GPS tracking or new intent/visit event. |
| In-app explore/map | Planned | Separate from external maps handoff; interaction design open. |
| Basic reviewer workflow | MVP, not implemented | Match places/locations, resolve duplicate offers, hold/approve/reject, preserve evidence and route problem reports. Full operations portal remains Planned. |
| Optional consumer sign-in | MVP, not implemented | Menu and submission entry; preserve guest profile/history; provider/recovery/synchronization choices remain open. |
| Shared Supabase/PostgreSQL development proof | Accepted experiment | Local SQL/import/browser/Android test files prepared; hosted deployment and JWT/device verification pending. Production plan and complete schema remain unapproved. |
| Favorites and photo/menu submissions | Future | Explicitly deferred from launch. |
| Offline mode | Post-launch | Shared MVP can require connectivity with a clear unavailable message; local Room storage remains. |
| Failed-deal review and ended-offer retirement | MVP, not implemented | One failure flags review without automatic hiding; confirmed ended offers leave recommendations but remain in historical evidence. |
| Selected-offer availability handling | MVP, not implemented | Preserve chosen place, explain expiry/withdrawal, allow another choice and earlier-use check-in; 25-minute cutoff only gates new recommendations. |
| Check-in outcome correction | MVP, not implemented | Edit outcome in meal history; brief Undo after No and Edit last check-in until next plan; no Recent plans section; corrected totals/signals retain provenance. |
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


## Development shared catalog connection — October 3, 2026

The configured debug app fetches `ww_app_catalog` on opening and eligible foreground returns, validates the complete snapshot, and updates Room in one transaction. The current endpoint supplies three restaurant locations and deliberately no offers: TEST ONLY fixtures remain confined to the proof tool. These restaurants can be recommended without a deal. Release configuration remains disconnected pending production setup.

Schema 6 adds shared-to-local identity mapping, refresh state and a replaceable current offer/location projection. Existing personal rows keep their local IDs. Withdrawn catalog rows become inactive; historical offer versions and pending-plan references remain available. Failed refresh keeps personal data and prior plans accessible, pauses new recommendations and exposes Retry. The snapshot stays stable while selecting dinner.

This is a bounded development connection, not the complete production deal model. Real offer publication, structured date/time and eligibility, distance, full multiple-location selection and a scalable paginated refresh contract remain work ahead. Outcome correction and Undo remain pending.

## Anonymous tester intake — October 4, 2026

For the private development feature test, anonymous Supabase-authenticated users may submit deals without email/password. This explicitly overrides the identifiable-sign-in submission requirement for testing only; public launch policy and problem-report access are not changed. Recommendations still require no account screen. The anonymous identity is created when Share a deal opens and is linked to the existing local app user. Reopening reuses the encrypted session; tokens refresh when needed. Clearing/resetting data loses submission access and starts a new identity; recovery and cross-device sync are deferred.

Schema 7 adds the user/auth-project identity association. Tokens are encrypted with Android Keystore and stored outside Android backup. No service-role credentials enter the app. Backend 005 provides authenticated submission and own-only read RPCs, idempotent retry IDs, basic length/location validation and ten submissions per identity per day. New entries remain PENDING and never enter recommendation results. This per-identity cap is not a complete public abuse control. Production CAPTCHA/access limits and public identity policy remain launch gates.

Implemented UI: Share a deal, choose a known location or describe another place, enter free-text offer details, send for review and refresh My submissions. Reviewer UI, clarification/resubmission, publication of real reviewed offers and complete end-to-end hosted verification remain pending. No offline queue is introduced.

## Submission experience — October 4, 2026

Share a deal now has two steps: search/select a location (or describe a missing place), then write one offer description. Search calls a bounded backend prefix query after a short typing pause, returning at most ten active locations with addresses and shared restaurant/location UUIDs. It does not load the whole catalog into the form or depend on the recommendation snapshot. A lowercase-name index supports this initial search; fuzzy/address search, paging, broader geography and catalog enrichment remain later needs. Wildcards and SQL-looking input are treated literally.

The selected place appears in a warm card with Change place. Description and retry ID survive configuration changes; changing the place or description produces a new submission ID before sending. Delivery uncertainty locks the submitted payload for an identical retry. Review fills structured schedule, eligibility and immutable published records; contributor text remains unchanged evidence. Confirmation directs to the separate My submissions page, also available from the menu. The reviewer workflow remains pending.

## Development reviewer queue — October 4, 2026

The owner approved reviewer editing, missing restaurant/location creation and a test-submission flag. The browser queue at backend/proof/review.html replaces the original permissions-proof screen; that screen remains at publication-proof.html. Status tabs load 20 entries per page. Original contribution text stays unchanged; edits live in review_draft, with optimistic revision checks and append-only application review events. Reviewer-only RPCs enforce access independently of the browser.

Actions are Save draft, Needs clarification, Reject and Approve. Clarification/rejection require a contributor-visible note. Approval resolves or explicitly creates restaurant/location UUIDs, requires offer wording, terms, source, weekdays, deal strength and review confirmation, and creates immutable published version/schedule records atomically. Matching published offer wording/terms/weekdays at a location is rejected as a duplicate. Approval means a possible offer (not verified). The current engine only supports recurring weekly all-day schedules: date-limited or timed offers must be held rather than represented as all-day offers. Structured eligibility expansion remains pending.

After SQL 008, all supported approved development offers and their places reach ww_app_catalog (D-77). No existing submission is automatically approved. Finalized reviews are read-only; published changes require a future new-version workflow. Contributor clarification/resubmission, retirement UI, fuzzy matching and production authentication/abuse controls remain pending.

## Development catalog and launch boundary — D-77 (October 4, 2026)

All approved, supported DEV offers participate in Android recommendations, including previously flagged Panda records. SQL 008 removes per-record test flags without changing published content, IDs, schedules or activity references. Small Development indicators identify the environment. Review approval still means possible deal, not verified deal; permissions, duplicate checks and immutable publication remain. This supersedes earlier test-flag exclusions in D-73/D-76 and sandbox labeling requirements in D-11.

Launch requires a fresh production database: apply reviewed schema, import curated restaurants/locations and genuine reviewed offers, configure the release app, verify environment separation, and begin fresh production activity. Do not copy DEV offers, test accounts or meal history. Preserve DEV for testing. This launch gate is documented, not implemented.
