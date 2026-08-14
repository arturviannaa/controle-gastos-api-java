package tech.pervian.gastos.common;

/** Vira 422: a requisicao esta bem formada, mas fere uma regra de negocio. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String mensagem) {
        super(mensagem);
    }
}
