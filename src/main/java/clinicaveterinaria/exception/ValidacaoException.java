package clinicaveterinaria.exception;

/** Regra de negócio violada ou dado inválido (HTTP 400). */
public class ValidacaoException extends RuntimeException {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
