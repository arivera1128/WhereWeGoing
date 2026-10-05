# Product requirements

Version 0.2 · Working design · September 26, 2026

## Vision and problem

Help people answer **“Where should we eat tonight?”** in approximately **30 seconds**. Reduce dinner decision fatigue and cost resistance by recommending relevant restaurant deals for this person or household, nearby, at the right time.

The product is a personalized decision engine. Savings information is fragmented across websites, restaurant apps, emails, recurring promotions and rewards. The application should do the comparison work and present one useful choice rather than ask users to interpret a large directory.

The initial persona is a cost-conscious household with two adults and two children, often deciding after work and school pickup. This is an example, not a fixed household structure: a single adult without children must receive appropriately different recommendations.

## MVP requirements

| ID | Requirement | Meaning |
|---|---|---|
| P-01 | Food/restaurants only | Entertainment, groceries and general coupons are outside MVP. |
| P-02 | Fast decision experience | A prominent “Where should we eat tonight?” action returns a clear winner and a small set of alternatives. Exact count TBD. |
| P-03 | Lightweight personalization | Household size, children, preferred/excluded restaurants or categories, approximate location and preferred radius inform results. Exact onboarding sequence TBD. |
| P-04 | Relevant ranking | Consider personal fit, eligibility, savings, timing/urgency, distance and eventually variety/behavior. Formula and tie-breakers TBD. Explicit exclusions must not be mistaken for weak preferences. |
| P-05 | Traceable ranking | Keep the factors behind a recommendation available for testing and support. Keep the consumer card concise: show the offer, eligibility terms and verification context rather than narrating the algorithm. |
| P-06 | Trustworthy deal context | Show applicable terms, location, source and verification context; do not imply unverified participation is confirmed. |
| P-07 | Real-data foundation | Move beyond hard-coded offers to real restaurant/location data, curated genuine deals and a separate synthetic test set. |

A recommended result should communicate restaurant/location, offer, eligibility or purchase terms, distance when available and confidence/verification. Savings remain estimates unless supported as realized savings; calculation method is open. Empty-result and missing-location experiences need design rather than fabricated recommendations.

When no active deal exists, the app still recommends a restaurant from food fit and labels it as a restaurant pick. Future offers do not affect tonight's score or appear inside tonight's result. Home may show the nearest eligible upcoming offer within seven days. An uncertain current offer is presented as a **Possible deal** with last-checked context and a request to confirm with the location; its ranking boost is lower than a verified offer.

Time-limited deals use the restaurant location's local time. A deal may appear before its valid hours on the same day for advance dinner planning, but it stops being an actionable option when fewer than 25 minutes remain. Overnight windows continue past midnight to their stated end time. Expired deals do not appear in recommendations.

Child-age deals are shown only when the saved household establishes that at least one child qualifies. Membership deals may remain visible because a user can choose to join; the requirement is stated clearly. When dependable information exists, details may provide source-dated instructions or an official link for joining through the restaurant app, website or location.

When a nearby deal depends on an unknown audience characteristic, the app may ask a concise contextual question and save the self-reported answer for later recommendations. For example: **“XYZ Diner offers a veteran discount. Does this apply to your household?”** Initial answers are **Yes, me**, **Yes, someone in my household**, **No**, and **Skip**. These questions appear because a relevant opportunity exists rather than as a long onboarding questionnaire. Answers remain editable, unknown or skipped responses never imply eligibility, and the restaurant may still require proof during redemption.

## Consumer journey and learning

The core journey is open app → request tonight's recommendation → see the winner and alternatives → understand the deal terms → choose.

Alternatives are selectable rather than passive detail cards. Selecting an alternative promotes it to the featured position, labels it as the user's selection, and provides the same intent and restaurant-rating actions as the original recommendation. The original algorithmic recommendation remains a distinct signal and returns to the alternatives list.

Pressing “I'll try this deal” saves a dinner plan and returns Home. The Home plan card supports viewing deal details, changing the plan and canceling it. A changed plan replaces the pending check-in; cancellation creates no meal record. A later launch presents the check-in described below.

The broader learning loop is **Recommendation → Intent → Visit → Deal Confirmation/Verification → Feedback**. Optional detail views are engagement events between recommendation and intent. “I'll try this deal” records intent. The later one-question check-in records the person's reported outcome. Preference feedback records whether the restaurant was a good fit. These are separate signals even if one screen collects several answers.

Learning develops from explicit profiles to behavior, then predictive personalization. A “not tonight” action must not silently become a permanent dislike. A failed deal must not automatically become a dislike of the restaurant. Learning weights, decay and conflict handling are TBD.

The tester-ready MVP includes a lightweight feedback loop. Inside each deal tile, users can add or edit a restaurant rating and separately record intent with “I’ll try this deal.” A 1-star rating asks for confirmation because it immediately removes that place from the featured position and recalculates the picks. Ratings 2–5 update future recommendations without unexpectedly replacing the current pick during the same flow. On the next app visit, one check-in asks “Did you try this deal?” with three one-tap outcomes: “Yes — the deal worked,” “Yes — but the deal didn’t work,” and “No.” The card can be dismissed and may return for up to three later app visits. Deal failure remains separate from restaurant preference. This compact MVP interaction does not separately prove or record a restaurant visit. GPS triggers and push reminders are outside this tester-ready scope. The later accepted launch scope below adds an external Open in maps handoff. Event correlation, retry and deduplication contracts remain open under Q-06 in [DECISIONS](DECISIONS.md).

Home acts as a personal dinner dashboard. Its history is meal based: a completed check-in with a worked or did-not-work outcome creates a meal record, while selections, usefulness votes and “No” responses do not. The initial dashboard reports meals recorded, deals that worked and unique places visited, followed by recent meal outcomes. Estimated savings is deferred until it can be calculated from dependable data without requiring burdensome user entry.

