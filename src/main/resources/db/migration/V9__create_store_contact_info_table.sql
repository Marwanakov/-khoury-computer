CREATE TABLE store_contact_info (
    id BIGSERIAL PRIMARY KEY,

    phone_number VARCHAR(30) NOT NULL,
    facebook_messenger_link TEXT NOT NULL DEFAULT '',
    whatsapp_number VARCHAR(30) NOT NULL DEFAULT '',

    email VARCHAR(150) NOT NULL,

    store_address_city VARCHAR(100) NOT NULL,
    store_address_street VARCHAR(150) NOT NULL,
    store_address_details TEXT NOT NULL DEFAULT ''
);