package es.prw.models;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Pregunta")
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    private Integer idPregunta;

    @Column(name = "texto_pregunta", nullable = false)
    private String textoPregunta;

    @ManyToOne
    @JoinColumn(name = "id_test", nullable = false)
    @JsonIgnore
    private Test test;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Respuesta> respuestas;

    public Pregunta() {}

    public Pregunta(Integer idPregunta, String textoPregunta, Test test, List<Respuesta> respuestas) {
        this.idPregunta = idPregunta;
        this.textoPregunta = textoPregunta;
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
        return textoPregunta;
    }

    public void setPregunta(String textoPregunta) {
        this.textoPregunta = textoPregunta;
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
