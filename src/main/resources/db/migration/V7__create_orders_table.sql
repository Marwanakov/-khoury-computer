CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    customer_name VARCHAR(200) NOT NULL,
    customer_email VARCHAR(150) NOT NULL,
    customer_phone_number VARCHAR(30) NOT NULL,

    customer_address_city VARCHAR(100) NOT NULL,
    customer_address_street VARCHAR(150) NOT NULL,
    customer_address_details TEXT NOT NULL DEFAULT '',

    status VARCHAR(30) NOT NULL CHECK (
        status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')
    ),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);