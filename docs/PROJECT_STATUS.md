# Where We Going — Project Status

- **Last reviewed:** October 4, 2026
- **Primary target:** Tester-ready MVP
- **Progress method:** Step progress is the percentage of checked checklist items. Overall progress is the weighted total of all steps.
- **Interpretation:** These percentages are a current evidence-based assessment, not release commitments. Ideas and proposed architecture do not count as completed implementation.

## Milestones

| Milestone | Progress | Meaning | Exit condition |
|---|---:|---|---|
| Prototype ready | 97 | The owner can run and evaluate first launch, personalization, recommendations, dinner plans, and the next-visit feedback loop locally. | Complete the remaining end-to-end prototype review and correct blocking UX defects. |
| Tester ready | 37 | Another person can install the app, use realistic data, and provide useful feedback safely. | Complete Steps 1–9 at the agreed tester-ready scope. |
| Public MVP ready | 8 | The app can serve real users with production data and operational safeguards. | Complete the public release requirements in Step 10 plus all required earlier gates. |

## Workstreams

| Workstream | Progress | Current evidence | Next useful result |
|---|---:|---|---|
| Product and UX | 80 | Core promise, implemented first-launch journey, profiles, Food Profile, selectable alternatives, manageable dinner plans, a meal-based Home dashboard, and tester-ready feedback flow are established. | Complete the remaining account-prompt and measurement decisions. |
| Android client | 60 | A modularized Compose prototype covers onboarding and major screens. Room repositories now persist household, food preferences, the prototype catalog and dinner journeys. The owner reported no unexpected behavior in meal testing. | Move top-level state behind clear boundaries and expand the catalog model for realistic data. |
| Recommendation engine | 70 | Plain Kotlin scoring uses deal strength, confidence, current-day availability, direct ratings, learned food traits, exact-age eligibility, exclusions and deliberate fallback rules; future offers cannot boost tonight. | Add geography, distance, structured date/time validity and production-data inputs. |
| Restaurant and deal data | 55 | The accepted logical model now has an incremental relational reference covering restaurant/location identity, typed attributes, immutable deal versions, schedules, effective-dated location applicability, deal-condition groups, and contextual user eligibility responses. | Finish typed condition details, then define the real-data sandbox, provider rules and seed sizes. |
| Backend and infrastructure | 10 | Supabase development proof completed in the owner's walkthrough: fixture imports succeeded, guests saw no drafts, the designated reviewer published one test offer, an ordinary account received 403, and Android read the same published offer. Android now refreshes the development restaurant/location catalog into Room; all supported approved DEV offers become available after SQL 008. The production backend is not complete. | Add a reviewed real-offer contract and publication workflow; hosted SQL 008 and an Android refresh remain to verify approved Panda availability. |
| Feedback and learning | 65 | Deal appeal, intent, and one-tap next-visit deal outcomes are stored locally without treating deal failure as restaurant dislike. | Define durable event contracts and connect accepted signals to recommendations. |
| Testing and launch | 35 | The app has been built, installed, and visually checked repeatedly; debug-only tools now provide repeatable weekday simulation, state visibility, onboarding preview and confirmed local reset. | Execute the end-to-end acceptance cases and define a tester distribution plan. |

## Step 1: Finish defining the MVP demonstration

- **Status:** In progress
- **Weight:** 12
- **Phase:** Product foundation
- **Summary:** Turn the strong product direction and working prototype into an explicit, testable MVP contract.
- **Depends on:** Existing product documents and prototype review
- **Next result:** Accepted MVP journey, feedback subset, edge-case behavior, and success criteria

### Checklist

- [x] Define the core promise: answer “Where should we eat tonight?” quickly.
- [x] Keep the MVP focused on food and restaurants.
- [x] Select Elk Grove as the initial supported market.
- [x] Support both household and solo-user contexts.
- [x] Establish one winner plus a small set of alternatives.
- [x] Include lightweight household and food preference profiles.
- [x] Require traceable recommendation factors while keeping consumer deal cards concise.
- [x] Decide the exact intent, visit, verification, and preference-feedback subset for the MVP.
- [x] Define no-result and weak-result behavior.
- [x] Define how the 30-second aspiration will be measured once representative data and external testers are available.
- [x] Confirm MVP community scope: reviewed submissions, My submissions, signed-in problem reports, and Open in maps; favorites and automated approval are deferred.
- [x] Confirm offline support is post-launch; one failed-deal response triggers review, and unavailable offers never silently replace an existing plan.
- [x] Confirm outcome correction scope: Edit outcome, brief Undo after No, and Edit last check-in until the next selected plan; no Recent plans section.
- [ ] Resolve the remaining shared-user experience requirements before implementing submissions.

