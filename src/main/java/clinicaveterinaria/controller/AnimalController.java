package clinicaveterinaria.controller;

import clinicaveterinaria.dto.Dtos.AnimalRequest;
import clinicaveterinaria.dto.Dtos.AnimalResponse;
import clinicaveterinaria.model.Animal;
import clinicaveterinaria.model.enums.TipoUsuario;
import clinicaveterinaria.security.SessaoUsuario;
import clinicaveterinaria.service.AnimalService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** RF001 - Cadastro de animais (somente ATENDENTE; GERENTE também pode). */
@RestController
@RequestMapping("/api/animais")
public class AnimalController {

    private final AnimalService animalService;
    private final SessaoUsuario sessaoUsuario;

    public AnimalController(AnimalService animalService, SessaoUsuario sessaoUsuario) {
        this.animalService = animalService;
        this.sessaoUsuario = sessaoUsuario;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnimalResponse cadastrar(@RequestBody AnimalRequest dados, HttpSession sessao) {
        sessaoUsuario.exigir(sessao, TipoUsuario.ATENDENTE);
        return AnimalResponse.from(animalService.cadastrar(dados.nome(), dados.tipo(), dados.raca(), dados.idade(), dados.cpfTutor()));
    }

    @GetMapping
    public List<AnimalResponse> listar(@RequestParam(name = "cpf", required = false) String cpf) {
        List<Animal> animais = (cpf == null || cpf.isBlank())
                ? animalService.listarTodos()
                : animalService.listarPorCpfTutor(cpf);
        return animais.stream().map(AnimalResponse::from).toList();
    }
}
