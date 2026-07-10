-- ============================================================
-- JatiStore v5.1 - Seed Data: Products
-- Dependencies: seed-02-stores-categories.sql (stores and categories must exist)
-- Purpose: Create diverse product catalog across all stores
-- ============================================================

-- Clear existing data (dev/test only)
TRUNCATE TABLE mst_products CASCADE;

-- ============================================================
-- PRODUCTS - TechStore Indonesia (Electronics)
-- ============================================================

INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('pr000000-0000-0000-0000-000000000001', 'st000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'Laptop ASUS ROG Strix G15', 'Gaming laptop with RTX 4060, AMD Ryzen 7, 16GB RAM, 512GB SSD', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/laptop-asus-rog.jpg', 18500000.0000, 15, NOW() - INTERVAL '30 days', NOW() - INTERVAL '5 days'),
('pr000000-0000-0000-0000-000000000002', 'st000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'Samsung Galaxy S24 Ultra', 'Flagship smartphone with 200MP camera, Snapdragon 8 Gen 3, 12GB RAM', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/samsung-s24-ultra.jpg', 19999000.0000, 25, NOW() - INTERVAL '25 days', NOW() - INTERVAL '3 days'),
('pr000000-0000-0000-0000-000000000003', 'st000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'Sony WH-1000XM5 Headphones', 'Premium noise-cancelling wireless headphones with 30h battery life', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/sony-wh1000xm5.jpg', 4500000.0000, 40, NOW() - INTERVAL '20 days', NOW() - INTERVAL '2 days'),
('pr000000-0000-0000-0000-000000000004', 'st000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'iPad Pro 12.9 inch M2', 'Apple tablet with Liquid Retina XDR display, 256GB, WiFi + Cellular', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/ipad-pro-m2.jpg', 16999000.0000, 12, NOW() - INTERVAL '15 days', NOW() - INTERVAL '1 day'),
('pr000000-0000-0000-0000-000000000005', 'st000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'Logitech MX Master 3S', 'Ergonomic wireless mouse with 8K DPI sensor and quiet clicks', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/logitech-mx-master-3s.jpg', 1450000.0000, 60, NOW() - INTERVAL '10 days', NOW()),
('pr000000-0000-0000-0000-000000000006', 'st000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'Samsung 32" 4K Monitor', 'UHD monitor with HDR10, 60Hz refresh rate, USB-C connectivity', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/samsung-monitor-32.jpg', 4200000.0000, 18, NOW() - INTERVAL '8 days', NOW()),
('pr000000-0000-0000-0000-000000000007', 'st000000-0000-0000-0000-000000000001', 'pc000000-0000-0000-0000-000000000001', 'Mechanical Keyboard RGB', 'Gaming keyboard with Cherry MX switches, RGB backlight, macro keys', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/keyboard-rgb.jpg', 1200000.0000, 35, NOW() - INTERVAL '5 days', NOW());

-- ============================================================
-- PRODUCTS - Fashion Paradise (Fashion)
-- ============================================================

INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('pr000000-0000-0000-0000-000000000011', 'st000000-0000-0000-0000-000000000002', 'pc000000-0000-0000-0000-000000000002', 'Levi''s 501 Original Jeans', 'Classic straight fit denim jeans, available in multiple washes', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/levis-501.jpg', 850000.0000, 50, NOW() - INTERVAL '28 days', NOW() - INTERVAL '4 days'),
('pr000000-0000-0000-0000-000000000012', 'st000000-0000-0000-0000-000000000002', 'pc000000-0000-0000-0000-000000000002', 'Nike Air Max 270 Sneakers', 'Lifestyle sneakers with visible Air Max cushioning, breathable mesh', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/nike-air-max-270.jpg', 1950000.0000, 30, NOW() - INTERVAL '22 days', NOW() - INTERVAL '2 days'),
('pr000000-0000-0000-0000-000000000013', 'st000000-0000-0000-0000-000000000002', 'pc000000-0000-0000-0000-000000000002', 'Cotton T-Shirt Basic Pack', 'Premium cotton tees, pack of 3, available in black/white/grey', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/tshirt-basic-pack.jpg', 250000.0000, 100, NOW() - INTERVAL '18 days', NOW() - INTERVAL '1 day'),
('pr000000-0000-0000-0000-000000000014', 'st000000-0000-0000-0000-000000000002', 'pc000000-0000-0000-0000-000000000002', 'Leather Crossbody Bag', 'Genuine leather handbag with adjustable strap, multiple compartments', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/leather-crossbody.jpg', 650000.0000, 25, NOW() - INTERVAL '12 days', NOW()),
('pr000000-0000-0000-0000-000000000015', 'st000000-0000-0000-0000-000000000002', 'pc000000-0000-0000-0000-000000000002', 'Denim Jacket Vintage', 'Classic denim jacket with distressed finish, oversized fit', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/denim-jacket.jpg', 550000.0000, 40, NOW() - INTERVAL '9 days', NOW()),
('pr000000-0000-0000-0000-000000000016', 'st000000-0000-0000-0000-000000000002', 'pc000000-0000-0000-0000-000000000002', 'Summer Dress Floral', 'Lightweight floral print dress, midi length, breathable fabric', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/summer-dress.jpg', 450000.0000, 35, NOW() - INTERVAL '6 days', NOW());

-- ============================================================
-- PRODUCTS - Home & Living (Home & Garden)
-- ============================================================

INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('pr000000-0000-0000-0000-000000000021', 'st000000-0000-0000-0000-000000000003', 'pc000000-0000-0000-0000-000000000003', 'IKEA KALLAX Shelf Unit', '4x4 storage shelf, white, perfect for books and decorations', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/ikea-kallax.jpg', 1200000.0000, 20, NOW() - INTERVAL '26 days', NOW() - INTERVAL '3 days'),
('pr000000-0000-0000-0000-000000000022', 'st000000-0000-0000-0000-000000000003', 'pc000000-0000-0000-0000-000000000003', 'Ceramic Dinnerware Set', '16-piece set for 4, includes plates, bowls, and mugs', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/ceramic-dinnerware.jpg', 850000.0000, 30, NOW() - INTERVAL '21 days', NOW() - INTERVAL '2 days'),
('pr000000-0000-0000-0000-000000000023', 'st000000-0000-0000-0000-000000000003', 'pc000000-0000-0000-0000-000000000003', 'Cotton Bed Sheet Set', 'Queen size, 300 thread count, includes pillowcases', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/bed-sheet-cotton.jpg', 450000.0000, 45, NOW() - INTERVAL '16 days', NOW() - INTERVAL '1 day'),
('pr000000-0000-0000-0000-000000000024', 'st000000-0000-0000-0000-000000000003', 'pc000000-0000-0000-0000-000000000003', 'Wooden Coffee Table', 'Scandinavian design, solid oak wood, 120x60cm', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/coffee-table-oak.jpg', 2500000.0000, 12, NOW() - INTERVAL '11 days', NOW()),
('pr000000-0000-0000-0000-000000000025', 'st000000-0000-0000-0000-000000000003', 'pc000000-0000-0000-0000-000000000003', 'LED Floor Lamp', 'Modern arc lamp with dimmer, adjustable height, energy efficient', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/led-floor-lamp.jpg', 950000.0000, 25, NOW() - INTERVAL '7 days', NOW()),
('pr000000-0000-0000-0000-000000000026', 'st000000-0000-0000-0000-000000000003', 'pc000000-0000-0000-0000-000000000003', 'Decorative Wall Mirror', 'Round mirror with gold frame, 80cm diameter', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/wall-mirror-gold.jpg', 650000.0000, 18, NOW() - INTERVAL '4 days', NOW());

-- ============================================================
-- PRODUCTS - BookHaven (Books & Media)
-- ============================================================

INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('pr000000-0000-0000-0000-000000000031', 'st000000-0000-0000-0000-000000000004', 'pc000000-0000-0000-0000-000000000004', 'Atomic Habits by James Clear', 'Bestselling self-improvement book on building good habits', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/atomic-habits.jpg', 150000.0000, 80, NOW() - INTERVAL '24 days', NOW() - INTERVAL '3 days'),
('pr000000-0000-0000-0000-000000000032', 'st000000-0000-0000-0000-000000000004', 'pc000000-0000-0000-0000-000000000004', 'The Psychology of Money', 'Financial wisdom book by Morgan Housel', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/psychology-of-money.jpg', 135000.0000, 65, NOW() - INTERVAL '19 days', NOW() - INTERVAL '2 days'),
('pr000000-0000-0000-0000-000000000033', 'st000000-0000-0000-0000-000000000004', 'pc000000-0000-0000-0000-000000000004', 'Clean Code by Robert Martin', 'Software engineering classic on writing maintainable code', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/clean-code.jpg', 180000.0000, 40, NOW() - INTERVAL '14 days', NOW() - INTERVAL '1 day'),
('pr000000-0000-0000-0000-000000000034', 'st000000-0000-0000-0000-000000000004', 'pc000000-0000-0000-0000-000000000004', 'Sapiens: A Brief History', 'Yuval Noah Harari''s bestselling history of humankind', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/sapiens.jpg', 160000.0000, 55, NOW() - INTERVAL '10 days', NOW()),
('pr000000-0000-0000-0000-000000000035', 'st000000-0000-0000-0000-000000000004', 'pc000000-0000-0000-0000-000000000004', 'The Hobbit by J.R.R. Tolkien', 'Classic fantasy adventure novel, illustrated edition', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/the-hobbit.jpg', 125000.0000, 70, NOW() - INTERVAL '6 days', NOW()),
('pr000000-0000-0000-0000-000000000036', 'st000000-0000-0000-0000-000000000004', 'pc000000-0000-0000-0000-000000000004', 'Notebook Set Premium', 'Pack of 3 leather-bound journals, lined pages, 200 pages each', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/notebook-set.jpg', 200000.0000, 50, NOW() - INTERVAL '3 days', NOW());

-- ============================================================
-- PRODUCTS - Sports Galaxy (Sports & Outdoors)
-- ============================================================

INSERT INTO mst_products (id, store_id, product_category_id, name, description, image, price, stock, created_at, updated_at) VALUES
('pr000000-0000-0000-0000-000000000041', 'st000000-0000-0000-0000-000000000005', 'pc000000-0000-0000-0000-000000000005', 'Yoga Mat Premium TPE', 'Non-slip exercise mat, 6mm thick, eco-friendly material', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/yoga-mat-tpe.jpg', 350000.0000, 45, NOW() - INTERVAL '23 days', NOW() - INTERVAL '3 days'),
('pr000000-0000-0000-0000-000000000042', 'st000000-0000-0000-0000-000000000005', 'pc000000-0000-0000-0000-000000000005', 'Adjustable Dumbbell Set', 'Pair of dumbbells, 5-25kg each, quick-change weight system', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/dumbbell-adjustable.jpg', 2500000.0000, 15, NOW() - INTERVAL '17 days', NOW() - INTERVAL '2 days'),
('pr000000-0000-0000-0000-000000000043', 'st000000-0000-0000-0000-000000000005', 'pc000000-0000-0000-0000-000000000005', 'Running Shoes Adidas', 'Ultraboost 23, responsive cushioning, continental rubber outsole', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/adidas-ultraboost.jpg', 2200000.0000, 28, NOW() - INTERVAL '13 days', NOW() - INTERVAL '1 day'),
('pr000000-0000-0000-0000-000000000044', 'st000000-0000-0000-0000-000000000005', 'pc000000-0000-0000-0000-000000000005', 'Camping Tent 4-Person', 'Waterproof dome tent, easy setup, includes carry bag', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/camping-tent-4p.jpg', 1800000.0000, 12, NOW() - INTERVAL '8 days', NOW()),
('pr000000-0000-0000-0000-000000000045', 'st000000-0000-0000-0000-000000000005', 'pc000000-0000-0000-0000-000000000005', 'Badminton Racket Yonex', 'Professional grade, carbon fiber, lightweight 85g', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/yonex-racket.jpg', 950000.0000, 22, NOW() - INTERVAL '5 days', NOW()),
('pr000000-0000-0000-0000-000000000046', 'st000000-0000-0000-0000-000000000005', 'pc000000-0000-0000-0000-000000000005', 'Mountain Bike 27.5"', 'Shimano gears, disc brakes, aluminum frame, suspension fork', 'https://res.cloudinary.com/jatistore/image/upload/v1/products/mountain-bike.jpg', 4500000.0000, 8, NOW() - INTERVAL '2 days', NOW());

-- ============================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ============================================================

-- SELECT COUNT(*) AS total_products FROM mst_products WHERE deleted_at IS NULL;
-- SELECT pc.name AS category, COUNT(p.id) AS product_count
-- FROM mst_product_categories pc
-- LEFT JOIN mst_products p ON pc.id = p.product_category_id AND p.deleted_at IS NULL
-- GROUP BY pc.name
-- ORDER BY product_count DESC;
-- SELECT s.store_name, COUNT(p.id) AS product_count, 
--        MIN(p.price) AS min_price, MAX(p.price) AS max_price,
--        SUM(p.stock) AS total_stock
-- FROM mst_stores s
-- LEFT JOIN mst_products p ON s.id = p.store_id AND p.deleted_at IS NULL
-- GROUP BY s.store_name
-- ORDER BY s.store_name;
