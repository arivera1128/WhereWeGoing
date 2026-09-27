# Instructions for Codex

## Read before implementation

Read [PRODUCT](docs/PRODUCT.md), [ARCHITECTURE](docs/ARCHITECTURE.md), [DATA_MODEL](docs/DATA_MODEL.md), [DEAL_LIFECYCLE](docs/DEAL_LIFECYCLE.md), [ROADMAP](docs/ROADMAP.md) and [DECISIONS](docs/DECISIONS.md) before implementation or architectural changes. Inspect existing code as well: these documents describe intended direction, not verified implementation status.

Respect the user's current explicit instructions. If code, documents or a new request contradict one another, identify the conflict and its consequences. Ask when a material product or architecture choice is required; do not invent requirements or silently select a conflicting interpretation. Continue independent work that does not depend on that choice.

## Product and scope boundaries

- MVP is food/restaurants. Optimize for “Where should we eat tonight?” and a confident decision in approximately 30 seconds.
- Build a personalized decision engine with one clear winner and a few alternatives, not a nearby-deals dump.
- Use ROADMAP.md as the scope register. Planned and Future capabilities are design seams, not permission to implement them. Unresolved launch assignments must stay unresolved until decided.
- Design the seams now, not all the features now. Make small, understandable changes; do not introduce infrastructure solely because a future capability is documented.

## Architectural invariants

- Initial client: Android, Kotlin, Jetpack Compose. Keep business concepts and backend contracts platform agnostic.
- Restaurant 1:N Location; Deal is separate, with deal scope and DealLocation applicability. Do not add a separate Brand hierarchy without a new decision.
- Discovery creates evidence/candidates. A crawl, AI output, user report or merchant submission is never automatically authoritative publication.
- Preserve evidence, lifecycle history and distinct behavioral events. Do not overwrite intent with visit outcome or equate a visit with successful deal use.
- Keep deal publication, scope, per-location applicability and confidence distinct. Unknown is not confirmed.
- Separate identity/authentication, profiles and activity. Backend authorization must protect operational actions when implemented.
- Isolate dev, QA/staging and production data. Synthetic deals and test users must not enter consumer production flows. Promote reviewed code/configuration, not test data.
- Do not directly translate the logical model into Firestore collections. Validate scenarios, define access patterns, review backend choice and physical design, then implement indexes and justified denormalization.

## Change and verification discipline

Before a material change, explain what changes and why, including affected decisions. Update DECISIONS.md when a material decision is made: date, status, rationale, consequences and affected documents. Preserve superseded decisions and update related docs together. Do not silently change architecture or upgrade suggestions to accepted requirements.

Use real restaurant/location data and clearly labeled synthetic deals for the sandbox, plus a smaller source-verified deal set. Verify external provider terms, permitted storage, costs and access before ingestion; historical chat claims are not current provider documentation.

Test behavior appropriate to the change, especially applicability, eligibility, time windows, environment isolation and event meaning. Report what was tested and any unresolved limitations. Never invent an offer, savings estimate, verification count or confirmed visit to fill missing production data.

Explain major technologies in plain language: what they are, their role here, why chosen, alternatives and what the owner needs to understand. Initial handoff work is a review of these docs and existing code, not an automatic implementation of the roadmap.
