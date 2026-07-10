-- ============================================================
-- JatiStore v5.1 - Seed Data: Flash Sales
-- Dependencies: seed-03-products.sql (products must exist)
-- Purpose: Create flash sale events and attach products
-- ============================================================

-- Clear existing data (dev/test only)
TRUNCATE TABLE mst_flash_sale_items CASCADE;
TRUNCATE TABLE mst_flash_sales CASCADE;

-- ============================================================
-- FLASH SALES (mst_flash_sales)
-- ============================================================

-- Past Flash Sale (Ended)
INSERT INTO mst_flash_sales (id, name, start_time, end_time) VALUES
('fs000000-0000-0000-0000-000000000001', 'Ramadan Sale 2026', 
 NOW() - INTERVAL '45 days', 
 NOW() - INTERVAL '35 days');

-- Current/Active Flash Sale (happening now)
INSERT INTO mst_flash_sales (id, name, start_time, end_time) VALUES
('fs000000-0000-0000-0000-000000000002', 'Mid-Year Mega Sale 2026', 
 NOW() - INTERVAL '2 days', 
 NOW() + INTERVAL '5 days');

-- Upcoming Flash Sale (not started yet)
INSERT INTO mst_flash_sales (id, name, start_time, end_time) VALUES
('fs000000-0000-0000-0000-000000000003', 'Independence Day Sale 2026', 
 NOW() + INTERVAL '7 days', 
 NOW() + INTERVAL '10 days');

-- Future Flash Sale (scheduled)
INSERT INTO mst_flash_sales (id, name, start_time, end_time) VALUES
('fs000000-0000-0000-0000-000000000004', 'Year End Clearance 2026', 
 NOW() + INTERVAL '150 days', 
 NOW() + INTERVAL '157 days');

-- ============================================================
-- FLASH SALE ITEMS (mst_flash_sale_items)
-- Active flash sale products with discounted prices
-- ============================================================

-- Past Flash Sale Items (already ended)
INSERT INTO mst_flash_sale_items (id, flash_sale_id, product_id, flash_price, remaining_quota) VALUES
('fi000000-0000-0000-0000-000000000001', 'fs000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000001', 15000000.0000, 0), -- Laptop 19% off
('fi000000-0000-0000-0000-000000000002', 'fs000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000002', 16999000.0000, 0), -- Samsung phone 15% off
('fi000000-0000-0000-0000-000000000003', 'fs000000-0000-0000-0000-000000000001', 'pr000000-0000-0000-0000-000000000012', 1650000.0000, 0); -- Nike shoes 15% off

-- Current/Active Flash Sale Items (happening NOW)
INSERT INTO mst_flash_sale_items (id, flash_sale_id, product_id, flash_price, remaining_quota) VALUES
-- Electronics
('fi000000-0000-0000-0000-000000000011', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000001', 14800000.0000, 8),  -- Laptop 20% off, 8 left
('fi000000-0000-0000-0000-000000000012', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000003', 3600000.0000, 15),  -- Sony headphones 20% off
('fi000000-0000-0000-0000-000000000013', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000004', 13599000.0000, 5),  -- iPad 20% off, low stock
('fi000000-0000-0000-0000-000000000014', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000006', 3360000.0000, 10), -- Samsung monitor 20% off

-- Fashion
('fi000000-0000-0000-0000-000000000015', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000012', 1560000.0000, 12), -- Nike shoes 20% off
('fi000000-0000-0000-0000-000000000016', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000014', 520000.0000, 18),  -- Leather bag 20% off
('fi000000-0000-0000-0000-000000000017', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000015', 440000.0000, 25),  -- Denim jacket 20% off

-- Home & Living
('fi000000-0000-0000-0000-000000000018', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000024', 2000000.0000, 6),  -- Coffee table 20% off
('fi000000-0000-0000-0000-000000000019', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000025', 760000.0000, 15),  -- LED lamp 20% off

-- Sports
('fi000000-0000-0000-0000-000000000020', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000042', 2000000.0000, 8),  -- Dumbbells 20% off
('fi000000-0000-0000-0000-000000000021', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000043', 1760000.0000, 14), -- Running shoes 20% off
('fi000000-0000-0000-0000-000000000022', 'fs000000-0000-0000-0000-000000000002', 'pr000000-0000-0000-0000-000000000046', 3600000.0000, 3);  -- Mountain bike 20% off, very low stock

-- Upcoming Flash Sale Items (scheduled but not active yet)
INSERT INTO mst_flash_sale_items (id, flash_sale_id, product_id, flash_price, remaining_quota) VALUES
('fi000000-0000-0000-0000-000000000031', 'fs000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000002', 15999000.0000, 20), -- Samsung phone 20% off
('fi000000-0000-0000-0000-000000000032', 'fs000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000005', 1160000.0000, 40),  -- Logitech mouse 20% off
('fi000000-0000-0000-0000-000000000033', 'fs000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000011', 680000.0000, 35),   -- Levi's jeans 20% off
('fi000000-0000-0000-0000-000000000034', 'fs000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000021', 960000.0000, 15),   -- IKEA shelf 20% off
('fi000000-0000-0000-0000-000000000035', 'fs000000-0000-0000-0000-000000000003', 'pr000000-0000-0000-0000-000000000041', 280000.0000, 30);   -- Yoga mat 20% off

-- ============================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ============================================================

-- -- Flash sales by status
-- SELECT 
--   name,
--   start_time,
--   end_time,
--   CASE 
--     WHEN NOW() < start_time THEN 'UPCOMING'
--     WHEN NOW() BETWEEN start_time AND end_time THEN 'ACTIVE'
--     ELSE 'ENDED'
--   END AS status,
--   (SELECT COUNT(*) FROM mst_flash_sale_items WHERE flash_sale_id = mst_flash_sales.id) AS item_count
-- FROM mst_flash_sales
-- ORDER BY start_time DESC;

-- -- Active flash sale products with discount percentage
-- SELECT 
--   fs.name AS flash_sale_name,
--   p.name AS product_name,
--   s.store_name,
--   p.price AS original_price,
--   fsi.flash_price,
--   ROUND(((p.price - fsi.flash_price) / p.price * 100)::numeric, 1) AS discount_percent,
--   fsi.remaining_quota,
--   p.stock AS total_stock
-- FROM mst_flash_sales fs
-- JOIN mst_flash_sale_items fsi ON fs.id = fsi.flash_sale_id
-- JOIN mst_products p ON fsi.product_id = p.id
-- JOIN mst_stores s ON p.store_id = s.id
-- WHERE NOW() BETWEEN fs.start_time AND fs.end_time
-- ORDER BY discount_percent DESC;
