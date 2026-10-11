package clinicaveterinaria.controller;

import clinicaveterinaria.dto.Dtos.VeterinarioResponse;
import clinicaveterinaria.repository.VeterinarioRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lista os veterinários para o agendamento (RF003). */
@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    private final VeterinarioRepository veterinarioRepository;

    public VeterinarioController(VeterinarioRepository veterinarioRepository) {
        this.veterinarioRepository = veterinarioRepository;
    }

    @GetMapping
    public List<VeterinarioResponse> listar() {
        return veterinarioRepository.findAll().stream().map(VeterinarioResponse::from).toList();
    }
}
