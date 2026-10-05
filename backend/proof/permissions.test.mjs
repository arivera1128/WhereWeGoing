import { PGlite } from '@electric-sql/pglite';
import { readFile } from 'node:fs/promises';
import assert from 'node:assert/strict';

// Embedded PostgreSQL verifies SQL/grants. Hosted Supabase JWT/API checks remain a separate gate.
const db = new PGlite();
const reviewer = '50000000-0000-4000-8000-000000000001';
const other = '50000000-0000-4000-8000-000000000002';
const version = '40000000-0000-4000-8000-000000000001';
try {
  await db.exec(`
    create role anon; create role authenticated;
    create schema auth;
    create table auth.users(id uuid primary key);
    create function auth.uid() returns uuid language sql as
      $$ select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid $$;
    create function auth.jwt() returns jsonb language sql as
      $$ select coalesce(nullif(current_setting('request.jwt.claims',true),''),'{}')::jsonb $$;
    insert into auth.users values ('${reviewer}'),('${other}');
  `);
  for (const file of ['001_catalog.sql','002_import.sql','003_test_offers.sql','004_app_catalog.sql','005_tester_submissions.sql','006_place_search.sql','007_review_queue.sql']) {
    await db.exec(await readFile(new URL(file, import.meta.url), 'utf8'));
  }
  // Reimporting the fixture must not duplicate or overwrite records.
  await db.exec(await readFile(new URL('002_import.sql', import.meta.url), 'utf8'));
  assert.equal((await db.query('select count(*)::int n from ww_proof.location')).rows[0].n,3);
  await db.query('insert into ww_proof.reviewer values ($1)', [reviewer]);
  async function as(role, actor, sql) {
    await db.exec('begin');
    try {
      await db.query("select set_config('request.jwt.claim.sub',$1,true)", [actor ?? '']);
      await db.exec(`set local role ${role}`);
      const result = await db.query(sql);
      await db.exec('commit');
      return result;
    } catch (error) { await db.exec('rollback'); throw error; }
  }
  const read = 'select public.ww_proof_catalog() catalog';
  const publish = `select public.ww_proof_publish('${version}'::uuid)`;
  assert.deepEqual((await as('anon',null,read)).rows[0].catalog.offers,[]);
  await assert.rejects(as('anon',null,publish), /permission denied/);
  await assert.rejects(as('authenticated',other,publish), /Reviewer access required/);
  await assert.rejects(as('authenticated',other,`insert into ww_proof.reviewer values ('${other}')`), /permission denied/);
  await assert.rejects(as('authenticated',reviewer,'select * from ww_proof.deal_version'), /permission denied/);
  await as('authenticated',reviewer,publish);
  const catalog = (await as('anon',null,read)).rows[0].catalog;
  assert.equal(catalog.environment,'DEV'); assert.equal(catalog.test_data,true);
  const appCatalog=(await as('anon',null,'select public.ww_app_catalog() catalog')).rows[0].catalog;
  assert.equal(appCatalog.complete,true); assert.equal(appCatalog.restaurants.length,3);
  assert.equal(appCatalog.locations.length,3); assert.deepEqual(appCatalog.offers,[]);
  assert.equal(appCatalog.excluded_test_offer_count,1);
  assert.equal(catalog.offers.length,1); assert.equal(catalog.offers[0].deal_version_id,version);
  assert.equal((await db.query('select count(*)::int n from ww_proof.publication_audit')).rows[0].n,1);
  await assert.rejects(as('authenticated',reviewer,publish), /Draft version not found/);
  await assert.rejects(db.exec(`update ww_proof.deal_version set offer='TEST ONLY changed' where deal_version_id='${version}'`), /immutable/);
  await assert.rejects(db.exec(`delete from ww_proof.deal_version where deal_version_id='${version}'`), /cannot be deleted/);
  await db.exec(`update ww_proof.deal_location set applicability='EXCLUDED' where deal_version_id='${version}'`);
  assert.equal((await as('anon',null,read)).rows[0].catalog.offers.length,0);
  await db.exec(`update ww_proof.deal_location set applicability='UNKNOWN' where deal_version_id='${version}';
    update ww_proof.deal_version set status='RETIRED' where deal_version_id='${version}'`);
  assert.equal((await as('anon',null,read)).rows[0].catalog.offers.length,0);
  const submission='60000000-0000-4000-8000-000000000001';
  const submit=`select public.ww_submit_deal('${submission}',null,null,'Fixture place, Elk Grove','Fixture submission') id`;
  await assert.rejects(as('anon',null,submit), /permission denied/);
  await as('authenticated',other,submit); await as('authenticated',other,submit);
  assert.equal((await db.query('select count(*)::int n from ww_proof.submission')).rows[0].n,1);
  assert.equal((await as('authenticated',other,'select public.ww_my_submissions() entries')).rows[0].entries.length,1);
  assert.equal((await as('authenticated',reviewer,'select public.ww_my_submissions() entries')).rows[0].entries.length,0);
  await assert.rejects(as('authenticated',reviewer,submit), /identity conflict/);
  await assert.rejects(as('authenticated',other,'select * from ww_proof.submission'), /permission denied/);
  await assert.rejects(as('authenticated',other,`update ww_proof.submission set status='APPROVED'`), /permission denied/);
  await assert.rejects(as('authenticated',other,`select public.ww_submit_deal('60000000-0000-4000-8000-000000000002','10000000-0000-4000-8000-000000000002','20000000-0000-4000-8000-000000000001','Wrong location','Offer')`), /Invalid location/);
  assert.deepEqual((await as('anon',null,'select public.ww_app_catalog() catalog')).rows[0].catalog.offers,[]);
  const literal="'); DROP TABLE ww_proof.submission; --";
  await db.exec('begin');
  await db.query("select set_config('request.jwt.claim.sub',$1,true)",[other]);
  await db.exec('set local role authenticated');
  await db.query('select public.ww_submit_deal($1,$2,$3,$4,$5)',['60000000-0000-4000-8000-000000000003',null,null,'Fixture place',literal]);
  await db.exec('commit');
  assert.equal((await db.query('select offer from ww_proof.submission where submission_id=$1',['60000000-0000-4000-8000-000000000003'])).rows[0].offer,literal);
  await assert.rejects(as('authenticated',null,submit), /Authentication required/);
  const search=async q=>(await db.query('select public.ww_search_places($1) entries',[q])).rows[0].entries;
  assert.equal((await search(' chEv ')).length,1);
  assert.equal((await search('Chev'))[0].restaurant_id,'10000000-0000-4000-8000-000000000001');
  assert.deepEqual(await search('C'),[]);
  assert.deepEqual(await search('%%'),[]);
  assert.deepEqual(await search("'; DROP TABLE ww_proof.restaurant; --"),[]);
  await db.query("update ww_proof.location set status='INACTIVE' where location_id='20000000-0000-4000-8000-000000000001'");
  assert.deepEqual(await search('Chev'),[]);
  await db.query("update ww_proof.location set status='ACTIVE' where location_id='20000000-0000-4000-8000-000000000001'");
  await db.exec(`insert into ww_proof.location(location_id,restaurant_id,import_key,address,source_note) select gen_random_uuid(),'10000000-0000-4000-8000-000000000001','test-search-'||n,'Fixture location '||n,'TEST ONLY' from generate_series(1,15) n`);
  assert.equal((await search('Chev')).length,10);
  assert.equal((await as('anon',null,"select public.ww_search_places('Chev') entries")).rows[0].entries.length,10);

  async function review(id, revision, action, draft, actor=reviewer) {
    await db.exec('begin');
    try {
      await db.query("select set_config('request.jwt.claim.sub',$1,true)",[actor]);
      await db.exec('set local role authenticated');
      const result=await db.query('select public.ww_review_submission($1,$2,$3,$4) result',[id,revision,action,JSON.stringify(draft)]);
      await db.exec('commit');return result.rows[0].result;
    } catch(error) { await db.exec('rollback');throw error; }
  }
  const queue="select public.ww_review_queue('PENDING',0) entries";
  await assert.rejects(as('anon',null,queue),/permission denied/);
  await assert.rejects(as('authenticated',other,queue),/Reviewer access required/);
  assert.equal((await as('authenticated',reviewer,queue)).rows[0].entries.length,2);
  const draft={restaurant_id:'10000000-0000-4000-8000-000000000001',location_id:'20000000-0000-4000-8000-000000000001',offer:'Reviewed fixture offer',terms:'Fixture conditions',days:[2,4],for_kids:false,savings_rank:1,source:'Isolated test evidence',note:'Please confirm conditions',weekly_only:true,details_checked:true,test_data:false};
  await assert.rejects(review(submission,0,'SAVE',draft,other),/Reviewer access required/);
  await review(submission,0,'SAVE',draft);
  assert.equal((await db.query('select offer from ww_proof.submission where submission_id=$1',[submission])).rows[0].offer,'Fixture submission');
  await assert.rejects(review(submission,0,'APPROVE',draft),/Review changed/);
  await review(submission,1,'CLARIFY',draft);
  assert.equal((await as('authenticated',other,'select public.ww_my_submissions() entries')).rows[0].entries.find(x=>x.id===submission).status,'NEEDS_CLARIFICATION');
  await assert.rejects(review(submission,2,'APPROVE',{...draft,weekly_only:false}),/weekly all-day/);
  await assert.rejects(review(submission,2,'APPROVE',{...draft,days:[9]}),/check constraint/);
  assert.equal((await db.query('select count(*)::int n from ww_proof.review_offer')).rows[0].n,0);
  const approved=await review(submission,2,'APPROVE',draft);
  const published=(await as('anon',null,'select public.ww_app_catalog() catalog')).rows[0].catalog.offers;
  assert.equal(published.length,1);assert.equal(published[0].id,approved.version_id);assert.equal(published[0].verified,false);assert.deepEqual(published[0].days,[2,4]);
  await assert.rejects(review(submission,3,'SAVE',draft),/finalized/);
  await assert.rejects(db.query('update ww_proof.review_schedule set weekday=3 where version_id=$1',[approved.version_id]),/immutable/);
  await assert.rejects(db.query('update ww_proof.deal_version set test_data=true where deal_version_id=$1',[approved.version_id]),/immutable/);
  await assert.rejects(as('authenticated',other,"select public.ww_review_place_offers('20000000-0000-4000-8000-000000000001')"),/Reviewer access required/);
  assert.equal((await as('authenticated',reviewer,"select public.ww_review_place_offers('20000000-0000-4000-8000-000000000001') entries")).rows[0].entries.length,1);
  const duplicate='60000000-0000-4000-8000-000000000006';
  await db.query("insert into ww_proof.submission(submission_id,auth_user_id,place_details,offer) values($1,$2,'Same place','Duplicate')",[duplicate,other]);
  await assert.rejects(review(duplicate,0,'APPROVE',draft),/Matching published offer exists/);
  const testEntry='60000000-0000-4000-8000-000000000004';
  await db.query("insert into ww_proof.submission(submission_id,auth_user_id,place_details,offer,test_data) values($1,$2,'Panda test entry','Original test offer',true)",[testEntry,other]);
  const newPlace={...draft,restaurant_id:null,location_id:null,restaurant_name:'Panda test restaurant',address:'Fixture address',test_data:true};
  const testApproval=await review(testEntry,0,'APPROVE',newPlace);
  assert(testApproval.test_data);
  const after=(await as('anon',null,'select public.ww_app_catalog() catalog')).rows[0].catalog;
  assert.equal(after.offers.length,1);assert.equal(after.restaurants.length,3);
  const marked='60000000-0000-4000-8000-000000000005';
  await db.query("insert into ww_proof.submission(submission_id,auth_user_id,place_details,offer,test_data) values($1,$2,'Another test','Test',true)",[marked,other]);
  await assert.rejects(review(marked,0,'SAVE',draft),/classification cannot be removed/);
  await review(marked,0,'REJECT',{...newPlace,note:'Test rejected'});
  assert.equal((await db.query('select count(*)::int n from ww_proof.review_audit')).rows[0].n,5);
  await db.exec(await readFile(new URL('./008_development_catalog.sql',import.meta.url),'utf8'));
  const migrated=(await as('anon',null,'select public.ww_app_catalog() catalog')).rows[0].catalog;
  assert.equal(migrated.offers.length,2);
  assert(migrated.offers.some(o=>o.id===testApproval.version_id));
  assert(migrated.offers.some(o=>o.id===approved.version_id));
  assert.equal(migrated.restaurants.length,4);
  assert.equal((await db.query("select count(*)::int n from information_schema.columns where table_schema='ww_proof' and column_name='test_data'")).rows[0].n,0);
  await assert.rejects(review(duplicate,0,'APPROVE',draft),/Matching published offer exists/);
  await assert.rejects(as('authenticated',other,"select public.ww_review_queue('PENDING',0)"),/Reviewer access required/);
  await assert.rejects(db.query('update ww_proof.review_schedule set weekday=3 where version_id=$1',[testApproval.version_id]),/immutable/);
  const extra='60000000-0000-4000-8000-000000000007';
  await db.query("insert into ww_proof.submission(submission_id,auth_user_id,place_details,offer) values($1,$2,'Test development entry','TEST ONLY additional offer')",[extra,other]);
  const newApproval=await review(extra,0,'APPROVE',{...draft,offer:'TEST ONLY additional offer'});
  assert((await as('anon',null,'select public.ww_app_catalog() catalog')).rows[0].catalog.offers.some(o=>o.id===newApproval.version_id));
  await assert.rejects(db.query('update ww_proof.deal_version set offer=$1 where deal_version_id=$2',['Edited',newApproval.version_id]),/immutable/);
  console.log('PASS: development migration exposes approved Panda, preserves IDs, removes flags and retains permissions/immutability.');
  console.log('PASS: reviewer-only queue/actions, preserved contribution, stale edits, holds, atomic publication, structured immutability, missing-place creation and test isolation.');

  console.log('PASS: bounded case-insensitive search, literal wildcards, active-only filtering and location IDs.');
  console.log('PASS: SQL-looking input stored as literal data; missing actor rejected.');
  console.log('PASS: private submissions, own-only reads, idempotent retries and mismatched location rejection.');
  console.log('PASS: imports, guest reads, reviewer-only publication, audit, immutability, excluded/retired filtering.');
} finally { await db.close(); }
