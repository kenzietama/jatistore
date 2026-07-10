-- ============================================================
-- JatiStore v5.1 - Seed Data: Audit Trails
-- Dependencies: All previous seed files (logs reference user actions)
-- Purpose: Create sample audit trail entries for system activity monitoring
-- ============================================================

-- Clear existing data (dev/test only)
TRUNCATE TABLE trx_audit_trails CASCADE;

-- ============================================================
-- AUDIT TRAILS (trx_audit_trails)
-- Sample logs covering various system actions
-- ============================================================

-- Authentication events
INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
('at000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', NULL, 'ADMIN', 'LOGIN', 'AUTH', 'Admin user logged in successfully', '{"email": "admin.john@jatistore.com"}', '203.0.113.10', NOW() - INTERVAL '1 hour'),
('at000000-0000-0000-0000-000000000002', 's0000000-0000-0000-0000-000000000001', NULL, 'SELLER', 'LOGIN', 'AUTH', 'Seller user logged in successfully', '{"email": "seller.tech@example.com"}', '203.0.113.15', NOW() - INTERVAL '3 hours'),
('at000000-0000-0000-0000-000000000003', 'u0000000-0000-0000-0000-000000000001', NULL, 'USER', 'LOGIN', 'AUTH', 'User logged in successfully', '{"email": "alice@example.com"}', '203.0.113.20', NOW() - INTERVAL '5 hours'),
('at000000-0000-0000-0000-000000000004', NULL, NULL, NULL, 'LOGIN_FAILED', 'AUTH', 'Failed login attempt - invalid credentials', '{"email": "unknown@example.com"}', '198.51.100.42', NOW() - INTERVAL '6 hours');

-- Product management events
INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
('at000000-0000-0000-0000-000000000011', 's0000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000001', 'SELLER', 'PRODUCT_CREATE', 'PRODUCT', 'Seller created new product: Laptop ASUS ROG Strix G15', '{"productId": "pr000000-0000-0000-0000-000000000001", "name": "Laptop ASUS ROG Strix G15", "price": 18500000.00}', '203.0.113.15', NOW() - INTERVAL '30 days'),
('at000000-0000-0000-0000-000000000012', 's0000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000001', 'SELLER', 'PRODUCT_UPDATE', 'PRODUCT', 'Seller updated product price', '{"productId": "pr000000-0000-0000-0000-000000000001", "oldPrice": 20000000.00, "newPrice": 18500000.00}', '203.0.113.15', NOW() - INTERVAL '5 days'),
('at000000-0000-0000-0000-000000000013', 's0000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000011', 'SELLER', 'PRODUCT_CREATE', 'PRODUCT', 'Seller created new product: Levi''s 501 Original Jeans', '{"productId": "pr000000-0000-0000-0000-000000000011", "name": "Levi''s 501 Original Jeans", "price": 850000.00}', '203.0.113.25', NOW() - INTERVAL '28 days');

