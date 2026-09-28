# User journey

Version 0.1 · Working design · September 27, 2026

This document records the agreed consumer journey. The first-launch flow through the first recommendation is implemented locally in the Android prototype; [PROJECT_STATUS](../PROJECT_STATUS.md) records broader delivery status.

## Journey principles

- Move a person toward a useful dinner recommendation with minimal typing and few decisions.
- Keep recommendations, user selections, dinner plans and confirmed meal outcomes distinct.
- Do not interpret unfamiliarity as dislike or a dinner plan as a completed meal.
- Use truthful lower-confidence and no-result messages when personalization or deal evidence is weak.
- Do not require an account before the user receives value.

## 1. First launch and location

Welcome and location share one screen. The message explains that location, household information and food preferences will improve nearby recommendations.

The user can choose **Use my location** or **Enter ZIP code**. The prototype implements ZIP entry and presents an honest message that automatic location is not available yet. It does not request Android location permission or pretend to detect a location. Supported Elk Grove ZIP codes continue; unsupported ZIP codes explain the current service area and allow another entry.

An app installation acts as one guest user for the tester-ready prototype. Account creation and synchronization are deferred. A future account can attach local guest information to a durable identity without making identity, household profile and activity the same concept.

## 2. Household setup

The app asks about the people usually included in dinner plans:

1. Number of adults, using quick numeric choices plus Other.
2. Number of children, using quick numeric choices plus Other.
3. Current age of each child, using a selector with no typing.

No child names, birth dates, gender or child accounts are required. Exact current ages support deal eligibility boundaries more accurately than broad ranges. If there are no children, age questions are skipped.

The initial search radius defaults to 10 miles and is editable later from Profile. It is omitted from onboarding. Until a routing provider is added, this represents an approximate geographic radius rather than calculated driving distance.

The prototype stores each child's exact current age. Onboarding and Profile edit the same household record. Current child deals apply a 12-and-under eligibility check; future offers with different boundaries require structured deal-specific eligibility terms.

## 3. Initial food profile

The initial Food Profile asks about five varied places. Each prompt accepts a 1–5 rating or **Haven't tried it**.

- A rating is an explicit opinion about that restaurant and contributes to broader food-theme learning.
- Haven't tried it records no positive or negative opinion, keeps the restaurant eligible and counts as completing the prompt.
- Remove this place is a separate deliberate exclusion.

If several places are unfamiliar, later prompts should favor broadly recognizable places while retaining variety. If all five are unfamiliar, onboarding still completes. The app describes the profile as started with low confidence, provides a recommendation using other available signals and offers Rate more places later. It does not claim an untried restaurant was liked.

After five prompts, a completion state explains that the app will use location, household details and food preferences. **See my first pick** opens the first recommendation. Lower-confidence copy is used when there are no direct ratings.

## 4. Recommendation and selection

Tonight's Picks presents one algorithmic recommendation plus selectable alternatives. Choosing an alternative promotes it to **Your selected deal** and returns the original recommendation to the alternatives. The app preserves which result it recommended and which result the user selected.

The featured deal provides terms/details, verification context, **I'll try this deal**, and the person's current restaurant rating. Rating controls expand inside the tile and save after one tap. Ratings 2–5 do not shuffle the current pick during the same decision flow; they affect later recommendation sessions. Selecting 1 shows a confirmation that the app will find something else; confirming recalculates the complete result and reports that the picks were updated. The consumer screen does not show algorithm narration or deal-usefulness voting.

The page shows up to three alternatives: the next two acceptable scores plus a different cuisine or dining style when available. Each alternative can be promoted and provides the same deal and restaurant-rating controls. A verified strong deal is preferred; weak and true no-result states use the established honest recovery behavior.

If no offer is active today, the page labels the result **Restaurant pick**, hides future offer terms from the tonight flow and says there is no confirmed deal today. The action becomes **I'll try this place**. A later check-in asks whether the person ate there rather than whether a deal worked. A possible current deal remains visible with **Confirm with this location** and its last-checked context.

## 5. Dinner plan

Pressing **I'll try this deal** records intent, saves Tonight's plan and automatically returns Home. It does not record a visit or meal.

Home shows a Tonight's plan card with:

- Restaurant and deal summary
- View deal details
- Change my plan
- Cancel plan

Changing the plan returns to Tonight's Picks with the current choice featured. Confirming a replacement replaces the pending check-in. Canceling removes the pending check-in and creates no meal.

## 6. Return and check-in

On a later app launch, the plan becomes a one-question check-in:

- Yes — the deal worked
- Yes — but the deal didn't work
- No

A worked or did-not-work answer creates a meal-history record. No closes the plan without creating a meal. Dismiss postpones the question, subject to the established three-appearance limit. Deal failure remains separate from restaurant preference.

After one of the three answers is selected, the check-in card changes in place to a short result message. The Home dashboard updates immediately, and the result message clears automatically after about four seconds. This gives confirmation without adding another screen or requiring another tap.

## 7. Home and meal history

Home is a personal dinner dashboard as well as the entry point to Tonight's Picks. It summarizes meals recorded, deals that worked and unique places visited. Recent activity is meal based; selections, restaurant ratings and profile edits do not appear as meals.

Home also shows one secondary upcoming-offer card: the nearest eligible offer within seven days. Tomorrow is called out when applicable. The card opens details, does not provide an intent action before the offer day and does not affect tonight's ranking.

When a dinner plan or pending check-in exists, that card replaces the large **Where should we eat tonight?** action and appears above the upcoming-offer card. Completing or closing the plan/check-in restores the main dinner action.

Estimated savings is deferred because the prototype cannot reliably derive actual order totals, percentage savings or eligibility usage without burdensome user input or dependable transaction data.

## Still to decide

- Returning-user greeting and the placement/timing of an optional Save your profile account prompt.
- Exact account linking, guest-data migration, synchronization and conflict behavior.
- Plan/check-in behavior across dates, time zones and plans left unanswered beyond the prototype appearance limit.
- The exact first-launch visual treatment and accessibility review.

## Repeatable prototype review

Debug builds provide **⚙ Prototype tools** at the bottom of the navigation menu. A tester can simulate a weekday, see the active test state, enter the non-destructive new-user Food Profile preview, or confirm a full local reset. Simulated days display a persistent testing banner and can be returned to the actual day without changing the emulator clock.

### First-time user

1. Clear the app's emulator storage and open the app.
2. Check the Use my location prototype message, then enter a supported Elk Grove ZIP.
3. Select adult and child counts; when children are included, select each age.
4. Answer five Food Profile prompts, including Haven't tried it where useful.
5. Confirm the completion wording matches the amount learned.
6. Select See my first pick and verify one recommendation plus alternatives.
7. Select I'll try this deal and verify Home shows Tonight's plan.

### Returning user

1. Close and reopen an installation that completed onboarding.
2. Verify onboarding is skipped and the saved Food Profile count is retained.
3. If a dinner plan exists, answer or dismiss the check-in.
4. Verify a worked or did-not-work answer updates meal history, while No does not create a meal.
