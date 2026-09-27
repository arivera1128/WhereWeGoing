Business Requirements Document — Working Draft
Version: 0.1
Product: Working name TBD
Status: Product discovery / MVP definition
Initial platform: Android, with platform-agnostic architecture
1. Product Vision
Help people answer:
“Where should we eat tonight?”

The application recommends restaurants based on current deals, the user's household/profile, preferences, location, rewards, and prior behavior.
The goal is not to create another restaurant directory or coupon database. It is a personalized decision engine designed to get a user from opening the app to a confident dining decision in approximately 30 seconds.
2. Problem
Consumers face two related problems:
Decision fatigue: Families repeatedly have to decide what to eat, particularly during situations such as two parents finishing work, picking up children, discovering nothing was planned for dinner, and needing an easy answer.
Cost resistance: Eating out has become expensive. Consumers may want the convenience of eating out but hesitate because of the cost.
At the same time, opportunities to save money are fragmented across restaurant apps, rewards programs, emails, websites, recurring promotions, coupons and loyalty programs.
Consumers often don't know:
- What deals exist tonight.
- Which deals apply to them.
- Which rewards they already have.
- When rewards expire.
- Which nearby restaurant represents the best value.
- Whether a deal found online is still valid.
The user currently has to do this research themselves.
3. Core Value Proposition
Instead of asking the user to search:
The application does the decision-making work for them.
Example:
BEST FOR YOU TONIGHT
Restaurant A
Kids eat free Wednesdays
2.3 miles away
Estimated family savings: $18
Deal verified 2 days ago
Why this?
Your family likes this restaurant + strong savings + Wednesday-only offer.
The user can accept the recommendation or scroll through alternatives.
4. MVP Scope
The MVP will focus specifically on food and restaurants.
Entertainment, grocery savings, activities and other deal categories may eventually use the same platform, but they are explicitly outside MVP scope.
MVP promise
Help me decide where to eat today based on deals that are relevant to me.

The MVP should prove four capabilities:
1. Reliable deal data
2. Basic user personalization
3. Useful recommendation/ranking
4. A fast decision experience
5. Primary User Persona
Initial persona is modeled around a household such as:
Two adults + two children
Characteristics:
- Cost conscious
- Frequently eats out
- Experiences dinner decision fatigue
- Interested in restaurant promotions
- Doesn't want numerous restaurant apps
- Values convenience
- Has restaurants the family likes/dislikes
- Wants savings without doing extensive research
The system must not assume every user is a family.
For example, a 20-year-old single user without children should receive materially different recommendations.
6. User Profile
Personalization is a fundamental requirement rather than a future enhancement.
Initial profile should remain lightweight.
MVP profile information
- Household size
- Kids: yes/no
- Preferred restaurants
- Restaurants/categories the user doesn't want
- Approximate location
- Preferred driving radius
A potential onboarding interaction:
“Here are restaurants near you. Pick five you like.”

This avoids requiring users to manually enter restaurant names.
More sophisticated preferences can be learned progressively.
7. Recommendation Philosophy
The product should be a decision engine, not an information dump.
Recommendations should consider:
Personal fit
Does this household actually like this restaurant/cuisine?
Savings
What is the estimated financial benefit?
Urgency
Is the promotion only available tonight?
Is it expiring soon?
Distance/convenience
How far does the user need to travel?
Eligibility
Does this particular user qualify?
Examples:
- Kids eat free
- Military
- Senior
- Student
- Rewards member
- New customer
- Birthday
- Other membership requirements
Variety
Has the user eaten here recently?
Historical behavior
Does the user routinely accept or reject this restaurant/category?
Importantly, personal fit can act as a gate.
A huge seafood discount is irrelevant to someone who doesn't eat seafood.
8. “Why This?” Recommendation Explainability
Recommendations should explain themselves.
We identified three useful concepts:
Why this?
Why is this restaurant a good fit?
Why you?
What about the user's profile/preferences makes it relevant?
Why now?
What makes tonight particularly valuable?
Example:
Why now? Kids eat free every Wednesday and you've got a reward expiring Friday.

This builds trust in the recommendation engine.
9. Primary UX
The home experience should optimize for:
30 seconds from opening the app to a confident decision.

The primary interface should feature a large, obvious action similar conceptually to an “Easy Button”:
WHERE SHOULD WE EAT TONIGHT?
The result screen should present:
One clear recommended winner
followed by a small number of strong alternatives that the user can scroll through.
The top recommendation should immediately communicate:
- Restaurant
- Deal
- Estimated savings
- Distance
- Why it was recommended
- Deal confidence/verification
The user should not have to interpret a large directory of restaurants.
10. Explore / Map Experience
Users should also be able to explore rather than accept the recommendation.
A map can display nearby restaurants with active deals.
Selecting a restaurant/deal should expose:
- Deal description
- Applicable dates/times
- Eligibility
- Location
- Estimated savings
- Source
- Date added
- Last verified
- User/community confirmations
- Navigation option
11. Deal Data Model
Each deal should eventually support information including:
Restaurant information
- Restaurant name
- Location
- Cuisine/category
- Price range
Promotion information
- Deal title
- Description
- Deal type
- Estimated savings/value
- Day(s) available
- Start/end time
- Recurring vs. one-time
- Expiration
- Eligibility
- Promo code if applicable
- Dine-in/takeout restrictions
- Location-specific vs. chain-wide
Trust information
- Source
- Date entered
- Last verified
- Community verification
The Dave & Buster's military promotion discussed previously demonstrated why eligibility must exist in the model from the beginning.
12. Data Strategy
There is no single reliable repository containing every restaurant promotion.
Therefore, the platform will likely eventually combine multiple sources.
Early MVP
Curated/manual deal entry.
Subsequent possibilities
- Public restaurant promotions
- Restaurant websites
- User-submitted deals
- Community verification
- Merchant-submitted deals
- Opt-in email offer detection
- Direct restaurant integrations
- Rewards integrations where technically/business feasible
Data freshness is a significant product challenge.
Every deal should therefore have a source and last-verified date.
13. Community Data
Users may eventually be able to submit deals.
For MVP/early versions, submissions should likely require approval rather than immediately becoming authoritative.
Users should also be able to provide extremely lightweight verification:
Did this deal work?
Yes / No
This creates community-powered freshness without requiring users to write reviews.
14. Learning Loop
The application should become more useful as the user interacts with it.
Proposed interaction:
Discover → Choose → Visit → Confirm → Learn
User selects:
“Let's go.”
Later, the application can ask:
Did you go to Restaurant X?

