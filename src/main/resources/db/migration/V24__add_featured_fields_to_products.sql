ALTER TABLE products
    ADD COLUMN is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN featured_marked_at TIMESTAMP;

ALTER TABLE products
    ADD CONSTRAINT chk_products_featured_state
    CHECK (
        (is_featured = TRUE AND featured_marked_at IS NOT NULL)
        OR
        (is_featured = FALSE AND featured_marked_at IS NULL)
    );