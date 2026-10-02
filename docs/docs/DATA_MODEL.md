# Logical data model

Version 0.2 · Working design · September 26, 2026

This is a business model, **not a Firestore collection specification**. Entity/field names are working vocabulary. Listed fields are candidates, not a finalized schema, required-field list or API contract. MVP/Planned/Future describe capability scope; they do not require one physical table or document per concept.

The incremental database-oriented translation is maintained in [RELATIONAL_MODEL.md](RELATIONAL_MODEL.md). The logical model remains authoritative for business meaning; the relational draft makes keys, foreign keys, nullability and constraints reviewable.

## Core relationships

```mermaid
erDiagram
  Restaurant ||--|{ Location : has
  Restaurant ||--o{ Deal : offers
  Deal ||--o{ DealLocation : applicability
  Location ||--o{ DealLocation : applicability
  User ||--o{ Household : dining_context
  User ||--o{ UserPreference : states
  User ||--o{ RecommendationEvent : receives
  RecommendationEvent ||--o{ IntentEvent : may_prompt
```

Restaurant 1:N Location is established. User-to-household multiplicity is a working extension-friendly representation: the MVP needs a basic household context, not multi-user household collaboration. Exact membership and ownership cardinalities are TBD. Funnel references beyond intent are deliberately optional: observations may exist without a complete preceding funnel.

The Android prototype's current `HouseholdProfile` stores adult count, one current age entry per child, ZIP code and radius. It does not store child names or birth dates. Exact ages support the current 12-and-under child-deal check; future deals need structured eligibility terms rather than parsing offer text.

The relational direction supports both guest and later authenticated use: `app_user` remains stable, an installation can reference the guest, and a future authentication identity links to the same user instead of replacing profile/history. Household access uses a bridge so shared accounts remain possible without requiring them in the MVP.

Each child receives an anonymous household row. The current age-only experience stores entered age plus an as-of date. A future optional full birthday can replace that approximate source for exact calculation, but collection is not assumed and requires PII/privacy review. Child names remain unnecessary.

Restaurant ratings and exclusions belong to individual application users. A rating, “not tried” response and permanent exclusion remain distinct states. Future linked household accounts or dining groups can combine independent profiles through join/context records without moving or merging the underlying preference data; those collaboration features are not part of the current build.

Direct cuisine/style/food-trait preferences and engine-learned affinities are separate. Explicit preferences remain user-authored current state. Learned affinity is a rebuildable, algorithm-versioned projection with evidence counts and never overwrites direct input. Dietary restrictions and allergies require a separate model and are never inferred from restaurant ratings.

Explicit vegetarian/vegan or similar dietary requirements may later become structured hard filters once matching restaurant capability data is dependable. Allergy data is not collected in the current scope; supporting it requires location-specific accommodation/cross-contact evidence, provenance/freshness rules and separate safety messaging rather than ordinary restaurant tags.

Normal recommendation history stores each engine run, the winner and alternatives actually presented, their exact restaurant/location/deal-version references, engine version and internal score components. It does not persist every rejected candidate. User selection remains a later intent event rather than rewriting the engine's original featured option.

## Restaurant and offer domain

| Entity | Scope | Logical identity and candidate fields |
|---|---|---|
| Restaurant | MVP | restaurant_id; name, description, cuisine/category, price level, website, image reference, active status, created/updated times. Business/concept, not a street address. |
| Location | MVP | location_id; restaurant_id; external provider/place references, address, coordinates, local time zone, phone, active status, timestamps. One physical outlet belongs to one Restaurant in the current model. |
| Deal | MVP | deal_id; restaurant_id; title, description, deal type, value estimate, date range, recurring days/time window, expiration, scope, structured eligibility, separate enrollment guidance/link with source and checked time, promo code, dine-in/takeout restrictions, lifecycle status and version/history reference. |
| DealLocation | MVP | Logical unique pair deal_id + location_id; applicability, confidence summary, last verification time and supporting evidence references. Optional surrogate ID is a physical-design choice. |

