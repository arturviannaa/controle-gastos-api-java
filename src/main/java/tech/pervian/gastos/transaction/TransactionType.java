package tech.pervian.gastos.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Despesa ou receita.
 *
 * <p>A constante segue a convencao Java (maiuscula), mas entra e sai do JSON
 * como "Expense" e "Income" — que e o que a versao .NET publicava e o que o
 * frontend ja espera. O mapeamento fica aqui em vez de espalhado nos DTOs.
 */
public enum TransactionType {

    EXPENSE("Expense"),
    INCOME("Income");

    private final String noContrato;

    TransactionType(String noContrato) {
        this.noContrato = noContrato;
    }

    @JsonValue
    public String noContrato() {
        return noContrato;
    }

    // aceita "Expense", "expense" e "EXPENSE": ser tolerante na entrada nao
    // custa nada e evita quebrar cliente que normaliza o texto
    @JsonCreator
    public static TransactionType de(String valor) {
        for (TransactionType tipo : values()) {
            if (tipo.noContrato.equalsIgnoreCase(valor) || tipo.name().equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("O tipo deve ser 'Expense' (despesa) ou 'Income' (receita).");
    }
}
