package clinicaveterinaria.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import clinicaveterinaria.exception.NaoAutenticadoException;
import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Usuario;
import clinicaveterinaria.model.enums.TipoUsuario;
import clinicaveterinaria.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** RNF003 - Autenticação com senha em hash BCrypt (BUG-006: antes a senha era comparada em texto puro). */
@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private AutenticacaoService service;
    private Usuario atendente;

    @BeforeEach
    void setUp() {
        service = new AutenticacaoService(usuarioRepository, encoder);
        atendente = new Usuario("atendente", encoder.encode("1234"), "Atendente", TipoUsuario.ATENDENTE);
    }

    @Test
    void T25_deveAutenticarComLoginESenhaCorretos() {
        when(usuarioRepository.findByLogin("atendente")).thenReturn(Optional.of(atendente));
        assertSame(atendente, service.autenticar("atendente", "1234"));
    }

    @Test
    void T26_naoDeveAutenticarComSenhaIncorreta() {
        when(usuarioRepository.findByLogin("atendente")).thenReturn(Optional.of(atendente));
        assertThrows(NaoAutenticadoException.class, () -> service.autenticar("atendente", "errada"));
    }

    @Test
    void T27_deveExigirLoginESenha() {
        assertThrows(ValidacaoException.class, () -> service.autenticar("", "1234"));
        assertThrows(ValidacaoException.class, () -> service.autenticar("atendente", " "));
    }
}
