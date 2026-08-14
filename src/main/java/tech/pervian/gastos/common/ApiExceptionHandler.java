package tech.pervian.gastos.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mantem o mesmo formato de erro da versao .NET, porque o frontend ja o le:
 * {"message": "..."} para regra de negocio e {"errors": {campo: [msgs]}} para
 * validacao, que era o ValidationProblemDetails do ASP.NET Core.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> businessRule(BusinessRuleException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
        Map<String, List<String>> erros = new LinkedHashMap<>();
        e.getFieldErrors().forEach(campo ->
                erros.computeIfAbsent(campo.getField(), chave -> new ArrayList<>())
                        .add(campo.getDefaultMessage()));

        return ResponseEntity.badRequest().body(Map.of("errors", erros));
    }

    /** Tipo invalido no JSON (ex.: type fora de Expense/Income) tambem e erro de validacao. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("errors", Map.of("body", List.of(motivo(e)))));
    }

    // Jackson embrulha a excecao do @JsonCreator em uma ou duas camadas, entao
    // vale percorrer a cadeia para achar a mensagem que o usuario precisa ler
    private static String motivo(Throwable e) {
        for (Throwable atual = e; atual != null; atual = atual.getCause()) {
            if (atual instanceof IllegalArgumentException && atual.getMessage() != null) {
                return atual.getMessage();
            }
        }
        return "O corpo da requisição está inválido.";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpected(Exception e) {
        // detalhe no log, mensagem generica na resposta
        log.error("erro nao tratado", e);
        return ResponseEntity.internalServerError()
                .body(Map.of("message", "Erro inesperado ao processar a requisição."));
    }
}
