-- Dados de demonstracao. Nomes ficticios, nenhum dado pessoal real.
-- O Joao tem 15 anos e so tem despesa: a regra de negocio nao deixaria
-- cadastrar receita para ele, e o seed respeita a mesma regra da API.
INSERT INTO people (name, age) VALUES
    ('Ana Ribeiro', 34),
    ('Bruno Sales', 41),
    ('Joao Ribeiro', 15);

INSERT INTO transactions (description, amount, type, person_id) VALUES
    ('Salario',              5200.00, 'INCOME',  1),
    ('Freelance',             850.00, 'INCOME',  1),
    ('Aluguel',              1800.00, 'EXPENSE', 1),
    ('Supermercado',          742.35, 'EXPENSE', 1),
    ('Salario',              3900.00, 'INCOME',  2),
    ('Financiamento do carro', 980.00, 'EXPENSE', 2),
    ('Plano de saude',        410.90, 'EXPENSE', 2),
    ('Lanche na escola',       45.00, 'EXPENSE', 3),
    ('Material escolar',      128.70, 'EXPENSE', 3);
