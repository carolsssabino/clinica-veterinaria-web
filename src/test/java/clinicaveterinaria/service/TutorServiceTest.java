package clinicaveterinaria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Tutor;
import clinicaveterinaria.repository.TutorRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** RF002 - T07 a T10 vêm da Etapa 7 (antes com DAO em memória; agora com repositório simulado - Mockito). */
@ExtendWith(MockitoExtension.class)
class TutorServiceTest {

    @Mock
    private TutorRepository repository;

    private TutorService service;

    @BeforeEach
    void setUp() {
        service = new TutorService(repository);
    }

    @Test
    void T07_deveCadastrarTutorComDadosValidos() {
        when(repository.save(any(Tutor.class))).thenAnswer(inv -> inv.getArgument(0));
        Tutor tutor = service.cadastrar("Carlos Oliveira", "52998224725", "(13) 98765-4321", "Rua das Flores, 123");
        assertEquals("Carlos Oliveira", tutor.getNome());
        assertEquals("52998224725", tutor.getCpf());
    }

    @Test
    void T08_naoDeveCadastrarTutorComCpfInvalido() {
        assertThrows(ValidacaoException.class, () -> service.cadastrar("Fulano", "12345678900", "", ""));
    }

    @Test
    void T09_naoDeveCadastrarTutorComNomeVazio() {
        assertThrows(ValidacaoException.class, () -> service.cadastrar("", "52998224725", "", ""));
    }

    @Test
    void T10_naoDevePermitirCpfDuplicado() {
        when(repository.findByCpf("52998224725")).thenReturn(Optional.of(new Tutor("Carlos Oliveira", "52998224725", "", "")));
        assertThrows(ValidacaoException.class, () -> service.cadastrar("Outro Nome", "52998224725", "", ""));
    }

    @Test
    void T11_naoDevePermitirCpfDuplicadoComMascaraDiferente() {
        // BUG-001: o CPF já gravado sem máscara precisa ser encontrado quando o cadastro chega com máscara.
        when(repository.findByCpf("52998224725")).thenReturn(Optional.of(new Tutor("Carlos Oliveira", "52998224725", "", "")));
        assertThrows(ValidacaoException.class, () -> service.cadastrar("Outro Nome", "529.982.247-25", "", ""));
    }
}
