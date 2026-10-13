CREATE TABLE hero_slides (
    id BIGSERIAL PRIMARY KEY,

    desktop_image_url TEXT NOT NULL,
    mobile_image_url TEXT NOT NULL,

    alt_text VARCHAR(160) NOT NULL,

    link_type VARCHAR(30) NOT NULL
        CHECK (
            link_type IN (
                'NONE',
                'PRODUCT',
                'PRODUCTS',
                'DEALS',
                'NEW_ARRIVALS',
                'BEST_SELLERS',
                'CONTACT'
            )
        ),

    linked_product_id BIGINT,

    display_order INTEGER NOT NULL DEFAULT 0
        CHECK (display_order >= 0),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_hero_slides_product
        FOREIGN KEY (linked_product_id)
        REFERENCES products(id)
        ON DELETE SET NULL,

    CONSTRAINT check_hero_product_link
        CHECK (
            link_type = 'PRODUCT'
            OR linked_product_id IS NULL
        )
);

CREATE INDEX idx_hero_slides_active_order
    ON hero_slides(active, display_order, id);