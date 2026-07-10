-- ============================================================
-- JatiStore v5.1 - Seed Data: Users & Roles
-- Dependencies: None (foundational data)
-- Purpose: Create test users with different roles (Admin, Seller, User)
-- ============================================================

-- Clear existing data (dev/test only - NEVER run in production)
TRUNCATE TABLE trx_tokens CASCADE;
TRUNCATE TABLE trx_audit_trails CASCADE;
TRUNCATE TABLE mst_admins CASCADE;
TRUNCATE TABLE mst_sellers CASCADE;
TRUNCATE TABLE mst_users CASCADE;

-- ============================================================
-- USERS (mst_users)
-- Password for all users: "password123" (bcrypt hash with salt 10)
-- ============================================================

-- Admin Users
INSERT INTO mst_users (id, username, email, phone_number, password_hash, full_name, date_of_birth, created_at, updated_at) VALUES
('a0000000-0000-0000-0000-000000000001', 'admin_john', 'admin.john@jatistore.com', '+6281234567001', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'John Admin', '1985-06-15', NOW(), NOW()),
('a0000000-0000-0000-0000-000000000002', 'admin_sarah', 'admin.sarah@jatistore.com', '+6281234567002', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Sarah Administrator', '1988-03-22', NOW(), NOW());

-- Seller Users
INSERT INTO mst_users (id, username, email, phone_number, password_hash, full_name, date_of_birth, created_at, updated_at) VALUES
('s0000000-0000-0000-0000-000000000001', 'seller_tech', 'seller.tech@example.com', '+6281234567101', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Tech Store Owner', '1990-08-10', NOW(), NOW()),
('s0000000-0000-0000-0000-000000000002', 'seller_fashion', 'seller.fashion@example.com', '+6281234567102', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Fashion Store Owner', '1987-11-05', NOW(), NOW()),
('s0000000-0000-0000-0000-000000000003', 'seller_home', 'seller.home@example.com', '+6281234567103', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Home Store Owner', '1992-01-18', NOW(), NOW()),
('s0000000-0000-0000-0000-000000000004', 'seller_books', 'seller.books@example.com', '+6281234567104', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Book Store Owner', '1986-07-30', NOW(), NOW()),
('s0000000-0000-0000-0000-000000000005', 'seller_sports', 'seller.sports@example.com', '+6281234567105', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Sports Store Owner', '1991-04-12', NOW(), NOW());

-- Regular Users (Buyers)
INSERT INTO mst_users (id, username, email, phone_number, password_hash, full_name, date_of_birth, created_at, updated_at) VALUES
('u0000000-0000-0000-0000-000000000001', 'buyer_alice', 'alice@example.com', '+6281234567201', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Alice Johnson', '1995-02-14', NOW(), NOW()),
('u0000000-0000-0000-0000-000000000002', 'buyer_bob', 'bob@example.com', '+6281234567202', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Bob Williams', '1993-09-08', NOW(), NOW()),
('u0000000-0000-0000-0000-000000000003', 'buyer_charlie', 'charlie@example.com', '+6281234567203', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Charlie Davis', '1996-12-25', NOW(), NOW()),
('u0000000-0000-0000-0000-000000000004', 'buyer_diana', 'diana@example.com', '+6281234567204', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Diana Martinez', '1994-06-03', NOW(), NOW()),
('u0000000-0000-0000-0000-000000000005', 'buyer_ethan', 'ethan@example.com', '+6281234567205', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Ethan Taylor', '1997-03-19', NOW(), NOW()),
('u0000000-0000-0000-0000-000000000006', 'buyer_fiona', 'fiona@example.com', '+6281234567206', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Fiona Anderson', '1998-11-07', NOW(), NOW()),
('u0000000-0000-0000-0000-000000000007', 'buyer_george', 'george@example.com', '+6281234567207', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'George Thomas', '1992-08-28', NOW(), NOW()),
('u0000000-0000-0000-0000-000000000008', 'buyer_hannah', 'hannah@example.com', '+6281234567208', '$2a$10$N9qo8uLOickgx2ZMRZoMye5JZ7vHx0fGhPzHGvNMF7OLbVv9E3Z6u', 'Hannah Jackson', '1999-01-16', NOW(), NOW());

-- ============================================================
-- ADMINS (mst_admins)
-- ============================================================

INSERT INTO mst_admins (id, user_id) VALUES
('aa000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001'),
('aa000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002');

-- ============================================================
-- SELLERS (mst_sellers)
-- ============================================================

INSERT INTO mst_sellers (id, user_id, cached_available_balance, cached_on_hold_balance, active) VALUES
('ss000000-0000-0000-0000-000000000001', 's0000000-0000-0000-0000-000000000001', 0.0000, 0.0000, TRUE),
('ss000000-0000-0000-0000-000000000002', 's0000000-0000-0000-0000-000000000002', 0.0000, 0.0000, TRUE),
('ss000000-0000-0000-0000-000000000003', 's0000000-0000-0000-0000-000000000003', 0.0000, 0.0000, TRUE),
('ss000000-0000-0000-0000-000000000004', 's0000000-0000-0000-0000-000000000004', 0.0000, 0.0000, TRUE),
('ss000000-0000-0000-0000-000000000005', 's0000000-0000-0000-0000-000000000005', 0.0000, 0.0000, FALSE); -- Inactive seller for testing

-- ============================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ============================================================

-- SELECT COUNT(*) AS total_users FROM mst_users;
-- SELECT COUNT(*) AS total_admins FROM mst_admins;
-- SELECT COUNT(*) AS total_sellers FROM mst_sellers;
-- SELECT u.username, u.email, u.full_name, 
--        CASE WHEN a.id IS NOT NULL THEN 'ADMIN'
--             WHEN s.id IS NOT NULL THEN 'SELLER'
--             ELSE 'USER' END AS role
-- FROM mst_users u
-- LEFT JOIN mst_admins a ON u.id = a.user_id
-- LEFT JOIN mst_sellers s ON u.id = s.user_id
-- ORDER BY role, u.username;
