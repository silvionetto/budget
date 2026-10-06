ALTER TABLE budget_sub_category
    ADD COLUMN category_name VARCHAR(255);

UPDATE budget_sub_category subcategory
SET category_name = COALESCE(category.name, '')
FROM budget_category category
WHERE category.id = subcategory.category_id;

UPDATE budget_sub_category
SET category_name = ''
WHERE category_name IS NULL;

ALTER TABLE budget_sub_category
    ALTER COLUMN category_name SET NOT NULL;

ALTER TABLE store
    ADD COLUMN category_name VARCHAR(255),
    ADD COLUMN sub_category_name VARCHAR(255);

UPDATE store AS store_record
SET category_name = COALESCE(category.name, ''),
    sub_category_name = COALESCE(subcategory.name, '')
FROM budget_sub_category subcategory
LEFT JOIN budget_category category ON category.id = subcategory.category_id
WHERE subcategory.id = store_record.sub_category_id;

UPDATE store
SET category_name = COALESCE(category_name, ''),
    sub_category_name = COALESCE(sub_category_name, '');

ALTER TABLE store
    ALTER COLUMN category_name SET NOT NULL,
    ALTER COLUMN sub_category_name SET NOT NULL;

ALTER TABLE budget_transactions
    ADD COLUMN category_name VARCHAR(255),
    ADD COLUMN sub_category_name VARCHAR(255);

UPDATE budget_transactions AS transaction_record
SET category_name = COALESCE(category.name, ''),
    sub_category_name = COALESCE(subcategory.name, '')
FROM budget_sub_category subcategory
LEFT JOIN budget_category category ON category.id = subcategory.category_id
WHERE subcategory.id = transaction_record.sub_category_id;

UPDATE budget_transactions
SET category_name = COALESCE(category_name, ''),
    sub_category_name = COALESCE(sub_category_name, '');

ALTER TABLE budget_transactions
    ALTER COLUMN category_name SET NOT NULL,
    ALTER COLUMN sub_category_name SET NOT NULL;

DROP INDEX IF EXISTS idx_budget_sub_category_category_id;
DROP INDEX IF EXISTS idx_store_sub_category_id;
DROP INDEX IF EXISTS idx_budget_transactions_sub_category_id;

ALTER TABLE budget_sub_category DROP COLUMN category_id;
ALTER TABLE store DROP COLUMN sub_category_id;
ALTER TABLE budget_transactions DROP COLUMN sub_category_id;

CREATE INDEX idx_budget_sub_category_category_name
    ON budget_sub_category (category_name);
CREATE INDEX idx_store_category_and_sub_category_name
    ON store (category_name, sub_category_name);
CREATE INDEX idx_budget_transactions_category_and_date
    ON budget_transactions (category_name, date);
