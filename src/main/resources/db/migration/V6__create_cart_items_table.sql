CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,

    cart_id BIGINT NOT NULL,

    -- due to the List in the CartEntity.java
    cart_item_order INTEGER NOT NULL, 

    product_id BIGINT NOT NULL,
    product_name VARCHAR(150) NOT NULL,

    unit_price NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),

    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
        REFERENCES carts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT,

    CONSTRAINT unique_cart_item_order
        UNIQUE (cart_id, cart_item_order),

    CONSTRAINT unique_cart_product
        UNIQUE (cart_id, product_id)
);