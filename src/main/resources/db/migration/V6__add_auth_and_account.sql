-- Add username, password, and role to existing customers table
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS username VARCHAR(100),
    ADD COLUMN IF NOT EXISTS password VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role     VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';

-- Backfill username for any existing rows (email local part + id to ensure uniqueness)
UPDATE customers
SET username = CONCAT(SPLIT_PART(email, '@', 1), '_', id),
    password = '$2a$10$placeholder.hashed.password.value.here'
WHERE username IS NULL;

-- Now enforce NOT NULL and unique constraints
ALTER TABLE customers
    ALTER COLUMN username SET NOT NULL,
    ALTER COLUMN password SET NOT NULL;

ALTER TABLE customers
    ADD CONSTRAINT customers_username_unique UNIQUE (username);

CREATE INDEX IF NOT EXISTS idx_customers_username ON customers (username);

-- Create accounts table (one account per customer)
CREATE TABLE IF NOT EXISTS accounts
(
    id          BIGSERIAL      PRIMARY KEY,
    customer_id BIGINT         NOT NULL UNIQUE REFERENCES customers (id) ON DELETE CASCADE,
    balance     NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    held_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_accounts_customer_id ON accounts (customer_id);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions
(
    id             BIGSERIAL      PRIMARY KEY,
    account_id     BIGINT         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    amount         NUMERIC(19, 2) NOT NULL,
    type           VARCHAR(20)    NOT NULL,
    payment_method VARCHAR(20),
    status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    reference_id   VARCHAR(255),
    description    VARCHAR(500),
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions (account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions (type);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions (status);
