-- ============================================================
-- JatiStore v5.1 - Seed Data: Payment Cards
-- Dependencies: seed-01-users-roles.sql (users must exist)
-- Purpose: Create test payment cards for users
-- ============================================================

-- Clear existing data (dev/test only)
TRUNCATE TABLE mst_payment_cards CASCADE;

-- ============================================================
-- PAYMENT CARDS (mst_payment_cards)
-- Card numbers aligned with payment gateway mock test scenarios:
-- - Ending in 4242: SUCCESS
-- - Ending in 3333: DECLINED (insufficient funds)
-- - Other: FAILED (fraud/invalid)
-- ============================================================

-- Alice's cards (2 cards: 1 success, 1 declined)
INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000001', 'u0000000-0000-0000-0000-000000000001', '4111111111114242', 'Alice Johnson', '12/28'),
('cd000000-0000-0000-0000-000000000002', 'u0000000-0000-0000-0000-000000000001', '5555555555553333', 'Alice Johnson', '06/27');

-- Bob's cards (1 success card)
INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000003', 'u0000000-0000-0000-0000-000000000002', '4111111111114242', 'Bob Williams', '09/29');

-- Charlie's cards (1 success, 1 failed)
INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000004', 'u0000000-0000-0000-0000-000000000003', '5555555555554242', 'Charlie Davis', '03/28'),
('cd000000-0000-0000-0000-000000000005', 'u0000000-0000-0000-0000-000000000003', '4111111111119999', 'Charlie Davis', '11/26');

-- Diana's card (success)
INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000006', 'u0000000-0000-0000-0000-000000000004', '4111111111114242', 'Diana Martinez', '08/30');

-- Ethan's cards (declined scenario for testing)
INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000007', 'u0000000-0000-0000-0000-000000000005', '5555555555553333', 'Ethan Taylor', '04/27');

-- Fiona's card (success)
INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000008', 'u0000000-0000-0000-0000-000000000006', '4111111111114242', 'Fiona Anderson', '07/29');

-- George's card (success)
INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000009', 'u0000000-0000-0000-0000-000000000007', '5555555555554242', 'George Thomas', '02/28');

-- Hannah's cards (1 success, 1 declined)
INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000010', 'u0000000-0000-0000-0000-000000000008', '4111111111114242', 'Hannah Jackson', '10/30'),
('cd000000-0000-0000-0000-000000000011', 'u0000000-0000-0000-0000-000000000008', '4111111111113333', 'Hannah Jackson', '05/27');

-- ============================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ============================================================

-- -- List all cards by user
-- SELECT 
--   u.username,
--   u.full_name,
--   pc.card_number,
--   pc.card_holder_name,
--   pc.expiry_date,
--   CASE 
--     WHEN RIGHT(pc.card_number, 4) = '4242' THEN 'SUCCESS'
--     WHEN RIGHT(pc.card_number, 4) = '3333' THEN 'DECLINED'
--     ELSE 'FAILED'
--   END AS mock_scenario
-- FROM mst_users u
-- JOIN mst_payment_cards pc ON u.id = pc.user_id
-- ORDER BY u.username, pc.card_number;

-- -- Count cards by test scenario
-- SELECT 
--   CASE 
--     WHEN RIGHT(card_number, 4) = '4242' THEN 'SUCCESS'
--     WHEN RIGHT(card_number, 4) = '3333' THEN 'DECLINED'
--     ELSE 'FAILED'
--   END AS scenario,
--   COUNT(*) AS card_count
-- FROM mst_payment_cards
-- GROUP BY scenario
-- ORDER BY scenario;
