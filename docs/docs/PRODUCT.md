# Product requirements

Version 0.2 · Working design · September 26, 2026

## Vision and problem

Help people answer **“Where should we eat tonight?”** in approximately **30 seconds**. Reduce dinner decision fatigue and cost resistance by recommending relevant restaurant deals for this person or household, nearby, at the right time.

The product is a personalized decision engine. Savings information is fragmented across websites, restaurant apps, emails, recurring promotions and rewards. The application should do the comparison work and explain its recommendation rather than ask users to interpret a large directory.

The initial persona is a cost-conscious household with two adults and two children, often deciding after work and school pickup. This is an example, not a fixed household structure: a single adult without children must receive appropriately different recommendations.

## MVP requirements

| ID | Requirement | Meaning |
|---|---|---|
| P-01 | Food/restaurants only | Entertainment, groceries and general coupons are outside MVP. |
| P-02 | Fast decision experience | A prominent “Where should we eat tonight?” action returns a clear winner and a small set of alternatives. Exact count TBD. |
| P-03 | Lightweight personalization | Household size, children, preferred/excluded restaurants or categories, approximate location and preferred radius inform results. Exact onboarding sequence TBD. |
| P-04 | Relevant ranking | Consider personal fit, eligibility, savings, timing/urgency, distance and eventually variety/behavior. Formula and tie-breakers TBD. Explicit exclusions must not be mistaken for weak preferences. |
| P-05 | Explainability | Show why this restaurant, why it fits this user and why now, grounded in actual inputs. |
| P-06 | Trustworthy deal context | Show applicable terms, location, source and verification context; do not imply unverified participation is confirmed. |
| P-07 | Real-data foundation | Move beyond hard-coded offers to real restaurant/location data, curated genuine deals and a separate synthetic test set. |

A recommended result should communicate restaurant/location, offer, estimated savings when supportable, distance, explanation and confidence/verification. Savings remain estimates unless supported as realized savings; calculation method is open. Empty-result and missing-location experiences need design rather than fabricated recommendations.

## Consumer journey and learning

The core journey is open app → request tonight's recommendation → see the winner and alternatives → understand terms and reasoning → choose.

The broader learning loop is **Recommendation → Intent → Visit → Deal Confirmation/Verification → Feedback**. Optional detail views are engagement events between recommendation and intent. “Let's go” records intent. A later “Did you go?” answer records a visit outcome. “Did the deal work?” records availability/honoring. Preference feedback records whether the recommendation or restaurant was a good fit. These are separate signals even if one screen collects several answers.

Learning develops from explicit profiles to behavior, then predictive personalization. A “not tonight” action must not silently become a permanent dislike. A failed deal must not automatically become a dislike of the restaurant. Learning weights, decay and conflict handling are TBD.

User verification and feedback are established requirements for the broader product. Their exact MVP UI and event-capture scope are unresolved because earlier discussion described an immediate lightweight loop while later modeling placed full event features in Planned scope. See Q-06 in [DECISIONS](DECISIONS.md).

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

The established UX aspiration is approximately 30 seconds from opening the app to a confident dining decision. Define the measurement population, start/end events, onboarding treatment and success threshold before calling it a measured result. Primary MVP success metric is still TBD.

Working validation scenarios: compare recommendations for family and solo profiles; enforce location/time/eligibility constraints; explain the winning result; distinguish unverified participation; demonstrate data changes without changing hard-coded app offers. Detailed acceptance thresholds require review.

Scope is maintained in [ROADMAP](ROADMAP.md); unresolved choices are maintained in [DECISIONS](DECISIONS.md).
