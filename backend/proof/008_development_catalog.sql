-- Apply once after 007. Development data is separated by database, not offer flags.
begin;
drop trigger lock_test_classification on ww_proof.deal_version;
drop function ww_proof.lock_test_classification();
create or replace function public.ww_review_queue(p_status text,p_offset integer default 0) returns jsonb language plpgsql security definer set search_path='' as $$
declare result jsonb;
begin
 perform ww_proof.require_reviewer();
 if p_status not in ('PENDING','NEEDS_CLARIFICATION','APPROVED','REJECTED') or p_offset<0 or p_offset>100000 then raise exception 'Invalid queue filter'; end if;
 select coalesce(jsonb_agg(to_jsonb(rows)),'[]'::jsonb) into result from (
 select s.submission_id as id,s.restaurant_id,s.location_id,s.place_details as place,s.offer,s.status,s.reviewer_note as note,s.review_revision as revision,s.review_draft as draft,s.created_at,r.name as restaurant_name,l.address
 from ww_proof.submission s left join ww_proof.restaurant r on r.restaurant_id=s.restaurant_id left join ww_proof.location l on l.location_id=s.location_id where s.status=p_status order by s.created_at,s.submission_id limit 20 offset p_offset) rows;
 return result;
end $$;
create or replace function public.ww_review_submission(p_id uuid,p_revision integer,p_action text,p_draft jsonb) returns jsonb
language plpgsql security definer set search_path='' as $$
#variable_conflict use_variable
declare entry ww_proof.submission; restaurant uuid; location uuid; deal uuid; version uuid; day integer; note text;
begin
 perform ww_proof.require_reviewer();
 select * into entry from ww_proof.submission where submission_id=p_id for update;
 if not found then raise exception 'Submission not found'; end if;
 if entry.review_revision<>p_revision then raise exception 'Review changed; reload the queue'; end if;
 if entry.status in ('APPROVED','REJECTED') then raise exception 'Review is finalized; published changes need a new version'; end if;
 if p_action not in ('SAVE','CLARIFY','REJECT','APPROVE') or p_draft is null or jsonb_typeof(p_draft)<>'object' or length(p_draft::text)>16000 then raise exception 'Invalid review'; end if;
 note:=coalesce(p_draft->>'note','');
 if length(note)>2000 or (p_action in ('CLARIFY','REJECT') and length(trim(note))=0) then raise exception 'A reviewer note is required'; end if;
 if p_action='APPROVE' then
  if coalesce((p_draft->>'details_checked')::boolean,false)=false then raise exception 'Check the location and essential terms before approval'; end if;
  if coalesce((p_draft->>'weekly_only')::boolean,false)=false then raise exception 'This development engine supports weekly all-day offers only; hold other schedules'; end if;
  if length(trim(coalesce(p_draft->>'offer',''))) not between 1 and 2000 or length(trim(coalesce(p_draft->>'terms','')))=0 or length(coalesce(p_draft->>'terms',''))>4000 or length(trim(coalesce(p_draft->>'source','')))=0 then raise exception 'Offer, eligibility/terms and source are required'; end if;
  restaurant:=nullif(p_draft->>'restaurant_id','')::uuid;location:=nullif(p_draft->>'location_id','')::uuid;
  if restaurant is null then
   if length(trim(coalesce(p_draft->>'restaurant_name',''))) not between 1 and 200 then raise exception 'Restaurant name is required'; end if;
   -- Explicit reviewer creation only; look for a same-name record before creating.
   if exists(select 1 from ww_proof.restaurant where lower(trim(name))=lower(trim(p_draft->>'restaurant_name'))) then raise exception 'Restaurant name exists; search and select it'; end if;
   restaurant:=gen_random_uuid();
   insert into ww_proof.restaurant(restaurant_id,import_key,name) values(restaurant,'review-'||restaurant,trim(p_draft->>'restaurant_name'));
  elsif not exists(select 1 from ww_proof.restaurant where restaurant_id=restaurant and status='ACTIVE') then raise exception 'Invalid restaurant'; end if;
  if location is null then
   if length(trim(coalesce(p_draft->>'address',''))) not between 3 and 1000 then raise exception 'Location address is required'; end if;
   if exists(select 1 from ww_proof.location where restaurant_id=restaurant and lower(trim(address))=lower(trim(p_draft->>'address'))) then raise exception 'Location exists; search and select it'; end if;
   location:=gen_random_uuid();
   insert into ww_proof.location(location_id,restaurant_id,import_key,address,source_note) values(location,restaurant,'review-'||location,trim(p_draft->>'address'),p_draft->>'source');
  elsif not exists(select 1 from ww_proof.location where location_id=location and restaurant_id=restaurant and status='ACTIVE') then raise exception 'Invalid location'; end if;
  if jsonb_typeof(p_draft->'days') is distinct from 'array' or jsonb_array_length(p_draft->'days') not between 1 and 7 then raise exception 'Choose weekdays'; end if;
  if exists(select 1 from ww_proof.deal_version v join ww_proof.deal_location a on a.deal_version_id=v.deal_version_id
   where a.location_id=location and a.applicability<>'EXCLUDED' and v.status='PUBLISHED'
   and lower(trim(v.offer))=lower(trim(p_draft->>'offer')) and lower(trim(v.terms))=lower(trim(p_draft->>'terms'))
   and array(select weekday from ww_proof.review_schedule where version_id=v.deal_version_id order by weekday)=array(select distinct value::integer from jsonb_array_elements_text(p_draft->'days') order by value::integer)) then raise exception 'Matching published offer exists; hold or reject the duplicate'; end if;
  deal:=gen_random_uuid();version:=gen_random_uuid();
  insert into ww_proof.deal values(deal,restaurant);
  insert into ww_proof.deal_version(deal_version_id,deal_id,version_number,offer,terms,status,published_at)
   values(version,deal,1,trim(p_draft->>'offer'),trim(p_draft->>'terms'),'DRAFT',null);
  insert into ww_proof.deal_location values(version,location,'UNKNOWN');
  insert into ww_proof.review_offer values(version,p_id,coalesce((p_draft->>'for_kids')::boolean,false),(p_draft->>'savings_rank')::integer,p_draft->>'source',to_char(now(),'YYYY-MM-DD'));
  for day in select value::integer from jsonb_array_elements_text(p_draft->'days') loop
   insert into ww_proof.review_schedule values(version,day); -- range/duplicate checks roll the transaction back
  end loop;
  update ww_proof.deal_version set status='PUBLISHED',published_at=now() where deal_version_id=version;
  insert into ww_proof.publication_audit(deal_version_id,reviewer_id) values(version,auth.uid());
 end if;
 update ww_proof.submission set review_draft=p_draft,review_revision=review_revision+1,reviewer_note=note,
 status=case p_action when 'APPROVE' then 'APPROVED' when 'CLARIFY' then 'NEEDS_CLARIFICATION' when 'REJECT' then 'REJECTED' else status end where submission_id=p_id;
 insert into ww_proof.review_audit(submission_id,reviewer_id,action,draft) values(p_id,auth.uid(),p_action,p_draft);
 return jsonb_build_object('revision',entry.review_revision+1,'version_id',version);
