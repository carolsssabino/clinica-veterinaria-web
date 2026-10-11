package clinicaveterinaria.service;

import clinicaveterinaria.exception.NaoAutenticadoException;
import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Usuario;
import clinicaveterinaria.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RNF003 - Autenticação (login e senha). A senha é conferida contra o hash BCrypt. */
@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(String login, String senha) {
        if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
            throw new ValidacaoException("Informe login e senha.");
        }
        return usuarioRepository.findByLogin(login.trim())
                .filter(u -> passwordEncoder.matches(senha, u.getSenha()))
                .orElseThrow(() -> new NaoAutenticadoException("Login ou senha incorretos."));
    }
}