Yes / No
If yes:
Did the deal work?

Yes / No
Potentially followed by a simple positive/negative preference signal.
This produces useful behavioral data without lengthy questionnaires.
Future versions could—with explicit permission—use location/proximity to improve the timing/relevance of visit confirmation.
15. Learning Strategy
The recommendation system should evolve through three broad stages.
Stage 1 — Explicit learning
What users tell us:
- Household
- Kids
- Restaurant preferences
- Distance
- Basic dislikes
Stage 2 — Behavioral learning
What users actually do:
- Recommendations selected
- Restaurants rejected
- Restaurants visited
- Deals successfully redeemed
- Frequency
- Distance actually traveled
Stage 3 — Predictive personalization
The system begins identifying patterns and anticipating what the user is likely to want.
This is strategically important.
The long-term product advantage isn't simply having more restaurant deals. It is knowing which available deal matters to this particular person right now.
16. Rewards
Rewards remain part of the broader vision.
Examples include:
- Loyalty points
- Punch cards
- Free-item rewards
- Birthday rewards
- Expiring restaurant rewards
The long-term experience could combine public deals with personal rewards:
“You have a free sandwich reward here expiring tomorrow.”

Deep integration with restaurant reward systems is technically difficult and is not required for initial MVP validation.
17. Merchant / Partner Vision
Longer term, restaurants may become a second customer of the platform.
Potential merchant capabilities:
- Create promotions
- Manage promotions
- Target relevant consumers
- Run loyalty programs
- Push offers
- Measure promotion engagement
- Potentially operate rewards through the platform
Small and midsize restaurants may particularly benefit because they often lack sophisticated first-party applications.
However:
Consumer value comes first.

The initial strategy is to build something consumers want before attempting to create a large merchant platform.
18. Sponsored Recommendations / Monetization
Partnerships could eventually influence discovery.
However, sponsored content must never destroy recommendation trust.
Core principle:
Relevant first. Sponsored second.

A restaurant should not be able to pay to become the “best recommendation” for someone to whom it is clearly irrelevant.
Sponsored recommendations should eventually be clearly identified as such.
MVP will not use paid ranking.
19. Platform Strategy
Although Android is being used for initial development, this is not intended to become an Android-only product.
The architecture should be platform agnostic.
Principle
Engine first. UI second.

User profiles, deal data, recommendation logic and behavioral learning should eventually live in shared backend services.
Android and iPhone applications should consume the same underlying system.
Therefore, development decisions should avoid unnecessarily coupling business logic to Android.
Initial Android development is a practical learning/MVP strategy rather than a permanent platform limitation.
20. Initial Technical Direction
Current development environment:
- Android Studio
- Kotlin
- Jetpack Compose
- Git
- GitHub
- VS Code
- Codex / AI-assisted development
- Android emulator
Potential future backend:
- Firebase or comparable cloud service
Potential capabilities include:
- User authentication
- Deal database
- User profiles
- Push notifications
- Analytics
- Recommendation services
The backend decision is not yet finalized.
21. Technical Learning Principle
The project will intentionally use AI-assisted coding while maintaining human understanding of the architecture.
For each major technology, document:
What is it?
What does it do in this application?
Why did we choose it?
What alternatives exist?
How much does the developer need to understand personally?
The objective isn't to become an expert Kotlin engineer before building.
It is to understand the system well enough to reason about architecture, inspect AI-generated work, recognize problems, and make informed decisions.
22. Current Development Status
Development environment has successfully been established.
As of this version:
✓ Android Studio installed
✓ Git installed
✓ GitHub account established
✓ Android project created
✓ Pixel 9 XL emulator installed
✓ Default Jetpack Compose application successfully running on emulator
✓ Local project connected to Codex
✓ Platform-agnostic architectural requirement identified
This means we're officially past pure ideation. There is now a running application that we can incrementally turn into the MVP.
23. Open Questions
Major questions we have deliberately not resolved yet include:
- Product name/branding
- Exact onboarding flow
- Exact recommendation scoring formula
- Deal sourcing methodology
- Verification cadence
- Backend selection
- Whether user accounts are required for earliest prototype
- Restaurant/location data provider
- Map provider
- Push notification strategy
- Privacy/location strategy
- Monetization model
- Merchant tools
- How rewards data can eventually be obtained
- Whether cross-platform development should replace native Android development before public launch
- Primary MVP success metric
24. Immediate Next Milestone
We should not start building dozens of features.
The first functional prototype should demonstrate the core product thesis:
User opens app → taps “Where should we eat tonight?” → receives one personalized recommendation based on real deal data → sees why it was recommended → can view alternatives.