-- Order events
INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
('at000000-0000-0000-0000-000000000021', 'u0000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'USER', 'ORDER_CREATE', 'ORDERS', 'User created new order', '{"orderId": "or000000-0000-0000-0000-000000000001", "totalAmount": 2150000.00, "itemCount": 2}', '203.0.113.30', NOW() - INTERVAL '15 days'),
('at000000-0000-0000-0000-000000000022', 'u0000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'USER', 'ORDER_PAID', 'ORDERS', 'Payment successful for order', '{"orderId": "or000000-0000-0000-0000-000000000001", "paymentMethod": "CARD", "transactionId": "tx000000-0000-0000-0000-000000000001"}', '203.0.113.30', NOW() - INTERVAL '15 days'),
('at000000-0000-0000-0000-000000000023', 's0000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'SELLER', 'ORDER_SHIPPED', 'ORDERS', 'Seller marked order as shipped', '{"orderId": "or000000-0000-0000-0000-000000000001", "trackingNumber": "JNE1234567890"}', '203.0.113.25', NOW() - INTERVAL '12 days'),
('at000000-0000-0000-0000-000000000024', 'u0000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'USER', 'ORDER_RECEIVED', 'ORDERS', 'User confirmed order delivery', '{"orderId": "or000000-0000-0000-0000-000000000001"}', '203.0.113.30', NOW() - INTERVAL '10 days'),
('at000000-0000-0000-0000-000000000025', 'u0000000-0000-0000-0000-000000000008', 'or000000-0000-0000-0000-000000000005', 'USER', 'ORDER_CANCELLED', 'ORDERS', 'Order cancelled due to payment declined', '{"orderId": "or000000-0000-0000-0000-000000000005", "reason": "PAYMENT_DECLINED"}', '203.0.113.35', NOW() - INTERVAL '7 days');

-- Flash sale events (admin actions)
INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
('at000000-0000-0000-0000-000000000031', 'a0000000-0000-0000-0000-000000000001', 'fs000000-0000-0000-0000-000000000002', 'ADMIN', 'FLASH_SALE_CREATE', 'FLASH_SALES', 'Admin created new flash sale event', '{"flashSaleId": "fs000000-0000-0000-0000-000000000002", "name": "Mid-Year Mega Sale 2026", "startTime": "2026-07-08T00:00:00Z", "endTime": "2026-07-15T23:59:59Z"}', '203.0.113.10', NOW() - INTERVAL '3 days'),
('at000000-0000-0000-0000-000000000032', 's0000000-0000-0000-0000-000000000001', 'fi000000-0000-0000-0000-000000000011', 'SELLER', 'FLASH_SALE_ITEM_ADD', 'FLASH_SALES', 'Seller added product to flash sale', '{"flashSaleItemId": "fi000000-0000-0000-0000-000000000011", "productId": "pr000000-0000-0000-0000-000000000001", "flashPrice": 14800000.00, "quota": 10}', '203.0.113.15', NOW() - INTERVAL '2 days'),
('at000000-0000-0000-0000-000000000033', 'a0000000-0000-0000-0000-000000000001', 'fs000000-0000-0000-0000-000000000003', 'ADMIN', 'FLASH_SALE_CREATE', 'FLASH_SALES', 'Admin created upcoming flash sale', '{"flashSaleId": "fs000000-0000-0000-0000-000000000003", "name": "Independence Day Sale 2026"}', '203.0.113.10', NOW() - INTERVAL '1 day');

-- Category management (admin actions)
INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
('at000000-0000-0000-0000-000000000041', 'a0000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'ADMIN', 'CATEGORY_CREATE', 'CATEGORIES', 'Admin created product category', '{"categoryId": "pc000000-0000-0000-0000-000000000001", "name": "Electronics"}', '203.0.113.10', NOW() - INTERVAL '60 days'),
('at000000-0000-0000-0000-000000000042', 'a0000000-0000-0000-0000-000000000002', 'pc000000-0000-0000-0000-000000000002', 'ADMIN', 'CATEGORY_CREATE', 'CATEGORIES', 'Admin created product category', '{"categoryId": "pc000000-0000-0000-0000-000000000002", "name": "Fashion"}', '203.0.113.11', NOW() - INTERVAL '60 days');

-- Seller management (admin actions)
INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
('at000000-0000-0000-0000-000000000051', 'a0000000-0000-0000-0000-000000000001', 'ss000000-0000-0000-0000-000000000005', 'ADMIN', 'SELLER_DEACTIVATE', 'SELLERS', 'Admin deactivated seller account', '{"sellerId": "ss000000-0000-0000-0000-000000000005", "reason": "Violation of platform policy"}', '203.0.113.10', NOW() - INTERVAL '10 days'),
('at000000-0000-0000-0000-000000000052', 'a0000000-0000-0000-0000-000000000001', 'ss000000-0000-0000-0000-000000000001', 'ADMIN', 'SELLER_ACTIVATE', 'SELLERS', 'Admin activated seller account', '{"sellerId": "ss000000-0000-0000-0000-000000000001"}', '203.0.113.10', NOW() - INTERVAL '65 days');

-- Withdrawal events
INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
('at000000-0000-0000-0000-000000000061', 's0000000-0000-0000-0000-000000000001', NULL, 'SELLER', 'WITHDRAWAL', 'FINANCIALS', 'Seller withdrew available balance', '{"amount": 5000000.00, "bankAccount": "***1234", "status": "PENDING"}', '203.0.113.15', NOW() - INTERVAL '8 days'),
('at000000-0000-0000-0000-000000000062', 's0000000-0000-0000-0000-000000000002', NULL, 'SELLER', 'WITHDRAWAL', 'FINANCIALS', 'Seller withdrew available balance', '{"amount": 2150000.00, "bankAccount": "***5678", "status": "PENDING"}', '203.0.113.25', NOW() - INTERVAL '5 days');

-- Recent session logs
INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
('at000000-0000-0000-0000-000000000071', 'u0000000-0000-0000-0000-000000000003', NULL, 'USER', 'LOGIN', 'AUTH', 'User logged in successfully', '{"email": "charlie@example.com"}', '203.0.113.40', NOW() - INTERVAL '2 hours'),
('at000000-0000-0000-0000-000000000072', 'u0000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000012', 'USER', 'PRODUCT_VIEW', 'PRODUCT', 'User viewed product details', '{"productId": "pr000000-0000-0000-0000-000000000012", "productName": "Nike Air Max 270 Sneakers"}', '203.0.113.40', NOW() - INTERVAL '90 minutes'),
('at000000-0000-0000-0000-000000000073', 'u0000000-0000-0000-0000-000000000003', 'ci000000-0000-0000-0000-000000000004', 'USER', 'CART_ADD', 'CART', 'User added item to cart', '{"productId": "pr000000-0000-0000-0000-000000000012", "quantity": 1}', '203.0.113.40', NOW() - INTERVAL '1 hour'),
('at000000-0000-0000-0000-000000000074', 'a0000000-0000-0000-0000-000000000001', NULL, 'ADMIN', 'LOGOUT', 'AUTH', 'Admin user logged out', '{"sessionDuration": "3600s"}', '203.0.113.10', NOW() - INTERVAL '30 minutes');

-- ============================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ============================================================

-- -- Audit logs by action type
-- SELECT action, COUNT(*) AS log_count
-- FROM trx_audit_trails
-- GROUP BY action
-- ORDER BY log_count DESC;

-- -- Recent audit logs (last 24 hours)
-- SELECT 
--   created_at,
--   COALESCE(u.username, 'SYSTEM') AS actor,
--   user_role,
--   action,
--   affected_module,
--   description
-- FROM trx_audit_trails at
-- LEFT JOIN mst_users u ON at.user_id = u.id
-- WHERE created_at >= NOW() - INTERVAL '24 hours'
-- ORDER BY created_at DESC;

-- -- Audit logs by user
-- SELECT 
--   u.username,
--   u.full_name,
--   at.user_role,
--   COUNT(*) AS action_count,
--   MAX(at.created_at) AS last_activity
-- FROM trx_audit_trails at
-- JOIN mst_users u ON at.user_id = u.id
-- GROUP BY u.username, u.full_name, at.user_role
-- ORDER BY action_count DESC;
