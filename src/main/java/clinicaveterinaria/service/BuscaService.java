package clinicaveterinaria.service;

import clinicaveterinaria.model.Animal;
import clinicaveterinaria.model.Tutor;
import clinicaveterinaria.repository.AnimalRepository;
import clinicaveterinaria.repository.TutorRepository;
import clinicaveterinaria.util.ValidadorCpf;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF006 - Busca por nome do tutor, nome do animal ou CPF. */
@Service
public class BuscaService {

    /** Um resultado: tutor e (opcionalmente) um de seus animais. */
    public record Resultado(Tutor tutor, Animal animal) {
    }

    private final TutorRepository tutorRepository;
    private final AnimalRepository animalRepository;

    public BuscaService(TutorRepository tutorRepository, AnimalRepository animalRepository) {
        this.tutorRepository = tutorRepository;
        this.animalRepository = animalRepository;
    }

    @Transactional(readOnly = true)
    public List<Resultado> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return List.of();
        }
        String q = termo.trim();
        Map<Long, Tutor> tutores = new LinkedHashMap<>();
        tutorRepository.findByNomeContainingIgnoreCase(q).forEach(t -> tutores.put(t.getId(), t));
        // Só procura por CPF quando o termo parece um CPF (apenas números e pontuação).
        String digitos = ValidadorCpf.somenteDigitos(q);
        if (!digitos.isEmpty() && q.matches("[\\d.\\-\\s]+")) {
            tutorRepository.findByCpfContaining(digitos).forEach(t -> tutores.put(t.getId(), t));
        }
        Map<String, Resultado> resultados = new LinkedHashMap<>();
        for (Tutor t : tutores.values()) {
            List<Animal> animais = animalRepository.findByTutorId(t.getId());
            if (animais.isEmpty()) {
                resultados.put(t.getId() + "-0", new Resultado(t, null));
            }
            for (Animal a : animais) {
                resultados.put(t.getId() + "-" + a.getId(), new Resultado(t, a));
            }
        }
        for (Animal a : animalRepository.findByNomeContainingIgnoreCase(q)) {
            resultados.putIfAbsent(a.getTutor().getId() + "-" + a.getId(), new Resultado(a.getTutor(), a));
        }
        return new ArrayList<>(resultados.values());
    }
}
