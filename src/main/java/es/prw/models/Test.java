package es.prw.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "Test")
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test")
    private Integer idTest;

    @Column(name = "nombre_test", nullable = false, unique = true)
    private String nombreTest;

    @Column(name = "activa", nullable = false)
    private boolean activa = false;
    
    @ManyToOne
    @JoinColumn(name = "id_materia", nullable = false)
    @JsonIgnore
    private Materia materia;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pregunta> preguntas = new ArrayList<>();
    public Test() {
        // Se mantiene la inicialización en el constructor vacío
    }

    public Test(Integer idTest, String nombreTest, Materia materia, boolean activa) {
        this.idTest = idTest;
        this.nombreTest = nombreTest;
        this.materia = materia;
        this.activa = activa;
        this.preguntas = new ArrayList<>(); // Se inicializa aquí también
    }

    public Integer getIdTest() {
        return idTest;
    }

    public void setIdTest(Integer idTest) {
        this.idTest = idTest;
    }

   
    public boolean isActivo() {
		return activa;
	}

	public void setActivo(boolean activa) {
		this.activa = activa;
	}

	public String getNombreTest() {
		return nombreTest;
	}

	public void setNombreTest(String nombreTest) {
		this.nombreTest = nombreTest;
	}

	public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }
}
