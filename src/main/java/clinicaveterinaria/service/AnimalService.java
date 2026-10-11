package clinicaveterinaria.service;

import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Animal;
import clinicaveterinaria.model.Tutor;
import clinicaveterinaria.repository.AnimalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF001 - Cadastro de animais (sempre vinculados a um tutor já cadastrado). */
@Service
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final TutorService tutorService;

    public AnimalService(AnimalRepository animalRepository, TutorService tutorService) {
        this.animalRepository = animalRepository;
        this.tutorService = tutorService;
    }

    @Transactional
    public Animal cadastrar(String nome, String tipo, String raca, Integer idade, String cpfTutor) {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("O nome do animal é obrigatório.");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new ValidacaoException("O tipo do animal (cachorro, gato...) é obrigatório.");
        }
        if (idade == null) {
            throw new ValidacaoException("Informe a idade do animal.");
        }
        if (idade < 0) {
            throw new ValidacaoException("A idade do animal não pode ser negativa.");
        }
        Tutor tutor = tutorService.buscarPorCpf(cpfTutor);
        Animal animal = new Animal(nome.trim(), tipo.trim(), raca == null ? null : raca.trim(), idade);
        animal.vincularTutor(tutor);
        return animalRepository.save(animal);
    }

    @Transactional(readOnly = true)
    public List<Animal> listarPorCpfTutor(String cpfTutor) {
        Tutor tutor = tutorService.buscarPorCpf(cpfTutor);
        return animalRepository.findByTutorId(tutor.getId());
    }

    @Transactional(readOnly = true)
    public List<Animal> listarTodos() {
        return animalRepository.findAll();
    }
}
