package clinicaveterinaria.exception;

/** Usuário sem sessão válida ou credenciais incorretas (HTTP 401). */
public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
