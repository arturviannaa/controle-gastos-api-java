package tech.pervian.gastos.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // JOIN FETCH porque a listagem devolve o nome da pessoa: sem ele o
    // Hibernate faria uma consulta por transacao (N+1)
    @Query("SELECT t FROM Transaction t JOIN FETCH t.person ORDER BY t.id")
    List<Transaction> listarComPessoa();
}
