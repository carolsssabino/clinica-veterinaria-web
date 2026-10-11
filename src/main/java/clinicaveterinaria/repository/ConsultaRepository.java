package clinicaveterinaria.repository;

import clinicaveterinaria.model.Consulta;
import clinicaveterinaria.model.enums.StatusConsulta;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByAnimal_Tutor_CpfOrderByDataHoraAsc(String cpf);

    List<Consulta> findByAnimal_Tutor_CpfAndStatusOrderByDataHoraAsc(String cpf, StatusConsulta status);

    boolean existsByVeterinarioIdAndDataHoraAndStatus(Long veterinarioId, LocalDateTime dataHora, StatusConsulta status);
}
