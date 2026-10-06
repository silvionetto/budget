WITH category_seed(category_name, category_type, subcategories) AS (
    VALUES
    (U&'Renda', 'INCOME', ARRAY[
        U&'ING',
        U&'FotografoBrasileiroemAmsterdam',
        U&'Gabriel',
        U&'Sociale Verzekeringsbank',
        U&'Debora',
        U&'Restituicao',
        U&'Restitui\00E7\00E3o',
        U&'Transferencia',
        U&'Receita de juros',
        U&'Retirada da Poupan\00E7a',
        U&'Retirada da Conta Investimento',
        U&'Dividendos',
        U&'Ajustes'
    ]),
    (U&'Unknown_Income', 'INCOME', ARRAY[U&'Unknown_Income']),
    (U&'Unknown_Expense', 'EXPENSE', ARRAY[U&'Unknown_Expense']),
    (U&'Despesas Domesticas', 'EXPENSE', ARRAY[U&'Hipoteca', U&'Eletricidade', U&'Gas', U&'Agua', U&'Celular', U&'Cafe', U&'TV a cabo', U&'Internet', U&'Moveis', U&'Eletrodomestico', U&'Suprimentos', U&'Manutencao', U&'Melhorias', U&'Empregada', U&'Cloud']),
    (U&'Vida Diaria', 'EXPENSE', ARRAY[U&'Almoco', U&'Mercearia', U&'Roupa', U&'Calcado', U&'Perfume', U&'Maquiagem', U&'Jantar', U&'Lavanderia', U&'Barbeiro', U&'Lanche', U&'Cabelo', U&'Unha', U&'Cafe da Manha']),
    (U&'Joao Pedro', 'EXPENSE', ARRAY[U&'Medicamento', U&'Roupa', U&'Escola', U&'Merenda', U&'Material escolar', U&'Baba', U&'Brinquedos', U&'Esporte', U&'Livros', U&'Revistas', U&'F\00E9rias', U&'Cabelo', U&'PSN', U&'Google', U&'Mesada', U&'Bicicleta', U&'Advogada', U&'Imigra\00E7\00E3o', U&'Celular', U&'Rel\00F3gio', U&'Festa', U&'Passagem', U&'Pens\00E3o']),
    (U&'Gabriel', 'EXPENSE', ARRAY[U&'Medicamento', U&'\00D3culos', U&'Leite', U&'Cal\00E7ado', U&'Roupa', U&'Escola', U&'Merenda', U&'Material escolar', U&'Cabelo', U&'PSN', U&'Nitendo', U&'Baba', U&'Brinquedos', U&'Esporte', U&'Livros', U&'Revistas', U&'Fralda', U&'Carrinho', U&'Foto', U&'Parque', U&'Festa']),
    (U&'Transporte', 'EXPENSE', ARRAY[U&'Aluguel', U&'Lease', U&'Moto', U&'Gasolina', U&'Estacionamento', U&'Manuten\00E7\00E3o', U&'Melhorias', U&'Habilita\00E7\00E3o', U&'Registro/Licensa', U&'Pedagio', U&'Bicicleta', U&'Taxi', U&'Multa', U&'Onibus', U&'Trem']),
    (U&'Saude', 'EXPENSE', ARRAY[U&'Medico', U&'Medicamento', U&'Plano de sa\00FAde', U&'Suplemento Alimentar', U&'Emergencia', U&'Dentista', U&'Fisioterapeuta', U&'Oculos']),
    (U&'Seguro', 'EXPENSE', ARRAY[U&'Carro', U&'Vida', U&'Casa', U&'Moto']),
    (U&'Educacao', 'EXPENSE', ARRAY[U&'Ensino', U&'Cursos', U&'Livros', U&'Jiu-Jitsu', U&'Academia']),
    (U&'Caridade/Presente', 'EXPENSE', ARRAY[U&'Presentes', U&'Doacoes', U&'Donativos Religiosos']),
    (U&'Poupanca', 'EXPENSE', ARRAY[U&'Fundo de emergencia', U&'Transferencia para poupan\00E7a', U&'Aposentadoria', U&'Barco', U&'Joao Pedro', U&'Gabriel', U&'Debora', U&'Casa']),
    (U&'Obrigacoes', 'EXPENSE', ARRAY[U&'Emprestimo escolar', U&'Emprestimo', U&'Cart\00E3o de cr\00E9dito', U&'Fun\00E7\00E3o cr\00E9dito', U&'Imposto', U&'Banco', U&'Taxas', U&'Imigra\00E7\00E3o', U&'Not\00E1rio']),
    (U&'Despesas de Negocio', 'EXPENSE', ARRAY[U&'Despesas deduziveis', U&'Despesas n\00E3o deduziveis', U&'Taxi', U&'Hotel', U&'Passagem', U&'Lavanderia']),
    (U&'Diversao', 'EXPENSE', ARRAY[U&'Filmes', U&'Musicas', U&'Jogos', U&'Cinema', U&'Livros', U&'Kite', U&'Surf', U&'Skate', U&'Corrida', U&'Camera', U&'Brinquedos', U&'Bebidas', U&'Jardinagem', U&'Parque', U&'Museum', U&'Teatro', U&'Barco', U&'Festa', U&'Spa', U&'Outros']),
    (U&'Animal', 'EXPENSE', ARRAY[U&'Comida', U&'Medicamento', U&'Brinquedos']),
    (U&'Assinatura', 'EXPENSE', ARRAY[U&'Jornal', U&'Revista', U&'Clube']),
    (U&'Ferias', 'EXPENSE', ARRAY[U&'Viagem', U&'Acomoda\00E7\00E3o', U&'Alimento', U&'Aluguel de carro', U&'Divers\00E3o', U&'Mala', U&'Combustivel']),
    (U&'Variados', 'EXPENSE', ARRAY[U&'Avaliador', U&'Contador', U&'Correio'])
),
inserted_categories AS (
    INSERT INTO budget_category (version, last_update_date, name, type)
    SELECT 0, CURRENT_DATE, category_name, category_type
    FROM category_seed
    ON CONFLICT (name) DO NOTHING
    RETURNING id, name
),
category_ids AS (
    SELECT id, name FROM inserted_categories
    UNION ALL
    SELECT category.id, category.name
    FROM budget_category category
    WHERE NOT EXISTS (
        SELECT 1
        FROM inserted_categories inserted
        WHERE inserted.id = category.id
    )
),
subcategory_seed AS (
    SELECT category_name, subcategory.subcategory_name
    FROM category_seed
    CROSS JOIN LATERAL unnest(subcategories) AS subcategory(subcategory_name)
)
INSERT INTO budget_sub_category (version, last_update_date, name, category_id)
SELECT 0, CURRENT_DATE, seed.subcategory_name, category.id
FROM subcategory_seed seed
JOIN category_ids category ON category.name = seed.category_name
WHERE NOT EXISTS (
    SELECT 1
    FROM budget_sub_category existing
    WHERE existing.category_id = category.id
      AND existing.name = seed.subcategory_name
);
