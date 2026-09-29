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

## Consumer journey and learning

The core journey is open app → request tonight's recommendation → see the winner and alternatives → understand the deal terms → choose.

Alternatives are selectable rather than passive detail cards. Selecting an alternative promotes it to the featured position, labels it as the user's selection, and provides the same intent and restaurant-rating actions as the original recommendation. The original algorithmic recommendation remains a distinct signal and returns to the alternatives list.

Pressing “I'll try this deal” saves a dinner plan and returns Home. The Home plan card supports viewing deal details, changing the plan and canceling it. A changed plan replaces the pending check-in; cancellation creates no meal record. A later launch presents the check-in described below.

The broader learning loop is **Recommendation → Intent → Visit → Deal Confirmation/Verification → Feedback**. Optional detail views are engagement events between recommendation and intent. “I'll try this deal” records intent. The later one-question check-in records the person's reported outcome. Preference feedback records whether the restaurant was a good fit. These are separate signals even if one screen collects several answers.

Learning develops from explicit profiles to behavior, then predictive personalization. A “not tonight” action must not silently become a permanent dislike. A failed deal must not automatically become a dislike of the restaurant. Learning weights, decay and conflict handling are TBD.

The tester-ready MVP includes a lightweight feedback loop. Inside each deal tile, users can add or edit a restaurant rating and separately record intent with “I’ll try this deal.” A 1-star rating asks for confirmation because it immediately removes that place from the featured position and recalculates the picks. Ratings 2–5 update future recommendations without unexpectedly replacing the current pick during the same flow. On the next app visit, one check-in asks “Did you try this deal?” with three one-tap outcomes: “Yes — the deal worked,” “Yes — but the deal didn’t work,” and “No.” The card can be dismissed and may return for up to three later app visits. Deal failure remains separate from restaurant preference. This compact MVP interaction does not separately prove or record a restaurant visit. GPS triggers, push reminders and native navigation are outside this tester-ready scope. Event correlation, retry and deduplication contracts remain open under Q-06 in [DECISIONS](DECISIONS.md).

Home acts as a personal dinner dashboard. Its history is meal based: a completed check-in with a worked or did-not-work outcome creates a meal record, while selections, usefulness votes and “No” responses do not. The initial dashboard reports meals recorded, deals that worked and unique places visited, followed by recent meal outcomes. Estimated savings is deferred until it can be calculated from dependable data without requiring burdensome user entry.

## Planned and future experiences

- **Planned:** explore/map with nearby active deals, detail views, navigation, lightweight community verification, user submissions subject to validation, and behavioral learning. Launch inclusion of map and navigation is unconfirmed.
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
