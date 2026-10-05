-- Development fixtures only. Generated from CSV; inserts never overwrite existing rows.
begin;
insert into ww_proof.restaurant(restaurant_id,import_key,name) values ('10000000-0000-4000-8000-000000000001','prototype-chevys','Chevy''s') on conflict do nothing;
insert into ww_proof.restaurant(restaurant_id,import_key,name) values ('10000000-0000-4000-8000-000000000002','prototype-dennys','Denny''s') on conflict do nothing;
insert into ww_proof.restaurant(restaurant_id,import_key,name) values ('10000000-0000-4000-8000-000000000003','prototype-smashburger','Smashburger') on conflict do nothing;
insert into ww_proof.location(location_id,restaurant_id,import_key,address,source_note) values ('20000000-0000-4000-8000-000000000001','10000000-0000-4000-8000-000000000001','prototype-chevys-location','7401 Laguna Blvd, Elk Grove','Copied from existing prototype; address not reverified. Development fixture only.') on conflict do nothing;
insert into ww_proof.location(location_id,restaurant_id,import_key,address,source_note) values ('20000000-0000-4000-8000-000000000002','10000000-0000-4000-8000-000000000002','prototype-dennys-location','8707 Elk Grove Blvd, Elk Grove','Copied from existing prototype; address not reverified. Development fixture only.') on conflict do nothing;
insert into ww_proof.location(location_id,restaurant_id,import_key,address,source_note) values ('20000000-0000-4000-8000-000000000003','10000000-0000-4000-8000-000000000003','prototype-smashburger-location','7701 Laguna Blvd, Elk Grove','Copied from existing prototype; address not reverified. Development fixture only.') on conflict do nothing;
commit;
