package clinicaveterinaria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Animal;
import clinicaveterinaria.model.Consulta;
import clinicaveterinaria.model.Veterinario;
import clinicaveterinaria.model.enums.StatusConsulta;
import clinicaveterinaria.repository.AnimalRepository;
import clinicaveterinaria.repository.ConsultaRepository;
import clinicaveterinaria.repository.VeterinarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** RF003 - Agendamento e RF004 - Registro de consultas. */
@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;
    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private VeterinarioRepository veterinarioRepository;

    private ConsultaService service;
    private Animal animal;
    private Veterinario veterinario;

    @BeforeEach
    void setUp() {
        service = new ConsultaService(consultaRepository, animalRepository, veterinarioRepository);
        animal = new Animal("Rex", "Cachorro", "Labrador", 4);
        animal.setId(1L);
        veterinario = new Veterinario("Dra. Helena Prado", "CRMV-SP 12345");
        veterinario.setId(1L);
    }

    private LocalDateTime amanha9h() {
        return LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void T15_naoDeveAgendarConsultaEmDataPassada() {
        assertThrows(ValidacaoException.class, () -> service.agendar(1L, 1L, LocalDateTime.now().minusHours(1)));
    }

    @Test
    void T16_naoDeveAgendarQuandoVeterinarioJaTemConsultaNoHorario() {
        // BUG-003: a regra de conflito de horário existia apenas no JavaScript.
        LocalDateTime horario = amanha9h();
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(veterinarioRepository.findById(1L)).thenReturn(Optional.of(veterinario));
        when(consultaRepository.existsByVeterinarioIdAndDataHoraAndStatus(1L, horario, StatusConsulta.AGENDADA)).thenReturn(true);
        assertThrows(ValidacaoException.class, () -> service.agendar(1L, 1L, horario));
    }

    @Test
    void T17_deveAgendarConsultaValidaComStatusAgendada() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(veterinarioRepository.findById(1L)).thenReturn(Optional.of(veterinario));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        Consulta consulta = service.agendar(1L, 1L, amanha9h());
        assertEquals(StatusConsulta.AGENDADA, consulta.getStatus());
        assertSame(animal, consulta.getAnimal());
    }

    @Test
    void T18_naoDeveRegistrarConsultaSemDiagnostico() {
        assertThrows(ValidacaoException.class, () -> service.registrarAtendimento(1L, " ", "Repouso", ""));
    }

    @Test
    void T19_deveRegistrarConsultaAgendadaComoRealizada() {
        Consulta agendada = new Consulta(animal, veterinario, amanha9h());
        agendada.setId(5L);
        when(consultaRepository.findById(5L)).thenReturn(Optional.of(agendada));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        Consulta registrada = service.registrarAtendimento(5L, "Otite", "Antibiótico", "Retorno em 7 dias");
        assertEquals(StatusConsulta.REALIZADA, registrada.getStatus());
        assertEquals("Otite", registrada.getDiagnostico());
    }

    @Test
    void T29_naoDeveRegistrarConsultaQueJaFoiRealizada() {
        // BUG-010: antes, uma consulta REALIZADA podia ser registrada de novo e sobrescrita.
        Consulta realizada = new Consulta(animal, veterinario, amanha9h());
        realizada.setId(6L);
        realizada.registrarConsulta("Otite", "Antibiótico", "");
        when(consultaRepository.findById(6L)).thenReturn(Optional.of(realizada));
        assertThrows(ValidacaoException.class, () -> service.registrarAtendimento(6L, "Outro diagnóstico", "", ""));
    }
}
