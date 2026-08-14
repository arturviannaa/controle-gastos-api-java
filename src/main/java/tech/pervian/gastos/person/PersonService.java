package tech.pervian.gastos.person;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.pervian.gastos.common.NotFoundException;
import tech.pervian.gastos.person.dto.CreatePersonRequest;
import tech.pervian.gastos.person.dto.PersonResponse;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PersonResponse> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(PersonResponse::de).toList();
    }

    @Transactional
    public PersonResponse criar(CreatePersonRequest request) {
        // trim para nao gravar espaco acidental nem nome so de espacos
        Person person = repository.save(new Person(request.name().trim(), request.age()));
        return PersonResponse.de(person);
    }

    @Transactional
    public void remover(Long id) {
        // as transacoes vao junto pelo ON DELETE CASCADE do schema
        repository.delete(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Person buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pessoa não encontrada."));
    }
}
