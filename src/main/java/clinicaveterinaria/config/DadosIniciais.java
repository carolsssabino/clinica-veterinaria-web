package clinicaveterinaria.config;

import clinicaveterinaria.model.Animal;
import clinicaveterinaria.model.Tutor;
import clinicaveterinaria.model.Usuario;
import clinicaveterinaria.model.Veterinario;
import clinicaveterinaria.model.enums.TipoUsuario;
import clinicaveterinaria.repository.AnimalRepository;
import clinicaveterinaria.repository.TutorRepository;
import clinicaveterinaria.repository.UsuarioRepository;
import clinicaveterinaria.repository.VeterinarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga inicial (só se as tabelas estiverem vazias): os mesmos dados de demonstração da Etapa 8.
 * Usuários atendente / gerente / veterinario com senha 1234 (trocar fora do ambiente acadêmico).
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final TutorRepository tutorRepository;
    private final AnimalRepository animalRepository;
    private final PasswordEncoder passwordEncoder;

    public DadosIniciais(UsuarioRepository usuarioRepository, VeterinarioRepository veterinarioRepository,
            TutorRepository tutorRepository, AnimalRepository animalRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.tutorRepository = tutorRepository;
        this.animalRepository = animalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(new Usuario("atendente", passwordEncoder.encode("1234"), "Atendente", TipoUsuario.ATENDENTE));
            usuarioRepository.save(new Usuario("gerente", passwordEncoder.encode("1234"), "Gerente", TipoUsuario.GERENTE));
            usuarioRepository.save(new Usuario("veterinario", passwordEncoder.encode("1234"), "Veterinário", TipoUsuario.VETERINARIO));
        }
        if (veterinarioRepository.count() == 0) {
            veterinarioRepository.save(new Veterinario("Dra. Helena Prado", "CRMV-SP 12345"));
            veterinarioRepository.save(new Veterinario("Dr. Marcos Lima", "CRMV-SP 67890"));
        }
        if (tutorRepository.count() == 0) {
            Tutor joao = tutorRepository.save(new Tutor("João Silva", "52998224725", "(11) 99999-9999", "Rua das Flores, 100"));
            Animal rex = new Animal("Rex", "Cachorro", "Labrador", 4);
            rex.vincularTutor(joao);
            animalRepository.save(rex);
        }
    }
}