end $$;
create or replace function public.ww_app_catalog() returns jsonb language sql stable security definer set search_path='' as $$
 select jsonb_build_object('schema_version',1,'environment','DEV','complete',true,'market','ELK_GROVE',
 'restaurants',coalesce((select jsonb_agg(jsonb_build_object('id',r.restaurant_id,'key',case r.import_key when 'prototype-chevys' then 'chevys' when 'prototype-dennys' then 'dennys' when 'prototype-smashburger' then 'smashburger' else r.restaurant_id::text end,'name',r.name,'status',r.status)) from ww_proof.restaurant r),'[]'::jsonb),
 'locations',coalesce((select jsonb_agg(jsonb_build_object('id',l.location_id,'restaurant_id',l.restaurant_id,'legacy_key',case l.import_key when 'prototype-chevys-location' then 'chevys' when 'prototype-dennys-location' then 'dennys' when 'prototype-smashburger-location' then 'smashburger' else null end,'address',l.address,'time_zone',l.time_zone,'status',l.status,'source_note',l.source_note)) from ww_proof.location l join ww_proof.restaurant r on r.restaurant_id=l.restaurant_id),'[]'::jsonb),
 'offers',coalesce((select jsonb_agg(jsonb_build_object('id',v.deal_version_id,'deal_id',d.deal_id,'restaurant_id',d.restaurant_id,'location_id',l.location_id,'version',v.version_number,'published_at',floor(extract(epoch from v.published_at)*1000),'offer',v.offer,'terms',v.terms,'days',(select jsonb_agg(weekday order by weekday) from ww_proof.review_schedule where version_id=v.deal_version_id),'for_kids',o.for_kids,'savings_rank',o.savings_rank,'verified',false,'source',o.source,'checked',o.checked))
 from ww_proof.deal_version v join ww_proof.deal d on d.deal_id=v.deal_id join ww_proof.review_offer o on o.version_id=v.deal_version_id
 join ww_proof.deal_location a on a.deal_version_id=v.deal_version_id join ww_proof.location l on l.location_id=a.location_id and l.restaurant_id=d.restaurant_id join ww_proof.restaurant r on r.restaurant_id=d.restaurant_id
 where v.status='PUBLISHED' and l.status='ACTIVE' and r.status='ACTIVE' and a.applicability<>'EXCLUDED'),'[]'::jsonb))
