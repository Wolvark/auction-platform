ALTER TABLE bids
    ADD COLUMN IF NOT EXISTS auction_id BIGINT REFERENCES auctions (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_bids_auction_id ON bids (auction_id);
