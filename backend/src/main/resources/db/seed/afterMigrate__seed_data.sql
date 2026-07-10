-- Flyway Callback: Seed data for dev/test
DO $$
BEGIN
  IF current_database() = 'jatistore_production' THEN
    RAISE NOTICE 'Production database - skipping seed';
    RETURN;
  END IF;
END $$;

\i seed-01-users-roles.sql
\i seed-02-stores-categories.sql
\i seed-03-products.sql
\i seed-04-flash-sales.sql
\i seed-05-payment-cards.sql
\i seed-06-orders-transactions.sql
\i seed-07-audit-trails.sql
