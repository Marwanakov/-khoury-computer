UPDATE products
SET availability_status = CASE
    WHEN stock_quantity = 0 THEN 'SOLD_OUT'
    WHEN stock_quantity <= 3 THEN 'LOW_STOCK'
    ELSE 'AVAILABLE'
END;