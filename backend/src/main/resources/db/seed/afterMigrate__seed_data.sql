-- ============================================================
-- JatiStore v5.1 - Combined Seed Data for Flyway Callback
-- Purpose: Auto-populate database with test data after migrations
-- Trigger: afterMigrate (runs after Flyway versioned migrations)
-- WARNING: TRUNCATES all data tables - DEV ONLY
-- ============================================================

-- Safety check - prevent running in production
DO $$
BEGIN
  IF current_database() = 'jatistore_production' THEN
    RAISE EXCEPTION 'ABORT: Cannot run seed data in production database!';
  END IF;
END $$;

-- ============================================================
-- [S1] Users & Roles
-- ============================================================

TRUNCATE TABLE trx_tokens CASCADE;
TRUNCATE TABLE trx_audit_trails CASCADE;
TRUNCATE TABLE mst_admins CASCADE;
TRUNCATE TABLE mst_sellers CASCADE;
TRUNCATE TABLE mst_users CASCADE;

INSERT INTO mst_users (id, username, email, phone_number, password_hash, full_name, date_of_birth, created_at, updated_at) VALUES
('a0000000-0000-0000-0000-000000000001', 'admin_john', 'admin.john@jatistore.com', '+6281234567001', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'John Admin', '1985-06-15', NOW(), NOW()),
('a0000000-0000-0000-0000-000000000002', 'admin_sarah', 'admin.sarah@jatistore.com', '+6281234567002', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Sarah Administrator', '1988-03-22', NOW(), NOW());

INSERT INTO mst_users (id, username, email, phone_number, password_hash, full_name, date_of_birth, created_at, updated_at) VALUES
('b0000000-0000-0000-0000-000000000001', 'seller_tech', 'seller.tech@example.com', '+6281234567101', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Tech Store Owner', '1990-08-10', NOW(), NOW()),
('b0000000-0000-0000-0000-000000000002', 'seller_fashion', 'seller.fashion@example.com', '+6281234567102', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Fashion Store Owner', '1987-11-05', NOW(), NOW()),
('b0000000-0000-0000-0000-000000000003', 'seller_home', 'seller.home@example.com', '+6281234567103', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Home Store Owner', '1992-01-18', NOW(), NOW()),
('b0000000-0000-0000-0000-000000000004', 'seller_books', 'seller.books@example.com', '+6281234567104', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Book Store Owner', '1986-07-30', NOW(), NOW()),
('b0000000-0000-0000-0000-000000000005', 'seller_sports', 'seller.sports@example.com', '+6281234567105', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Sports Store Owner', '1991-04-12', NOW(), NOW());

INSERT INTO mst_users (id, username, email, phone_number, password_hash, full_name, date_of_birth, created_at, updated_at) VALUES
('c0000000-0000-0000-0000-000000000001', 'buyer_alice', 'alice@example.com', '+6281234567201', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Alice Johnson', '1995-02-14', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000002', 'buyer_bob', 'bob@example.com', '+6281234567202', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Bob Williams', '1993-09-08', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000003', 'buyer_charlie', 'charlie@example.com', '+6281234567203', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Charlie Davis', '1996-12-25', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000004', 'buyer_diana', 'diana@example.com', '+6281234567204', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Diana Martinez', '1994-06-03', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000005', 'buyer_ethan', 'ethan@example.com', '+6281234567205', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Ethan Taylor', '1997-03-19', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000006', 'buyer_fiona', 'fiona@example.com', '+6281234567206', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Fiona Anderson', '1998-11-07', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000007', 'buyer_george', 'george@example.com', '+6281234567207', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'George Thomas', '1992-08-28', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000008', 'buyer_hannah', 'hannah@example.com', '+6281234567208', '$2a$10$.Iz/E2rnCbRmZ0B9KJkitebI9Mst7JNPnD5exP09Z/4J7HXOGgNg2', 'Hannah Jackson', '1999-01-16', NOW(), NOW());

INSERT INTO mst_admins (id, user_id) VALUES
('aa000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001'),
('aa000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002');

INSERT INTO mst_sellers (id, user_id, cached_available_balance, cached_on_hold_balance, active) VALUES
('bb000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 0.0000, 0.0000, TRUE),
('bb000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 0.0000, 0.0000, TRUE),
('bb000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003', 0.0000, 0.0000, TRUE),
('bb000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004', 0.0000, 0.0000, TRUE),
('bb000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000005', 0.0000, 0.0000, FALSE);

-- ============================================================
-- [S2] Stores & Categories
-- ============================================================

TRUNCATE TABLE mst_products CASCADE;
TRUNCATE TABLE mst_stores CASCADE;
TRUNCATE TABLE mst_product_categories CASCADE;

INSERT INTO mst_stores (id, store_name, image, seller_id) VALUES
('cc000000-0000-0000-0000-000000000001', 'TechStore Indonesia', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1783989728/jatistore/products/jatistore_cf0faa4e-8f65-4c1d-86a6-201364ce4c5f.jpg', 'bb000000-0000-0000-0000-000000000001'),
('cc000000-0000-0000-0000-000000000002', 'Fashion Paradise', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1783989728/jatistore/products/jatistore_cf0faa4e-8f65-4c1d-86a6-201364ce4c5f.jpg', 'bb000000-0000-0000-0000-000000000002'),
('cc000000-0000-0000-0000-000000000003', 'Home & Living', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1783989728/jatistore/products/jatistore_cf0faa4e-8f65-4c1d-86a6-201364ce4c5f.jpg', 'bb000000-0000-0000-0000-000000000003'),
('cc000000-0000-0000-0000-000000000004', 'BookHaven', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1783989728/jatistore/products/jatistore_cf0faa4e-8f65-4c1d-86a6-201364ce4c5f.jpg', 'bb000000-0000-0000-0000-000000000004'),
('cc000000-0000-0000-0000-000000000005', 'Sports Galaxy', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1783989728/jatistore/products/jatistore_cf0faa4e-8f65-4c1d-86a6-201364ce4c5f.jpg', 'bb000000-0000-0000-0000-000000000005');

INSERT INTO mst_product_categories (id, name) VALUES
('dd000000-0000-0000-0000-000000000001', 'Electronics'),
('dd000000-0000-0000-0000-000000000002', 'Fashion'),
('dd000000-0000-0000-0000-000000000003', 'Home & Garden'),
('dd000000-0000-0000-0000-000000000004', 'Books & Media'),
('dd000000-0000-0000-0000-000000000005', 'Sports & Outdoors'),
('dd000000-0000-0000-0000-000000000006', 'Health & Beauty'),
('dd000000-0000-0000-0000-000000000007', 'Toys & Games'),
('dd000000-0000-0000-0000-000000000008', 'Automotive'),
('dd000000-0000-0000-0000-000000000009', 'Food & Beverages'),
('dd000000-0000-0000-0000-000000000010', 'Office Supplies');

-- ============================================================
-- [S3] Products
-- ============================================================

-- TechStore
INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('dd000000-0000-0000-0000-000000000001', 'cc000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000001', 'Laptop ASUS ROG Strix G15', 'Gaming laptop with RTX 4060, AMD Ryzen 7, 16GB RAM, 512GB SSD', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193333/jatistore/products_api/laptop-asus-rog-strix-g15.png', 18500000.0000, 15, NOW() - INTERVAL '30 days', NOW() - INTERVAL '5 days'),
('dd000000-0000-0000-0000-000000000002', 'cc000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000001', 'Samsung Galaxy S24 Ultra', 'Flagship smartphone with 200MP camera, Snapdragon 8 Gen 3, 12GB RAM', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193318/jatistore/products_api/samsung-galaxy-s24-ultra.webp', 19999000.0000, 25, NOW() - INTERVAL '25 days', NOW() - INTERVAL '3 days'),
('dd000000-0000-0000-0000-000000000003', 'cc000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000001', 'Sony WH-1000XM5 Headphones', 'Premium noise-cancelling wireless headphones with 30h battery life', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193353/jatistore/products_api/sony-wh-1000xm5-headphones.webp', 4500000.0000, 40, NOW() - INTERVAL '20 days', NOW() - INTERVAL '2 days'),
('dd000000-0000-0000-0000-000000000004', 'cc000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000001', 'iPad Pro 12.9 inch M2', 'Apple tablet with Liquid Retina XDR display, 256GB, WiFi + Cellular', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193348/jatistore/products_api/ipad-pro-12.9-inch-m2.jpg', 16999000.0000, 12, NOW() - INTERVAL '15 days', NOW() - INTERVAL '1 day'),
('dd000000-0000-0000-0000-000000000005', 'cc000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000001', 'Logitech MX Master 3S', 'Ergonomic wireless mouse with 8K DPI sensor and quiet clicks', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193324/jatistore/products_api/logitech-mx-master-3s.webp', 1450000.0000, 60, NOW() - INTERVAL '10 days', NOW()),
('dd000000-0000-0000-0000-000000000006', 'cc000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000001', 'Samsung 32" 4K Monitor', 'UHD monitor with HDR10, 60Hz refresh rate, USB-C connectivity', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193326/jatistore/products_api/samsung-32-4k-monitor.webp', 4200000.0000, 18, NOW() - INTERVAL '8 days', NOW()),
('dd000000-0000-0000-0000-000000000007', 'cc000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000001', 'Mechanical Keyboard RGB', 'Gaming keyboard with Cherry MX switches, RGB backlight, macro keys', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193357/jatistore/products_api/mechanical-keyboard-rgb.webp', 1200000.0000, 35, NOW() - INTERVAL '5 days', NOW());

-- Fashion Paradise
INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('dd000000-0000-0000-0000-000000000011', 'cc000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000002', 'Levi''s 501 Original Jeans', 'Classic straight fit denim jeans, available in multiple washes', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193321/jatistore/products_api/levis-501-original-jeans.webp', 850000.0000, 50, NOW() - INTERVAL '28 days', NOW() - INTERVAL '4 days'),
('dd000000-0000-0000-0000-000000000012', 'cc000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000002', 'Nike Air Max 270 Sneakers', 'Lifestyle sneakers with visible Air Max cushioning, breathable mesh', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193337/jatistore/products_api/nike-air-max-270-sneakers.webp', 1950000.0000, 30, NOW() - INTERVAL '22 days', NOW() - INTERVAL '2 days'),
('dd000000-0000-0000-0000-000000000013', 'cc000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000002', 'Cotton T-Shirt Basic Pack', 'Premium cotton tees, pack of 3, available in black/white/grey', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193320/jatistore/products_api/cotton-t-shirt-basic-pack.avif', 250000.0000, 100, NOW() - INTERVAL '18 days', NOW() - INTERVAL '1 day'),
('dd000000-0000-0000-0000-000000000014', 'cc000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000002', 'Leather Crossbody Bag', 'Genuine leather handbag with adjustable strap, multiple compartments', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193359/jatistore/products_api/leather-crossbody-bag.webp', 650000.0000, 25, NOW() - INTERVAL '12 days', NOW()),
('dd000000-0000-0000-0000-000000000015', 'cc000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000002', 'Denim Jacket Vintage', 'Classic denim jacket with distressed finish, oversized fit', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193334/jatistore/products_api/denim-jacket-vintage.webp', 550000.0000, 40, NOW() - INTERVAL '9 days', NOW()),
('dd000000-0000-0000-0000-000000000016', 'cc000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000002', 'Summer Dress Floral', 'Lightweight floral print dress, midi length, breathable fabric', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193350/jatistore/products_api/summer-dress-floral.webp', 450000.0000, 35, NOW() - INTERVAL '6 days', NOW());

-- Home & Living
INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('dd000000-0000-0000-0000-000000000021', 'cc000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000003', 'IKEA KALLAX Shelf Unit', '4x4 storage shelf, white, perfect for books and decorations', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193356/jatistore/products_api/ikea-kallax-shelf-unit.avif', 1200000.0000, 20, NOW() - INTERVAL '26 days', NOW() - INTERVAL '3 days'),
('dd000000-0000-0000-0000-000000000022', 'cc000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000003', 'Ceramic Dinnerware Set', '16-piece set for 4, includes plates, bowls, and mugs', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193342/jatistore/products_api/ceramic-dinnerware-set.webp', 850000.0000, 30, NOW() - INTERVAL '21 days', NOW() - INTERVAL '2 days'),
('dd000000-0000-0000-0000-000000000023', 'cc000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000003', 'Cotton Bed Sheet Set', 'Queen size, 300 thread count, includes pillowcases', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193316/jatistore/products_api/cotton-bed-sheet-set.jpg', 450000.0000, 45, NOW() - INTERVAL '16 days', NOW() - INTERVAL '1 day'),
('dd000000-0000-0000-0000-000000000024', 'cc000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000003', 'Wooden Coffee Table', 'Scandinavian design, solid oak wood, 120x60cm', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193336/jatistore/products_api/wooden-coffee-table.avif', 2500000.0000, 12, NOW() - INTERVAL '11 days', NOW()),
('dd000000-0000-0000-0000-000000000025', 'cc000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000003', 'LED Floor Lamp', 'Modern arc lamp with dimmer, adjustable height, energy efficient', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193344/jatistore/products_api/led-floor-lamp.webp', 950000.0000, 25, NOW() - INTERVAL '7 days', NOW()),
('dd000000-0000-0000-0000-000000000026', 'cc000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000003', 'Decorative Wall Mirror', 'Round mirror with gold frame, 80cm diameter', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193340/jatistore/products_api/decorative-wall-mirror.jpg', 650000.0000, 18, NOW() - INTERVAL '4 days', NOW());

-- BookHaven
INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('dd000000-0000-0000-0000-000000000031', 'cc000000-0000-0000-0000-000000000004', 'dd000000-0000-0000-0000-000000000004', 'Atomic Habits by James Clear', 'Bestselling self-improvement book on building good habits', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193355/jatistore/products_api/atomic-habits-by-james-clear.jpg', 150000.0000, 80, NOW() - INTERVAL '24 days', NOW() - INTERVAL '3 days'),
('dd000000-0000-0000-0000-000000000032', 'cc000000-0000-0000-0000-000000000004', 'dd000000-0000-0000-0000-000000000004', 'The Psychology of Money', 'Financial wisdom book by Morgan Housel', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193345/jatistore/products_api/the-psychology-of-money.webp', 135000.0000, 65, NOW() - INTERVAL '19 days', NOW() - INTERVAL '2 days'),
('dd000000-0000-0000-0000-000000000033', 'cc000000-0000-0000-0000-000000000004', 'dd000000-0000-0000-0000-000000000004', 'Clean Code by Robert Martin', 'Software engineering classic on writing maintainable code', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193317/jatistore/products_api/clean-code-by-robert-martin.webp', 180000.0000, 40, NOW() - INTERVAL '14 days', NOW() - INTERVAL '1 day'),
('dd000000-0000-0000-0000-000000000034', 'cc000000-0000-0000-0000-000000000004', 'dd000000-0000-0000-0000-000000000004', 'Sapiens: A Brief History', 'Yuval Noah Harari''s bestselling history of humankind', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193323/jatistore/products_api/sapiens:-a-brief-history.jpg', 160000.0000, 55, NOW() - INTERVAL '10 days', NOW()),
('dd000000-0000-0000-0000-000000000035', 'cc000000-0000-0000-0000-000000000004', 'dd000000-0000-0000-0000-000000000004', 'The Hobbit by J.R.R. Tolkien', 'Classic fantasy adventure novel, illustrated edition', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193338/jatistore/products_api/the-hobbit-by-j.r.r.-tolkien.webp', 125000.0000, 70, NOW() - INTERVAL '6 days', NOW()),
('dd000000-0000-0000-0000-000000000036', 'cc000000-0000-0000-0000-000000000004', 'dd000000-0000-0000-0000-000000000004', 'Notebook Set Premium', 'Pack of 3 leather-bound journals, lined pages, 200 pages each', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193341/jatistore/products_api/notebook-set-premium.jpg', 200000.0000, 50, NOW() - INTERVAL '3 days', NOW());

-- Sports Galaxy
INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('dd000000-0000-0000-0000-000000000041', 'cc000000-0000-0000-0000-000000000005', 'dd000000-0000-0000-0000-000000000005', 'Yoga Mat Premium TPE', 'Non-slip exercise mat, 6mm thick, eco-friendly material', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193351/jatistore/products_api/yoga-mat-premium-tpe.jpg', 350000.0000, 45, NOW() - INTERVAL '23 days', NOW() - INTERVAL '3 days'),
('dd000000-0000-0000-0000-000000000042', 'cc000000-0000-0000-0000-000000000005', 'dd000000-0000-0000-0000-000000000005', 'Adjustable Dumbbell Set', 'Pair of dumbbells, 5-25kg each, quick-change weight system', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193328/jatistore/products_api/adjustable-dumbbell-set.jpg', 2500000.0000, 15, NOW() - INTERVAL '17 days', NOW() - INTERVAL '2 days'),
('dd000000-0000-0000-0000-000000000043', 'cc000000-0000-0000-0000-000000000005', 'dd000000-0000-0000-0000-000000000005', 'Running Shoes Adidas', 'Ultraboost 23, responsive cushioning, continental rubber outsole', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193311/jatistore/products_api/running-shoes-adidas.jpg', 2200000.0000, 28, NOW() - INTERVAL '13 days', NOW() - INTERVAL '1 day'),
('dd000000-0000-0000-0000-000000000044', 'cc000000-0000-0000-0000-000000000005', 'dd000000-0000-0000-0000-000000000005', 'Camping Tent 4-Person', 'Waterproof dome tent, easy setup, includes carry bag', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193313/jatistore/products_api/camping-tent-4-person.jpg', 1800000.0000, 12, NOW() - INTERVAL '8 days', NOW()),
('dd000000-0000-0000-0000-000000000045', 'cc000000-0000-0000-0000-000000000005', 'dd000000-0000-0000-0000-000000000005', 'Badminton Racket Yonex', 'Professional grade, carbon fiber, lightweight 85g', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193314/jatistore/products_api/badminton-racket-yonex.jpg', 950000.0000, 22, NOW() - INTERVAL '5 days', NOW()),
('dd000000-0000-0000-0000-000000000046', 'cc000000-0000-0000-0000-000000000005', 'dd000000-0000-0000-0000-000000000005', 'Mountain Bike 27.5"', 'Shimano gears, disc brakes, aluminum frame, suspension fork', 'https://res.cloudinary.com/dqoqfiucw/image/upload/v1784193310/jatistore/products_api/mountain-bike-27.5.jpg', 4500000.0000, 8, NOW() - INTERVAL '2 days', NOW());

-- ============================================================
-- [S4] Flash Sales
-- ============================================================

TRUNCATE TABLE mst_flash_sale_items CASCADE;
TRUNCATE TABLE mst_flash_sales CASCADE;

INSERT INTO mst_flash_sales (id, name, start_time, end_time) VALUES
('de000000-0000-0000-0000-000000000001', 'Ramadan Sale 2026', NOW() - INTERVAL '45 days', NOW() - INTERVAL '35 days'),
('de000000-0000-0000-0000-000000000002', 'Mid-Year Mega Sale 2026', NOW() - INTERVAL '2 days', NOW() + INTERVAL '5 days'),
('de000000-0000-0000-0000-000000000003', 'Independence Day Sale 2026', NOW() + INTERVAL '7 days', NOW() + INTERVAL '10 days'),
('de000000-0000-0000-0000-000000000004', 'Year End Clearance 2026', NOW() + INTERVAL '150 days', NOW() + INTERVAL '157 days');

INSERT INTO mst_flash_sale_items (id, flash_sale_id, product_id, flash_price, remaining_quota) VALUES
('fa000000-0000-0000-0000-000000000001', 'de000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000001', 15000000.0000, 0),
('fa000000-0000-0000-0000-000000000002', 'de000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000002', 16999000.0000, 0),
('fa000000-0000-0000-0000-000000000003', 'de000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000012', 1650000.0000, 0),
('fa000000-0000-0000-0000-000000000011', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000001', 14800000.0000, 8),
('fa000000-0000-0000-0000-000000000012', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000003', 3600000.0000, 15),
('fa000000-0000-0000-0000-000000000013', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000004', 13599000.0000, 5),
('fa000000-0000-0000-0000-000000000014', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000006', 3360000.0000, 10),
('fa000000-0000-0000-0000-000000000015', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000012', 1560000.0000, 12),
('fa000000-0000-0000-0000-000000000016', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000014', 520000.0000, 18),
('fa000000-0000-0000-0000-000000000017', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000015', 440000.0000, 25),
('fa000000-0000-0000-0000-000000000018', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000024', 2000000.0000, 6),
('fa000000-0000-0000-0000-000000000019', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000025', 760000.0000, 15),
('fa000000-0000-0000-0000-000000000020', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000042', 2000000.0000, 8),
('fa000000-0000-0000-0000-000000000021', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000043', 1760000.0000, 14),
('fa000000-0000-0000-0000-000000000022', 'de000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000046', 3600000.0000, 3),
('fa000000-0000-0000-0000-000000000031', 'de000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000002', 15999000.0000, 20),
('fa000000-0000-0000-0000-000000000032', 'de000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000005', 1160000.0000, 40),
('fa000000-0000-0000-0000-000000000033', 'de000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000011', 680000.0000, 35),
('fa000000-0000-0000-0000-000000000034', 'de000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000021', 960000.0000, 15),
('fa000000-0000-0000-0000-000000000035', 'de000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000041', 280000.0000, 30);

-- ============================================================
-- [S5] Payment Cards
-- ============================================================

TRUNCATE TABLE mst_payment_cards CASCADE;

INSERT INTO mst_payment_cards (id, user_id, card_number, card_holder_name, expiry_date) VALUES
('cd000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', '4111111111114242', 'Alice Johnson', '12/28'),
('cd000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', '5155555555553333', 'Alice Johnson', '06/27'),
('cd000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000002', '4211111111114242', 'Bob Williams', '09/29'),
('cd000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000003', '5255555555554242', 'Charlie Davis', '03/28'),
('cd000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000003', '4311111111119999', 'Charlie Davis', '11/26'),
('cd000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000004', '4311111111114242', 'Diana Martinez', '08/30'),
('cd000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000005', '5455555555553333', 'Ethan Taylor', '04/27'),
('cd000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000006', '4411111111114242', 'Fiona Anderson', '07/29'),
('cd000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000007', '5555555555554242', 'George Thomas', '02/28'),
('cd000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000008', '4511111111114242', 'Hannah Jackson', '10/30'),
('cd000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000008', '4611111111113333', 'Hannah Jackson', '05/27');

-- ============================================================
-- [S6] Orders & Transactions
-- ============================================================

TRUNCATE TABLE trx_seller_ledger CASCADE;
TRUNCATE TABLE trx_transactions CASCADE;
TRUNCATE TABLE trx_order_details CASCADE;
TRUNCATE TABLE trx_orders CASCADE;
TRUNCATE TABLE trx_cart_items CASCADE;
TRUNCATE TABLE trx_carts CASCADE;

INSERT INTO trx_carts (id, user_id, created_at, updated_at) VALUES
('ca000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 hour'),
('ca000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000003', NOW() - INTERVAL '1 day', NOW() - INTERVAL '3 hours'),
('ca000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000005', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '30 minutes');

INSERT INTO trx_cart_items (id, cart_id, product_id, quantity) VALUES
('cf000000-0000-0000-0000-000000000001', 'ca000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000003', 1),
('cf000000-0000-0000-0000-000000000002', 'ca000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000031', 2),
('cf000000-0000-0000-0000-000000000003', 'ca000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000041', 1),
('cf000000-0000-0000-0000-000000000004', 'ca000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000012', 1),
('cf000000-0000-0000-0000-000000000005', 'ca000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000015', 1),
('cf000000-0000-0000-0000-000000000006', 'ca000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000001', 1);
INSERT INTO trx_orders (id, user_id, total_amount, status, created_at) VALUES
('ff000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 2150000.0000, 'RECEIVED', NOW() - INTERVAL '15 days'),
('ff000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000004', 4500000.0000, 'SHIPPED', NOW() - INTERVAL '3 days'),
('ff000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000006', 2950000.0000, 'PAID_ON_HOLD', NOW() - INTERVAL '1 day'),
('ff000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000007', 14800000.0000, 'RECEIVED', NOW() - INTERVAL '5 days'),
('ff000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000008', 850000.0000, 'CANCELLED', NOW() - INTERVAL '7 days'),
('ff000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000001', 1200000.0000, 'PENDING', NOW() - INTERVAL '2 hours'),
('ff000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000003', 1850000.0000, 'RECEIVED', NOW() - INTERVAL '20 days');

INSERT INTO trx_order_details (id, order_id, product_id, quantity, price_per_item, flash_sale, created_at) VALUES
('ef000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000012', 1, 1950000.0000, FALSE, NOW() - INTERVAL '15 days'),
('ef000000-0000-0000-0000-000000000002', 'ff000000-0000-0000-0000-000000000001', 'dd000000-0000-0000-0000-000000000013', 1, 200000.0000, FALSE, NOW() - INTERVAL '15 days'),
('ef000000-0000-0000-0000-000000000003', 'ff000000-0000-0000-0000-000000000002', 'dd000000-0000-0000-0000-000000000003', 1, 4500000.0000, FALSE, NOW() - INTERVAL '3 days'),
('ef000000-0000-0000-0000-000000000004', 'ff000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000024', 1, 2500000.0000, FALSE, NOW() - INTERVAL '1 day'),
('ef000000-0000-0000-0000-000000000005', 'ff000000-0000-0000-0000-000000000003', 'dd000000-0000-0000-0000-000000000023', 1, 450000.0000, FALSE, NOW() - INTERVAL '1 day'),
('ef000000-0000-0000-0000-000000000006', 'ff000000-0000-0000-0000-000000000004', 'dd000000-0000-0000-0000-000000000001', 1, 14800000.0000, TRUE, NOW() - INTERVAL '5 days'),
('ef000000-0000-0000-0000-000000000007', 'ff000000-0000-0000-0000-000000000005', 'dd000000-0000-0000-0000-000000000022', 1, 850000.0000, FALSE, NOW() - INTERVAL '7 days'),
('ef000000-0000-0000-0000-000000000008', 'ff000000-0000-0000-0000-000000000006', 'dd000000-0000-0000-0000-000000000007', 1, 1200000.0000, FALSE, NOW() - INTERVAL '2 hours'),
('ef000000-0000-0000-0000-000000000009', 'ff000000-0000-0000-0000-000000000007', 'dd000000-0000-0000-0000-000000000005', 1, 1450000.0000, FALSE, NOW() - INTERVAL '20 days'),
('ef000000-0000-0000-0000-000000000010', 'ff000000-0000-0000-0000-000000000007', 'dd000000-0000-0000-0000-000000000032', 2, 200000.0000, FALSE, NOW() - INTERVAL '20 days');

INSERT INTO trx_transactions (id, order_id, payment_method, payment_card_id, payment_gateway_ref, status, created_at, updated_at) VALUES
('ad000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000001', 'CARD', 'cd000000-0000-0000-0000-000000000003', 'TXN-mock-success-001', 'SUCCESS', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
('ad000000-0000-0000-0000-000000000002', 'ff000000-0000-0000-0000-000000000002', 'CARD', 'cd000000-0000-0000-0000-000000000006', 'TXN-mock-success-002', 'SUCCESS', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
('ad000000-0000-0000-0000-000000000003', 'ff000000-0000-0000-0000-000000000003', 'WALLET', NULL, 'TXN-mock-wallet-001', 'SUCCESS', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
('ad000000-0000-0000-0000-000000000004', 'ff000000-0000-0000-0000-000000000004', 'CARD', 'cd000000-0000-0000-0000-000000000009', 'TXN-mock-success-004', 'SUCCESS', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
('ad000000-0000-0000-0000-000000000005', 'ff000000-0000-0000-0000-000000000005', 'CARD', 'cd000000-0000-0000-0000-000000000011', 'TXN-mock-declined-001', 'DECLINED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
('ad000000-0000-0000-0000-000000000006', 'ff000000-0000-0000-0000-000000000006', 'CARD', 'cd000000-0000-0000-0000-000000000002', NULL, 'FAILED', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
('ad000000-0000-0000-0000-000000000007', 'ff000000-0000-0000-0000-000000000007', 'WALLET', NULL, 'TXN-mock-wallet-002', 'SUCCESS', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days');

INSERT INTO trx_seller_ledger (id, seller_id, order_id, amount, balance_type, created_at) VALUES
('ae000000-0000-0000-0000-000000000001', 'bb000000-0000-0000-0000-000000000002', 'ff000000-0000-0000-0000-000000000001', 2150000.0000, 'ON_HOLD', NOW() - INTERVAL '15 days'),
('ae000000-0000-0000-0000-000000000002', 'bb000000-0000-0000-0000-000000000002', 'ff000000-0000-0000-0000-000000000001', -2150000.0000, 'ON_HOLD', NOW() - INTERVAL '10 days'),
('ae000000-0000-0000-0000-000000000003', 'bb000000-0000-0000-0000-000000000002', 'ff000000-0000-0000-0000-000000000001', 2150000.0000, 'AVAILABLE', NOW() - INTERVAL '10 days'),
('ae000000-0000-0000-0000-000000000004', 'bb000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000002', 4500000.0000, 'ON_HOLD', NOW() - INTERVAL '3 days'),
('ae000000-0000-0000-0000-000000000005', 'bb000000-0000-0000-0000-000000000003', 'ff000000-0000-0000-0000-000000000003', 2950000.0000, 'ON_HOLD', NOW() - INTERVAL '1 day'),
('ae000000-0000-0000-0000-000000000006', 'bb000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000004', 14800000.0000, 'ON_HOLD', NOW() - INTERVAL '5 days'),
('ae000000-0000-0000-0000-000000000007', 'bb000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000004', -14800000.0000, 'ON_HOLD', NOW() - INTERVAL '2 days'),
('ae000000-0000-0000-0000-000000000008', 'bb000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000004', 14800000.0000, 'AVAILABLE', NOW() - INTERVAL '2 days'),
('ae000000-0000-0000-0000-000000000009', 'bb000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000007', 1450000.0000, 'ON_HOLD', NOW() - INTERVAL '20 days'),
('ae000000-0000-0000-0000-000000000010', 'bb000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000007', -1450000.0000, 'ON_HOLD', NOW() - INTERVAL '15 days'),
('ae000000-0000-0000-0000-000000000011', 'bb000000-0000-0000-0000-000000000001', 'ff000000-0000-0000-0000-000000000007', 1450000.0000, 'AVAILABLE', NOW() - INTERVAL '15 days'),
('ae000000-0000-0000-0000-000000000012', 'bb000000-0000-0000-0000-000000000004', 'ff000000-0000-0000-0000-000000000007', 400000.0000, 'ON_HOLD', NOW() - INTERVAL '20 days'),
('ae000000-0000-0000-0000-000000000013', 'bb000000-0000-0000-0000-000000000004', 'ff000000-0000-0000-0000-000000000007', -400000.0000, 'ON_HOLD', NOW() - INTERVAL '15 days'),
('ae000000-0000-0000-0000-000000000014', 'bb000000-0000-0000-0000-000000000004', 'ff000000-0000-0000-0000-000000000007', 400000.0000, 'AVAILABLE', NOW() - INTERVAL '15 days');

-- ============================================================
-- [S7] Audit Trails
-- ============================================================

-- TRUNCATE TABLE trx_audit_trails CASCADE;
--
-- INSERT INTO trx_audit_trails (id, user_id, entity_id, user_role, action, affected_module, description, payload, ip_address, created_at) VALUES
-- ('aaa00000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', NULL, 'ADMIN', 'LOGIN', 'AUTH', 'Admin user logged in successfully', '{"email": "admin.john@jatistore.com"}', '203.0.113.10', NOW() - INTERVAL '1 hour'),
-- ('aaa00000-0000-0000-0000-000000000002', 's0000000-0000-0000-0000-000000000001', NULL, 'SELLER', 'LOGIN', 'AUTH', 'Seller user logged in successfully', '{"email": "seller.tech@example.com"}', '203.0.113.15', NOW() - INTERVAL '3 hours'),
-- ('aaa00000-0000-0000-0000-000000000003', 'u0000000-0000-0000-0000-000000000001', NULL, 'USER', 'LOGIN', 'AUTH', 'User logged in successfully', '{"email": "alice@example.com"}', '203.0.113.20', NOW() - INTERVAL '5 hours'),
-- ('aaa00000-0000-0000-0000-000000000004', NULL, NULL, NULL, 'LOGIN_FAILED', 'AUTH', 'Failed login attempt - invalid credentials', '{"email": "unknown@example.com"}', '198.51.100.42', NOW() - INTERVAL '6 hours'),
-- ('aaa00000-0000-0000-0000-000000000011', 's0000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000001', 'SELLER', 'PRODUCT_CREATE', 'PRODUCT', 'Seller created new product: Laptop ASUS ROG Strix G15', '{"productId": "pr000000-0000-0000-0000-000000000001"}', '203.0.113.15', NOW() - INTERVAL '30 days'),
-- ('aaa00000-0000-0000-0000-000000000012', 's0000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000001', 'SELLER', 'PRODUCT_UPDATE', 'PRODUCT', 'Seller updated product price', '{"productId": "pr000000-0000-0000-0000-000000000001", "oldPrice": 20000000.00, "newPrice": 18500000.00}', '203.0.113.15', NOW() - INTERVAL '5 days'),
-- ('aaa00000-0000-0000-0000-000000000013', 's0000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000011', 'SELLER', 'PRODUCT_CREATE', 'PRODUCT', 'Seller created new product', '{"productId": "pr000000-0000-0000-0000-000000000011"}', '203.0.113.25', NOW() - INTERVAL '28 days'),
-- ('aaa00000-0000-0000-0000-000000000021', 'u0000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'USER', 'ORDER_CREATE', 'ORDERS', 'User created new order', '{"orderId": "or000000-0000-0000-0000-000000000001"}', '203.0.113.30', NOW() - INTERVAL '15 days'),
-- ('aaa00000-0000-0000-0000-000000000022', 'u0000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'USER', 'ORDER_PAID', 'ORDERS', 'Payment successful for order', '{"orderId": "or000000-0000-0000-0000-000000000001"}', '203.0.113.30', NOW() - INTERVAL '15 days'),
-- ('aaa00000-0000-0000-0000-000000000023', 's0000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'SELLER', 'ORDER_SHIPPED', 'ORDERS', 'Seller marked order as shipped', '{"orderId": "or000000-0000-0000-0000-000000000001"}', '203.0.113.25', NOW() - INTERVAL '12 days'),
-- ('aaa00000-0000-0000-0000-000000000024', 'u0000000-0000-0000-0000-000000000002', 'or000000-0000-0000-0000-000000000001', 'USER', 'ORDER_RECEIVED', 'ORDERS', 'User confirmed order delivery', '{"orderId": "or000000-0000-0000-0000-000000000001"}', '203.0.113.30', NOW() - INTERVAL '10 days'),
-- ('aaa00000-0000-0000-0000-000000000025', 'u0000000-0000-0000-0000-000000000008', 'or000000-0000-0000-0000-000000000005', 'USER', 'ORDER_CANCELLED', 'ORDERS', 'Order cancelled due to payment declined', '{"orderId": "or000000-0000-0000-0000-000000000005"}', '203.0.113.35', NOW() - INTERVAL '7 days'),
-- ('aaa00000-0000-0000-0000-000000000031', 'a0000000-0000-0000-0000-000000000001', 'fs000000-0000-0000-0000-000000000002', 'ADMIN', 'FLASH_SALE_CREATE', 'FLASH_SALES', 'Admin created Mid-Year Mega Sale', '{"name": "Mid-Year Mega Sale 2026"}', '203.0.113.10', NOW() - INTERVAL '3 days'),
-- ('aaa00000-0000-0000-0000-000000000032', 's0000000-0000-0000-0000-000000000001', 'fi000000-0000-0000-0000-000000000011', 'SELLER', 'FLASH_SALE_ITEM_ADD', 'FLASH_SALES', 'Seller added product to flash sale', '{"productId": "pr000000-0000-0000-0000-000000000001"}', '203.0.113.15', NOW() - INTERVAL '2 days'),
-- ('aaa00000-0000-0000-0000-000000000033', 'a0000000-0000-0000-0000-000000000001', 'fs000000-0000-0000-0000-000000000003', 'ADMIN', 'FLASH_SALE_CREATE', 'FLASH_SALES', 'Admin created Independence Day Sale', '{"name": "Independence Day Sale 2026"}', '203.0.113.10', NOW() - INTERVAL '1 day'),
-- ('aaa00000-0000-0000-0000-000000000041', 'a0000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'ADMIN', 'CATEGORY_CREATE', 'CATEGORIES', 'Admin created Electronics category', '{"name": "Electronics"}', '203.0.113.10', NOW() - INTERVAL '60 days'),
-- ('aaa00000-0000-0000-0000-000000000051', 'a0000000-0000-0000-0000-000000000001', 'ss000000-0000-0000-0000-000000000005', 'ADMIN', 'SELLER_DEACTIVATE', 'SELLERS', 'Admin deactivated seller account', '{"sellerId": "ss000000-0000-0000-0000-000000000005"}', '203.0.113.10', NOW() - INTERVAL '10 days'),
-- ('aaa00000-0000-0000-0000-000000000052', 'a0000000-0000-0000-0000-000000000001', 'ss000000-0000-0000-0000-000000000001', 'ADMIN', 'SELLER_ACTIVATE', 'SELLERS', 'Admin activated seller account', '{"sellerId": "ss000000-0000-0000-0000-000000000001"}', '203.0.113.10', NOW() - INTERVAL '65 days'),
-- ('aaa00000-0000-0000-0000-000000000061', 's0000000-0000-0000-0000-000000000001', NULL, 'SELLER', 'WITHDRAWAL', 'FINANCIALS', 'Seller withdrew available balance', '{"amount": 5000000.00}', '203.0.113.15', NOW() - INTERVAL '8 days'),
-- ('aaa00000-0000-0000-0000-000000000074', 'a0000000-0000-0000-0000-000000000001', NULL, 'ADMIN', 'LOGOUT', 'AUTH', 'Admin user logged out', '{}', '203.0.113.10', NOW() - INTERVAL '30 minutes');
