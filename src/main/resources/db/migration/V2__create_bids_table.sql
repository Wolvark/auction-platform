CREATE TABLE IF NOT EXISTS bids
(
    id          BIGSERIAL PRIMARY KEY,
    item_name   VARCHAR(255)   NOT NULL,
    amount      NUMERIC(19, 2) NOT NULL,
    customer_id BIGINT         NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bids_customer_id ON bids (customer_id);
CREATE INDEX idx_bids_status ON bids (status);
