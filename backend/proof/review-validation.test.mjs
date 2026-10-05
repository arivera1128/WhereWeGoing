import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import vm from 'node:vm';
const scope=vm.createContext({});vm.runInContext(await readFile(new URL('review-validation.js',import.meta.url),'utf8'),scope);
const missing=d=>Array.from(scope.reviewApprovalMissing(d));
const draft={restaurant_name:'Panda',address:'Fixture address',offer:'Test',terms:'Fixture',source:'Fixture',days:[2],weekly_only:true,details_checked:true,test_data:true};
assert.deepEqual(missing(draft),[]);assert(missing({...draft,source:''}).includes('Source / evidence reference'));
assert.equal(missing({...draft,weekly_only:false,details_checked:false}).length,2);assert.equal(missing({}).length,8);
console.log('PASS: test offers allowed; missing approval fields and checks identified.');
