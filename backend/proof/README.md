# Shared catalog development proof

This is a small Supabase/PostgreSQL experiment, not the production schema or completed MVP backend. It demonstrates CSV location import, guest reads of published test offers, and reviewer-only publication. It does not migrate household data, change recommendations, implement consumer sign-in, or build a full review queue.

## 1. Create the free development project

1. Open https://supabase.com/dashboard and create an account and a Free organization.
2. Create **WhereWeGoing-dev**, choosing a US West region if available. Save its database password privately. No paid production project is required.
3. Find the project URL and publishable key in the connection/API settings. These client connection values are not administrator credentials. Never place a secret/service-role key or database password in Android, HTML, chat, or Git.

## 2. Prepare and load data

Use only a fresh development project. SQL Editor runs with administrator access: that is suitable for setup but is NOT evidence of reviewer permission enforcement.

Run these files in order in SQL Editor:

1. `001_catalog.sql` once: private proof tables and narrowly granted public functions.
2. `002_import.sql`: generated from restaurants.csv and locations.csv. Run `./backend/proof/build_import.ps1` from the repository root to regenerate it. UUIDs are application-owned, import keys are separate, and insert-only conflict handling never silently overwrites reviewed data. This is a fixture importer, not a general merge tool.
3. `003_test_offers.sql`: three synthetic drafts. They are not actual restaurant deals.

The three addresses are copied from the existing prototype, not freshly verified. All records are development fixtures; no live restaurant data provider was selected. Guest reads initially return zero offers, because no drafts are published.

Only the `public` schema needs Data API exposure; do not expose `ww_proof`. The proof functions are called through `/rest/v1/rpc/ww_proof_catalog` and `/rest/v1/rpc/ww_proof_publish`. If a project disables the Data API, enable it for this development experiment.

## 3. Designate your reviewer

In Authentication → Users, create your development email/password user (or use a suitable existing confirmed development user). Create a second ordinary development user for the deny test. This tests auth without choosing the final consumer sign-in UX or configuring public signup emails.

Copy your reviewer's **Auth user UUID**, then run in SQL Editor:

```sql
insert into ww_proof.reviewer(auth_user_id)
values ('YOUR-REVIEWER-AUTH-USER-UUID'::uuid);
```

The Auth UUID is an operational authorization reference, not the future application's primary user ID. App-user/auth identity linkage remains future work. Never grant review access through user-editable metadata.

## 4. Verify through the browser

Serve this folder locally rather than opening the file directly. For example, with Python available:

```text
python -m http.server 8765 --bind 127.0.0.1 --directory backend/proof
```

Open http://127.0.0.1:8765/review.html. Enter the development URL and publishable key. This page has no third-party scripts and holds login tokens only in memory. It is a developer proof, not a hardened deployed admin portal.

Check in this order:

1. Guest Read: zero offers; drafts are invisible.
2. Ordinary account Sign in → Publish: denied, no catalog change.
3. Sign out → Reviewer Sign in → Publish: succeeds for fixture A.
4. Guest Read: exactly one published TEST ONLY offer.
5. Repeat Publish: denied because the version is no longer a draft; no duplicate audit event.

Publishing approval does not establish verified restaurant participation; fixtures remain UNKNOWN. The proof stores publication audit and prevents changing published wording. Full schedules, structured eligibility, effective-dated applicability, source evidence, submissions, correction history, retirement RPC and production isolation controls remain later work. Do not promote this synthetic proof project into production.

## 5. Verify from Android

Add these entries to the existing root `local.properties` (already ignored by Git), preserving its SDK entry:

```properties
proof.supabase.url=https://YOUR-PROJECT.supabase.co
proof.supabase.publishableKey=YOUR-PUBLISHABLE-KEY
```

Rebuild/run the debug app. Menu → Prototype tools → **Check shared catalog**. Before publication it should show zero; after the browser reviewer test it should show fixture A. Errors are visible, not replaced by sample successes. Release BuildConfig values are blank and the tool is debug-only. The proof tool remains separate from the app catalog. After applying 004 below, the configured debug app imports shared locations into Room.

## Local checks

Install dev dependencies in this folder with `pnpm install --ignore-scripts`, then run `node permissions.test.mjs`. The embedded PostgreSQL test uses stubbed Supabase auth functions to verify role grants, draft invisibility, denied ordinary-user publication and self-promotion, allowed reviewer publication, audit, immutable published terms and excluded/retired filtering. It also checks repeat CSV imports do not duplicate records.

