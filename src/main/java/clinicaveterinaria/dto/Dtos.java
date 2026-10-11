package clinicaveterinaria.dto;

import clinicaveterinaria.model.Animal;
import clinicaveterinaria.model.Consulta;
import clinicaveterinaria.model.Pagamento;
import clinicaveterinaria.model.Tutor;
import clinicaveterinaria.model.Usuario;
import clinicaveterinaria.model.Veterinario;
import clinicaveterinaria.model.enums.FormaPagamento;
import clinicaveterinaria.model.enums.StatusConsulta;
import clinicaveterinaria.model.enums.StatusPagamento;
import clinicaveterinaria.model.enums.TipoUsuario;
import clinicaveterinaria.service.BuscaService;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Objetos de entrada/saída da API REST (JSON). As entidades JPA nunca são expostas diretamente. */
public final class Dtos {

    private Dtos() {
    }

    // ---------- entrada ----------
    public record LoginRequest(String login, String senha) { }

    public record TutorRequest(String nome, String cpf, String telefone, String endereco) { }

    public record AnimalRequest(String nome, String tipo, String raca, Integer idade, String cpfTutor) { }

    public record ConsultaRequest(Long animalId, Long veterinarioId, LocalDateTime dataHora) { }

    public record RegistroConsultaRequest(String diagnostico, String tratamento, String observacoes) { }

    public record PagamentoRequest(Long consultaId, BigDecimal valor, BigDecimal valorPago,
            FormaPagamento formaPagamento, StatusPagamento status) { }

    // ---------- saída ----------
    public record UsuarioResponse(String login, String nome, TipoUsuario tipo) {
        public static UsuarioResponse from(Usuario u) {
            return new UsuarioResponse(u.getLogin(), u.getNome(), u.getTipo());
        }
    }

    public record TutorResponse(Long id, String nome, String cpf, String telefone, String endereco) {
        public static TutorResponse from(Tutor t) {
            return new TutorResponse(t.getId(), t.getNome(), t.getCpf(), t.getTelefone(), t.getEndereco());
        }
    }

    public record AnimalResponse(Long id, String nome, String tipo, String raca, int idade, Long tutorId, String tutorNome) {
        public static AnimalResponse from(Animal a) {
            return new AnimalResponse(a.getId(), a.getNome(), a.getTipo(), a.getRaca(), a.getIdade(),
                    a.getTutor().getId(), a.getTutor().getNome());
        }
    }

    public record VeterinarioResponse(Long id, String nome, String crmv) {
        public static VeterinarioResponse from(Veterinario v) {
            return new VeterinarioResponse(v.getId(), v.getNome(), v.getCrmv());
        }
    }

    public record ConsultaResponse(Long id, Long animalId, String animalNome, Long veterinarioId, String veterinarioNome,
            LocalDateTime dataHora, StatusConsulta status, String diagnostico, String tratamento, String observacoes) {
        public static ConsultaResponse from(Consulta c) {
            return new ConsultaResponse(c.getId(), c.getAnimal().getId(), c.getAnimal().getNome(),
                    c.getVeterinario().getId(), c.getVeterinario().getNome(), c.getDataHora(), c.getStatus(),
                    c.getDiagnostico(), c.getTratamento(), c.getObservacoes());
        }
    }

    public record PagamentoResponse(Long id, Long consultaId, BigDecimal valor, BigDecimal valorPago,
            FormaPagamento formaPagamento, StatusPagamento status, LocalDateTime dataPagamento) {
        public static PagamentoResponse from(Pagamento p) {
            return new PagamentoResponse(p.getId(), p.getConsulta().getId(), p.getValor(), p.getValorPago(),
                    p.getFormaPagamento(), p.getStatus(), p.getDataPagamento());
        }
    }

    public record BuscaResponse(Long tutorId, String tutorNome, String cpf, String telefone, Long animalId, String animalNome) {
        public static BuscaResponse from(BuscaService.Resultado r) {
            Tutor t = r.tutor();
            Animal a = r.animal();
            return new BuscaResponse(t.getId(), t.getNome(), t.getCpf(), t.getTelefone(),
                    a == null ? null : a.getId(), a == null ? null : a.getNome());
        }
    }
}
