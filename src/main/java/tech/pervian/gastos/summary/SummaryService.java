package tech.pervian.gastos.summary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.pervian.gastos.summary.dto.PersonSummaryResponse;
import tech.pervian.gastos.summary.dto.SummaryResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@Service
public class SummaryService {

    private final SummaryRepository repository;

    public SummaryService(SummaryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SummaryResponse consultar() {
        List<PersonSummaryResponse> pessoas = repository.totaisPorPessoa();

        return new SummaryResponse(
                pessoas,
                somar(pessoas, PersonSummaryResponse::totalIncome),
                somar(pessoas, PersonSummaryResponse::totalExpense),
                somar(pessoas, PersonSummaryResponse::balance));
    }

    private static BigDecimal somar(List<PersonSummaryResponse> pessoas,
                                    Function<PersonSummaryResponse, BigDecimal> campo) {
        return pessoas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
