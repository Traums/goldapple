--liquibase formatted sql

--changeset goldapple:004-more-customers-and-orders splitStatements:false
-- 45 клиентов и 200 заказов. Остатки на складе не меняются: это история продаж,
-- текущие количества по-прежнему заданы в 002.
DO $$
DECLARE
    first_names text[] := ARRAY[
        'Elena', 'Olga', 'Daria', 'Sofia', 'Polina', 'Anna', 'Vera', 'Ksenia', 'Alina',
        'Natalia', 'Irina', 'Tatiana', 'Ekaterina', 'Yulia', 'Victoria', 'Alexandra', 'Marina', 'Svetlana',
        'Oksana', 'Nina', 'Pavel', 'Sergey', 'Dmitry', 'Alexey', 'Nikolay', 'Andrey', 'Mikhail', 'Roman',
        'Artem', 'Kirill', 'Egor', 'Ilya', 'Maxim', 'Denis', 'Oleg', 'Vladimir', 'Timur', 'Gleb',
        'Mark', 'Lev', 'Eva', 'Maya', 'Lada', 'Zoya', 'Inna'
    ];
    last_names text[] := ARRAY[
        'Orlova', 'Volkova', 'Morozova', 'Lebedeva', 'Kuznetsova', 'Popova', 'Novikova', 'Fedorova', 'Romanova',
        'Egorova', 'Smirnova', 'Ivanova', 'Petrova', 'Vasilieva', 'Kozlova', 'Stepanova', 'Nikolaeva', 'Pavlova',
        'Semenova', 'Golubeva', 'Vinogradov', 'Bogdanov', 'Frolov', 'Sorokin', 'Belov', 'Komarov', 'Orlov', 'Kiselev',
        'Makarov', 'Andreev', 'Kovalev', 'Ilyin', 'Gusev', 'Titov', 'Kuzmin', 'Kudryavtsev', 'Baranov', 'Mironov',
        'Zhukov', 'Savelyev', 'Belyaeva', 'Gromova', 'Efimova', 'Loginova', 'Zaitseva'
    ];
    active_products int[] := ARRAY[1, 2, 3, 4, 5, 9, 10];
    any_products int[] := ARRAY[1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    customer_ids bigint[] := ARRAY[]::bigint[];
    customer_blocked boolean[] := ARRAY[]::boolean[];
    line_products int[] := ARRAY[]::int[];
    line_qty int[] := ARRAY[]::int[];
    line_price numeric[] := ARRAY[]::numeric[];
    customer_status text;
    order_status text;
    pool int[];
    cid bigint;
    oid bigint;
    pid int;
    qty int;
    line numeric(15, 2);
    total numeric(15, 2);
    cust_idx int;
    nlines int;
    attempt int;
    j int;
    i int;
BEGIN
    FOR i IN 1..45 LOOP
        customer_status := CASE WHEN i % 9 = 0 THEN 'BLOCKED' ELSE 'ACTIVE' END;
        INSERT INTO customers (first_name, last_name, phone, status, created_at)
        VALUES (
            first_names[i],
            last_names[i],
            '+7904' || lpad(i::text, 7, '0'),
            customer_status,
            TIMESTAMP '2024-02-01 09:00:00' + (i || ' days')::interval
        )
        RETURNING id INTO cid;
        customer_ids := customer_ids || cid;
        customer_blocked := customer_blocked || (customer_status = 'BLOCKED');
    END LOOP;

    FOR i IN 1..200 LOOP
        IF i % 20 <= 2 THEN
            order_status := 'NEW';
        ELSIF i % 20 <= 7 THEN
            order_status := 'PAID';
        ELSIF i % 20 <= 10 THEN
            order_status := 'CANCELLED';
        ELSE
            order_status := 'COMPLETED';
        END IF;

        IF i % 10 < 7 THEN
            cust_idx := 1 + (i % 20);
        ELSE
            cust_idx := 21 + (i % 25);
        END IF;

        IF customer_blocked[cust_idx] AND order_status = 'NEW' THEN
            order_status := 'COMPLETED';
        END IF;

        nlines := CASE WHEN i % 5 = 0 THEN 3 WHEN i % 5 = 1 THEN 1 ELSE 2 END;
        pool := CASE WHEN order_status = 'CANCELLED' THEN any_products ELSE active_products END;
        line_products := ARRAY[]::int[];
        line_qty := ARRAY[]::int[];
        line_price := ARRAY[]::numeric[];
        attempt := 0;

        WHILE coalesce(array_length(line_products, 1), 0) < nlines LOOP
            attempt := attempt + 1;
            IF attempt > 30 THEN
                RAISE EXCEPTION 'cannot pick distinct products for order %', i;
            END IF;
            pid := pool[1 + ((i + attempt) % array_length(pool, 1))];
            IF pid = ANY (line_products) THEN
                CONTINUE;
            END IF;
            qty := 1 + ((i + attempt) % 3);
            IF pid IN (5, 6, 7, 8) THEN
                qty := 1;
            END IF;
            SELECT p.price
            INTO line
            FROM products p
            WHERE p.id = pid;
            IF i % 4 = 0 AND order_status IN ('COMPLETED', 'CANCELLED') THEN
                line := round(line * 0.90, 2);
            END IF;
            line_products := line_products || pid;
            line_qty := line_qty || qty;
            line_price := line_price || line;
        END LOOP;

        total := 0;
        FOR j IN 1..nlines LOOP
            total := total + line_price[j] * line_qty[j];
        END LOOP;

        INSERT INTO orders (customer_id, status, total_amount, created_at)
        VALUES (
            customer_ids[cust_idx],
            order_status,
            total,
            TIMESTAMP '2024-04-01 08:00:00'
                + ((i * 47) || ' hours')::interval
                + ((i % 17) || ' minutes')::interval
        )
        RETURNING id INTO oid;

        FOR j IN 1..nlines LOOP
            INSERT INTO order_items (order_id, product_id, quantity, price)
            VALUES (oid, line_products[j], line_qty[j], line_price[j]);
        END LOOP;
    END LOOP;

    PERFORM setval(pg_get_serial_sequence('customers', 'id'), (SELECT MAX(id) FROM customers));
    PERFORM setval(pg_get_serial_sequence('orders', 'id'), (SELECT MAX(id) FROM orders));
    PERFORM setval(pg_get_serial_sequence('order_items', 'id'), (SELECT MAX(id) FROM order_items));
END $$;

--rollback DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE customer_id IN (SELECT id FROM customers WHERE phone LIKE '+7904%'));
--rollback DELETE FROM orders WHERE customer_id IN (SELECT id FROM customers WHERE phone LIKE '+7904%');
--rollback DELETE FROM customers WHERE phone LIKE '+7904%';
