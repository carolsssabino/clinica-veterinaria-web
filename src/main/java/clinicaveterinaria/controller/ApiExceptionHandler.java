package clinicaveterinaria.controller;

import clinicaveterinaria.exception.AcessoNegadoException;
import clinicaveterinaria.exception.NaoAutenticadoException;
import clinicaveterinaria.exception.RegistroNaoEncontradoException;
import clinicaveterinaria.exception.ValidacaoException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte as exceções de negócio em respostas JSON {"erro": "mensagem"} com o status HTTP adequado. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<Map<String, String>> validacao(ValidacaoException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(RegistroNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> naoAutenticado(NaoAutenticadoException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, String>> acessoNegado(AcessoNegadoException e) {
        return resposta(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida: confira os dados enviados.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridade(DataIntegrityViolationException e) {
        return resposta(HttpStatus.CONFLICT, "Dado duplicado ou inválido para o banco de dados.");
    }
}
