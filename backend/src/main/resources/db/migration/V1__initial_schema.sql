-- ============================================================
-- JatiStore v5 - PostgreSQL DDL
-- Generated from ERD-Jatistore-v5.txt
-- Stack: Spring Boot + PostgreSQL (Supabase for dev)
-- Prefix: mst_ (Master data), trx_ (Transactional data)
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- ENUM TYPES
-- ============================================================

CREATE TYPE balance_type AS ENUM (
    'ON_HOLD',    -- user not confirmed item arrival yet
    'AVAILABLE'   -- withdrawable balance
);

CREATE TYPE transaction_status AS ENUM (
    'PENDING',   -- backend sending request to mock payment gateway
    'SUCCESS',   -- payment gateway authorized the card
    'DECLINED',  -- insufficient funds
    'FAILED'     -- payment gateway error / not authorized
);

CREATE TYPE order_status AS ENUM (
    'PENDING',
    'PAID_ON_HOLD',
    'SHIPPED',
    'RECEIVED',
    'CANCELLED'
);

CREATE TYPE payment_method AS ENUM (
    'CARD',   -- mock success/fail from payment gateway
    'WALLET'  -- user balance from mock service
);

-- ============================================================
-- MASTER TABLES (mst_)
-- ============================================================

CREATE TABLE mst_users (
                           id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                           username        VARCHAR(255) NOT NULL UNIQUE,
                           email           VARCHAR(255) NOT NULL UNIQUE,
                           phone_number    VARCHAR(20)  NOT NULL UNIQUE,
                           password_hash   VARCHAR(255) NOT NULL,
                           full_name       VARCHAR(255) NOT NULL,
                           date_of_birth   DATE,
                           created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                           updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE mst_admins (
                            id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID NOT NULL UNIQUE,

                            CONSTRAINT fk_admins_user FOREIGN KEY (user_id) REFERENCES mst_users (id)
);

CREATE TABLE mst_sellers (
                             id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id                 UUID            NOT NULL UNIQUE,
                             cached_available_balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
                             cached_on_hold_balance   DECIMAL(19, 4) NOT NULL DEFAULT 0,
                             active                  BOOLEAN         NOT NULL DEFAULT TRUE,

                             CONSTRAINT fk_sellers_user FOREIGN KEY (user_id) REFERENCES mst_users (id)
);

CREATE TABLE mst_stores (
                            id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                            store_name  VARCHAR(255) NOT NULL UNIQUE,
                            image       TEXT,
                            seller_id   UUID         NOT NULL UNIQUE,

                            CONSTRAINT fk_stores_seller FOREIGN KEY (seller_id) REFERENCES mst_sellers (id)
);

CREATE TABLE mst_product_categories (
                                        id   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                        name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE mst_products (
                              id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                              store_id            UUID            NOT NULL,
                              product_category_id UUID            NOT NULL,
                              name                VARCHAR(255)    NOT NULL,
                              description         TEXT,
                              image               TEXT,           -- e.g. Cloudinary URL
                              price               DECIMAL(19, 4)  NOT NULL,
                              stock               INTEGER         NOT NULL DEFAULT 0,
                              created_at          TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                              updated_at          TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                              deleted_at          TIMESTAMPTZ,      -- soft delete

                              CONSTRAINT fk_products_store    FOREIGN KEY (store_id)            REFERENCES mst_stores (id),
                              CONSTRAINT fk_products_category FOREIGN KEY (product_category_id) REFERENCES mst_product_categories (id)
);

CREATE TABLE mst_flash_sales (
                                 id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                 name       TEXT        NOT NULL,
                                 start_time TIMESTAMPTZ NOT NULL,
                                 end_time   TIMESTAMPTZ NOT NULL
);

CREATE TABLE mst_flash_sale_items (
                                      id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                                      flash_sale_id   UUID           NOT NULL,
                                      product_id      UUID           NOT NULL,
                                      flash_price     DECIMAL(19, 4) NOT NULL,
                                      remaining_quota INTEGER        NOT NULL DEFAULT 0,

                                      CONSTRAINT fk_fsi_flash_sale FOREIGN KEY (flash_sale_id) REFERENCES mst_flash_sales (id),
                                      CONSTRAINT fk_fsi_product    FOREIGN KEY (product_id)    REFERENCES mst_products (id)
);

CREATE TABLE mst_payment_cards (
                                   id               UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id          UUID     NOT NULL,
                                   card_number      CHAR(16) NOT NULL UNIQUE,  -- plain text, mock payment gateway
                                   card_holder_name VARCHAR(255) NOT NULL,
                                   expiry_date      CHAR(5)  NOT NULL,         -- format: MM/YY, e.g. 06/28

                                   CONSTRAINT fk_payment_cards_user FOREIGN KEY (user_id) REFERENCES mst_users (id)
);

-- ============================================================
-- TRANSACTION TABLES (trx_)
-- ============================================================

CREATE TABLE trx_carts (
                           id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
                           user_id    UUID      NOT NULL UNIQUE,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES mst_users (id)
);

CREATE TABLE trx_cart_items (
                                id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
                                cart_id    UUID    NOT NULL,
                                product_id UUID    NOT NULL,
                                quantity   INTEGER NOT NULL DEFAULT 1,

                                CONSTRAINT uq_cart_items_cart_product UNIQUE (cart_id, product_id),
                                CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES trx_carts (id),
                                CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES mst_products (id)
);

CREATE TABLE trx_orders (
                            id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id      UUID         NOT NULL,
                            total_amount DECIMAL(19, 4) NOT NULL,
                            status       order_status NOT NULL DEFAULT 'PENDING',
                            created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                            CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES mst_users (id)
);

CREATE TABLE trx_order_details (
                                   id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                                   order_id        UUID           NOT NULL,
                                   product_id      UUID           NOT NULL,
                                   quantity        INTEGER        NOT NULL,
                                   price_per_item  DECIMAL(19, 4) NOT NULL,
                                   flash_sale      BOOLEAN        NOT NULL DEFAULT FALSE,
                                   created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),

                                   CONSTRAINT fk_order_details_order   FOREIGN KEY (order_id)   REFERENCES trx_orders (id),
                                   CONSTRAINT fk_order_details_product FOREIGN KEY (product_id) REFERENCES mst_products (id)
);

CREATE TABLE trx_transactions (
                                  id                   UUID               PRIMARY KEY DEFAULT gen_random_uuid(),
                                  order_id             UUID               NOT NULL,
                                  payment_method       payment_method     NOT NULL,
                                  payment_card_id      UUID,              -- nullable when WALLET method
                                  payment_gateway_ref  VARCHAR(255),
                                  status               transaction_status NOT NULL DEFAULT 'PENDING',
                                  created_at           TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
                                  updated_at           TIMESTAMPTZ          NOT NULL DEFAULT NOW(),

                                  CONSTRAINT fk_transactions_order        FOREIGN KEY (order_id)        REFERENCES trx_orders (id),
                                  CONSTRAINT fk_transactions_payment_card FOREIGN KEY (payment_card_id) REFERENCES mst_payment_cards (id)
);

CREATE TABLE trx_seller_ledger (
                                   id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                                   seller_id    UUID           NOT NULL,
                                   order_id     UUID,          -- nullable for withdrawals
                                   amount       DECIMAL(19, 4) NOT NULL,   -- positive = credit, negative = debit
                                   balance_type balance_type   NOT NULL,
                                   created_at   TIMESTAMPTZ      NOT NULL DEFAULT NOW(),

                                   CONSTRAINT fk_seller_ledger_seller FOREIGN KEY (seller_id) REFERENCES mst_sellers (id),
                                   CONSTRAINT fk_seller_ledger_order  FOREIGN KEY (order_id)  REFERENCES trx_orders (id)
);

CREATE TABLE trx_tokens (
                            id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id         UUID        NOT NULL UNIQUE,
                            token           TEXT        NOT NULL,
                            expires_at      TIMESTAMPTZ NOT NULL,
                            last_updated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

                            CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES mst_users (id)
);

CREATE TABLE trx_audit_trails (
                                  id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id         UUID,     -- nullable: failed login attempts have no user
                                  entity_id       UUID,     -- nullable
                                  user_role       VARCHAR(50),
                                  action          VARCHAR(100) NOT NULL,   -- e.g. PRODUCT_UPDATE, ORDER_RECEIVE
                                  affected_module VARCHAR(100) NOT NULL,   -- e.g. ORDERS, PRODUCT
                                  description     TEXT,
                                  payload         JSONB,    -- sanitized request body or data diff
                                  ip_address      VARCHAR(45),
                                  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                  CONSTRAINT fk_audit_trails_user FOREIGN KEY (user_id) REFERENCES mst_users (id)
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_products_store_id            ON mst_products (store_id);
CREATE INDEX idx_products_category_id         ON mst_products (product_category_id);
CREATE INDEX idx_products_deleted_at          ON mst_products (deleted_at);
CREATE INDEX idx_flash_sale_items_sale_id     ON mst_flash_sale_items (flash_sale_id);
CREATE INDEX idx_flash_sale_items_product_id  ON mst_flash_sale_items (product_id);
CREATE INDEX idx_cart_items_cart_id           ON trx_cart_items (cart_id);
CREATE INDEX idx_order_details_order_id       ON trx_order_details (order_id);
CREATE INDEX idx_order_details_product_id     ON trx_order_details (product_id);
CREATE INDEX idx_transactions_order_id        ON trx_transactions (order_id);
CREATE INDEX idx_seller_ledger_seller_id      ON trx_seller_ledger (seller_id);
CREATE INDEX idx_seller_ledger_order_id       ON trx_seller_ledger (order_id);
CREATE INDEX idx_audit_trails_user_id         ON trx_audit_trails (user_id);
CREATE INDEX idx_audit_trails_created_at      ON trx_audit_trails (created_at DESC);
CREATE INDEX idx_payment_cards_user_id        ON mst_payment_cards (user_id);
CREATE INDEX idx_orders_user_id               ON trx_orders (user_id);
CREATE INDEX idx_orders_status                ON trx_orders (status);

-- ============================================================
-- TRIGGERS
-- ============================================================

-- Auto-update updated_at columns
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON mst_users
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON mst_products
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER trg_carts_updated_at
    BEFORE UPDATE ON trx_carts
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER trg_transactions_updated_at
    BEFORE UPDATE ON trx_transactions
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

-- Sync seller cached balances from ledger inserts
CREATE OR REPLACE FUNCTION sync_seller_balance()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.balance_type = 'AVAILABLE' THEN
UPDATE mst_sellers
SET cached_available_balance = cached_available_balance + NEW.amount
WHERE id = NEW.seller_id;
ELSIF NEW.balance_type = 'ON_HOLD' THEN
UPDATE mst_sellers
SET cached_on_hold_balance = cached_on_hold_balance + NEW.amount
WHERE id = NEW.seller_id;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_ledger_insert
    AFTER INSERT ON trx_seller_ledger
    FOR EACH ROW EXECUTE FUNCTION sync_seller_balance();