## Step 2: Modularize the Android prototype

- **Status:** In progress
- **Weight:** 12
- **Phase:** Application foundation
- **Summary:** Make the code understandable and replaceable without changing the working user experience.
- **Depends on:** Current Android prototype
- **Next result:** Separate UI, application state, domain logic, and local data boundaries

### Checklist

- [x] Separate the visual theme into dedicated files.
- [x] Separate prototype restaurant and deal data from the main activity.
- [x] Move each major screen into its own file or feature package.
- [x] Extract reusable UI components.
- [ ] Move top-level app state out of the screen-rendering function.
- [x] Introduce a household profile repository interface.
- [x] Introduce a food-preference repository interface.
- [x] Introduce a restaurant/deal repository interface.
- [ ] Add Open in maps on location details, with Copy address when no maps app can open it; keep this separate from plan intent and outcomes.
- [x] Move recommendation rules into plain Kotlin.
- [x] Preserve all current flows during the refactor.
- [x] Document the resulting package responsibilities for the owner.

## Step 3: Build recommendation scoring version 1

- **Status:** In progress
- **Weight:** 14
- **Phase:** Core product engine
- **Summary:** Replace the limited prototype selection rule with a transparent, testable recommendation method.
- **Depends on:** Steps 1 and 2
- **Next result:** One traceable winner and meaningful alternatives for contrasting profiles

### Checklist

- [x] Filter removed restaurants from recommendations.
- [x] Consider basic deal availability and direct thumbs feedback.
- [ ] Define hard filters for geography, validity, applicability, and eligibility.
- [x] Define version 1 soft scores for preference and deal strength; leave urgency, distance, and history-based variety for production data.
- [x] Connect Food Profile ratings and inferred themes to ranking.
- [x] Define tie-breakers and weak-match behavior.
- [x] Retain internal score details and show eligibility/verification without algorithm narration.
- [x] Verify materially different results for family and solo profiles.

## Step 4: Validate the logical data model

- **Status:** In progress
- **Weight:** 10
- **Phase:** Data foundation
- **Summary:** Confirm that the documented business concepts hold up under realistic and difficult cases.
- **Depends on:** Product requirements
- **Next result:** Accepted logical relationships and an explicit list of unresolved product rules

### Checklist

- [x] Separate Restaurant from physical Location.
- [x] Model one Restaurant with one or many Locations.
- [x] Separate Deal from Location.
- [x] Represent per-location deal applicability.
- [x] Separate explicit preferences from permanent exclusions.
- [x] Keep recommendation, intent, visit, deal outcome, and preference feedback distinct.
- [x] Review a single-location independent restaurant scenario.
- [x] Review a chain offer with included, excluded, and unknown locations.
- [x] Review recurring, overnight, expired, and near-closing offers.
- [x] Review eligibility-restricted offers.
- [x] Formally accept the logical model after scenario review.

## Step 5: Define access patterns and expected scale

- **Status:** In progress
- **Weight:** 10
- **Phase:** Data foundation
- **Summary:** Describe the reads, writes, volume, latency, and authorization needs before selecting storage.
- **Depends on:** Steps 3 and 4
- **Next result:** Reviewed query and event catalogue suitable for backend comparison

### Checklist

- [x] Identify nearby-location lookup as a core query.
- [x] Identify current applicable-deal lookup as a core query.
- [x] Identify profile, preference, lifecycle, and event access areas.
- [ ] Define each MVP screen’s exact data requirements.
- [ ] Estimate initial restaurant, location, deal, user, and event volumes.
- [ ] Define expected read/write frequency and latency needs.
- [ ] Define geographic, time-zone, pagination, and indexing needs.
- [ ] Define authorization and operational access patterns.

## Step 6: Select the backend, database, and API boundary

