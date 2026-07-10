-- ============================================================
-- JatiStore v5.1 - Seed Data: Orders & Transactions
-- Dependencies: seed-03-products.sql, seed-05-payment-cards.sql
-- Purpose: Create sample orders with various statuses and payment scenarios
-- ============================================================

-- Clear existing data (dev/test only)
TRUNCATE TABLE trx_seller_ledger CASCADE;
TRUNCATE TABLE trx_transactions CASCADE;
TRUNCATE TABLE trx_order_details CASCADE;
TRUNCATE TABLE trx_orders CASCADE;
TRUNCATE TABLE trx_cart_items CASCADE;
TRUNCATE TABLE trx_carts CASCADE;

-- ============================================================
-- CARTS (trx_carts)
-- Active carts for some users
-- ============================================================

INSERT INTO trx_carts (id, user_id, created_at, updated_at) VALUES
('ct000000-0000-0000-0000-000000000001', 'u0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 hour'),
('ct000000-0000-0000-0000-000000000002', 'u0000000-0000-0000-0000-000000000003', NOW() - INTERVAL '1 day', NOW() - INTERVAL '3 hours'),
('ct000000-0000-0000-0000-000000000003', 'u0000000-0000-0000-0000-000000000005', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '30 minutes');

-- ============================================================
-- CART ITEMS (trx_cart_items)
-- ============================================================

-- Alice's cart (3 items)
INSERT INTO trx_cart_items (id, cart_id, product_id, quantity) VALUES
('ci000000-0000-0000-0000-000000000001', 'ct000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000003', 1), -- Sony headphones
('ci000000-0000-0000-0000-000000000002', 'ct000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000031', 2), -- Atomic Habits book x2
('ci000000-0000-0000-0000-000000000003', 'ct000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000041', 1); -- Yoga mat

-- Charlie's cart (2 items)
INSERT INTO trx_cart_items (id, cart_id, product_id, quantity) VALUES
('ci000000-0000-0000-0000-000000000004', 'ct000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000012', 1), -- Nike shoes
('ci000000-0000-0000-0000-000000000005', 'ct000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000015', 1); -- Denim jacket

-- Ethan's cart (1 flash sale item)
INSERT INTO trx_cart_items (id, cart_id, product_id, quantity) VALUES
('ci000000-0000-0000-0000-000000000006', 'ct000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000001', 1); -- Laptop (flash sale)

-- ============================================================
-- ORDERS (trx_orders)
-- Various order statuses for testing
-- ============================================================

-- Order 1: Bob - RECEIVED (completed successfully)
INSERT INTO trx_orders (id, user_id, total_amount, status, created_at) VALUES
('or000000-0000-0000-0000-000000000001', 'u0000000-0000-0000-0000-000000000002', 2150000.0000, 'RECEIVED', NOW() - INTERVAL '15 days');

INSERT INTO trx_order_details (id, order_id, product_id, quantity, price_per_item, flash_sale, created_at) VALUES
('od000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000012', 1, 1950000.0000, FALSE, NOW() - INTERVAL '15 days'),
('od000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000013', 1, 200000.0000, FALSE, NOW() - INTERVAL '15 days');