$$;
create or replace function public.ww_proof_catalog() returns jsonb
language sql stable security definer set search_path = '' as $$
  select jsonb_build_object('environment','DEV','test_data',true,'offers',
    coalesce((select jsonb_agg(row_data order by row_data->>'restaurant_name', row_data->>'location_id')
      from (select jsonb_build_object(
        'restaurant_id',r.restaurant_id,'restaurant_name',r.name,
        'location_id',l.location_id,'address',l.address,'source_note',l.source_note,
        'deal_version_id',v.deal_version_id,'offer',v.offer,'terms',v.terms,
        'applicability',dl.applicability) row_data
      from ww_proof.deal_version v
      join ww_proof.deal d on d.deal_id=v.deal_id
      join ww_proof.restaurant r on r.restaurant_id=d.restaurant_id
      join ww_proof.deal_location dl on dl.deal_version_id=v.deal_version_id
      join ww_proof.location l on l.location_id=dl.location_id and l.restaurant_id=r.restaurant_id
      where v.offer like 'TEST ONLY%' and v.status='PUBLISHED' and dl.applicability <> 'EXCLUDED'
        and v.version_number=(select max(v2.version_number) from ww_proof.deal_version v2
          where v2.deal_id=v.deal_id and v2.status='PUBLISHED')
      order by r.name, l.location_id limit 20) visible), '[]'::jsonb))
$$;

create or replace function public.ww_review_place_offers(p_location uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare result jsonb;
begin
 perform ww_proof.require_reviewer();
 select coalesce(jsonb_agg(jsonb_build_object('offer',v.offer,'terms',v.terms,'days',(select jsonb_agg(weekday order by weekday) from ww_proof.review_schedule where version_id=v.deal_version_id))),'[]'::jsonb) into result
 from ww_proof.deal_version v join ww_proof.deal_location a on a.deal_version_id=v.deal_version_id where a.location_id=p_location and a.applicability<>'EXCLUDED' and v.status='PUBLISHED';
 return result;
end $$;

alter table ww_proof.restaurant drop column test_data;
alter table ww_proof.location drop column test_data;
alter table ww_proof.submission drop column test_data;
alter table ww_proof.deal_version drop column test_data;
alter table ww_proof.deal_version add constraint deal_version_offer_length check(length(trim(offer)) between 1 and 2000);
commit;
