package tech.pervian.gastos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Trava o contrato da API contra um PostgreSQL real.
 *
 * <p>Este e o teste que da sentido ao projeto: ele afirma que a versao Java
 * responde exatamente o que a versao .NET respondia — mesmas rotas, mesmos
 * nomes de campo, mesmos codigos de status. Se algum deles mudar, o frontend
 * quebra, e e aqui que isso aparece.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Sql(scripts = "/reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ContratoDaApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/people devolve 201 com id, name e age")
    void criaPessoa() throws Exception {
        mockMvc.perform(post("/api/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "  Artur  ", "age": 27}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Artur"))
                .andExpect(jsonPath("$.age").value(27));
    }

    @Test
    @DisplayName("POST /api/people sem nome devolve 400 no formato {errors}")
    void recusaPessoaSemNome() throws Exception {
        mockMvc.perform(post("/api/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "age": 200}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name[0]").value("O nome é obrigatório."))
                .andExpect(jsonPath("$.errors.age[0]").value("A idade deve estar entre 0 e 150."));
    }

    @Test
    @DisplayName("DELETE /api/people/{id} inexistente devolve 404 no formato {message}")
    void removePessoaInexistente() throws Exception {
        mockMvc.perform(delete("/api/people/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pessoa não encontrada."));
    }

    @Test
    @DisplayName("apagar a pessoa apaga as transacoes dela em cascata")
    void removePessoaEmCascata() throws Exception {
        long pessoa = criarPessoa("Artur", 27);
        criarTransacao("Salario", "1500.00", "Income", pessoa);

        mockMvc.perform(delete("/api/people/" + pessoa)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/transactions traz o nome da pessoa junto e o tipo como texto")
    void listaTransacoes() throws Exception {
        long pessoa = criarPessoa("Artur", 27);
        criarTransacao("Supermercado", "250.90", "Expense", pessoa);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Supermercado"))
                .andExpect(jsonPath("$[0].amount").value(250.90))
                .andExpect(jsonPath("$[0].type").value("Expense"))
                .andExpect(jsonPath("$[0].personId").value((int) pessoa))
                .andExpect(jsonPath("$[0].personName").value("Artur"));
    }

    @Test
    @DisplayName("menor de 18 anos cadastrando receita devolve 422")
    void menorNaoCadastraReceita() throws Exception {
        long menor = criarPessoa("Joao", 15);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Mesada", "amount": 100.00, "type": "Income", "personId": %d}"""
                                .formatted(menor)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Menores de 18 anos podem cadastrar apenas despesas."));
    }

    @Test
    @DisplayName("transacao para pessoa inexistente devolve 404")
    void transacaoParaPessoaInexistente() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Teste", "amount": 10.00, "type": "Expense", "personId": 9999}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pessoa não encontrada."));
    }

    @Test
    @DisplayName("valor zero ou negativo e recusado")
    void recusaValorNaoPositivo() throws Exception {
        long pessoa = criarPessoa("Artur", 27);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Teste", "amount": 0, "type": "Expense", "personId": %d}"""
                                .formatted(pessoa)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount[0]").value("O valor deve ser maior que zero."));
    }

    @Test
    @DisplayName("tipo fora de Expense/Income e recusado com 400")
    void recusaTipoInvalido() throws Exception {
        long pessoa = criarPessoa("Artur", 27);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Teste", "amount": 10, "type": "Transferencia", "personId": %d}"""
                                .formatted(pessoa)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("GET /api/summary soma por pessoa e devolve o total geral")
    void consultaTotais() throws Exception {
        long artur = criarPessoa("Artur", 27);
        long joao = criarPessoa("Joao", 15);
        criarTransacao("Salario", "3000.00", "Income", artur);
        criarTransacao("Aluguel", "1200.00", "Expense", artur);
        criarTransacao("Lanche", "50.00", "Expense", joao);

        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.people.length()").value(2))
                .andExpect(jsonPath("$.people[0].personName").value("Artur"))
                .andExpect(jsonPath("$.people[0].totalIncome").value(3000.00))
                .andExpect(jsonPath("$.people[0].totalExpense").value(1200.00))
                .andExpect(jsonPath("$.people[0].balance").value(1800.00))
                .andExpect(jsonPath("$.people[1].balance").value(-50.00))
                .andExpect(jsonPath("$.grandTotalIncome").value(3000.00))
                .andExpect(jsonPath("$.grandTotalExpense").value(1250.00))
                .andExpect(jsonPath("$.grandTotalBalance").value(1750.00));
    }

    @Test
    @DisplayName("pessoa sem transacao aparece nos totais com tudo zerado")
    void pessoaSemTransacaoApareceZerada() throws Exception {
        criarPessoa("Sem movimento", 40);

        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.people.length()").value(1))
                .andExpect(jsonPath("$.people[0].totalIncome").value(0))
                .andExpect(jsonPath("$.people[0].balance").value(0))
                .andExpect(jsonPath("$.grandTotalBalance").value(0));
    }

    private long criarPessoa(String nome, int idade) throws Exception {
        String corpo = mockMvc.perform(post("/api/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "age": %d}""".formatted(nome, idade)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return Long.parseLong(corpo.replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1"));
    }

    private void criarTransacao(String descricao, String valor, String tipo, long pessoa) throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "%s", "amount": %s, "type": "%s", "personId": %d}"""
                                .formatted(descricao, valor, tipo, pessoa)))
                .andExpect(status().isCreated());
    }
}
