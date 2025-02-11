package es.prw.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Pregunta")
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPregunta;

    @Column(nullable = false)
    private String pregunta;

    @ManyToOne
    @JoinColumn(name = "id_test", nullable = false)
    private Test test;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Respuesta> respuestas;

    public Pregunta() {}

    public Pregunta(Integer idPregunta, String pregunta, Test test, List<Respuesta> respuestas) {
        this.idPregunta = idPregunta;
        this.pregunta = pregunta;
        this.test = test;
        this.respuestas = respuestas;
    }

    public Integer getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(Integer idPregunta) {
        this.idPregunta = idPregunta;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public List<Respuesta> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<Respuesta> respuestas) {
        this.respuestas = respuestas;
    }
}
