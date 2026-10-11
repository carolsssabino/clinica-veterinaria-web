package clinicaveterinaria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "veterinario")
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 40)
    private String crmv;

    @OneToMany(mappedBy = "veterinario")
    private List<Consulta> consultas = new ArrayList<>();

    public Veterinario() {
    }

    public Veterinario(String nome, String crmv) {
        this.nome = nome;
        this.crmv = crmv;
    }

    public void registrarConsulta(Consulta consulta) {
        this.consultas.add(consulta);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCrmv() { return crmv; }
    public void setCrmv(String crmv) { this.crmv = crmv; }
    public List<Consulta> getConsultas() { return consultas; }
    public void setConsultas(List<Consulta> consultas) { this.consultas = consultas; }
}
