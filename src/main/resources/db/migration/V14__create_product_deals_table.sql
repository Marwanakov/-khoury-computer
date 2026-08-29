CREATE TABLE product_deals (
    id BIGSERIAL PRIMARY KEY,

    product_id BIGINT NOT NULL,

    deal_price NUMERIC(10, 2) NOT NULL CHECK (
        deal_price > 0
    ),

    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,

    featured BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_product_deals_schedule
        CHECK (ends_at > starts_at),

    CONSTRAINT fk_product_deals_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_product_deals_product_id
    ON product_deals(product_id);

CREATE INDEX idx_product_deals_schedule
    ON product_deals(starts_at, ends_at);

CREATE INDEX idx_product_deals_featured
    ON product_deals(featured);