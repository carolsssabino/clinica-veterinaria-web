package clinicaveterinaria.repository;

import clinicaveterinaria.model.Animal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    List<Animal> findByTutorId(Long tutorId);

    List<Animal> findByNomeContainingIgnoreCase(String nome);
}
