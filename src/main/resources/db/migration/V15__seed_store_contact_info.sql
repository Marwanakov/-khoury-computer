INSERT INTO store_contact_info (
    phone_number,
    facebook_messenger_link,
    whatsapp_number,
    email,
    store_address_city,
    store_address_street,
    store_address_details
)
SELECT
    '022744433',
    'https://www.facebook.com/khourycomputer',
    '0598941102',
    'khouryco@palnet.com',
    'Bethlehem',
    'Manger St',
    'Opposite Bank of Jerusalem'
WHERE NOT EXISTS (
    SELECT 1
    FROM store_contact_info
);