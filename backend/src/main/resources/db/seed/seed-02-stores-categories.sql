-- ============================================================
-- JatiStore v5.1 - Seed Data: Stores & Categories
-- Dependencies: seed-01-users-roles.sql (sellers must exist)
-- Purpose: Create seller stores and product categories
-- ============================================================

-- Clear existing data (dev/test only)
TRUNCATE TABLE mst_products CASCADE;
TRUNCATE TABLE mst_stores CASCADE;
TRUNCATE TABLE mst_product_categories CASCADE;

-- ============================================================
-- STORES (mst_stores)
-- ============================================================

INSERT INTO mst_stores (id, store_name, image, seller_id) VALUES
('st000000-0000-0000-0000-000000000001', 'TechStore Indonesia', 'https://res.cloudinary.com/jatistore/image/upload/v1/stores/techstore.jpg', 'ss000000-0000-0000-0000-000000000001'),
('st000000-0000-0000-0000-000000000002', 'Fashion Paradise', 'https://res.cloudinary.com/jatistore/image/upload/v1/stores/fashionparadise.jpg', 'ss000000-0000-0000-0000-000000000002'),
('st000000-0000-0000-0000-000000000003', 'Home & Living', 'https://res.cloudinary.com/jatistore/image/upload/v1/stores/homeliving.jpg', 'ss000000-0000-0000-0000-000000000003'),
('st000000-0000-0000-0000-000000000004', 'BookHaven', 'https://res.cloudinary.com/jatistore/image/upload/v1/stores/bookhaven.jpg', 'ss000000-0000-0000-0000-000000000004'),
('st000000-0000-0000-0000-000000000005', 'Sports Galaxy', 'https://res.cloudinary.com/jatistore/image/upload/v1/stores/sportsgalaxy.jpg', 'ss000000-0000-0000-0000-000000000005');

-- ============================================================
-- PRODUCT CATEGORIES (mst_product_categories)
-- ============================================================

INSERT INTO mst_product_categories (id, name) VALUES
('pc000000-0000-0000-0000-000000000001', 'Electronics'),
('pc000000-0000-0000-0000-000000000002', 'Fashion'),
('pc000000-0000-0000-0000-000000000003', 'Home & Garden'),
('pc000000-0000-0000-0000-000000000004', 'Books & Media'),
('pc000000-0000-0000-0000-000000000005', 'Sports & Outdoors'),
('pc000000-0000-0000-0000-000000000006', 'Health & Beauty'),
('pc000000-0000-0000-0000-000000000007', 'Toys & Games'),
('pc000000-0000-0000-0000-000000000008', 'Automotive'),
('pc000000-0000-0000-0000-000000000009', 'Food & Beverages'),
('pc000000-0000-0000-0000-000000000010', 'Office Supplies');

-- ============================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ============================================================

-- SELECT s.store_name, u.full_name AS seller_name, se.active AS seller_active
-- FROM mst_stores s
-- JOIN mst_sellers se ON s.seller_id = se.id
-- JOIN mst_users u ON se.user_id = u.id
-- ORDER BY s.store_name;

-- SELECT name AS category_name FROM mst_product_categories ORDER BY name;