- **Status:** Proposed
- **Weight:** 12
- **Phase:** Shared platform
- **Summary:** Choose technology based on validated access patterns rather than translating the logical model directly into storage.
- **Depends on:** Steps 4 and 5
- **Next result:** Development proof complete; next session plan shared restaurant/location catalog integration, then finish physical design and production environment decisions

### Checklist

- [x] Establish that the Android client should use platform-agnostic data and business boundaries.
- [x] Compare Firebase/Firestore with a relational database and API; accept Supabase/PostgreSQL for a bounded development proof.
- [x] Prepare the development proof SQL, insert-only CSV fixtures, browser publication test, Android debug reader and local PostgreSQL permission tests.
- [x] Create/connect the Free Supabase development project and verify guest reads and reviewer-only publishing through the hosted API and Android (owner walkthrough, October 3: no drafts visible, reviewer publication succeeded, ordinary account denied with 403, guest/browser and Android read one published test offer).
- [ ] Compare Kotlin server, TypeScript, and other practical backend choices.
- [x] Decide anonymous versus authenticated MVP use: guests recommend, plan and check in; signed-in users submit deals and report problems.
- [ ] Implement optional sign-in from the menu and submission entry, preserving the guest profile/history; authentication provider remains undecided.
- [ ] Connect shared published deals, submissions and review actions to an authorized backend; local Room remains the current prototype store.
- [ ] Define the initial API or repository contracts.
- [ ] Design the physical data layout and justified denormalization.
- [ ] Define dev, QA/staging, and production isolation.
- [ ] Define authorization and secret management.
- [ ] Show a clear connection-unavailable message for shared MVP actions; offline recommendations and queued submissions are deferred.
- [ ] Define backup, recovery, retention, and deletion responsibilities.
- [ ] Record the accepted choice, alternatives, cost, and owner learning needs.

## Step 7: Build the real-data sandbox and deal lifecycle

- **Status:** In progress
- **Weight:** 10
- **Phase:** Data implementation
- **Summary:** Replace hard-coded supply with safe, traceable data at a useful Elk Grove scale.
- **Depends on:** Steps 4–6
- **Next result:** Repeatable sandbox containing real locations, synthetic edge cases, and verified genuine deals

### Checklist

- [x] Research an initial set of real Elk Grove restaurants and official sources.
- [x] Document the evidence-to-publication lifecycle and trust boundary.
- [ ] Select a restaurant/location data provider or permitted curation method.
- [ ] Review storage, caching, cost, and refresh terms.
- [ ] Define the exact initial market and seed sizes.
- [ ] Load real restaurant and location records outside Kotlin constants.
- [ ] Create clearly labeled synthetic deals for edge cases.
- [ ] Create a smaller source-verified genuine deal set.
- [ ] Exercise validation, publication, monitoring, and retirement.
- [ ] Prove synthetic data cannot enter production flows.
- [x] Build private-test anonymous text submission with known-location selection, missing-place details and a separate local/auth identity link; verify hosted ownership and retry isolation.
- [x] Replace restaurant buttons with bounded backend location search, selected-place cards, two-step text submission, confirmation and separate My submissions.
- [ ] Complete on-device encrypted-session, submission-delivery and reviewer walkthrough.
- [x] Build reviewer location search, explicit restaurant/location creation, duplicate-offer checks and separate editable drafts preserving original contributions.
- [x] Confirm the product owner is the sole MVP reviewer and the review interface is browser based.
- [x] Build the development manual queue with Pending, Needs clarification, Approved and Rejected; reviewer-only atomic approval, notes and published immutability.
- [x] Apply SQL 007 and complete the owner’s hosted review/approval walkthrough (Panda approved).
- [ ] Apply SQL 008 and confirm approved Panda reaches Android; extend date/time support before publishing timed offers.
- [x] Build My submissions status and reviewer-note display with own-only backend reads.
- [ ] Add updates/resubmission for held entries and verify the clarification workflow end to end.
- [ ] Build signed-in Report a problem with offer ended, incorrect details, wrong location or other, optional note and review routing; reports do not automatically change published deals.
- [ ] Flag a single failed-deal check-in for review without automatically hiding the offer or changing restaurant preference.
- [ ] Remove reviewer-confirmed ended offers from new recommendations while preserving their original details in past meals.

