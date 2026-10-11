package clinicaveterinaria.controller;

import clinicaveterinaria.dto.Dtos.PagamentoRequest;
import clinicaveterinaria.dto.Dtos.PagamentoResponse;
import clinicaveterinaria.model.enums.TipoUsuario;
import clinicaveterinaria.security.SessaoUsuario;
import clinicaveterinaria.service.PagamentoService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** RF005 - Controle de pagamentos (ATENDENTE; GERENTE também pode). */
@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final SessaoUsuario sessaoUsuario;

    public PagamentoController(PagamentoService pagamentoService, SessaoUsuario sessaoUsuario) {
        this.pagamentoService = pagamentoService;
        this.sessaoUsuario = sessaoUsuario;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoResponse registrar(@RequestBody PagamentoRequest dados, HttpSession sessao) {
        sessaoUsuario.exigir(sessao, TipoUsuario.ATENDENTE);
        return PagamentoResponse.from(pagamentoService.registrar(dados.consultaId(), dados.valor(),
                dados.valorPago(), dados.formaPagamento(), dados.status()));
    }

    @GetMapping("/pendentes")
    public List<PagamentoResponse> pendentes() {
        return pagamentoService.listarPendentes().stream().map(PagamentoResponse::from).toList();
    }
}
