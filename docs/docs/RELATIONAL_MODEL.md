# Relational model working draft

Version 0.1 · Working design · September 30, 2026

This document translates the accepted logical model into a normalized relational reference model. It is intentionally incremental. It does not select PostgreSQL, a hosted service, or the final backend, and the SQL types remain working choices until access patterns and technology are reviewed.

## Accepted conventions

- Major business records use application-owned UUID primary keys.
- External provider identifiers never serve as application primary keys.
- Provider identifiers are stored separately so one location can be connected to multiple sources without changing its identity.
- `location.restaurant_id` is a required foreign key, implementing Restaurant 1:N Location.
- Timestamps should represent an absolute instant, equivalent to SQL `timestamp with time zone`; location time-zone names are stored separately for local deal evaluation.

## Restaurant and location foundation

### restaurant

| Column | Working type | Null? | Key / rule | Purpose |
|---|---|---:|---|---|
| restaurant_id | UUID | No | Primary key | Stable application identity for the restaurant concept. |
| name | Text | No | | Consumer-facing name. Name alone is not unique. |
| description | Text | Yes | | Optional summary. |
| price_level | Small integer | Yes | Check range TBD | Coarse price classification. |
| website_url | Text | Yes | | Official restaurant website when known. |
| image_reference | Text | Yes | | Image or asset reference; storage mechanism TBD. |
| status | Text/code | No | Allowed values TBD | Supports active and retired records without deleting history. |
| created_at | Timestamp with time zone | No | | Record creation instant. |
| updated_at | Timestamp with time zone | No | | Most recent material update instant. |

Cuisine, dining style, and food traits use the typed many-to-many structure below.

### restaurant_attribute

| Column | Working type | Null? | Key / rule | Purpose |
|---|---|---:|---|---|
| attribute_id | UUID | No | Primary key | Stable identity for one classification value. |
| attribute_type | Text/code | No | Unique with `name` | Dimension such as `CUISINE`, `DINING_STYLE`, or `FOOD_TRAIT`. |
| name | Text | No | Unique with `attribute_type` | Value such as Mexican, fast casual, or vegetarian friendly. |
| status | Text/code | No | Allowed values TBD | Allows a value to be retired without breaking history. |
| created_at | Timestamp with time zone | No | | Record creation instant. |
| updated_at | Timestamp with time zone | No | | Most recent material update instant. |

`(attribute_type, name)` must be unique after applying the database's agreed case-normalization rule.

### restaurant_attribute_assignment

| Column | Working type | Null? | Key / rule | Purpose |
|---|---|---:|---|---|
| restaurant_id | UUID | No | Composite primary key; foreign key → `restaurant.restaurant_id` | Restaurant being classified. |
| attribute_id | UUID | No | Composite primary key; foreign key → `restaurant_attribute.attribute_id` | Assigned cuisine, dining style, or food trait. |
| is_primary | Boolean | No | Default false | Marks the leading value within a dimension when useful for display. |
| created_at | Timestamp with time zone | No | | When the assignment was created. |
| updated_at | Timestamp with time zone | No | | Most recent material update instant. |

This many-to-many structure lets one restaurant carry several useful signals without adding columns for each cuisine or trait. Provider provenance and confidence may be added when ingestion rules are defined; they are not implied by the assignment itself.

### location

| Column | Working type | Null? | Key / rule | Purpose |
|---|---|---:|---|---|
| location_id | UUID | No | Primary key | Stable application identity for one physical outlet. |
| restaurant_id | UUID | No | Foreign key → `restaurant.restaurant_id` | Restaurant that operates this outlet. |
| display_name | Text | Yes | | Optional location label, such as a neighborhood. |
| address_line_1 | Text | No | | Street address. |
| address_line_2 | Text | Yes | | Suite or unit. |
| city | Text | No | | Locality. |
| region_code | Text | No | | State or region code. |
| postal_code | Text | No | | ZIP or postal code; stored as text. |
| country_code | Text | No | | Country code. |
| latitude | Decimal | Yes | Range check | Required before distance queries; precision TBD. |
| longitude | Decimal | Yes | Range check | Required before distance queries; precision TBD. |
| time_zone | Text | No | IANA name | Evaluates recurring and overnight offers in local time. |
| phone | Text | Yes | | Location-specific contact number. |
| status | Text/code | No | Allowed values TBD | Supports current, closed, moved, or retired treatment. |
| created_at | Timestamp with time zone | No | | Record creation instant. |
| updated_at | Timestamp with time zone | No | | Most recent material update instant. |

Deleting a restaurant with dependent locations should not be a normal operation. The exact foreign-key delete action will be selected with retention and correction rules; a restrictive action is the current direction.

### location_external_reference

| Column | Working type | Null? | Key / rule | Purpose |
|---|---|---:|---|---|
| location_external_reference_id | UUID | No | Primary key | Application identity for this source mapping. |
| location_id | UUID | No | Foreign key → `location.location_id` | Internal location being identified. |
| provider | Text/code | No | | Source system, such as a restaurant provider. |
| provider_location_id | Text | No | | Identifier assigned by that provider. |
| source_url | Text | Yes | | Direct source reference when applicable. |
| first_seen_at | Timestamp with time zone | No | | When the mapping was first recorded. |
| last_checked_at | Timestamp with time zone | Yes | | Most recent confirmation of the mapping. |

`(provider, provider_location_id)` must be unique. The mapping can be corrected or retired without replacing the internal `location_id` used by deals and history.

## Relationship sketch

```mermaid
erDiagram
  RESTAURANT ||--|{ LOCATION : operates
  LOCATION ||--o{ LOCATION_EXTERNAL_REFERENCE : identified_by
  RESTAURANT ||--o{ RESTAURANT_ATTRIBUTE_ASSIGNMENT : classified_as
  RESTAURANT_ATTRIBUTE ||--o{ RESTAURANT_ATTRIBUTE_ASSIGNMENT : assigned_to
```

## Next review

Add Deal, DealVersion, and DealLocation using the already accepted applicability, timing, eligibility, and evidence rules.
