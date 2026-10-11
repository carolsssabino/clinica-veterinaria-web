package clinicaveterinaria.controller;

import clinicaveterinaria.dto.Dtos.BuscaResponse;
import clinicaveterinaria.service.BuscaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** RF006 - Busca por nome do tutor, nome do animal ou CPF (ATENDENTE e VETERINARIO). */
@RestController
@RequestMapping("/api/busca")
public class BuscaController {

    private final BuscaService buscaService;

    public BuscaController(BuscaService buscaService) {
        this.buscaService = buscaService;
    }

    @GetMapping
    public List<BuscaResponse> buscar(@RequestParam(name = "termo", required = false) String termo) {
        return buscaService.buscar(termo).stream().map(BuscaResponse::from).toList();
    }
}
