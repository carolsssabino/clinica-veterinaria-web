package clinicaveterinaria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Animal atendido pela clínica; sempre vinculado a um tutor já cadastrado. */
@Entity
@Table(name = "animal")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(length = 60)
    private String raca;

    @Column(nullable = false)
    private int idade;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    public Animal() {
    }

    public Animal(String nome, String tipo, String raca, int idade) {
        this.nome = nome;
        this.tipo = tipo;
        this.raca = raca;
        this.idade = idade;
    }

    public void vincularTutor(Tutor tutor) {
        this.tutor = tutor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }
    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }
    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    @Override
    public String toString() {
        return "Animal: " + nome + " (" + tipo + ") - Tutor: " + (tutor != null ? tutor.getNome() : "Sem tutor");
    }
}
