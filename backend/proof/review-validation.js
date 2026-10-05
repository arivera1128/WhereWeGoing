'use strict';
function reviewApprovalMissing(d) {
 const missing=[];
 if(!d.restaurant_id && !d.restaurant_name?.trim())missing.push('Restaurant name or an existing restaurant selection');
 if(!d.location_id && !d.address?.trim())missing.push('Location address or an existing location selection');
 if(!d.offer?.trim())missing.push('Offer wording');
 if(!d.terms?.trim())missing.push('Eligibility and conditions');
 if(!d.source?.trim())missing.push('Source / evidence reference');
 if(!Array.isArray(d.days)||!d.days.length)missing.push('At least one weekday');
 if(!d.weekly_only)missing.push('Confirm this is a recurring weekly, all-day offer');
 if(!d.details_checked)missing.push('Confirm you checked the location, terms and duplicate offers');
 return missing;
}
if(typeof module!=='undefined')module.exports={reviewApprovalMissing};