## Step 8: Complete the MVP feedback loop

- **Status:** In progress
- **Weight:** 8
- **Phase:** Product learning
- **Summary:** Preserve the meaning of user actions while collecting only the feedback needed for the MVP.
- **Depends on:** Steps 1 and 3
- **Next result:** Accepted and implemented recommendation-to-feedback workflow

### Checklist

- [x] Collect restaurant ratings and “haven’t tried” responses.
- [x] Support light thumbs feedback and permanent restaurant removal.
- [x] Present a basic “Did you try the recommendation?” question.
- [x] Decide which intent, visit, deal outcome, and preference events ship in the MVP.
- [x] Build the local next-visit card with a three-visit limit and dismiss option.
- [ ] Define event correlation, retries, duplication, and missing responses.
- [x] Keep deal failure separate from restaurant preference.
- [x] Feed restaurant ratings and learned food traits into future recommendations.
- [x] Explain and test how users edit or reverse prior restaurant ratings.
- [ ] Preserve a chosen place when its offer expires or is withdrawn; show the appropriate unavailable message and Choose another place, retaining check-in for earlier use.
- [ ] Add Edit outcome to meal history; corrections update Home totals and derived reliability signals while preserving prior responses.
- [ ] Add brief Undo after No and Edit last check-in on Home until the next dinner plan is selected; preserve original offer access even after expiration, without a Recent plans section or unused-plan counts.

## Step 9: Validate the complete tester-ready journey

- **Status:** In progress
- **Weight:** 7
- **Phase:** Validation
- **Summary:** Test the complete experience systematically rather than validating only individual screens.
- **Depends on:** Steps 1–8 at tester-ready scope
- **Next result:** Test evidence, known limitations, and prioritized corrections

### Checklist

- [x] Build and install the prototype successfully on the Android emulator.
- [x] Visually inspect major screens and selected interaction states.
- [x] Define repeatable first-time and returning-user test scripts.
- [x] Test family and solo profiles against the same candidate supply.
- [ ] Test strong match, weak match, and no-result cases.
- [ ] Test excluded, expired, unknown, and conflicting deals.
- [ ] Measure the agreed decision-time experience with representative data and external testers.
- [ ] Conduct an external tester session and prioritize findings.
- [x] Verify Room catalog reads, reset and schema 4-to-6 migration using isolated emulator test fixtures.
- [x] Connect the development shared catalog to Room with atomic refresh, stable identity mapping, preserved personal data and historical plans, Retry on failure and development environment checks.
- [ ] Test guest/account permissions, guest-to-account continuity, submission clarification/resubmission, reviewer publication and problem reports end to end.
- [ ] Test check-in corrections, corrected totals, selected-offer expiry/withdrawal, and the distinction between the 25-minute recommendation cutoff and actual offer end time.

## Step 10: Prepare the public MVP release

- **Status:** Parked
- **Weight:** 5
- **Phase:** Release
- **Summary:** Add the production safeguards, policies, monitoring, and distribution required for real users.
- **Depends on:** Tester-ready MVP validation
- **Next result:** Reviewed Google Play release candidate

### Checklist

- [ ] Confirm production privacy and location-consent behavior.
- [ ] Complete production authentication or anonymous-user policy.
- [ ] Configure production environments, secrets, and release controls.
- [ ] Add crash reporting and essential operational monitoring.
- [ ] Define support, data correction, and deal incident processes.
- [ ] Complete accessibility and supported-device review.
- [ ] Prepare store listing, screenshots, disclosures, and privacy materials.
- [ ] Run a closed Google Play test.
- [ ] Resolve launch-blocking tester findings.
- [ ] Approve and publish the production MVP.

## Update procedure

1. Update checklist evidence and wording in this file.
2. Update `docs/docs/DECISIONS.md` when a material decision is accepted or superseded.
3. Run `./tools/build_project_hub.ps1` from the project root.
4. Review `docs/PROJECT_HUB.html` in a browser.
5. Commit the tracker, generated dashboard, and any related source changes together.

## October 3 connection verification

