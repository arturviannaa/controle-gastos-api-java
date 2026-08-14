package tech.pervian.gastos.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.pervian.gastos.common.BusinessRuleException;
import tech.pervian.gastos.common.NotFoundException;
import tech.pervian.gastos.person.Person;
import tech.pervian.gastos.person.PersonService;
import tech.pervian.gastos.transaction.dto.CreateTransactionRequest;
import tech.pervian.gastos.transaction.dto.TransactionResponse;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private PersonService personService;

    @InjectMocks
    private TransactionService service;

    @Test
    @DisplayName("menor de idade nao cadastra receita")
    void menorNaoCadastraReceita() {
        when(personService.buscarEntidade(1L)).thenReturn(pessoa("Joao", 17));

        assertThatThrownBy(() -> service.criar(request(TransactionType.INCOME)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Menores de 18 anos podem cadastrar apenas despesas.");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("menor de idade cadastra despesa normalmente")
    void menorCadastraDespesa() {
        when(personService.buscarEntidade(1L)).thenReturn(pessoa("Joao", 17));
        when(repository.save(any(Transaction.class))).thenAnswer(chamada -> chamada.getArgument(0));

        TransactionResponse resposta = service.criar(request(TransactionType.EXPENSE));

        assertThat(resposta.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(resposta.personName()).isEqualTo("Joao");
    }

    @Test
    @DisplayName("exatamente 18 anos ja pode cadastrar receita")
    void maioridadeNoLimite() {
        when(personService.buscarEntidade(1L)).thenReturn(pessoa("Ana", 18));
        when(repository.save(any(Transaction.class))).thenAnswer(chamada -> chamada.getArgument(0));

        assertThat(service.criar(request(TransactionType.INCOME)).type()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    @DisplayName("pessoa inexistente propaga o 404 e nao salva nada")
    void pessoaInexistente() {
        when(personService.buscarEntidade(1L)).thenThrow(new NotFoundException("Pessoa não encontrada."));

        assertThatThrownBy(() -> service.criar(request(TransactionType.EXPENSE)))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("descricao com espaco sobrando e gravada sem ele")
    void descricaoComTrim() {
        when(personService.buscarEntidade(1L)).thenReturn(pessoa("Ana", 30));
        when(repository.save(any(Transaction.class))).thenAnswer(chamada -> chamada.getArgument(0));

        TransactionResponse resposta = service.criar(new CreateTransactionRequest(
                "  Supermercado  ", new BigDecimal("10.00"), TransactionType.EXPENSE, 1L));

        assertThat(resposta.description()).isEqualTo("Supermercado");
    }

    private static CreateTransactionRequest request(TransactionType tipo) {
        return new CreateTransactionRequest("Salario", new BigDecimal("1500.00"), tipo, 1L);
    }

    private static Person pessoa(String nome, int idade) {
        Person person = new Person(nome, idade);
        ReflectionTestUtils.setField(person, "id", 1L);
        return person;
    }
}
