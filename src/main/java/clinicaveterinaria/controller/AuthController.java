package clinicaveterinaria.controller;

import clinicaveterinaria.dto.Dtos.LoginRequest;
import clinicaveterinaria.dto.Dtos.UsuarioResponse;
import clinicaveterinaria.model.Usuario;
import clinicaveterinaria.security.SessaoUsuario;
import clinicaveterinaria.service.AutenticacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** RNF003 - Login, logout e consulta do usuário logado (sessão HTTP). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticacaoService autenticacaoService;
    private final SessaoUsuario sessaoUsuario;

    public AuthController(AutenticacaoService autenticacaoService, SessaoUsuario sessaoUsuario) {
        this.autenticacaoService = autenticacaoService;
        this.sessaoUsuario = sessaoUsuario;
    }

    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody LoginRequest dados, HttpServletRequest request) {
        Usuario usuario = autenticacaoService.autenticar(dados.login(), dados.senha());
        HttpSession antiga = request.getSession(false);
        if (antiga != null) {
            antiga.invalidate(); // nova sessão a cada login (evita fixação de sessão)
        }
        sessaoUsuario.iniciar(request.getSession(true), usuario);
        return UsuarioResponse.from(usuario);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
    }

    @GetMapping("/me")
    public UsuarioResponse me(HttpServletRequest request) {
        SessaoUsuario.Dados dados = sessaoUsuario.atual(request.getSession(false));
        return new UsuarioResponse(dados.login(), dados.nome(), dados.tipo());
    }
}
