package tech.pervian.gastos.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.pervian.gastos.transaction.TransactionType;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @Schema(example = "Supermercado")
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
        String description,

        // sempre positivo: quem decide se soma ou subtrai do saldo e o type,
        // nao o sinal do numero
        @Schema(example = "250.90")
        @NotNull(message = "O valor é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        BigDecimal amount,

        @Schema(example = "Expense", allowableValues = {"Expense", "Income"})
        @NotNull(message = "O tipo deve ser 'Expense' (despesa) ou 'Income' (receita).")
        TransactionType type,

        @Schema(example = "1")
        @NotNull(message = "A pessoa é obrigatória.")
        Long personId
) {
}