An independent restaurant with one location and a chain with many use the same model. No separate Brand parent is required. A promotion exists once logically even when many outlets participate. DealLocation is a many-to-many applicability relationship; it must not be replaced by duplicated deals or a location-specific boolean.

### Validated scenario: single-location independent restaurant

**Accepted September 28, 2026.** A local independent restaurant receives one Restaurant record for the business concept and one Location record for its current physical outlet. The Restaurant stores shared identity and classification; the Location stores the address, coordinates, phone and other outlet facts. A deal remains a separate record, with the applicable outlet represented through DealLocation even when there is only one.

This apparent one-to-one case still uses the same one-to-many structure as a chain. If the restaurant opens another outlet, the new Location can be added without changing its Restaurant identity. If it moves, the prior Location can be retired and a new one created so historical visits, evidence and deal applicability continue to refer to the place that actually existed at that time.

The current model associates a deal with one restaurant concept. Multi-restaurant campaigns would need a future decision, not an invented relationship now. Location-specific changes to terms may require versions or related offers; representation TBD.

## Scope, applicability and confidence

Working scope vocabulary: UNKNOWN, ALL_LOCATIONS, PARTICIPATING_LOCATIONS, REGION, LOCATION_SET, SINGLE_LOCATION. Exact enum names and regional representation remain open. “National” describes breadth and does not prove every location participates.

Working per-location applicability values: INCLUDED, EXCLUDED, UNKNOWN. Confidence captures strength/freshness of supporting evidence; it is separate from applicability. A known inclusion may become stale. A published deal may have confirmed locations and unknown locations at the same time.

For verified ALL_LOCATIONS scope, exception-based mappings were proposed to avoid enumerating thousands of locations. Whether and how to inherit applicability, prioritize conflicting evidence and represent inclusions/exclusions is unresolved. Absence of a mapping must not silently mean confirmed participation. UNKNOWN is not EXCLUDED, and neither is a verification event.

### Validated scenario: chain offer with mixed participation

**Accepted September 28, 2026.** One chain Deal may be linked to many locations with different DealLocation applicability values. INCLUDED locations may show the deal as confirmed when the supporting evidence is current. EXCLUDED locations do not show the offer. UNKNOWN locations may show it as a **Possible deal** with **Confirm with this location**, allowing the user to consider a useful lead without presenting uncertain participation as fact.

A user's worked or did-not-work result becomes evidence for the specific location and time involved. It can raise or lower that location's confidence as evidence accumulates, but one report does not confirm or invalidate the offer for the entire chain. The numeric weighting, conflict threshold and freshness decay remain to be defined before production scoring.

### Validated scenario: recurring and time-limited offers

**Accepted September 28, 2026.** Deal availability is evaluated in the physical location's local time zone. A recurring offer may be shown earlier on its valid day so a person can plan ahead, with its operating window visible. An overnight window belongs to its starting day and remains active after midnight until its stated end time. Once a deal is expired, it cannot be recommended, although its record and historical evidence remain available.

An otherwise active deal needs at least **25 minutes remaining** to appear as an actionable tonight option. At 25 minutes or more, the user decides whether the remaining time is practical. Below 25 minutes, the deal is removed from the actionable results because it is unlikely to be useful. The same cutoff applies to households with and without children.

## Evidence and lifecycle domain

| Concept | Scope | Candidate content and relationships |
|---|---|---|
| DealEvidence / source observation | MVP basic; richer ingestion Planned | evidence_id; source type/reference, observation time, recorded time, submitter/actor if known, raw claim/reference, observed location, supporting material. May precede any canonical Deal. |
| DealCandidate | MVP logical workflow | candidate_id; evidence links, proposed restaurant/location, extracted terms, tentative scope, workflow state. Can be rejected, merged or become a published deal. |
| Validation/transition history | MVP basic record; richer automation Planned | change/event ID, candidate/deal reference, old/new state, actor or process, time, rationale, evidence/version references, human/automated method. |
| DealVerificationEvent | Planned consumer feature; source/admin evidence available initially | verification_id; deal/location references, actor if available, time, availability and honored outcomes, verification method and evidence reference. |

