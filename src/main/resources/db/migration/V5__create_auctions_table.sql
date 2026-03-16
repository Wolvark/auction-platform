CREATE TABLE IF NOT EXISTS auctions
(
    id                BIGSERIAL      PRIMARY KEY,
    item_id           BIGINT         NOT NULL REFERENCES items (id) ON DELETE CASCADE,
    start_price       NUMERIC(19, 2) NOT NULL,
    reserve_price     NUMERIC(19, 2),
    buy_out_price     NUMERIC(19, 2),
    min_bid_increment NUMERIC(19, 2) NOT NULL DEFAULT 1.00,
    start_time        TIMESTAMP      NOT NULL,
    end_time          TIMESTAMP      NOT NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'SCHEDULED',
    created_at        TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auctions_item_id ON auctions (item_id);
CREATE INDEX idx_auctions_status ON auctions (status);
