CREATE TABLE IF NOT EXISTS items
(
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    category    VARCHAR(50)  NOT NULL,
    condition   VARCHAR(50)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    owner_id    BIGINT       NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_items_owner_id ON items (owner_id);
CREATE INDEX idx_items_status ON items (status);
CREATE INDEX idx_items_category ON items (category);