- Debug build and Kotlin unit tests passed; release build passed before the final onboarding-summary adjustment.
- Four emulator tests passed: schema 4-to-6 upgrade, local catalog persistence, atomic shared refresh with ratings/exclusions/household/pending-offer preservation and rollback, and the live Supabase-to-Room development import.
- Embedded PostgreSQL permissions checks passed, including the separate app endpoint excluding published synthetic offers.
- Owner review of the refreshed Android screens and real reviewed offer publication remain pending. Existing app data was preserved during installation.

## October 4 tester intake progress

- Accepted anonymous authenticated submissions for private development testing (D-74); public sign-in policy remains open.
- Added separate local/auth identity linkage, encrypted session persistence and refresh, the Share a deal form and own-submissions status list.
- Added private pending submission RPCs with identity ownership, location checks and retry deduplication.
- Build/unit checks and isolated PostgreSQL permission tests pass. Hosted setup/owner walkthrough, review UI, clarification and real-offer publication remain pending.

### Tester intake verification detail

The development intake SQL passes isolated PostgreSQL checks for direct-table denial, own-only reads, actor enforcement, invalid-location rejection, idempotent retry and SQL-looking input stored as literal data. Owner enabled anonymous sign-in and applied SQL 005. Live backend checks passed: anonymous signup, session refresh, idempotent pending submission, own-only reads and isolation from recommendation offers. Two development test identities and one TEST ONLY pending entry were created; never approve that entry. The emulator initially reported unavailable package/activity services during installation; local identity instrumentation is not yet claimed as passed.

### Submission redesign verification

Debug build and unit tests passed. Isolated PostgreSQL checks cover bounded search, case-insensitive prefixes, literal wildcards, active-only filtering, matching location IDs and existing permissions. SQL 006 was applied by the owner; live read-only search checks passed without creating accounts or submissions. The updated debug app was installed in the emulator.

The emulator form navigation test now passes: missing-place validation, movement between steps, and preservation of the offer description when changing place. Updated Espresso from 3.5.1 to 3.7.0 to resolve its removed InputManager reflection call on the Android 17 emulator (test dependency only). The normal app was reopened after the test.

### Reviewer queue verification

Isolated PostgreSQL tests pass for denied guest/ordinary-review access, original text preservation, saved drafts, clarification, stale revisions, missing-place creation, duplicate rejection, publication rollback, published schedule/test-classification immutability, and test offer/place isolation. A focused emulator test passes for real-shaped published-offer import into Room and preservation of the exact historical version after withdrawal. The browser editor was visually checked with an explicitly labeled local fixture, including unchanged original text after draft edits. Hosted reviewer walkthrough awaits SQL 007 application and owner sign-in; no real hosted offers were published by Codex.

### Review action feedback correction

Approval validation now lists the exact missing fields/checks next to the action buttons and brings errors into view. Saving, updating and approval show progress and explicit completion. An approval remains reported as successful even if the subsequent queue refresh fails. Validation tests confirm that test classification does not block test approval; PostgreSQL review/permission checks pass. Test offers remain excluded from recommendations pending the separate test-catalog option.

## Development catalog and launch boundary — D-77 (October 4, 2026)

All approved, supported DEV offers participate in Android recommendations, including previously flagged Panda records. SQL 008 removes per-record test flags without changing published content, IDs, schedules or activity references. Small Development indicators identify the environment. Review approval still means possible deal, not verified deal; permissions, duplicate checks and immutable publication remain. This supersedes earlier test-flag exclusions in D-73/D-76 and sandbox labeling requirements in D-11.

Launch requires a fresh production database: apply reviewed schema, import curated restaurants/locations and genuine reviewed offers, configure the release app, verify environment separation, and begin fresh production activity. Do not copy DEV offers, test accounts or meal history. Preserve DEV for testing. This launch gate is documented, not implemented.

Implementation checks: embedded PostgreSQL migration verifies previously approved Panda becomes visible with the same version ID; permissions, duplicate prevention and immutable schedules remain enforced. Hosted SQL 008 and emulator refresh remain pending.

Android assembleDebug and Android test-source compilation passed after D-77. Reviewer approval validation and embedded PostgreSQL checks passed, including new post-migration approval of development wording. Emulator behavior after hosted SQL 008 is still awaiting the owner’s walkthrough.
