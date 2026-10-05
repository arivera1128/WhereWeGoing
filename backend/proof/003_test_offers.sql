-- All offers are synthetic, not restaurant promotions. All start unpublished.
begin;
insert into ww_proof.deal values
 ('30000000-0000-4000-8000-000000000001','10000000-0000-4000-8000-000000000001'),
 ('30000000-0000-4000-8000-000000000002','10000000-0000-4000-8000-000000000002'),
 ('30000000-0000-4000-8000-000000000003','10000000-0000-4000-8000-000000000003') on conflict do nothing;
insert into ww_proof.deal_version(deal_version_id,deal_id,version_number,offer,terms) values
 ('40000000-0000-4000-8000-000000000001','30000000-0000-4000-8000-000000000001',1,'TEST ONLY — publication check A','Synthetic development offer. Do not redeem.'),
 ('40000000-0000-4000-8000-000000000002','30000000-0000-4000-8000-000000000002',1,'TEST ONLY — publication check B','Synthetic development offer. Do not redeem.'),
 ('40000000-0000-4000-8000-000000000003','30000000-0000-4000-8000-000000000003',1,'TEST ONLY — publication check C','Synthetic development offer. Do not redeem.') on conflict do nothing;
insert into ww_proof.deal_location values
 ('40000000-0000-4000-8000-000000000001','20000000-0000-4000-8000-000000000001','UNKNOWN'),
 ('40000000-0000-4000-8000-000000000002','20000000-0000-4000-8000-000000000002','UNKNOWN'),
 ('40000000-0000-4000-8000-000000000003','20000000-0000-4000-8000-000000000003','UNKNOWN') on conflict do nothing;
commit;
