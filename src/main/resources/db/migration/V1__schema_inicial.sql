CREATE TABLE people (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    age  INTEGER NOT NULL,

    CONSTRAINT ck_people_age CHECK (age BETWEEN 0 AND 150)
);

CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    description VARCHAR(200) NOT NULL,
    -- NUMERIC, nunca DOUBLE: dinheiro em ponto flutuante acumula erro de arredondamento
    amount      NUMERIC(12, 2) NOT NULL,
    type        VARCHAR(10) NOT NULL,
    -- ON DELETE CASCADE: apagar a pessoa apaga as transacoes dela, como na versao .NET
    person_id   BIGINT NOT NULL REFERENCES people (id) ON DELETE CASCADE,

    CONSTRAINT ck_transactions_amount CHECK (amount > 0),
    -- no banco fica EXPENSE/INCOME; a traducao para o "Expense"/"Income" do
    -- contrato acontece na serializacao, em TransactionType
    CONSTRAINT ck_transactions_type CHECK (type IN ('EXPENSE', 'INCOME'))
);

CREATE INDEX idx_transactions_person_id ON transactions (person_id);
