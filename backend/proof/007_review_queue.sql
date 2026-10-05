-- Development review queue. Apply once after 006. No entries are auto-approved.
begin;
alter table ww_proof.restaurant add column test_data boolean not null default false;
alter table ww_proof.location add column test_data boolean not null default false;
alter table ww_proof.submission add column test_data boolean not null default false;
alter table ww_proof.submission add column review_revision integer not null default 0;
alter table ww_proof.submission add column review_draft jsonb not null default '{}'::jsonb;
-- The owner identified the current Panda contribution and verification fixtures as tests.
update ww_proof.submission set test_data=true where place_details ilike '%panda%' or place_details like 'TEST ONLY%';
alter table ww_proof.deal_version add column test_data boolean not null default true;
alter table ww_proof.deal_version drop constraint deal_version_offer_check;
alter table ww_proof.deal_version add check(length(trim(offer)) between 1 and 2000 and (offer not like 'TEST ONLY%' or test_data));
create table ww_proof.review_offer (
 version_id uuid primary key references ww_proof.deal_version,
 submission_id uuid not null unique references ww_proof.submission,
 for_kids boolean not null, savings_rank integer not null check(savings_rank between 0 and 2),
 source text not null check(length(trim(source))>0),checked text not null
);
create table ww_proof.review_schedule (
 version_id uuid not null references ww_proof.deal_version,weekday integer not null check(weekday between 1 and 7),primary key(version_id,weekday)
);
create table ww_proof.review_audit (
 audit_id uuid primary key default gen_random_uuid(),submission_id uuid not null references ww_proof.submission,
 reviewer_id uuid not null references auth.users(id),action text not null,draft jsonb not null,occurred_at timestamptz not null default now()
);
alter table ww_proof.review_offer enable row level security;
alter table ww_proof.review_schedule enable row level security;
alter table ww_proof.review_audit enable row level security;
revoke all on ww_proof.review_offer,ww_proof.review_schedule,ww_proof.review_audit from public,anon,authenticated;

create function ww_proof.lock_review_content() returns trigger language plpgsql set search_path='' as $$
declare parent uuid;
begin
 if tg_op <> 'INSERT' then
  parent:=old.version_id;
  if exists(select 1 from ww_proof.deal_version where deal_version_id=parent and published_at is not null) then raise exception 'Published review content is immutable'; end if;
 end if;
 if tg_op <> 'DELETE' then
  parent:=new.version_id;
  if exists(select 1 from ww_proof.deal_version where deal_version_id=parent and published_at is not null) then raise exception 'Published review content is immutable'; end if;
  return new;
 end if;
 return old;
end $$;
create trigger lock_review_offer before insert or update or delete on ww_proof.review_offer for each row execute function ww_proof.lock_review_content();
create trigger lock_review_schedule before insert or update or delete on ww_proof.review_schedule for each row execute function ww_proof.lock_review_content();
create function ww_proof.lock_test_classification() returns trigger language plpgsql set search_path='' as $$
begin
 if old.published_at is not null and new.test_data is distinct from old.test_data then raise exception 'Published test classification is immutable'; end if;
 return new;
end $$;
create trigger lock_test_classification before update on ww_proof.deal_version for each row execute function ww_proof.lock_test_classification();

create function ww_proof.require_reviewer() returns void language plpgsql security definer set search_path='' as $$
begin if auth.uid() is null or not exists(select 1 from ww_proof.reviewer where auth_user_id=auth.uid()) then raise exception 'Reviewer access required'; end if; end $$;
revoke all on function ww_proof.require_reviewer() from public,anon,authenticated;
create function public.ww_review_queue(p_status text,p_offset integer default 0) returns jsonb language plpgsql security definer set search_path='' as $$
declare result jsonb;
begin
 perform ww_proof.require_reviewer();
 if p_status not in ('PENDING','NEEDS_CLARIFICATION','APPROVED','REJECTED') or p_offset<0 or p_offset>100000 then raise exception 'Invalid queue filter'; end if;
 select coalesce(jsonb_agg(to_jsonb(rows)),'[]'::jsonb) into result from (
 select s.submission_id as id,s.restaurant_id,s.location_id,s.place_details as place,s.offer,s.status,s.reviewer_note as note,s.test_data,s.review_revision as revision,s.review_draft as draft,s.created_at,r.name as restaurant_name,l.address
 from ww_proof.submission s left join ww_proof.restaurant r on r.restaurant_id=s.restaurant_id left join ww_proof.location l on l.location_id=s.location_id where s.status=p_status order by s.created_at,s.submission_id limit 20 offset p_offset) rows;
 return result;
