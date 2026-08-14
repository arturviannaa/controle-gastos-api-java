# controle-gastos-api-java

[![CI](https://github.com/arturviannaa/controle-gastos-api-java/actions/workflows/ci.yml/badge.svg)](https://github.com/arturviannaa/controle-gastos-api-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Demo](https://img.shields.io/badge/demo-online-success.svg)](https://gastos.pervian.tech)

Controle de gastos residenciais: cadastro de pessoas, lançamento de receitas e
despesas e consulta de totais.

**A mesma API implementada em duas stacks.** A primeira versão era C#/.NET 8 com
Entity Framework e SQLite. Esta é Java 21 com Spring Boot, JPA e PostgreSQL —
**mesmo contrato**: mesmas rotas, mesmos nomes de campo, mesmos códigos de
status.

O frontend React/TypeScript é o mesmo da versão anterior, sem uma linha de
lógica alterada — só o endereço da API virou variável de build. Que ele continue
funcionando é a prova de que o contrato foi preservado, e há [um teste de
integração](src/test/java/tech/pervian/gastos/ContratoDaApiIntegrationTest.java)
que trava isso rota por rota.

> **▶ Demo ao vivo: <https://gastos.pervian.tech>**
> A aplicação rodando, com o mesmo frontend que consumia a versão .NET. A documentação da API fica em [/swagger-ui.html](https://gastos.pervian.tech/swagger-ui.html).
>
> Ambiente de demonstração: os dados são reiniciados periodicamente e nada ali
> deve ser tratado como persistente.

## Como rodar

Um comando, e o único pré-requisito é Docker:

```bash
docker compose up
```

| O que | Onde |
|---|---|
| Aplicação (frontend) | <http://localhost:5173> |
| Swagger UI | <http://localhost:8092> |
| OpenAPI JSON | <http://localhost:8092/v3/api-docs> |
| Health check | <http://localhost:8092/actuator/health> |

Testes (o de integração sobe um PostgreSQL em container):

```bash
./mvnw test
```

## Contrato da API

### Pessoas

| Método | Rota | Resposta |
|---|---|---|
| `GET` | `/api/people` | `200` `[{ id, name, age }]` |
| `POST` | `/api/people` | `201` `{ id, name, age }` |
| `DELETE` | `/api/people/{id}` | `204`, ou `404` se não existir |

```bash
curl -X POST http://localhost:8092/api/people \
  -H 'Content-Type: application/json' \
  -d '{"name":"Artur","age":27}'
# {"id":1,"name":"Artur","age":27}
```

### Transações

| Método | Rota | Resposta |
|---|---|---|
| `GET` | `/api/transactions` | `200` `[{ id, description, amount, type, personId, personName }]` |
| `POST` | `/api/transactions` | `201`, `404` se a pessoa não existir, `422` se ferir a regra de idade |

`type` é `"Expense"` (despesa) ou `"Income"` (receita). `amount` é sempre
positivo — quem decide se soma ou subtrai do saldo é o `type`, não o sinal.

```bash
curl -X POST http://localhost:8092/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{"description":"Mesada","amount":100,"type":"Income","personId":2}'
# HTTP 422
# {"message":"Menores de 18 anos podem cadastrar apenas despesas."}
```

### Totais

`GET /api/summary` devolve os totais de cada pessoa e o total geral. Quem não
tem transação aparece na lista com tudo zerado.

```json
{
  "people": [
    { "personId": 1, "personName": "Artur", "totalIncome": 3000.00, "totalExpense": 1200.00, "balance": 1800.00 },
    { "personId": 2, "personName": "Joao",  "totalIncome": 0,       "totalExpense": 50.00,   "balance": -50.00 }
  ],
  "grandTotalIncome": 3000.00,
  "grandTotalExpense": 1250.00,
  "grandTotalBalance": 1750.00
}
```

### Erros

Formato idêntico ao da versão .NET, porque o frontend já o consome:

| Situação | Status | Corpo |
|---|---|---|
| Regra de negócio | `422` | `{ "message": "..." }` |
| Recurso inexistente | `404` | `{ "message": "..." }` |
| Validação de campo | `400` | `{ "errors": { "campo": ["mensagem"] } }` |

## Regras de negócio

1. **Menor de 18 anos só cadastra despesa.** Tentar lançar receita devolve `422`.
2. **A pessoa precisa existir** para receber uma transação; senão, `404`.
3. **Apagar uma pessoa apaga as transações dela**, em cascata.
4. **Saldo = receitas − despesas**, por pessoa e no total geral.
5. **Valor sempre maior que zero.**

## Stack

| Camada | Versão .NET (antes) | Versão Java (esta) |
|---|---|---|
| Linguagem | C# / .NET 8 | Java 21 |
| Framework | ASP.NET Core Web API | Spring Boot 3.5 |
| ORM | Entity Framework Core | Spring Data JPA / Hibernate |
| Banco | SQLite em arquivo | PostgreSQL 16 |
| Schema | `EnsureCreated()` | Migrations com Flyway |
| Documentação | Swashbuckle | springdoc-openapi |
| Testes | — | JUnit 5, Mockito, Testcontainers |
| Frontend | React + TypeScript (Vite) | o mesmo, sem alteração de lógica |

## O que mudou por dentro

Contrato igual não significa implementação igual. Três diferenças que valem
explicação:

**A consulta de totais virou agregação no banco.** A versão .NET carregava todas
as transações de todas as pessoas para a memória e somava em código. Aqui é uma
única query com `LEFT JOIN` e `GROUP BY`: o PostgreSQL soma e devolve pronto.
Para um controle residencial a diferença é imperceptível — mas o custo da versão
anterior cresce junto com o histórico, e o desta não.

**Schema versionado em vez de criado na primeira execução.** `EnsureCreated()`
resolve o primeiro dia e trava no segundo: qualquer mudança de estrutura vira
migração manual. Com Flyway, toda alteração é um arquivo `.sql` revisável em PR,
e o Hibernate roda com `ddl-auto: validate` — ele não cria nem altera nada, só
confere se o mapeamento bate com o schema.

**Regra de integridade também no banco.** `CHECK` no valor positivo, `CHECK` na
faixa de idade e `ON DELETE CASCADE` na chave estrangeira. A validação de
aplicação dá a mensagem boa para o usuário; a constraint garante que nada entra
errado nem por caminho torto.

E uma coisa que deliberadamente **não** mudou: `type` continua serializado como
`"Expense"`/`"Income"`. Em Java a constante do enum é `EXPENSE`, como manda a
convenção — a tradução acontece na serialização, num lugar só, em vez de vazar
para os DTOs.

## Testes

16 testes, um comando:

```
TransactionServiceTest         5 testes  regras de negócio, com mocks
ContratoDaApiIntegrationTest  11 testes  contrato completo, PostgreSQL real
```

O teste de integração é o que dá sentido ao projeto: ele afirma, rota por rota,
que a versão Java responde exatamente o que a versão .NET respondia. Se um nome
de campo ou um código de status mudar, o frontend quebra — e é ali que isso
aparece, não em produção.

## Estrutura

```
src/main/java/tech/pervian/gastos/
├── person/       entidade, repository, service, controller e DTOs de pessoa
├── transaction/  o mesmo para transação, mais o enum do tipo
├── summary/      a consulta de totais
└── common/       tratamento de erro, exceções de domínio, CORS e OpenAPI

frontend/         React + TypeScript, reaproveitado da versão .NET
```

Os nomes de classe seguem o contrato da API (`Person`, `Transaction`,
`Summary`), não a tradução em português. Assim o que está no código é o que está
no JSON, sem uma camada de tradução mental no meio.

## Configuração

Nenhum segredo no repositório:

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/gastos` |
| `DB_USER` | `gastos` |
| `DB_PASSWORD` | `gastos` |
| `SERVER_PORT` | `8092` |
| `CORS_ORIGINS` | `http://localhost:5173` |
| `VITE_API_URL` (build do frontend) | `http://localhost:8092/api` |

CORS é lista explícita de origens, nunca `*` — o frontend é conhecido, então não
há motivo para liberar qualquer site.

## Licença

[MIT](LICENSE).
