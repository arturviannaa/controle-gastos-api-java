package tech.pervian.gastos.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.pervian.gastos.common.BusinessRuleException;
import tech.pervian.gastos.person.Person;
import tech.pervian.gastos.person.PersonService;
import tech.pervian.gastos.transaction.dto.CreateTransactionRequest;
import tech.pervian.gastos.transaction.dto.TransactionResponse;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final PersonService personService;

    public TransactionService(TransactionRepository repository, PersonService personService) {
        this.repository = repository;
        this.personService = personService;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listar() {
        return repository.listarComPessoa().stream().map(TransactionResponse::de).toList();
    }

    @Transactional
    public TransactionResponse criar(CreateTransactionRequest request) {
        // regra 1: a pessoa precisa existir (o buscarEntidade estoura 404)
        Person person = personService.buscarEntidade(request.personId());

        // regra 2: menor de idade so cadastra despesa
        if (person.menorDeIdade() && request.type() == TransactionType.INCOME) {
            throw new BusinessRuleException("Menores de 18 anos podem cadastrar apenas despesas.");
        }

        Transaction transaction = repository.save(new Transaction(
                request.description().trim(), request.amount(), request.type(), person));

        return TransactionResponse.de(transaction);
    }
}
