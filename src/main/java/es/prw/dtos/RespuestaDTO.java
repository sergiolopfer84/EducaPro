package es.prw.dtos;

import es.prw.models.Respuesta;

public class RespuestaDTO {
    private Integer idRespuesta;
    private String textoRespuesta;
    private String textoExplicacion; 
    private Double nota;
    private Integer idPregunta;
    private String textoPregunta;

    public RespuestaDTO(Respuesta respuesta) {
        this.idRespuesta = respuesta.getIdRespuesta();
        this.textoRespuesta = respuesta.getTextoRespuesta();
        this.textoExplicacion= respuesta.getTextoExplicacion();
        this.nota = respuesta.getNota();
        this.idPregunta = respuesta.getPregunta().getIdPregunta();
        this.textoPregunta = respuesta.getPregunta().getTextoPregunta();
    }
    public RespuestaDTO() {
        // Constructor vacío para deserialización
    }
    public String getTextoExplicacion() {
		return textoExplicacion;
	}

	public void setTextoExplicacion(String textoExplicacion) {
		this.textoExplicacion = textoExplicacion;
	}

	public Integer getIdRespuesta() {
        return idRespuesta;
    }

    public String getTextoRespuesta() {
        return textoRespuesta;
    }

    public Double getNota() {
        return nota;
    }

    public Integer getIdPregunta() {
        return idPregunta;
    }

    public String getTextoPregunta() {
        return textoPregunta;
    }

	public void setIdRespuesta(Integer idRespuesta) {
		this.idRespuesta = idRespuesta;
	}

	public void setTextoRespuesta(String textoRespuesta) {
		this.textoRespuesta = textoRespuesta;
	}

	public void setNota(Double nota) {
		this.nota = nota;
	}

	public void setIdPregunta(Integer idPregunta) {
		this.idPregunta = idPregunta;
	}

	public void setTextoPregunta(String textoPregunta) {
		this.textoPregunta = textoPregunta;
	}
}
