# Deal lifecycle

Version 0.2 · Working design · September 26, 2026

## Established flow

**Discovery/Evidence → Candidate → Enrichment → Validation → Publication → Monitoring → Retirement**

This is the trust-building process, not merely an expiration date. The data platform stores state and history; the lifecycle engine applies transitions; internal operations lets authorized people inspect and intervene. The first implementation can be manual and simple.

| Stage | Purpose | Information to preserve |
|---|---|---|
| Discovery/Evidence | Record an observation from any source | Source/reference, observed and recorded times, raw claim, location context and actor if known. |
| Candidate | Group a possible offer for review | Original evidence links, tentative identity, possible duplicates and uncertainties. |
| Enrichment | Structure and investigate the claim | Terms, recurrence/time, eligibility, restaurant/location match, proposed scope and unresolved facts. |
| Validation | Assess support for publication and applicability | Supporting/conflicting evidence, decision, reviewer/process, rationale and remaining uncertainty. |
| Publication | Expose a vetted representation to consumers | Published version, scope, location applicability, confidence and source/verification context. |
| Monitoring | Reassess freshness and outcomes | New sources, user reports, conflicts, checks and changes to confidence/applicability. |
| Retirement | Stop serving an offer when no longer suitable | Reason, time, actor/process and retained history. |

Stage names are established; exact persisted statuses, transition contracts and approval thresholds remain a working design. Monitoring may coexist with published status rather than replace it. Publication does not mean universal location confirmation.

## Source-agnostic trust boundary

Manual research, a user report, search, a crawl, AI extraction or a future merchant submission all create evidence/candidates. None bypass validation because of the source's technology or identity. AI can help structure messy information; the model itself is not the authoritative source. Automation never equals truth.

Example: a user reports an offer at one McDonald's location. That supports a local observation at a particular time. It does not establish a national promotion or confirm neighboring outlets. Enrichment may locate broader supporting evidence; validation then assesses scope separately from each location's known participation. Restaurant names and offers here are hypothetical examples, not live deals.

## Working transition rules for review

- Retain discovery before the canonical deal exists; link evidence through promotion.
- Route incomplete or conflicting candidates back to enrichment/review. Do not fill missing facts with invented terms.
- Allow reject and merge outcomes with reasons and original evidence retained. Rejected candidates need not become published deals.
- Record who or what approved a publication, supporting evidence and version. Distinguish human and automated approval when automation is introduced.
- After publication, new evidence may revise scope/applicability/confidence or trigger revalidation/retirement. The exact withdrawal/suspension mechanism is TBD.
- Retire expired, withdrawn or invalid offers without deleting their evidence or behavioral references. Reactivation versus a new version requires a decision.

These rules operationalize the agreed preservation/trust principles. Exact permissions, required fields, conflict precedence and automated gates need review before coding.

## Independent dimensions

1. **Lifecycle:** where the offer is in the workflow.
2. **Scope:** claimed and validated geographic/business breadth.
3. **Applicability:** whether a specific location is included, excluded or unknown.
4. **Confidence/freshness:** strength and recency of evidence.
5. **User eligibility and current validity:** whether the offer fits this user and this time.

A published deal can be confirmed at Location A and unknown at Location B. Unknown participation must not be displayed as confirmed. An unknown location may be recommended as a **Possible deal** with a **Confirm with this location** prompt. A confirmed exclusion is not shown. This consumer policy does not change the underlying applicability state.

User verification records availability and honoring separately. Worked and did-not-work reports add location-specific, time-specific evidence and may adjust confidence as evidence accumulates. A negative report must not erase prior confirmations or automatically establish chain-wide invalidity. Confidence formula, age decay, weighting, minimum sample size and response to conflicts are TBD. Do not invent numeric confidence from raw counts without an agreed method.

## Operations and environments

The envisioned internal tool exposes candidate queues, enrichment/validation work, pending publication, approved/published offers, monitoring issues and retired records, with linked evidence. Operators can approve, reject, merge and retire under defined permissions. Restaurant, user and eventual merchant management share the platform rather than create disconnected stores.

Exercise the lifecycle in dev and QA/staging with synthetic deals and messy cases before promoting code to prod. Synthetic labels are retained through every transition. A successful QA publication must never publish that test offer to real consumers.

## Review checks and open questions

Check that a single-location report cannot silently become national truth; duplicate candidates retain provenance; published deals preserve unknown applicability; failed verification remains distinct from poor preference fit; retirement removes consumer eligibility while retaining history; and test offers cannot cross environments.

Before operational implementation, settle Q-02 through Q-05 and Q-09 in [DECISIONS](DECISIONS.md): scope inheritance, publication/confidence policy, terms/eligibility semantics, sourcing, and permission/transition design. Manual workflow remains the initial direction; automated discovery, validation and approval are later capabilities, with timing TBD.