## Planned and future experiences

### Accepted launch scope — October 2, 2026

These requirements are agreed but not implemented. They extend the working local prototype into a shared MVP; backend and authentication technology are still undecided.

- Guests can receive personalized recommendations, declare plan intent and complete the existing meal/deal outcome check-in, including worked or did not work. An account is not required for onboarding.
- Only signed-in users can submit deals or report problems. Sign-in is available in the menu and when entering deal submission. Signing in preserves the guest profile and history; provider, recovery and cross-device behavior need further design.
- Deal submission initially requires only restaurant/location selection or missing-place details, plus a short offer description. Photos/menu uploads and mandatory supporting evidence are deferred.
- Every submission is reviewed before visibility. Review matches existing restaurant/location records, resolves uncertain addresses and duplicate offers, and fills appropriate deal terms, schedule and eligibility. Missing material information causes a hold for clarification, not publication. Preserve the original contribution and review notes.
- My submissions shows Pending, Needs clarification, Approved or Rejected, with reviewer notes and a way to update/resubmit held entries. Exact screen copy is not final.
- Review approval allows publication as a Possible deal; approval is not restaurant confirmation. Community check-ins are self-reported evidence, not automatic verification or automatic edits to published terms. Confidence thresholds remain open.
- Signed-in users may report offer ended, incorrect details, wrong location or other, with an optional note. Reports go to review and do not automatically withdraw or alter an offer.
- Place/deal details includes Open in maps for the specific location, with Copy address as a fallback if no maps app can open it. Guests may use it. This is an external handoff, not in-app maps, GPS tracking or a visit/intent signal.
- Favorites, automated approval (including trusted-contributor shortcuts), photo/menu uploads, GPS tracking and push notifications are outside launch scope. Revisit automation after review tests provide evidence.

Detailed future UX and operational choices below are subordinate to this accepted launch subset.

### Additional accepted MVP rules — October 2, 2026

- Offline mode is deferred until after launch. The shared MVP may require internet for recommendations and community actions and should show a clear connection-unavailable message. Existing Room prototype storage remains; cached offline recommendations and queued submissions are not launch commitments.
- One self-reported failed-deal check-in flags an offer for review but does not automatically hide it or change restaurant preference. Automated aggregation/withdrawal thresholds remain undecided.
- Reviewer-confirmed ended offers leave new recommendations, while historical meal records retain the original offer details.
- Existing plans retain the chosen place when an offer becomes unavailable. An elapsed time window says “This offer has ended for today”; a reviewer-withdrawn offer says “This offer is no longer available.” Offer a Choose another place action, without silently replacing the plan. Users can still report earlier use. The 25-minute cutoff excludes new actionable recommendations; an already selected offer is not marked ended until its actual end time.
- Completed meals allow Edit outcome. Corrections update dashboard totals and derived reliability signals while retaining the original response for traceability; superseded responses must not count as additional confirmations.
- After No/did not try, show brief Undo. Keep Edit last check-in in Home's check-in area until the user selects their next dinner plan. Reopen the original choices even after the offer expires; this is historical correction, not re-recommendation. No creates no meal-history entry/count. A correction to Yes creates the appropriate meal. No Recent plans section is needed. Correcting an existing meal to No removes it from meal totals/history without deleting response provenance.

These rules are accepted requirements, not claims that the current UI implements correction or unavailable-plan handling.

- **Planned:** in-app explore/map, richer community verification and behavioral learning. The accepted launch subset above includes reviewed text submissions and an external maps handoff.
- **Future:** permission-based proximity to improve visit prompts; predictive personalization; multiple dining contexts such as solo/date night/family; personal rewards, loyalty points, punch cards, birthday/free-item rewards and expiration reminders; opt-in email parsing and restaurant integrations.
- Manual rewards entry and a savings dashboard appeared in early sketches. They are retained as ideas with unresolved release scope, not mandatory MVP features. Deep rewards integrations are outside MVP.
- Notifications are a possible way to collect follow-up feedback. “30 minutes later” was an example, not an approved timing rule. Channel, consent, frequency and timing remain TBD.

## Merchant and business direction

Consumer value comes first. A future merchant platform may allow restaurant claims, location management, offer creation/submission, verification, targeted promotions, loyalty/reward programs and lead-generation analytics.

Analytics must distinguish eligible audience, impressions, engagements, intent, user-confirmed visits and deal confirmations. Household size is potential dining context, not proof of actual diners. User-confirmed deal use is not automatically a merchant-verified transaction.

MVP has no paid ranking. Future sponsored placements should be identified and remain relevant: “relevant first, sponsored second.” Monetization, attribution rules and merchant reporting are unresolved.

## Success and validation

The established UX aspiration is approximately 30 seconds from opening the app to a confident dining decision. Measure this with returning users who already have a household and Food Profile, using representative restaurant and deal data. Start timing when Home is visible and stop when the person confirms a dinner plan. Record completion time, whether help was needed, and whether the person accepted the recommendation or selected an alternative. Report the share completed within 30 seconds rather than treating the product owner's own walkthrough as user validation. Onboarding is measured separately.

The current prototype appears short enough in owner walkthroughs, but the 30-second aspiration remains unvalidated until representative data and external testers are available. The minimum tester sample and acceptable completion rate will be chosen after an initial pilot rather than invented from prototype-only evidence.

Working validation scenarios: compare recommendations for family and solo profiles; enforce location/time/eligibility constraints; inspect the internal score components; distinguish unverified participation; demonstrate data changes without changing hard-coded app offers. Detailed acceptance thresholds require review.

Scope is maintained in [ROADMAP](ROADMAP.md); unresolved choices are maintained in [DECISIONS](DECISIONS.md).


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
