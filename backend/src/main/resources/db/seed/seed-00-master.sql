-- ============================================================
-- JatiStore v5.1 - Master Seed Script
-- Purpose: Execute all seed files in correct dependency order
-- Usage: psql -U postgres -d jatistore < seed-00-master.sql
-- ============================================================

-- WARNING: This script will TRUNCATE all data tables
-- Only run this in development/test environments
-- NEVER run this in production

\echo '============================================================'
\echo 'JatiStore v5.1 - Database Seeding'
\echo 'Starting at: ' :`date`
\echo '============================================================'
\echo ''

-- Verify we're not in production
DO $$
BEGIN
  IF current_database() = 'jatistore_production' THEN
    RAISE EXCEPTION 'ABORT: Cannot run seed data in production database!';
  END IF;
END $$;

\echo '✓ Environment check passed'
\echo ''

-- ============================================================
-- STEP 1: Users & Roles
-- ============================================================

\echo '[1/7] Seeding Users & Roles...'
\i seed-01-users-roles.sql
\echo '✓ Users & Roles seeded'
\echo ''

-- ============================================================
-- STEP 2: Stores & Categories
-- ============================================================

\echo '[2/7] Seeding Stores & Categories...'
\i seed-02-stores-categories.sql
\echo '✓ Stores & Categories seeded'
\echo ''

-- ============================================================
-- STEP 3: Products
-- ============================================================

\echo '[3/7] Seeding Products...'
\i seed-03-products.sql
\echo '✓ Products seeded'
\echo ''

-- ============================================================
-- STEP 4: Flash Sales
-- ============================================================

\echo '[4/7] Seeding Flash Sales...'
\i seed-04-flash-sales.sql
\echo '✓ Flash Sales seeded'
\echo ''

-- ============================================================
-- STEP 5: Payment Cards
-- ============================================================

\echo '[5/7] Seeding Payment Cards...'
\i seed-05-payment-cards.sql
\echo '✓ Payment Cards seeded'
\echo ''

-- ============================================================
-- STEP 6: Orders & Transactions
-- ============================================================

\echo '[6/7] Seeding Orders & Transactions...'
\i seed-06-orders-transactions.sql
\echo '✓ Orders & Transactions seeded'
\echo ''

-- ============================================================
-- STEP 7: Audit Trails
-- ============================================================

\echo '[7/7] Seeding Audit Trails...'
\i seed-07-audit-trails.sql
\echo '✓ Audit Trails seeded'
\echo ''

-- ============================================================
-- VERIFICATION SUMMARY
-- ============================================================

\echo '============================================================'
\echo 'Seeding Complete - Data Summary'
\echo '============================================================'
\echo ''

SELECT 'Users' AS table_name, COUNT(*) AS record_count FROM mst_users
UNION ALL
SELECT 'Admins', COUNT(*) FROM mst_admins
UNION ALL
SELECT 'Sellers', COUNT(*) FROM mst_sellers
UNION ALL
SELECT 'Stores', COUNT(*) FROM mst_stores
UNION ALL
SELECT 'Categories', COUNT(*) FROM mst_product_categories
UNION ALL
SELECT 'Products', COUNT(*) FROM mst_products WHERE deleted_at IS NULL
UNION ALL
SELECT 'Flash Sales', COUNT(*) FROM mst_flash_sales
UNION ALL
SELECT 'Flash Sale Items', COUNT(*) FROM mst_flash_sale_items
UNION ALL
SELECT 'Payment Cards', COUNT(*) FROM mst_payment_cards
UNION ALL
SELECT 'Carts', COUNT(*) FROM trx_carts
UNION ALL
SELECT 'Cart Items', COUNT(*) FROM trx_cart_items
UNION ALL
SELECT 'Orders', COUNT(*) FROM trx_orders
UNION ALL
SELECT 'Order Details', COUNT(*) FROM trx_order_details
UNION ALL
SELECT 'Transactions', COUNT(*) FROM trx_transactions
UNION ALL
SELECT 'Seller Ledger', COUNT(*) FROM trx_seller_ledger
UNION ALL
SELECT 'Audit Trails', COUNT(*) FROM trx_audit_trails
ORDER BY table_name;

\echo ''
\echo '============================================================'
\echo 'Completed at: ' :`date`
\echo '============================================================'