DealSource from earlier sketches is consolidated into the evidence/source-observation concept. Do not require a published deal_id merely to store discovery. Multiple evidence records may support one candidate/deal; candidate merge and shared-evidence linkage details are TBD. Preserve links through promotion and merge rather than discard the original observations.

Evidence and meaningful history are append-only in ordinary operations. Corrections add a new observation or superseding record. Current confidence and status can be derived summaries; they do not replace history. Retention, redaction and deletion requirements must be resolved separately before production—append-only is not an indefinite personal-data retention policy.

## Consumer and identity domain

| Concept | Scope | Candidate content |
|---|---|---|
| User | MVP | Stable user_id, created time, approximate home region, preferred radius, onboarding state, active status. Does not imply mandatory sign-in. |
| Household | MVP basic | household_id, owning user reference, household/party size and children information. Adult/child counts were proposed; exact required fields TBD. |
| UserPreference | MVP basic | user reference, restaurant/category target, preference or exclusion, origin and time. |
| UserEligibilityCharacteristic | MVP incremental | user and household context; characteristic type; response of `SELF`, `HOUSEHOLD_MEMBER`, `NOT_ELIGIBLE`, or `UNKNOWN`; prompting deal when applicable; answer/update times; optional recheck time. Collected contextually when a relevant deal makes the question useful. |
| UserOccasion | Planned personalization | user reference; typed recurring month/day such as birthday; answer/update times. Birth year is not required solely for birthday matching. |
| LearnedPreference | Planned | Derived signals with source/provenance, confidence and update time; storage form TBD. Never silently overwrites explicit preference. |
| Identity/account linkage | Separate architectural seam | Application user linked to authentication subject(s) if enabled. Provider, migration and cardinality TBD. |
| Roles/permissions | Planned tool capability; enforcement as needed | Authorized actions and resource scope. Consumer/operator/merchant concepts; exact matrix TBD. |

Eligibility belongs both in offer requirements and relevant user context. Examples include children, military, senior, student, rewards membership, new-customer or birthday conditions. These examples do not mandate collecting every attribute. Exact representation for additional eligibility types and their privacy requirements remains open.

### Validated scenario: age and membership eligibility

**Accepted September 28, 2026.** A child-age offer is shown when at least one saved child satisfies the stated age range. It is hidden when the household has no children, all saved children are outside the range, or the age needed to establish eligibility is missing. The restaurant may still be recommended without that offer.

A membership requirement is treated differently because the user may be willing and able to join. The deal remains visible with a clear term such as **Rewards members only**. When verified guidance exists, the deal may explain how to enroll through the restaurant app, official website or at the location and may link to the official enrollment destination. The requirement and enrollment guidance are stored separately. Guidance retains its source and last-checked time, and the app does not promise immediate enrollment unless current evidence supports that claim.

Other audience requirements include veteran, first responder, healthcare worker, student, educator, senior, birthday and customer-history status. These are not assumed profile fields. When a nearby deal makes one relevant, the app may ask whether it applies to the user or household and persist the self-reported answer for later matching. Unknown, declined and known-ineligible states must not be converted into eligible. Purchase requirements, redemption channels, usage limits and combination exclusions remain separate from audience eligibility.

Expandable characteristics use definition and response rows rather than one nullable column per possible characteristic. Date-based facts such as birthday use typed occasion data so the engine can evaluate a real calendar window. Structured conditions drive matching; published free-text terms, disclaimers and official links remain available for requirements that are not fully machine-evaluable.

