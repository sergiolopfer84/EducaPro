package es.prw.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Materia")
public class Materia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMateria;

    @Column(nullable = false, unique = true)
    private String materia;

    @OneToMany(mappedBy = "materia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Test> tests;

    public Materia() {}

    public Materia(Integer idMateria, String materia) {
        this.idMateria = idMateria;
        this.materia = materia;
    }

    public Integer getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(Integer idMateria) {
        this.idMateria = idMateria;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }
}
