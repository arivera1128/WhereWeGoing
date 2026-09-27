# Where We Going — Project Status

- **Last reviewed:** September 27, 2026
- **Primary target:** Tester-ready MVP
- **Progress method:** Step progress is the percentage of checked checklist items. Overall progress is the weighted total of all steps.
- **Interpretation:** These percentages are a current evidence-based assessment, not release commitments. Ideas and proposed architecture do not count as completed implementation.

## Milestones

| Milestone | Progress | Meaning | Exit condition |
|---|---:|---|---|
| Prototype ready | 90 | The owner can run and evaluate the major product flows locally. | Complete the remaining end-to-end prototype review and correct blocking UX defects. |
| Tester ready | 34 | Another person can install the app, use realistic data, and provide useful feedback safely. | Complete Steps 1–9 at the agreed tester-ready scope. |
| Public MVP ready | 8 | The app can serve real users with production data and operational safeguards. | Complete the public release requirements in Step 10 plus all required earlier gates. |

## Workstreams

| Workstream | Progress | Current evidence | Next useful result |
|---|---:|---|---|
| Product and UX | 75 | Core promise, target audience, Elk Grove focus, profiles, Food Profile, recommendation screens, and tester-ready feedback flow are established. | Close the uncertainty, no-result, and measurement decisions. |
| Android client | 45 | A working Compose prototype covers the major screens and persists local preferences. | Separate screens, state, domain logic, and repositories without changing behavior. |
| Recommendation engine | 25 | Basic availability, savings rank, direct feedback, and removal rules affect the pick. | Define and implement explainable filtering and scoring using the full profile. |
| Restaurant and deal data | 30 | Curated Elk Grove examples, sources, and a detailed logical model exist. | Validate model scenarios and define the real-data sandbox. |
| Backend and infrastructure | 10 | Platform boundaries and environment requirements are documented. | Define access patterns, then compare backend and database options. |
| Feedback and learning | 45 | Ratings, thumbs feedback, removal, and a basic visit question exist; the tester-ready check-in meanings are now accepted. | Build the next-visit card and connect signals without conflating outcomes. |
| Testing and launch | 20 | The app has been built, installed, and visually checked repeatedly on an emulator. | Create repeatable end-to-end acceptance cases and a tester distribution plan. |

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
- [x] Require an explanation for the recommendation.
- [x] Decide the exact intent, visit, verification, and preference-feedback subset for the MVP.
- [ ] Define no-result and weak-result behavior.
- [ ] Define how the 30-second aspiration and MVP success will be measured.

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
- [ ] Move each major screen into its own file or feature package.
- [ ] Extract reusable UI components.
- [ ] Move top-level app state out of the screen-rendering function.
- [ ] Introduce profile and preference repository interfaces.
- [ ] Introduce a restaurant/deal repository interface.
- [ ] Move recommendation rules into plain Kotlin.
- [ ] Preserve all current flows during the refactor.
- [ ] Document the resulting package responsibilities for the owner.

## Step 3: Build recommendation scoring version 1

- **Status:** In progress
- **Weight:** 14
- **Phase:** Core product engine
- **Summary:** Replace the limited prototype selection rule with a transparent, testable recommendation method.
- **Depends on:** Steps 1 and 2
- **Next result:** One explainable winner and meaningful alternatives for contrasting profiles

### Checklist

- [x] Filter removed restaurants from recommendations.
- [x] Consider basic deal availability and direct thumbs feedback.
- [ ] Define hard filters for geography, validity, applicability, and eligibility.
- [ ] Define soft scores for preference, savings, urgency, distance, and variety.
- [ ] Connect Food Profile ratings and inferred themes to ranking.
- [ ] Define tie-breakers and weak-match behavior.
- [ ] Generate grounded “why this,” “why you,” and “why now” explanations.
- [ ] Verify materially different results for family and solo profiles.

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
- [ ] Review a single-location independent restaurant scenario.
- [ ] Review a chain offer with included, excluded, and unknown locations.
- [ ] Review recurring, overnight, expired, and eligibility-restricted offers.
- [ ] Formally accept or revise the logical model after scenario review.

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
- **Next result:** Accepted technology decision, physical design, and environment plan

### Checklist

- [x] Establish that the Android client should use platform-agnostic data and business boundaries.
- [ ] Compare Firebase/Firestore with a relational database and API.
- [ ] Compare Kotlin server, TypeScript, and other practical backend choices.
- [ ] Decide anonymous versus authenticated MVP use.
- [ ] Define the initial API or repository contracts.
- [ ] Design the physical data layout and justified denormalization.
- [ ] Define dev, QA/staging, and production isolation.
- [ ] Define authorization and secret management.
- [ ] Define backup, recovery, retention, and deletion responsibilities.
- [ ] Record the accepted choice, alternatives, cost, and owner learning needs.

## Step 7: Build the real-data sandbox and deal lifecycle

- **Status:** Proposed
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
- [ ] Define event correlation, retries, duplication, and missing responses.
- [ ] Keep deal failure separate from restaurant preference.
- [ ] Feed accepted signals into future recommendations.
- [ ] Explain and test how users edit or reverse prior feedback.

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
- [ ] Define repeatable first-time and returning-user test scripts.
- [ ] Test family and solo profiles against the same candidate supply.
- [ ] Test strong match, weak match, and no-result cases.
- [ ] Test excluded, expired, unknown, and conflicting deals.
- [ ] Measure the agreed decision-time experience.
- [ ] Conduct an external tester session and prioritize findings.

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
