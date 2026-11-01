--liquibase formatted sql

--changeset goldapple:003-product-images
ALTER TABLE products ADD COLUMN image_url VARCHAR(500);

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=900&q=80' WHERE id = 1;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=900&q=80' WHERE id = 2;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1631214524020-7e18db9a8f92?auto=format&fit=crop&w=900&q=80' WHERE id = 3;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?auto=format&fit=crop&w=900&q=80' WHERE id = 4;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=900&q=80' WHERE id = 5;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=900&q=80' WHERE id = 6;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1519699047748-de8e457a634e?auto=format&fit=crop&w=900&q=80' WHERE id = 7;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?auto=format&fit=crop&w=900&q=80' WHERE id = 8;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=900&q=80' WHERE id = 9;
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1571781926291-c477ebfd024b?auto=format&fit=crop&w=900&q=80' WHERE id = 10;

ALTER TABLE products ALTER COLUMN image_url SET NOT NULL;
--rollback ALTER TABLE products DROP COLUMN image_url;
