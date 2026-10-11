package clinicaveterinaria.exception;

/** Registro inexistente (HTTP 404). */
public class RegistroNaoEncontradoException extends RuntimeException {
    public RegistroNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
