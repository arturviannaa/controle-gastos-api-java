package tech.pervian.gastos.common;

/** Vira 404 com corpo {"message": "..."}, formato que o frontend ja consome. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String mensagem) {
        super(mensagem);
    }
}
