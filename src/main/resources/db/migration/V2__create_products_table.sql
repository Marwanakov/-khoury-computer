CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(150) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    brand VARCHAR(100) NOT NULL DEFAULT '',

    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),

    availability_status VARCHAR(30) NOT NULL CHECK (
        availability_status IN ('AVAILABLE', 'LOW_STOCK', 'SOLD_OUT')
    ),

    image_url TEXT NOT NULL DEFAULT '',

    category_id BIGINT NOT NULL,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE RESTRICT
);