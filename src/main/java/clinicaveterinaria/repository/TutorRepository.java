package clinicaveterinaria.repository;

import clinicaveterinaria.model.Tutor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByCpf(String cpf);

    List<Tutor> findByNomeContainingIgnoreCase(String nome);

    List<Tutor> findByCpfContaining(String parteDoCpf);
}
