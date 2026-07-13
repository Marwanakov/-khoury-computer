CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,

    phone_number VARCHAR(30) NOT NULL,

    address_city VARCHAR(100) NOT NULL,
    address_street VARCHAR(150) NOT NULL,
    address_details TEXT NOT NULL DEFAULT '',

    role VARCHAR(30) NOT NULL CHECK (
        role IN ('CUSTOMER', 'ADMIN')
    )
);