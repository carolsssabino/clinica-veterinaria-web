package clinicaveterinaria.security;

import clinicaveterinaria.exception.AcessoNegadoException;
import clinicaveterinaria.exception.NaoAutenticadoException;
import clinicaveterinaria.model.Usuario;
import clinicaveterinaria.model.enums.TipoUsuario;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import org.springframework.stereotype.Component;

/** Guarda o usuário logado na sessão HTTP e confere o perfil de acesso (GERENTE, ATENDENTE, VETERINARIO). */
@Component
public class SessaoUsuario {

    public static final String ATRIBUTO = "usuarioLogado";

    /** Dados mínimos do usuário guardados na sessão (nunca a senha). */
    public record Dados(Long id, String login, String nome, TipoUsuario tipo) implements Serializable { }

    public void iniciar(HttpSession sessao, Usuario usuario) {
        sessao.setAttribute(ATRIBUTO, new Dados(usuario.getId(), usuario.getLogin(), usuario.getNome(), usuario.getTipo()));
    }

    public Dados atual(HttpSession sessao) {
        Object valor = (sessao == null) ? null : sessao.getAttribute(ATRIBUTO);
        if (valor instanceof Dados dados) {
            return dados;
        }
        throw new NaoAutenticadoException("Sessão expirada ou usuário não autenticado.");
    }

    /** O GERENTE tem acesso a todas as funcionalidades (documento de requisitos, seção 3). */
    public void exigir(HttpSession sessao, TipoUsuario... permitidos) {
        Dados dados = atual(sessao);
        if (dados.tipo() == TipoUsuario.GERENTE) {
            return;
        }
        for (TipoUsuario permitido : permitidos) {
            if (permitido == dados.tipo()) {
                return;
            }
        }
        throw new AcessoNegadoException("Seu perfil (" + dados.tipo() + ") não tem permissão para esta operação.");
    }
}
