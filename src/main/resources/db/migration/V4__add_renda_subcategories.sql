INSERT INTO budget_sub_category (version, last_update_date, name, category_id)
SELECT 0, CURRENT_DATE, seed.subcategory_name, category.id
FROM (
    VALUES
        (U&'ING'),
        (U&'FotografoBrasileiroemAmsterdam'),
        (U&'Gabriel'),
        (U&'Sociale Verzekeringsbank'),
        (U&'Debora'),
        (U&'Restitui\00E7\00E3o'),
        (U&'Transferencia'),
        (U&'Receita de juros'),
        (U&'Retirada da Poupan\00E7a'),
        (U&'Retirada da Conta Investimento'),
        (U&'Dividendos'),
        (U&'Ajustes')
) AS seed(subcategory_name)
JOIN budget_category category ON category.name = U&'Renda'
WHERE NOT EXISTS (
    SELECT 1
    FROM budget_sub_category existing
    WHERE existing.category_id = category.id
      AND existing.name = seed.subcategory_name
);
