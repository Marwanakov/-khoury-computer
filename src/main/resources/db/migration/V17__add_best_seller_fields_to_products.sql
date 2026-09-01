ALTER TABLE products
    ADD COLUMN is_best_seller BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN best_seller_marked_at TIMESTAMP;

ALTER TABLE products
    ADD CONSTRAINT chk_products_best_seller_state
    CHECK (
        (is_best_seller = TRUE AND best_seller_marked_at IS NOT NULL)
        OR
        (is_best_seller = FALSE AND best_seller_marked_at IS NULL)
    );