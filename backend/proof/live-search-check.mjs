import { readFile } from 'node:fs/promises';
import assert from 'node:assert/strict';
const config=await readFile(new URL('../../local.properties',import.meta.url),'utf8');
const get=name=>config.split(/\r?\n/).find(s=>s.startsWith(name+'='))?.slice(name.length+1).replace(/\\:/g,':');
const url=get('proof.supabase.url'),key=get('proof.supabase.publishableKey');
assert(/^https:\/\/[a-z0-9-]+\.supabase\.co\/?$/.test(url));
assert(key.startsWith('sb_publishable_') || JSON.parse(Buffer.from(key.split('.')[1],'base64url')).role==='anon');
async function search(query){const response=await fetch(new URL('/rest/v1/rpc/ww_search_places',url),{method:'POST',redirect:'error',headers:{apikey:key,'Content-Type':'application/json',...(!key.startsWith('sb_publishable_')?{Authorization:`Bearer ${key}`}:{})},body:JSON.stringify({p_query:query}),signal:AbortSignal.timeout(15000)});assert.equal(response.status,200);return response.json()}
const rows=await search('chev');assert.equal(rows.length,1);assert.equal(rows[0].name,"Chevy's");assert(rows[0].restaurant_id);assert(rows[0].location_id);assert(rows[0].address);
assert.deepEqual(await search('%%'),[]);assert.deepEqual(await search('no such place'),[]);
console.log('PASS: live shared location search, mapped IDs and literal/no-match queries; no accounts or submissions created.');
