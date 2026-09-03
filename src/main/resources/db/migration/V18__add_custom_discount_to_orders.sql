ALTER TABLE orders
ADD COLUMN custom_discount_amount NUMERIC(10, 2)
    NOT NULL DEFAULT 0.00,
ADD COLUMN custom_discount_applied_at TIMESTAMP;

ALTER TABLE orders
ADD CONSTRAINT check_orders_custom_discount_non_negative
CHECK (custom_discount_amount >= 0);

ALTER TABLE orders
ADD CONSTRAINT check_orders_custom_discount_state
CHECK (
    (
        custom_discount_amount = 0
        AND custom_discount_applied_at IS NULL
    )
    OR
    (
        custom_discount_amount > 0
        AND custom_discount_applied_at IS NOT NULL
    )
);