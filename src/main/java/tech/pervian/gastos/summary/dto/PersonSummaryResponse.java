package tech.pervian.gastos.summary.dto;

import java.math.BigDecimal;

/** Totais de uma pessoa. Saldo = receitas - despesas. */
public record PersonSummaryResponse(
        Long personId,
        String personName,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {
}
