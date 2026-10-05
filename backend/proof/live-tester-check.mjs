// Optional live DEV check. Never prints client keys, sessions or actor IDs.
import {readFile} from 'node:fs/promises';
import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
const properties=await readFile(new URL('../../local.properties',import.meta.url),'utf8');
const value=name=>properties.split(/\r?\n/).find(line=>line.startsWith(name+'='))?.slice(name.length+1).replace(/\\:/g,':');
const url=value('proof.supabase.url'); const key=value('proof.supabase.publishableKey');
assert(url && /^https:\/\/[a-z0-9-]+\.supabase\.co\/?$/.test(url));
assert(key && (key.startsWith('sb_publishable_') || JSON.parse(Buffer.from(key.split('.')[1],'base64url')).role==='anon'));
async function call(path,body,token){
 const response=await fetch(new URL(path,url),{method:'POST',redirect:'error',headers:{apikey:key,'Content-Type':'application/json',...(token?{Authorization:`Bearer ${token}`}:{})},body:JSON.stringify(body),signal:AbortSignal.timeout(15000)});
 if(!response.ok)throw new Error(`Development request failed (${response.status}) at ${path.split('?')[0]}`);
 return response.json();
}
const initial=await call('/auth/v1/signup',{});
assert(initial.user.is_anonymous);assert(initial.access_token);assert(initial.refresh_token);
const renewed=await call('/auth/v1/token?grant_type=refresh_token',{refresh_token:initial.refresh_token});
assert.equal(renewed.user.id,initial.user.id);
const id=randomUUID();
const payload={p_id:id,p_restaurant:null,p_location:null,p_place:'TEST ONLY — backend intake verification; not a real restaurant',p_offer:'TEST ONLY — anonymous intake check; never publish'};
assert.equal(await call('/rest/v1/rpc/ww_submit_deal',payload,renewed.access_token),id);
assert.equal(await call('/rest/v1/rpc/ww_submit_deal',payload,renewed.access_token),id);
const entries=await call('/rest/v1/rpc/ww_my_submissions',{},renewed.access_token);
assert.equal(entries.filter(row=>row.id===id).length,1);assert.equal(entries.find(row=>row.id===id).status,'PENDING');
const other=await call('/auth/v1/signup',{});
assert.deepEqual(await call('/rest/v1/rpc/ww_my_submissions',{},other.access_token),[]);
const catalog=await call('/rest/v1/rpc/ww_app_catalog',{});
assert.deepEqual(catalog.offers,[]);
console.log('PASS: live anonymous signup, token refresh, idempotent pending submission, own-only reads and catalog isolation.');
console.log('Created two anonymous development test identities and one clearly labeled TEST ONLY pending entry. Do not approve it.');
