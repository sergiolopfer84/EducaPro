package es.prw.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Respuesta")
public class Respuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRespuesta;

    @Column(nullable = false)
    private String respuesta;

    @Column(nullable = false)
    private String explicacion;

    @Column(nullable = false)
    private float nota;

    @ManyToOne
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    public Respuesta() {}

    public Respuesta(Integer idRespuesta, String respuesta, String explicacion, float nota, Pregunta pregunta) {
        this.idRespuesta = idRespuesta;
        this.respuesta = respuesta;
        this.explicacion = explicacion;
        this.nota = nota;
        this.pregunta = pregunta;
    }

    public Integer getIdRespuesta() {
        return idRespuesta;
    }

    public void setIdRespuesta(Integer idRespuesta) {
        this.idRespuesta = idRespuesta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getExplicacion() {
        return explicacion;
    }

    public void setExplicacion(String explicacion) {
        this.explicacion = explicacion;
    }

    public float getNota() {
        return nota;
    }

    public void setNota(float nota) {
        this.nota = nota;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }
}
