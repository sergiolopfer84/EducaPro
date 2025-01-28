package es.prw.models;

public class Respuesta {
	private Integer idRespuesta; 
	private String respuesta;
	private String explicacion;
	private float nota;
	private Integer idPregunta;
	public Respuesta() {
		super();
		
	}
	public Respuesta(Integer idRespuesta, String respuesta, String explicacion, float nota, Integer idPregunta) {
		super();
		this.idRespuesta = idRespuesta;
		this.respuesta = respuesta;
		this.explicacion = explicacion;
		this.nota = nota;
		this.idPregunta = idPregunta;
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
	public Integer getIdPregunta() {
		return idPregunta;
	}
	public void setIdPregunta(Integer idPregunta) {
		this.idPregunta = idPregunta;
	}
	
	
}