These checks do not verify live Supabase JWT validation, Data API configuration, networking, email delivery or the Android-to-cloud request. The browser/Android steps above are required before declaring the hosted proof complete.

Official references: [access policies](https://supabase.com/docs/guides/database/postgres/row-level-security), [database functions](https://supabase.com/docs/guides/database/functions), [API keys](https://supabase.com/docs/guides/getting-started/api-keys).

## 6. Connect the app catalog

Apply `004_app_catalog.sql` once after 001–003 in the development SQL Editor. This adds the guest-readable `ww_app_catalog` endpoint while retaining the original proof page. It exposes the three imported locations and explicitly excludes TEST ONLY offers. Rebuild the debug app with the existing local configuration. Normal picks now use those locations with no published deals. Prototype tools show refresh status; failure offers Retry and preserves personal records. Do not promote this development project or its fixtures into production.

## 7. Private anonymous tester submissions

Enable Allow anonymous sign-ins under Authentication → Sign In / Providers in WhereWeGoing-dev, then apply `005_tester_submissions.sql` once. Menu → Share a deal creates a separate anonymous Auth identity, keeps the existing local profile and sends private PENDING entries. Known locations use the mapped shared UUIDs; missing places are submitted as text for review. My submissions returns only entries belonging to the current identity. Resetting local app data removes the session; prior submissions remain on the server without recovery in this test.

This step does not add the reviewer queue UI or publish submissions. The original proof publication endpoint cannot publish submitted entries. Review/publication will be the next bounded step. Testers can neither change approval state nor read other contributors’ entries. Hosted identity/submission checks require the two setup steps above. Avoid submitting synthetic offers as real restaurant claims; reviewer checks remain mandatory.

Anonymous auth references: https://supabase.com/docs/guides/auth/auth-anonymous and https://supabase.com/docs/guides/auth/sessions. Public rollout still requires abuse controls; development per-identity intake limits do not prevent repeated account creation.

Optional `node backend/proof/live-tester-check.mjs` checks the configured development project, reading only the local publishable configuration without printing credentials. It creates two anonymous test identities and one TEST ONLY pending entry; do not approve that entry. Repeated runs create additional test identities.

## 8. Search for a submission location

Apply `006_place_search.sql` once after 005. The two-step Android form searches by restaurant-name prefix (minimum two letters), returning at most ten active locations with addresses and UUIDs. This narrow search endpoint is readable by guests and authenticated testers and does not expose private submissions. `node backend/proof/live-search-check.mjs` runs read-only hosted search checks without creating records.

## 9. Review submissions

Apply `007_review_queue.sql` once after 006 in WhereWeGoing-dev. It adds private review drafts, revisions, audit events and structured published offer/schedule records, and marks the owner-identified existing Panda/test contributions as tests. It approves nothing automatically.

Open http://127.0.0.1:8765/review.html on the existing local server. Enter the development URL/publishable key and sign in with the designated reviewer. Choose Pending review, select an entry, edit its separate draft, and search/select an existing location or explicitly create a missing place. Review already-published offers at that location to avoid duplicates. Save draft, request clarification or reject with a note. Approval requires complete supported weekly/all-day details and a source reference, creates immutable published records atomically, and exposes only non-test records through the app catalog as possible offers. Test classification cannot be removed; test-created places also stay excluded.

The original permissions proof is retained at publication-proof.html. Finalized reviews cannot be edited; new-version/retirement tooling, contributor resubmission and timed/date-limited offer support remain follow-up work. The development queue is not a production hosted admin site.

Checks: `node permissions.test.mjs` includes review permissions, optimistic edits, original preservation, clarification, rejection, missing-place creation, duplicate prevention, atomic publication, immutable schedules/test flags and app-catalog isolation. Hosted owner sign-in/approval is a separate gate; do not approve Panda as a real offer.

## Current setup: SQL 008 supersedes test classification

Apply `008_development_catalog.sql` once after 007 in the development SQL Editor, then reload the review page and rebuild/run Android. All supported reviewed approvals, including Panda, enter the normal app catalog. No reapproval is needed. Weekday availability still applies. The original publication proof remains a historical probe; its old fixtures lack reviewed schedule metadata and are not automatically normal app offers.

The migration preserves published IDs and content, removes per-record test flags, and retains reviewer permissions/duplicate checks/immutability. Historical audit JSON is preserved. Launch uses a fresh production database populated only with curated real places and genuine reviewed deals; never promote DEV data/accounts/activity. Earlier 007 test-exclusion instructions above describe the superseded behavior.
