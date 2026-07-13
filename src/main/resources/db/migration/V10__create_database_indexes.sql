-- Indexes help the database find data faster.

CREATE INDEX idx_products_category_id
    ON products(category_id);

CREATE INDEX idx_products_name
    ON products(name);

CREATE INDEX idx_product_tags_tag
    ON product_tags(tag);

CREATE INDEX idx_carts_user_id
    ON carts(user_id);

CREATE INDEX idx_cart_items_cart_id
    ON cart_items(cart_id);

CREATE INDEX idx_orders_user_id
    ON orders(user_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);