--liquibase formatted sql

--changeset goldapple:002-seed-data
INSERT INTO customers (id, first_name, last_name, phone, status, created_at)
VALUES (1, 'Anastasia', 'Cherevko', '+79031112233', 'ACTIVE', TIMESTAMP '2024-01-15 10:00:00'),
       (2, 'Ivan', 'Petrov', '+79035556677', 'BLOCKED', TIMESTAMP '2024-03-02 12:30:00'),
       (3, 'Maria', 'Sokolova', '+79160001122', 'ACTIVE', TIMESTAMP '2025-02-01 09:40:00');

INSERT INTO products (id, sku, name, brand, category, price, stock_quantity, status, created_at)
VALUES (1, 'LL-SERUM-30', 'Сыворотка с витамином C, 30 мл', 'Lumière Lab', 'SKINCARE', 2490.00, 25, 'ACTIVE', TIMESTAMP '2024-02-01 09:00:00'),
       (2, 'VL-FOUND-20', 'Тональный крем, оттенок 20', 'Velvet', 'MAKEUP', 1890.00, 8, 'ACTIVE', TIMESTAMP '2024-02-01 09:00:00'),
       (3, 'VL-MASCARA', 'Тушь для ресниц', 'Velvet', 'MAKEUP', 990.00, 40, 'ACTIVE', TIMESTAMP '2024-03-12 09:00:00'),
       (4, 'BT-SHAMPOO', 'Шампунь для окрашенных волос, 250 мл', 'Botanica', 'HAIRCARE', 790.00, 15, 'ACTIVE', TIMESTAMP '2024-04-02 09:00:00'),
       (5, 'ND-PARFUM-50', 'Парфюмерная вода Nuit, 50 мл', 'Nuit Dorée', 'FRAGRANCE', 7990.00, 3, 'ACTIVE', TIMESTAMP '2024-05-20 09:00:00'),
       (6, 'BK-LIP-BALM', 'Бальзам для губ', 'Berry Kiss', 'MAKEUP', 450.00, 1, 'ACTIVE', TIMESTAMP '2024-06-01 09:00:00'),
       (7, 'BT-HAIR-MASK', 'Маска для волос, 200 мл', 'Botanica', 'HAIRCARE', 690.00, 0, 'OUT_OF_STOCK', TIMESTAMP '2024-06-15 09:00:00'),
       (8, 'VL-PALETTE', 'Палетка теней Nude', 'Velvet', 'MAKEUP', 3200.00, 4, 'ARCHIVED', TIMESTAMP '2023-11-01 09:00:00'),
       (9, 'LL-HAND-75', 'Крем для рук, 75 мл', 'Lumière Lab', 'SKINCARE', 590.00, 60, 'ACTIVE', TIMESTAMP '2024-07-01 09:00:00'),
       (10, 'PS-MICELLAR', 'Мицеллярная вода, 400 мл', 'Pure Skin', 'SKINCARE', 540.50, 27, 'ACTIVE', TIMESTAMP '2024-08-01 09:00:00');

INSERT INTO orders (id, customer_id, status, total_amount, created_at)
VALUES (1, 1, 'COMPLETED', 4270.00, TIMESTAMP '2025-06-01 11:15:00'),
       (2, 1, 'PAID', 7990.00, TIMESTAMP '2025-08-20 18:40:00'),
       (3, 1, 'NEW', 1630.00, TIMESTAMP '2025-09-18 13:05:00'),
       (4, 1, 'CANCELLED', 790.00, TIMESTAMP '2025-07-11 16:20:00'),
       (5, 2, 'COMPLETED', 1890.00, TIMESTAMP '2025-04-05 12:00:00'),
       (6, 1, 'COMPLETED', 2170.00, TIMESTAMP '2025-09-01 10:30:00'),
       (7, 1, 'PAID', 1621.50, TIMESTAMP '2025-05-02 15:45:00');

INSERT INTO order_items (id, order_id, product_id, quantity, price)
VALUES (1, 1, 1, 1, 2290.00),
       (2, 1, 3, 2, 990.00),
       (3, 2, 5, 1, 7990.00),
       (4, 3, 6, 1, 450.00),
       (5, 3, 9, 2, 590.00),
       (6, 4, 4, 1, 790.00),
       (7, 5, 2, 1, 1890.00),
       (8, 6, 4, 2, 790.00),
       (9, 6, 9, 1, 590.00),
       (10, 7, 10, 3, 540.50);

SELECT setval(pg_get_serial_sequence('customers', 'id'), (SELECT MAX(id) FROM customers));
SELECT setval(pg_get_serial_sequence('products', 'id'), (SELECT MAX(id) FROM products));
SELECT setval(pg_get_serial_sequence('orders', 'id'), (SELECT MAX(id) FROM orders));
SELECT setval(pg_get_serial_sequence('order_items', 'id'), (SELECT MAX(id) FROM order_items));
--rollback DELETE FROM order_items; DELETE FROM orders; DELETE FROM products; DELETE FROM customers;
