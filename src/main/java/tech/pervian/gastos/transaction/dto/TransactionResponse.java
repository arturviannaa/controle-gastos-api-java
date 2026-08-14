package tech.pervian.gastos.transaction.dto;

import tech.pervian.gastos.transaction.Transaction;
import tech.pervian.gastos.transaction.TransactionType;

import java.math.BigDecimal;

/** Inclui o nome da pessoa para a listagem do frontend nao precisar de uma segunda requisicao. */
public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        TransactionType type,
        Long personId,
        String personName
) {

    public static TransactionResponse de(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getPerson().getId(),
                transaction.getPerson().getName());
    }
}
