package tech.pervian.gastos.summary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import tech.pervian.gastos.person.Person;
import tech.pervian.gastos.summary.dto.PersonSummaryResponse;

import java.util.List;

public interface SummaryRepository extends Repository<Person, Long> {

    /**
     * Uma consulta so, agregando no banco. A versao .NET carregava todas as
     * transacoes para a memoria e somava em Java; aqui o Postgres soma e
     * devolve pronto, o que nao degrada conforme o historico cresce.
     *
     * <p>LEFT JOIN porque pessoa sem transacao tambem entra na lista, zerada.
     */
    @Query("""
            SELECT new tech.pervian.gastos.summary.dto.PersonSummaryResponse(
                p.id,
                p.name,
                COALESCE(SUM(CASE WHEN t.type = tech.pervian.gastos.transaction.TransactionType.INCOME THEN t.amount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.type = tech.pervian.gastos.transaction.TransactionType.EXPENSE THEN t.amount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.type = tech.pervian.gastos.transaction.TransactionType.INCOME THEN t.amount ELSE -t.amount END), 0)
            )
            FROM Person p LEFT JOIN Transaction t ON t.person = p
            GROUP BY p.id, p.name
            ORDER BY p.id
            """)
    List<PersonSummaryResponse> totaisPorPessoa();
}
