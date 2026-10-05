-- DEVELOPMENT PROOF ONLY. Apply once in a new Free development Supabase project.
-- Private tables are not exposed through the Data API. Clients receive two narrow RPCs.
begin;
create schema ww_proof;
revoke all on schema ww_proof from public, anon, authenticated;

create table ww_proof.restaurant (
  restaurant_id uuid primary key,
  import_key text not null unique,
  name text not null check (length(trim(name)) > 0)
);
create table ww_proof.location (
  location_id uuid primary key,
  restaurant_id uuid not null references ww_proof.restaurant,
  import_key text not null unique,
  address text not null,
  time_zone text not null default 'America/Los_Angeles',
  source_note text not null
);
create index on ww_proof.location(restaurant_id);
create table ww_proof.deal (
  deal_id uuid primary key,
  restaurant_id uuid not null references ww_proof.restaurant
);
create index on ww_proof.deal(restaurant_id);
create table ww_proof.deal_version (
  deal_version_id uuid primary key,
  deal_id uuid not null references ww_proof.deal,
  version_number integer not null check (version_number > 0),
  offer text not null check (offer like 'TEST ONLY%'),
  terms text not null,
  status text not null default 'DRAFT' check (status in ('DRAFT','PUBLISHED','RETIRED')),
  published_at timestamptz,
  unique (deal_id, version_number),
  check (status = 'DRAFT' or published_at is not null)
);
create table ww_proof.deal_location (
  deal_version_id uuid not null references ww_proof.deal_version,
  location_id uuid not null references ww_proof.location,
  applicability text not null check (applicability in ('INCLUDED','UNKNOWN','EXCLUDED')),
  primary key (deal_version_id, location_id)
);
create index on ww_proof.deal_location(location_id);
create table ww_proof.reviewer (
  auth_user_id uuid primary key references auth.users(id)
);
create table ww_proof.publication_audit (
  audit_id uuid primary key default gen_random_uuid(),
  deal_version_id uuid not null references ww_proof.deal_version,
  reviewer_id uuid not null references auth.users(id),
  occurred_at timestamptz not null default now()
);

-- Defense in depth: no client table grants or policies. Only owner-executed RPCs access these.
alter table ww_proof.restaurant enable row level security;
alter table ww_proof.location enable row level security;
alter table ww_proof.deal enable row level security;
alter table ww_proof.deal_version enable row level security;
alter table ww_proof.deal_location enable row level security;
alter table ww_proof.reviewer enable row level security;
alter table ww_proof.publication_audit enable row level security;
revoke all on all tables in schema ww_proof from public, anon, authenticated;

create function ww_proof.lock_published_version() returns trigger
language plpgsql set search_path = '' as $$
begin
  if old.published_at is not null then
    if tg_op = 'DELETE' then raise exception 'Published versions cannot be deleted'; end if;
    if (new.deal_version_id, new.deal_id, new.version_number, new.offer, new.terms, new.published_at)
        is distinct from
       (old.deal_version_id, old.deal_id, old.version_number, old.offer, old.terms, old.published_at)
       or new.status not in ('PUBLISHED','RETIRED') then
      raise exception 'Published content is immutable; create a new version';
    end if;
  end if;
  if tg_op = 'DELETE' then return old; end if;
  return new;
end $$;
create trigger immutable_published_version before update or delete on ww_proof.deal_version
for each row execute function ww_proof.lock_published_version();

create function public.ww_proof_catalog() returns jsonb
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
      where v.status='PUBLISHED' and dl.applicability <> 'EXCLUDED'
        and v.version_number=(select max(v2.version_number) from ww_proof.deal_version v2
          where v2.deal_id=v.deal_id and v2.status='PUBLISHED')
      order by r.name, l.location_id limit 20) visible), '[]'::jsonb))
$$;
revoke all on function public.ww_proof_catalog() from public;
grant execute on function public.ww_proof_catalog() to anon, authenticated;

create function public.ww_proof_publish(p_version_id uuid) returns void
language plpgsql security definer set search_path = '' as $$
declare actor uuid := auth.uid();
begin
  if actor is null or coalesce((auth.jwt()->>'is_anonymous')::boolean,false)
     or not exists(select 1 from ww_proof.reviewer where auth_user_id=actor) then
    raise exception 'Reviewer access required' using errcode='42501';
  end if;
  if not exists(select 1 from ww_proof.deal_location dl
      join ww_proof.deal_version v on v.deal_version_id=dl.deal_version_id
      join ww_proof.deal d on d.deal_id=v.deal_id
      join ww_proof.location l on l.location_id=dl.location_id and l.restaurant_id=d.restaurant_id
      where v.deal_version_id=p_version_id and dl.applicability <> 'EXCLUDED') then
    raise exception 'A matching location is required';
  end if;
  update ww_proof.deal_version set status='PUBLISHED',published_at=now()
    where deal_version_id=p_version_id and status='DRAFT';
  if not found then raise exception 'Draft version not found'; end if;
  insert into ww_proof.publication_audit(deal_version_id,reviewer_id) values(p_version_id,actor);
end $$;
revoke all on function public.ww_proof_publish(uuid) from public, anon;
grant execute on function public.ww_proof_publish(uuid) to authenticated;
revoke all on function ww_proof.lock_published_version() from public;
commit;
