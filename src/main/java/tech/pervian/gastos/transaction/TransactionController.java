package tech.pervian.gastos.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import tech.pervian.gastos.transaction.dto.CreateTransactionRequest;
import tech.pervian.gastos.transaction.dto.TransactionResponse;

import java.util.List;

@Tag(name = "Transações", description = "Receitas e despesas por pessoa")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @Operation(summary = "Lista todas as transações cadastradas")
    @GetMapping
    public List<TransactionResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Cria uma transação; menores de 18 anos só podem cadastrar despesas")
    @PostMapping
    public ResponseEntity<TransactionResponse> criar(@RequestBody @Valid CreateTransactionRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        TransactionResponse transaction = service.criar(request);
        return ResponseEntity.created(uriBuilder.path("/api/transactions").build().toUri()).body(transaction);
    }
}
