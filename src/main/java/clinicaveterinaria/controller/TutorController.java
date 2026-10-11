package clinicaveterinaria.controller;

import clinicaveterinaria.dto.Dtos.TutorRequest;
import clinicaveterinaria.dto.Dtos.TutorResponse;
import clinicaveterinaria.model.Tutor;
import clinicaveterinaria.model.enums.TipoUsuario;
import clinicaveterinaria.security.SessaoUsuario;
import clinicaveterinaria.service.TutorService;
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

/** RF002 - Cadastro de tutores (somente ATENDENTE; GERENTE também pode). */
@RestController
@RequestMapping("/api/tutores")
public class TutorController {

    private final TutorService tutorService;
    private final SessaoUsuario sessaoUsuario;

    public TutorController(TutorService tutorService, SessaoUsuario sessaoUsuario) {
        this.tutorService = tutorService;
        this.sessaoUsuario = sessaoUsuario;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TutorResponse cadastrar(@RequestBody TutorRequest dados, HttpSession sessao) {
        sessaoUsuario.exigir(sessao, TipoUsuario.ATENDENTE);
        return TutorResponse.from(tutorService.cadastrar(dados.nome(), dados.cpf(), dados.telefone(), dados.endereco()));
    }

    @GetMapping("/buscar")
    public TutorResponse buscarPorCpf(@RequestParam("cpf") String cpf) {
        return TutorResponse.from(tutorService.buscarPorCpf(cpf));
    }

    @GetMapping
    public List<TutorResponse> listar(@RequestParam(name = "nome", required = false) String nome) {
        List<Tutor> tutores = (nome == null || nome.isBlank())
                ? tutorService.listarTodos()
                : tutorService.buscarPorNome(nome.trim());
        return tutores.stream().map(TutorResponse::from).toList();
    }
}