end $$;
create function public.ww_review_submission(p_id uuid,p_revision integer,p_action text,p_draft jsonb) returns jsonb
language plpgsql security definer set search_path='' as $$
#variable_conflict use_variable
declare entry ww_proof.submission; restaurant uuid; location uuid; deal uuid; version uuid; is_test boolean; day integer; note text;
begin
 perform ww_proof.require_reviewer();
 select * into entry from ww_proof.submission where submission_id=p_id for update;
 if not found then raise exception 'Submission not found'; end if;
 if entry.review_revision<>p_revision then raise exception 'Review changed; reload the queue'; end if;
 if entry.status in ('APPROVED','REJECTED') then raise exception 'Review is finalized; published changes need a new version'; end if;
 if p_action not in ('SAVE','CLARIFY','REJECT','APPROVE') or p_draft is null or jsonb_typeof(p_draft)<>'object' or length(p_draft::text)>16000 then raise exception 'Invalid review'; end if;
 note:=coalesce(p_draft->>'note','');is_test:=coalesce((p_draft->>'test_data')::boolean,entry.test_data);
 if entry.test_data and not is_test then raise exception 'Test classification cannot be removed'; end if;
 if length(note)>2000 or (p_action in ('CLARIFY','REJECT') and length(trim(note))=0) then raise exception 'A reviewer note is required'; end if;
 if p_action='APPROVE' then
  if coalesce((p_draft->>'details_checked')::boolean,false)=false then raise exception 'Check the location and essential terms before approval'; end if;
  if coalesce((p_draft->>'weekly_only')::boolean,false)=false then raise exception 'This development engine supports weekly all-day offers only; hold other schedules'; end if;
  if length(trim(coalesce(p_draft->>'offer',''))) not between 1 and 2000 or length(trim(coalesce(p_draft->>'terms','')))=0 or length(coalesce(p_draft->>'terms',''))>4000 or length(trim(coalesce(p_draft->>'source','')))=0 then raise exception 'Offer, eligibility/terms and source are required'; end if;
  if not is_test and (entry.test_data or entry.place_details like 'TEST ONLY%' or entry.offer like 'TEST ONLY%' or p_draft->>'offer' like 'TEST ONLY%') then raise exception 'Test submissions cannot be published as real offers'; end if;
  restaurant:=nullif(p_draft->>'restaurant_id','')::uuid;location:=nullif(p_draft->>'location_id','')::uuid;
  if restaurant is null then
   if length(trim(coalesce(p_draft->>'restaurant_name',''))) not between 1 and 200 then raise exception 'Restaurant name is required'; end if;
   -- Explicit reviewer creation only; look for a same-name record before creating.
   if exists(select 1 from ww_proof.restaurant where lower(trim(name))=lower(trim(p_draft->>'restaurant_name'))) then raise exception 'Restaurant name exists; search and select it'; end if;
   restaurant:=gen_random_uuid();
   insert into ww_proof.restaurant(restaurant_id,import_key,name,test_data) values(restaurant,'review-'||restaurant,trim(p_draft->>'restaurant_name'),is_test);
  elsif not exists(select 1 from ww_proof.restaurant where restaurant_id=restaurant and status='ACTIVE' and (is_test or not test_data)) then raise exception 'Invalid restaurant'; end if;
  if location is null then
   if length(trim(coalesce(p_draft->>'address',''))) not between 3 and 1000 then raise exception 'Location address is required'; end if;
   if exists(select 1 from ww_proof.location where restaurant_id=restaurant and lower(trim(address))=lower(trim(p_draft->>'address'))) then raise exception 'Location exists; search and select it'; end if;
   location:=gen_random_uuid();
   insert into ww_proof.location(location_id,restaurant_id,import_key,address,source_note,test_data) values(location,restaurant,'review-'||location,trim(p_draft->>'address'),p_draft->>'source',is_test);
  elsif not exists(select 1 from ww_proof.location where location_id=location and restaurant_id=restaurant and status='ACTIVE' and (is_test or not test_data)) then raise exception 'Invalid location'; end if;
  if jsonb_typeof(p_draft->'days') is distinct from 'array' or jsonb_array_length(p_draft->'days') not between 1 and 7 then raise exception 'Choose weekdays'; end if;
  if exists(select 1 from ww_proof.deal_version v join ww_proof.deal_location a on a.deal_version_id=v.deal_version_id
   where a.location_id=location and a.applicability<>'EXCLUDED' and v.status='PUBLISHED' and v.test_data=is_test
   and lower(trim(v.offer))=lower(trim(p_draft->>'offer')) and lower(trim(v.terms))=lower(trim(p_draft->>'terms'))
   and array(select weekday from ww_proof.review_schedule where version_id=v.deal_version_id order by weekday)=array(select distinct value::integer from jsonb_array_elements_text(p_draft->'days') order by value::integer)) then raise exception 'Matching published offer exists; hold or reject the duplicate'; end if;
  deal:=gen_random_uuid();version:=gen_random_uuid();
  insert into ww_proof.deal values(deal,restaurant);
  insert into ww_proof.deal_version(deal_version_id,deal_id,version_number,offer,terms,status,published_at,test_data)
   values(version,deal,1,trim(p_draft->>'offer'),trim(p_draft->>'terms'),'DRAFT',null,is_test);
  insert into ww_proof.deal_location values(version,location,'UNKNOWN');
  insert into ww_proof.review_offer values(version,p_id,coalesce((p_draft->>'for_kids')::boolean,false),(p_draft->>'savings_rank')::integer,p_draft->>'source',to_char(now(),'YYYY-MM-DD'));
  for day in select value::integer from jsonb_array_elements_text(p_draft->'days') loop
   insert into ww_proof.review_schedule values(version,day); -- range/duplicate checks roll the transaction back
  end loop;
  update ww_proof.deal_version set status='PUBLISHED',published_at=now() where deal_version_id=version;
  insert into ww_proof.publication_audit(deal_version_id,reviewer_id) values(version,auth.uid());
 end if;
 update ww_proof.submission set review_draft=p_draft,review_revision=review_revision+1,test_data=is_test,reviewer_note=note,
 status=case p_action when 'APPROVE' then 'APPROVED' when 'CLARIFY' then 'NEEDS_CLARIFICATION' when 'REJECT' then 'REJECTED' else status end where submission_id=p_id;
 insert into ww_proof.review_audit(submission_id,reviewer_id,action,draft) values(p_id,auth.uid(),p_action,p_draft);
 return jsonb_build_object('revision',entry.review_revision+1,'version_id',version,'test_data',is_test);
