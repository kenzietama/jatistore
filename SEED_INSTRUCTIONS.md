# JatiStore Seed Data Instructions

## Seed Data Location

**File**: `seed.sql` (918 lines, ~390KB)
**Source**: Copied from `backend/src/main/resources/db/seed/afterMigrate__seed_data.sql`

## What's Included

- **2 Admin users** (john, sarah) - password: `password123`
- **5 Seller users** with stores (Tech, Fashion, Home, Books, Sports)
- **8 Buyer users** (alice, bob, charlie, diana, ethan, fiona, george, hannah)
- **625+ Products** across all categories (Electronics, Fashion, Home, Books, Sports)
- **10 Product categories**
- **Flash sales** with items and quota
- **Sample orders** and transactions
- **User balances** for testing payments

All passwords are bcrypt-hashed: `password123`

## How to Run Seed Data

### After Supabase Database Setup

1. **Copy seed.sql to your local machine**
   ```bash
   # The file is in project root: seed.sql
   ```

2. **Go to Supabase Dashboard**
   - Navigate to your project
   - Click "SQL Editor" in left sidebar

3. **Execute the seed script**
   - Click "New Query"
   - Copy-paste contents of `seed.sql` (or upload file if supported)
   - Click "Run" or press Ctrl+Enter
   - Wait for completion (~30 seconds for 918 lines)

### Safety Features

The seed script includes:
```sql
-- Safety check - prevent running in production
DO $$
BEGIN
    IF current_database() = 'jatistore_production' THEN
        RAISE EXCEPTION 'ABORT: Cannot run seed data in production database!';
    END IF;
END $$;
```

**Production Protection**: Script will abort if database name is `jatistore_production`

### What Gets Seeded

1. **Users & Roles** (truncates existing data first)
   - Admins: `admin.john@jatistore.com`, `admin.sarah@jatistore.com`
   - Sellers: `seller.tech@example.com`, `seller.fashion@example.com`, etc.
   - Buyers: `alice@example.com`, `bob@example.com`, etc.

2. **Stores** (5 stores linked to sellers)
   - TechStore Indonesia
   - Fashion Paradise
   - Home & Living
   - BookHaven
   - Sports Galaxy

3. **Product Categories** (10 categories)
   - Electronics, Fashion, Home & Garden, Books & Media, Sports & Outdoors, etc.

4. **Products** (625+ products with realistic data)
   - Laptops, smartphones, headphones, monitors, peripherals
   - T-shirts, jeans, sneakers, bags, jackets
   - Furniture, bedding, dinnerware
   - Books, notebooks
   - Yoga mats, dumbbells, tents, bikes

5. **Flash Sales** (sample flash sale events)
   - Active flash sales with start/end times
   - Flash sale items with discounted prices
   - Quota management

6. **Orders & Transactions** (sample order history)
   - Pending, paid, shipped, received, cancelled orders
   - Payment records

7. **User Wallets** (sample balance data)
   - Some users have wallet balance for testing wallet payments

## Test User Credentials

### Admin Users
```
Email: admin.john@jatistore.com
Password: password123

Email: admin.sarah@jatistore.com
Password: password123
```

### Seller Users
```
Email: seller.tech@example.com
Password: password123

Email: seller.fashion@example.com
Password: password123

(+ 3 more sellers)
```

### Buyer Users
```
Email: alice@example.com
Password: password123

Email: bob@example.com
Password: password123

Email: charlie@example.com
Password: password123

(+ 5 more buyers)
```

## Verification After Seeding

Run these queries in Supabase SQL Editor to verify:

```sql
-- Check users
SELECT COUNT(*) as total_users FROM mst_users;
-- Expected: 15 users (2 admins + 5 sellers + 8 buyers)

-- Check products
SELECT COUNT(*) as total_products FROM mst_products;
-- Expected: 625+ products

-- Check stores
SELECT store_name FROM mst_stores;
-- Expected: 5 stores

-- Check categories
SELECT name FROM mst_product_categories ORDER BY name;
-- Expected: 10 categories
```

## Troubleshooting

### Error: "relation does not exist"
**Cause**: Flyway migrations haven't run yet
**Solution**: Deploy Railway backend first (migrations run automatically on first start)

### Error: "duplicate key value"
**Cause**: Seed data already exists
**Solution**: The script truncates tables first, but if foreign key issues occur:
```sql
-- Clear all data first
TRUNCATE TABLE mst_users CASCADE;
-- Then run seed.sql again
```

### Error: "ABORT: Cannot run seed data in production"
**Cause**: Database name is `jatistore_production`
**Solution**: This is intentional protection. Don't seed production databases.

## Important Notes

1. **TRUNCATES DATA**: This script wipes existing users, products, stores before inserting
2. **Dev/Test Only**: Never run on production database
3. **UUIDs are Fixed**: All IDs are predetermined (good for testing, consistent references)
4. **Timestamps**: Use `NOW() - INTERVAL 'N days'` for realistic created_at/updated_at
5. **Phone Numbers**: Masked with `+628****NNNN` format

## Next Steps After Seeding

1. Verify data loaded correctly (run verification queries above)
2. Test login with any test account
3. Browse products on frontend
4. Test checkout flow with seeded products
5. Test seller dashboard with seeded orders

---

**Ready for deployment?** Follow DEPLOYMENT.md for Railway/Vercel/Supabase setup.
