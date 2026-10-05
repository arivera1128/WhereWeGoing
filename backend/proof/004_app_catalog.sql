-- Apply once after 001–003 in WhereWeGoing-dev. Keeps the proof endpoint unchanged.
-- Complete bounded DEV snapshot: locations are usable for integration; synthetic offers are excluded.
begin;
alter table ww_proof.restaurant add column status text not null default 'ACTIVE'
  check(status in ('ACTIVE','INACTIVE'));
alter table ww_proof.location add column status text not null default 'ACTIVE'
  check(status in ('ACTIVE','INACTIVE'));
create function public.ww_app_catalog() returns jsonb
language sql stable security definer set search_path = '' as $$
select jsonb_build_object(
  'schema_version',1,'environment','DEV','complete',true,'market','ELK_GROVE',
  'restaurants',coalesce((select jsonb_agg(jsonb_build_object(
    'id',r.restaurant_id,'key',case r.import_key
      when 'prototype-chevys' then 'chevys' when 'prototype-dennys' then 'dennys'
      when 'prototype-smashburger' then 'smashburger' else r.restaurant_id::text end,
    'name',r.name,'status',r.status) order by r.import_key) from ww_proof.restaurant r),'[]'::jsonb),
  'locations',coalesce((select jsonb_agg(jsonb_build_object(
    'id',l.location_id,'restaurant_id',l.restaurant_id,'legacy_key',case l.import_key
      when 'prototype-chevys-location' then 'chevys' when 'prototype-dennys-location' then 'dennys'
      when 'prototype-smashburger-location' then 'smashburger' else null end,
    'address',l.address,'time_zone',l.time_zone,'status',l.status,
    'source_note',l.source_note) order by l.import_key) from ww_proof.location l),'[]'::jsonb),
  -- This proof has no non-synthetic offer source. Never promote TEST ONLY offers into recommendations.
  'offers','[]'::jsonb,
  'excluded_test_offer_count',(select count(*) from ww_proof.deal_version where status='PUBLISHED'))
$$;
revoke all on function public.ww_app_catalog() from public;
grant execute on function public.ww_app_catalog() to anon, authenticated;
commit;
