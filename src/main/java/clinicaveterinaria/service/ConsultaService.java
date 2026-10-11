package clinicaveterinaria.service;

import clinicaveterinaria.exception.RegistroNaoEncontradoException;
import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Animal;
import clinicaveterinaria.model.Consulta;
import clinicaveterinaria.model.Veterinario;
import clinicaveterinaria.model.enums.StatusConsulta;
import clinicaveterinaria.repository.AnimalRepository;
import clinicaveterinaria.repository.ConsultaRepository;
import clinicaveterinaria.repository.VeterinarioRepository;
import clinicaveterinaria.util.ValidadorCpf;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF003 - Agendamento e RF004 - Registro de consultas. */
@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final AnimalRepository animalRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ConsultaService(ConsultaRepository consultaRepository, AnimalRepository animalRepository,
            VeterinarioRepository veterinarioRepository) {
        this.consultaRepository = consultaRepository;
        this.animalRepository = animalRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @Transactional
    public Consulta agendar(Long animalId, Long veterinarioId, LocalDateTime dataHora) {
        if (animalId == null) {
            throw new ValidacaoException("Selecione um animal para agendar a consulta.");
        }
        if (veterinarioId == null) {
            throw new ValidacaoException("Selecione um veterinário responsável.");
        }
        if (dataHora == null || dataHora.isBefore(LocalDateTime.now())) {
            throw new ValidacaoException("A data/hora da consulta deve ser futura.");
        }
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Animal não encontrado."));
        Veterinario veterinario = veterinarioRepository.findById(veterinarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Veterinário não encontrado."));
        if (consultaRepository.existsByVeterinarioIdAndDataHoraAndStatus(veterinarioId, dataHora, StatusConsulta.AGENDADA)) {
            throw new ValidacaoException("Esse veterinário já tem consulta nesse horário.");
        }
        return consultaRepository.save(new Consulta(animal, veterinario, dataHora));
    }

    @Transactional
    public Consulta registrarAtendimento(Long consultaId, String diagnostico, String tratamento, String observacoes) {
        if (diagnostico == null || diagnostico.isBlank()) {
            throw new ValidacaoException("O diagnóstico é obrigatório para registrar a consulta.");
        }
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Consulta não encontrada."));
        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new ValidacaoException("Somente consultas agendadas podem ser registradas.");
        }
        consulta.registrarConsulta(diagnostico.trim(), tratamento, observacoes);
        return consultaRepository.save(consulta);
    }

    @Transactional(readOnly = true)
    public List<Consulta> listarPorTutor(String cpf, StatusConsulta status, Long animalId) {
        String cpfNumerico = ValidadorCpf.somenteDigitos(cpf);
        if (cpfNumerico.isEmpty()) {
            throw new ValidacaoException("Informe o CPF do tutor.");
        }
        List<Consulta> consultas = (status == null)
                ? consultaRepository.findByAnimal_Tutor_CpfOrderByDataHoraAsc(cpfNumerico)
                : consultaRepository.findByAnimal_Tutor_CpfAndStatusOrderByDataHoraAsc(cpfNumerico, status);
        if (animalId != null) {
            consultas = consultas.stream().filter(c -> animalId.equals(c.getAnimal().getId())).toList();
        }
        return consultas;
    }
}
