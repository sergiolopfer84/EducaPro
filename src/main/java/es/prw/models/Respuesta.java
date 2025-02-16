package es.prw.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "Respuesta")
public class Respuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    private Integer idRespuesta;

    @Column(name = "texto_respuesta", nullable = false)
    private String textoRespuesta;

    @Column(name = "texto_explicacion", nullable = false)
    private String textoExplicacion;

    @Column(name = "nota", nullable = false)
    private Double nota;

    @ManyToOne
    @JoinColumn(name = "id_pregunta", nullable = false)
    @JsonIgnore
    private Pregunta pregunta;

    public Respuesta() {}

	public Respuesta(Integer idRespuesta, String textoRespuesta, String textoExplicacion, Double nota,
			Pregunta pregunta) {
		super();
		this.idRespuesta = idRespuesta;
		this.textoRespuesta = textoRespuesta;
		this.textoExplicacion = textoExplicacion;
		this.nota = nota;
		this.pregunta = pregunta;
	}

	public Integer getIdRespuesta() {
		return idRespuesta;
	}

	public void setIdRespuesta(Integer idRespuesta) {
		this.idRespuesta = idRespuesta;
	}

	public String getTextoRespuesta() {
		return textoRespuesta;
	}

	public void setTextoRespuesta(String textoRespuesta) {
		this.textoRespuesta = textoRespuesta;
	}

	public String getTextoExplicacion() {
		return textoExplicacion;
	}

	public void setTextoExplicacion(String textoExplicacion) {
		this.textoExplicacion = textoExplicacion;
	}

	public Double getNota() {
		return nota;
	}

	public void setNota(Double nota) {
		this.nota = nota;
	}

	public Pregunta getPregunta() {
		return pregunta;
	}

	public void setPregunta(Pregunta pregunta) {
		this.pregunta = pregunta;
	}

    
}