end $$;
revoke all on function public.ww_review_queue(text,integer),public.ww_review_submission(uuid,integer,text,jsonb) from public,anon;
grant execute on function public.ww_review_queue(text,integer),public.ww_review_submission(uuid,integer,text,jsonb) to authenticated;
-- Preserve the existing endpoint/contract, replacing only offers and omitting test-created places.
create or replace function public.ww_app_catalog() returns jsonb language sql stable security definer set search_path='' as $$
 select jsonb_build_object('schema_version',1,'environment','DEV','complete',true,'market','ELK_GROVE',
 'restaurants',coalesce((select jsonb_agg(jsonb_build_object('id',r.restaurant_id,'key',case r.import_key when 'prototype-chevys' then 'chevys' when 'prototype-dennys' then 'dennys' when 'prototype-smashburger' then 'smashburger' else r.restaurant_id::text end,'name',r.name,'status',r.status)) from ww_proof.restaurant r where not r.test_data),'[]'::jsonb),
 'locations',coalesce((select jsonb_agg(jsonb_build_object('id',l.location_id,'restaurant_id',l.restaurant_id,'legacy_key',case l.import_key when 'prototype-chevys-location' then 'chevys' when 'prototype-dennys-location' then 'dennys' when 'prototype-smashburger-location' then 'smashburger' else null end,'address',l.address,'time_zone',l.time_zone,'status',l.status,'source_note',l.source_note)) from ww_proof.location l join ww_proof.restaurant r on r.restaurant_id=l.restaurant_id where not l.test_data and not r.test_data),'[]'::jsonb),
 'offers',coalesce((select jsonb_agg(jsonb_build_object('id',v.deal_version_id,'deal_id',d.deal_id,'restaurant_id',d.restaurant_id,'location_id',l.location_id,'version',v.version_number,'published_at',floor(extract(epoch from v.published_at)*1000),'offer',v.offer,'terms',v.terms,'days',(select jsonb_agg(weekday order by weekday) from ww_proof.review_schedule where version_id=v.deal_version_id),'for_kids',o.for_kids,'savings_rank',o.savings_rank,'verified',false,'source',o.source,'checked',o.checked,'test_data',false))
 from ww_proof.deal_version v join ww_proof.deal d on d.deal_id=v.deal_id join ww_proof.review_offer o on o.version_id=v.deal_version_id
 join ww_proof.deal_location a on a.deal_version_id=v.deal_version_id join ww_proof.location l on l.location_id=a.location_id and l.restaurant_id=d.restaurant_id join ww_proof.restaurant r on r.restaurant_id=d.restaurant_id
 where not v.test_data and not l.test_data and not r.test_data and v.status='PUBLISHED' and l.status='ACTIVE' and r.status='ACTIVE' and a.applicability<>'EXCLUDED'),'[]'::jsonb),
 'excluded_test_offer_count',(select count(*) from ww_proof.deal_version where test_data and status='PUBLISHED'))
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
      where v.test_data and v.offer like 'TEST ONLY%' and v.status='PUBLISHED' and dl.applicability <> 'EXCLUDED'
        and v.version_number=(select max(v2.version_number) from ww_proof.deal_version v2
          where v2.deal_id=v.deal_id and v2.status='PUBLISHED')
      order by r.name, l.location_id limit 20) visible), '[]'::jsonb))
$$;

create function public.ww_review_place_offers(p_location uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare result jsonb;
begin
 perform ww_proof.require_reviewer();
 select coalesce(jsonb_agg(jsonb_build_object('offer',v.offer,'terms',v.terms,'test_data',v.test_data,'days',(select jsonb_agg(weekday order by weekday) from ww_proof.review_schedule where version_id=v.deal_version_id))),'[]'::jsonb) into result
 from ww_proof.deal_version v join ww_proof.deal_location a on a.deal_version_id=v.deal_version_id where a.location_id=p_location and a.applicability<>'EXCLUDED' and v.status='PUBLISHED';
 return result;
end $$;
revoke all on function public.ww_review_place_offers(uuid) from public,anon;
grant execute on function public.ww_review_place_offers(uuid) to authenticated;

commit;
