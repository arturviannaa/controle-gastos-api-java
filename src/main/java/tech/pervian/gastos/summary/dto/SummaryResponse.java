package tech.pervian.gastos.summary.dto;

import java.math.BigDecimal;
import java.util.List;

/** Totais de cada pessoa e o total geral. */
public record SummaryResponse(
        List<PersonSummaryResponse> people,
        BigDecimal grandTotalIncome,
        BigDecimal grandTotalExpense,
        BigDecimal grandTotalBalance
) {
}
