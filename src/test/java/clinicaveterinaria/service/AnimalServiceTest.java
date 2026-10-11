package clinicaveterinaria.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import clinicaveterinaria.exception.RegistroNaoEncontradoException;
import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.repository.AnimalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** RF001 - Cadastro de animais. */
@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private TutorService tutorService;

    private AnimalService service;

    @BeforeEach
    void setUp() {
        service = new AnimalService(animalRepository, tutorService);
    }

    @Test
    void T12_naoDeveCadastrarAnimalComNomeEmBranco() {
        assertThrows(ValidacaoException.class, () -> service.cadastrar("  ", "Cachorro", "SRD", 3, "52998224725"));
    }

    @Test
    void T13_naoDeveCadastrarAnimalSemTipo() {
        // BUG-007: a tela exigia o tipo, mas o back-end não.
        assertThrows(ValidacaoException.class, () -> service.cadastrar("Rex", "", "SRD", 3, "52998224725"));
    }

    @Test
    void T14_naoDeveCadastrarAnimalParaTutorInexistente() {
        when(tutorService.buscarPorCpf("52998224725"))
                .thenThrow(new RegistroNaoEncontradoException("Tutor não encontrado para o CPF informado."));
        assertThrows(RegistroNaoEncontradoException.class, () -> service.cadastrar("Rex", "Cachorro", "SRD", 3, "52998224725"));
    }
}
