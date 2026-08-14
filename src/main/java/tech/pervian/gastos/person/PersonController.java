package tech.pervian.gastos.person;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import tech.pervian.gastos.person.dto.CreatePersonRequest;
import tech.pervian.gastos.person.dto.PersonResponse;

import java.util.List;

@Tag(name = "Pessoas", description = "Cadastro de pessoas")
@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    @Operation(summary = "Lista todas as pessoas cadastradas")
    @GetMapping
    public List<PersonResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Cria uma pessoa")
    @PostMapping
    public ResponseEntity<PersonResponse> criar(@RequestBody @Valid CreatePersonRequest request,
                                                UriComponentsBuilder uriBuilder) {
        PersonResponse person = service.criar(request);
        // aponta para a colecao, como fazia o CreatedAtAction(nameof(GetAll)) do .NET
        return ResponseEntity.created(uriBuilder.path("/api/people").build().toUri()).body(person);
    }

    @Operation(summary = "Remove uma pessoa e, em cascata, as transações dela")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
