INSERT INTO item (name) SELECT 'item-' || g FROM generate_series(1, 100) g
WHERE NOT EXISTS (SELECT 1 FROM item);
