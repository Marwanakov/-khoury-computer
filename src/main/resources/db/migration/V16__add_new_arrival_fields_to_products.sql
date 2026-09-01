ALTER TABLE products
    ADD COLUMN is_new_arrival BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN new_arrival_marked_at TIMESTAMP;

ALTER TABLE products
    ADD CONSTRAINT chk_products_new_arrival_state
    CHECK (
        (is_new_arrival = TRUE AND new_arrival_marked_at IS NOT NULL)
        OR
        (is_new_arrival = FALSE AND new_arrival_marked_at IS NULL)
    );