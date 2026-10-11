package clinicaveterinaria.controller;

import clinicaveterinaria.dto.Dtos.ConsultaRequest;
import clinicaveterinaria.dto.Dtos.ConsultaResponse;
import clinicaveterinaria.dto.Dtos.RegistroConsultaRequest;
import clinicaveterinaria.model.enums.StatusConsulta;
import clinicaveterinaria.model.enums.TipoUsuario;
import clinicaveterinaria.security.SessaoUsuario;
import clinicaveterinaria.service.ConsultaService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** RF003 - Agendar (ATENDENTE) e RF004 - Registrar atendimento (VETERINARIO). GERENTE pode ambos. */
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final SessaoUsuario sessaoUsuario;

    public ConsultaController(ConsultaService consultaService, SessaoUsuario sessaoUsuario) {
        this.consultaService = consultaService;
        this.sessaoUsuario = sessaoUsuario;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaResponse agendar(@RequestBody ConsultaRequest dados, HttpSession sessao) {
        sessaoUsuario.exigir(sessao, TipoUsuario.ATENDENTE);
        return ConsultaResponse.from(consultaService.agendar(dados.animalId(), dados.veterinarioId(), dados.dataHora()));
    }

    @PutMapping("/{id}/registro")
    public ConsultaResponse registrar(@PathVariable("id") Long id, @RequestBody RegistroConsultaRequest dados, HttpSession sessao) {
        sessaoUsuario.exigir(sessao, TipoUsuario.VETERINARIO);
        return ConsultaResponse.from(consultaService.registrarAtendimento(id, dados.diagnostico(), dados.tratamento(), dados.observacoes()));
    }

    @GetMapping
    public List<ConsultaResponse> listar(@RequestParam("cpf") String cpf,
            @RequestParam(name = "status", required = false) StatusConsulta status,
            @RequestParam(name = "animalId", required = false) Long animalId) {
        return consultaService.listarPorTutor(cpf, status, animalId).stream().map(ConsultaResponse::from).toList();
    }
}
