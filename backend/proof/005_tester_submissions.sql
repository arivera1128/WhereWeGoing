-- Development tester intake only. Apply once after 004; does not publish deals.
begin;
create table ww_proof.submission (
 submission_id uuid primary key,
 auth_user_id uuid not null references auth.users(id),
 restaurant_id uuid references ww_proof.restaurant,
 location_id uuid references ww_proof.location,
 place_details text not null,
 offer text not null check(length(trim(offer)) between 1 and 2000),
 status text not null default 'PENDING' check(status in ('PENDING','NEEDS_CLARIFICATION','APPROVED','REJECTED')),
 reviewer_note text not null default '', created_at timestamptz not null default now()
);
create index on ww_proof.submission(auth_user_id,created_at);
alter table ww_proof.submission enable row level security;
revoke all on ww_proof.submission from public,anon,authenticated;
create function public.ww_submit_deal(p_id uuid,p_restaurant uuid,p_location uuid,p_place text,p_offer text)
returns uuid language plpgsql security definer set search_path='' as $$
declare actor uuid:=auth.uid(); existing ww_proof.submission;
begin
 if actor is null then raise exception 'Authentication required'; end if;
 if length(trim(p_offer)) not between 1 and 2000 or length(p_place)>1000 then raise exception 'Invalid submission'; end if;
 if p_location is not null and not exists(select 1 from ww_proof.location where location_id=p_location and restaurant_id=p_restaurant and status='ACTIVE') then raise exception 'Invalid location'; end if;
 if p_location is null and length(trim(p_place))<3 then raise exception 'Describe the place and address'; end if;
 -- Serialize per-actor retries and cap intake for this bounded development test.
 perform pg_advisory_xact_lock(hashtextextended(actor::text,0));
 select * into existing from ww_proof.submission where submission_id=p_id;
 if found then
  if existing.auth_user_id<>actor or (existing.restaurant_id,existing.location_id,existing.place_details,existing.offer) is distinct from (p_restaurant,p_location,trim(p_place),trim(p_offer)) then raise exception 'Submission identity conflict'; end if;
  return p_id;
 end if;
 if (select count(*) from ww_proof.submission where auth_user_id=actor and created_at>now()-interval '1 day')>=10 then raise exception 'Daily submission limit reached'; end if;
 insert into ww_proof.submission(submission_id,auth_user_id,restaurant_id,location_id,place_details,offer) values(p_id,actor,p_restaurant,p_location,trim(p_place),trim(p_offer));
 return p_id;
end $$;
create function public.ww_my_submissions() returns jsonb language sql stable security definer set search_path='' as $$
 select coalesce(jsonb_agg(jsonb_build_object('id',submission_id,'place',place_details,'offer',offer,'status',status,'note',reviewer_note) order by created_at desc),'[]'::jsonb) from ww_proof.submission where auth_user_id=auth.uid()
$$;
revoke all on function public.ww_submit_deal(uuid,uuid,uuid,text,text),public.ww_my_submissions() from public,anon;
grant execute on function public.ww_submit_deal(uuid,uuid,uuid,text,text),public.ww_my_submissions() to authenticated;
commit;
