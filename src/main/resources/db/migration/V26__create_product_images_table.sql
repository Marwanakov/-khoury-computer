CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,

    product_id BIGINT NOT NULL,

    image_url TEXT NOT NULL,

    display_order INTEGER NOT NULL
        CHECK (display_order >= 0),

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_product_images_url
        CHECK (BTRIM(image_url) <> ''),

    CONSTRAINT uq_product_images_order
        UNIQUE (product_id, display_order),

    CONSTRAINT uq_product_images_url
        UNIQUE (product_id, image_url)
);

CREATE UNIQUE INDEX uq_product_images_primary
    ON product_images(product_id)
    WHERE is_primary = TRUE;

CREATE INDEX idx_product_images_product_order
    ON product_images(product_id, display_order);

INSERT INTO product_images (
    product_id,
    image_url,
    display_order,
    is_primary
)
SELECT
    id,
    image_url,
    0,
    TRUE
FROM products
WHERE image_url IS NOT NULL
  AND BTRIM(image_url) <> '';