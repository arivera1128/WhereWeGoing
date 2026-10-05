-- Apply once after 005. Bounded prefix search of active shared locations.
begin;
create index restaurant_name_search on ww_proof.restaurant(lower(name) text_pattern_ops);
create function public.ww_search_places(p_query text) returns jsonb
language sql stable security definer set search_path='' as $$
 select coalesce(jsonb_agg(jsonb_build_object('restaurant_id',restaurant_id,'location_id',location_id,'name',name,'address',address) order by name,address,location_id),'[]'::jsonb)
 from (
  select r.restaurant_id,l.location_id,r.name,l.address from ww_proof.restaurant r
  join ww_proof.location l on l.restaurant_id=r.restaurant_id
  where r.status='ACTIVE' and l.status='ACTIVE' and length(trim(p_query)) between 2 and 100
   and lower(r.name) like replace(replace(replace(lower(trim(p_query)),chr(92),chr(92)||chr(92)),'%',chr(92)||'%'),'_',chr(92)||'_')||'%' escape E'\\'
  order by r.name,l.address,l.location_id limit 10
 ) results
$$;
revoke all on function public.ww_search_places(text) from public;
grant execute on function public.ww_search_places(text) to anon,authenticated;
commit;