INSERT INTO trx_transactions (id, order_id, payment_method, payment_card_id, payment_gateway_ref, status, created_at, updated_at) VALUES
('tx000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000001', 'CARD', 'cd000000-0000-0000-0000-000000000003', 'TXN-mock-success-001', 'SUCCESS', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days');

-- Order 2: Diana - SHIPPED (in transit)
INSERT INTO trx_orders (id, user_id, total_amount, status, created_at) VALUES
('or000000-0000-0000-0000-000000000002', 'u0000000-0000-0000-0000-000000000004', 4500000.0000, 'SHIPPED', NOW() - INTERVAL '3 days');

INSERT INTO trx_order_details (id, order_id, product_id, quantity, price_per_item, flash_sale, created_at) VALUES
('od000000-0000-0000-0000-000000000003', 'or000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000003', 1, 4500000.0000, FALSE, NOW() - INTERVAL '3 days');

INSERT INTO trx_transactions (id, order_id, payment_method, payment_card_id, payment_gateway_ref, status, created_at, updated_at) VALUES
('tx000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000002', 'CARD', 'cd000000-0000-0000-0000-000000000006', 'TXN-mock-success-002', 'SUCCESS', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

-- Order 3: Fiona - PAID_ON_HOLD (awaiting shipment)
INSERT INTO trx_orders (id, user_id, total_amount, status, created_at) VALUES
('or000000-0000-0000-0000-000000000003', 'u0000000-0000-0000-0000-000000000006', 2950000.0000, 'PAID_ON_HOLD', NOW() - INTERVAL '1 day');

INSERT INTO trx_order_details (id, order_id, product_id, quantity, price_per_item, flash_sale, created_at) VALUES
('od000000-0000-0000-0000-000000000004', 'or000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000024', 1, 2500000.0000, FALSE, NOW() - INTERVAL '1 day'),
('od000000-0000-0000-0000-000000000005', 'or000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000023', 1, 450000.0000, FALSE, NOW() - INTERVAL '1 day');

INSERT INTO trx_transactions (id, order_id, payment_method, payment_card_id, payment_gateway_ref, status, created_at, updated_at) VALUES
('tx000000-0000-0000-0000-000000000003', 'or000000-0000-0000-0000-000000000003', 'WALLET', NULL, 'TXN-mock-wallet-001', 'SUCCESS', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

-- Order 4: George - RECEIVED with flash sale item
INSERT INTO trx_orders (id, user_id, total_amount, status, created_at) VALUES
('or000000-0000-0000-0000-000000000004', 'u0000000-0000-0000-0000-000000000007', 14800000.0000, 'RECEIVED', NOW() - INTERVAL '5 days');

INSERT INTO trx_order_details (id, order_id, product_id, quantity, price_per_item, flash_sale, created_at) VALUES
('od000000-0000-0000-0000-000000000006', 'or000000-0000-0000-0000-000000000004', 'pr000000-0000-0000-0000-000000000001', 1, 14800000.0000, TRUE, NOW() - INTERVAL '5 days');

INSERT INTO trx_transactions (id, order_id, payment_method, payment_card_id, payment_gateway_ref, status, created_at, updated_at) VALUES
('tx000000-0000-0000-0000-000000000004', 'or000000-0000-0000-0000-000000000004', 'CARD', 'cd000000-0000-0000-0000-000000000009', 'TXN-mock-success-004', 'SUCCESS', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

-- Order 5: Hannah - CANCELLED (payment declined)
INSERT INTO trx_orders (id, user_id, total_amount, status, created_at) VALUES
('or000000-0000-0000-0000-000000000005', 'u0000000-0000-0000-0000-000000000008', 850000.0000, 'CANCELLED', NOW() - INTERVAL '7 days');

INSERT INTO trx_order_details (id, order_id, product_id, quantity, price_per_item, flash_sale, created_at) VALUES
('od000000-0000-0000-0000-000000000007', 'or000000-0000-0000-0000-000000000005', 'pr000000-0000-0000-0000-000000000022', 1, 850000.0000, FALSE, NOW() - INTERVAL '7 days');

INSERT INTO trx_transactions (id, order_id, payment_method, payment_card_id, payment_gateway_ref, status, created_at, updated_at) VALUES
('tx000000-0000-0000-0000-000000000005', 'or000000-0000-0000-0000-000000000005', 'CARD', 'cd000000-0000-0000-0000-000000000011', 'TXN-mock-declined-001', 'DECLINED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

-- Order 6: Alice - PENDING (payment failed)
INSERT INTO trx_orders (id, user_id, total_amount, status, created_at) VALUES
('or000000-0000-0000-0000-000000000006', 'u0000000-0000-0000-0000-000000000001', 1200000.0000, 'PENDING', NOW() - INTERVAL '2 hours');

INSERT INTO trx_order_details (id, order_id, product_id, quantity, price_per_item, flash_sale, created_at) VALUES
('od000000-0000-0000-0000-000000000008', 'or000000-0000-0000-0000-000000000006', 'pr000000-0000-0000-0000-000000000007', 1, 1200000.0000, FALSE, NOW() - INTERVAL '2 hours');

INSERT INTO trx_transactions (id, order_id, payment_method, payment_card_id, payment_gateway_ref, status, created_at, updated_at) VALUES
('tx000000-0000-0000-0000-000000000006', 'or000000-0000-0000-0000-000000000006', 'CARD', 'cd000000-0000-0000-0000-000000000002', NULL, 'FAILED', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours');

-- Order 7: Charlie - RECEIVED (multiple items from different stores)
INSERT INTO trx_orders (id, user_id, total_amount, status, created_at) VALUES
('or000000-0000-0000-0000-000000000007', 'u0000000-0000-0000-0000-000000000003', 1850000.0000, 'RECEIVED', NOW() - INTERVAL '20 days');

INSERT INTO trx_order_details (id, order_id, product_id, quantity, price_per_item, flash_sale, created_at) VALUES
('od000000-0000-0000-0000-000000000009', 'or000000-0000-0000-0000-000000000007', 'pr000000-0000-0000-0000-000000000005', 1, 1450000.0000, FALSE, NOW() - INTERVAL '20 days'),
('od000000-0000-0000-0000-000000000010', 'or000000-0000-0000-0000-000000000007', 'pr000000-0000-0000-0000-000000000032', 2, 200000.0000, FALSE, NOW() - INTERVAL '20 days');

INSERT INTO trx_transactions (id, order_id, payment_method, payment_card_id, payment_gateway_ref, status, created_at, updated_at) VALUES
('tx000000-0000-0000-0000-000000000007', 'or000000-0000-0000-0000-000000000007', 'WALLET', NULL, 'TXN-mock-wallet-002', 'SUCCESS', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days');

-- ============================================================
-- SELLER LEDGER (trx_seller_ledger)
-- Ledger entries for completed orders (RECEIVED status)
-- ON_HOLD entries created when order is PAID, moved to AVAILABLE when RECEIVED
-- ============================================================

-- Order 1 ledger (Bob's order - Fashion Paradise - RECEIVED)
INSERT INTO trx_seller_ledger (id, seller_id, order_id, amount, balance_type, created_at) VALUES
('lg000000-0000-0000-0000-000000000001', 'ss000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 2150000.0000, 'ON_HOLD', NOW() - INTERVAL '15 days'),
('lg000000-0000-0000-0000-000000000002', 'ss000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', -2150000.0000, 'ON_HOLD', NOW() - INTERVAL '10 days'),
('lg000000-0000-0000-0000-000000000003', 'ss000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 2150000.0000, 'AVAILABLE', NOW() - INTERVAL '10 days');

-- Order 2 ledger (Diana's order - TechStore - SHIPPED, still ON_HOLD)
INSERT INTO trx_seller_ledger (id, seller_id, order_id, amount, balance_type, created_at) VALUES
('lg000000-0000-0000-0000-000000000004', 'ss000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000002', 4500000.0000, 'ON_HOLD', NOW() - INTERVAL '3 days');

-- Order 3 ledger (Fiona's order - Home & Living - PAID_ON_HOLD)
INSERT INTO trx_seller_ledger (id, seller_id, order_id, amount, balance_type, created_at) VALUES
('lg000000-0000-0000-0000-000000000005', 'ss000000-0000-0000-0000-000000000003', 'or000000-0000-0000-0000-000000000003', 2950000.0000, 'ON_HOLD', NOW() - INTERVAL '1 day');

-- Order 4 ledger (George's order - TechStore - RECEIVED)
INSERT INTO trx_seller_ledger (id, seller_id, order_id, amount, balance_type, created_at) VALUES
('lg000000-0000-0000-0000-000000000006', 'ss000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000004', 14800000.0000, 'ON_HOLD', NOW() - INTERVAL '5 days'),
('lg000000-0000-0000-0000-000000000007', 'ss000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000004', -14800000.0000, 'ON_HOLD', NOW() - INTERVAL '2 days'),
('lg000000-0000-0000-0000-000000000008', 'ss000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000004', 14800000.0000, 'AVAILABLE', NOW() - INTERVAL '2 days');

-- Order 7 ledger (Charlie's mixed order - TechStore + BookHaven - RECEIVED)
INSERT INTO trx_seller_ledger (id, seller_id, order_id, amount, balance_type, created_at) VALUES
-- TechStore portion (Logitech mouse)
('lg000000-0000-0000-0000-000000000009', 'ss000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000007', 1450000.0000, 'ON_HOLD', NOW() - INTERVAL '20 days'),
('lg000000-0000-0000-0000-000000000010', 'ss000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000007', -1450000.0000, 'ON_HOLD', NOW() - INTERVAL '15 days'),
('lg000000-0000-0000-0000-000000000011', 'ss000000-0000-0000-0000-000000000001', 'or000000-0000-0000-0000-000000000007', 1450000.0000, 'AVAILABLE', NOW() - INTERVAL '15 days'),
-- BookHaven portion (books)
('lg000000-0000-0000-0000-000000000012', 'ss000000-0000-0000-0000-000000000004', 'or000000-0000-0000-0000-000000000007', 400000.0000, 'ON_HOLD', NOW() - INTERVAL '20 days'),
('lg000000-0000-0000-0000-000000000013', 'ss000000-0000-0000-0000-000000000004', 'or000000-0000-0000-0000-000000000007', -400000.0000, 'ON_HOLD', NOW() - INTERVAL '15 days'),
('lg000000-0000-0000-0000-000000000014', 'ss000000-0000-0000-0000-000000000004', 'or000000-0000-0000-0000-000000000007', 400000.0000, 'AVAILABLE', NOW() - INTERVAL '15 days');

-- ============================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ============================================================

-- -- Orders by status
-- SELECT status, COUNT(*) AS order_count, SUM(total_amount) AS total_value
-- FROM trx_orders
-- GROUP BY status
-- ORDER BY status;

-- -- Seller balances (should match cached_*_balance in mst_sellers)
-- SELECT 
--   s.id AS seller_id,
--   u.full_name AS seller_name,
--   st.store_name,
--   COALESCE(SUM(CASE WHEN l.balance_type = 'AVAILABLE' THEN l.amount ELSE 0 END), 0) AS available_balance,
--   COALESCE(SUM(CASE WHEN l.balance_type = 'ON_HOLD' THEN l.amount ELSE 0 END), 0) AS on_hold_balance
-- FROM mst_sellers s
-- JOIN mst_users u ON s.user_id = u.id
-- JOIN mst_stores st ON s.id = st.seller_id
-- LEFT JOIN trx_seller_ledger l ON s.id = l.seller_id
-- GROUP BY s.id, u.full_name, st.store_name
-- ORDER BY st.store_name;
