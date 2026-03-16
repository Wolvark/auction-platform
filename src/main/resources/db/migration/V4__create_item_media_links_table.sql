CREATE TABLE IF NOT EXISTS item_media_links
(
    id            BIGSERIAL    PRIMARY KEY,
    item_id       BIGINT       NOT NULL REFERENCES items (id) ON DELETE CASCADE,
    url           VARCHAR(2048) NOT NULL,
    media_type    VARCHAR(10)  NOT NULL,
    display_order INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_item_media_links_item_id ON item_media_links (item_id);
