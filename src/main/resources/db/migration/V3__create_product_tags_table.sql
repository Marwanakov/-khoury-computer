CREATE TABLE product_tags (
    product_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,

    CONSTRAINT pk_product_tags
        PRIMARY KEY (product_id, tag),

    CONSTRAINT fk_product_tags_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);