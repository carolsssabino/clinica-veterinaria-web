package clinicaveterinaria.model;

import clinicaveterinaria.model.enums.StatusConsulta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Consulta: nasce AGENDADA; vira REALIZADA quando o veterinário registra o atendimento. */
@Entity
@Table(name = "consulta")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 2000)
    private String diagnostico;

    @Column(length = 2000)
    private String tratamento;

    @Column(length = 2000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status = StatusConsulta.AGENDADA;

    public Consulta() {
    }

    public Consulta(Animal animal, Veterinario veterinario, LocalDateTime dataHora) {
        this.animal = animal;
        this.veterinario = veterinario;
        this.dataHora = dataHora;
        this.status = StatusConsulta.AGENDADA;
    }

    public void agendar() {
        this.status = StatusConsulta.AGENDADA;
    }

    public void registrarConsulta(String diagnostico, String tratamento, String observacoes) {
        this.diagnostico = diagnostico;
        this.tratamento = tratamento;
        this.observacoes = observacoes;
        this.status = StatusConsulta.REALIZADA;
    }

    public void cancelar() {
        this.status = StatusConsulta.CANCELADA;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Animal getAnimal() { return animal; }
    public void setAnimal(Animal animal) { this.animal = animal; }
    public Veterinario getVeterinario() { return veterinario; }
    public void setVeterinario(Veterinario veterinario) { this.veterinario = veterinario; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamento() { return tratamento; }
    public void setTratamento(String tratamento) { this.tratamento = tratamento; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }
}