Purchase requirements are preserved as classified, immutable deal terms and are not copied into the user profile. Detailed purchase fields are deferred until an accepted query or engine rule needs them; the app does not infer what a person intends to order.

Usage limits are also preserved as classified, immutable deal terms. Detailed counters, quantities and reset periods are deferred because self-reported deal outcomes are not verified redemption accounting.

Exclusions and combination rules remain classified, immutable deal terms unless an accepted structure such as redemption channel already represents the restriction. Detailed stacking logic is deferred while the product recommends one offer at a time and has no checkout integration.

## Logical model acceptance

**Accepted September 28, 2026.** The logical model is approved as the basis for access-pattern review and later physical database design. Acceptance covers the entity boundaries and scenario rules documented here; it does not choose a database, finalize field names, or resolve the remaining questions explicitly marked open.

## Behavioral event domain

Preserve **Recommendation → Intent → Visit → Deal Confirmation/Verification → Feedback** as distinct observations, not a mutable funnel-status field. Full capture timing is open (Q-06); add events when the corresponding interactions exist, without inferring unobserved outcomes.

| Event | Meaning | Candidate fields beyond its own ID/time |
|---|---|---|
| RecommendationEvent | An option presented to the user | user, restaurant/location, deal/version, context, rank, score/components, explanation. Generated-versus-presented event boundary needs definition. |
| EngagementEvent | Optional detail view or other engagement | recommendation reference, action type, user/context. Planned analytics. |
| IntentEvent | User expresses a choice, e.g. “Let's go” | recommendation reference when known, user, location/deal, action. |
| VisitEvent | Evidence or report of visiting/not visiting | user, location, recommendation/intent reference if known, outcome, confirmation method, optional actual party size. |
| DealVerificationEvent | Deal available/honored outcome | deal, location, user/actor, visit reference if known, method and separate availability/honored answers. |
| FeedbackEvent | Preference or recommendation usefulness | relevant user/recommendation/visit/deal references, feedback target and response, situational versus persistent meaning if supplied. |

Earlier DealFeedback sketches combined deal outcomes and preference feedback. The canonical distinction here keeps their meanings separate even if collected in one interaction. Exact storage boundaries are TBD.

One recommendation can have multiple related observations over time. A visit need not follow tracked intent; a verification can arrive without a tracked visit. Do not require synthetic predecessor events. Cardinalities, deduplication, corrections and attribution windows need physical/event-contract review.

Examples: intent YES then visit NO preserves useful intent; visit YES then deal honored NO records a deal-quality failure. Missing response is unknown, not NO. Household size does not establish actual diner count. Location proximity is not proof of redemption.

## Future merchant domain

Reserve seams for merchant identity, restaurant/location claims, permissions, offer submission and aggregate funnel reporting. Detailed entities, ownership verification and reporting thresholds are TBD. Merchant submissions still enter the evidence lifecycle. No merchant system is required for MVP.

## Access patterns to review before physical design

1. Nearby locations by position/radius and supported market.
2. Published offers for represented restaurants, valid at the requested local date/time.
3. Resolve deal scope, location applicability, eligibility and evidence/confidence.
4. Load user/household and explicit preferences for ranking.
5. Show source/verification context and deal details.
6. Record and correlate actual interactions; query learning history when enabled.
7. List lifecycle queues and evidence; perform authorized audited transitions.
8. Future: aggregate merchant funnel results without inflating intent into visits.

Review query volume, latency, read/write cost, geographic indexes, pagination, denormalization, authorization, version references, event retries and environment isolation before choosing collections or tables.

## Scenario checklist

Validate a single-location independent restaurant; one chain deal across many locations; participating locations with unknowns/exclusions; one local report claiming a national offer; recurring and overnight time windows; expired offers; membership or family eligibility; conflicting/stale evidence; rejected/merged candidates; intent without visit; visit without a successful deal; and synthetic-data isolation. Time zones, precedence rules and thresholds are open decisions, not implied by this checklist.
