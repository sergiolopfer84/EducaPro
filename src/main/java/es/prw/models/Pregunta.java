package es.prw.models;

import java.util.List;

public class Pregunta {
	
	private Integer idPregunta;
	private String pregunta;
	private Integer idTest;
	 private List<Respuesta> respuestas;
	public Pregunta() {
		super();
		
	}
	public Pregunta(Integer idPregunta, String pregunta, Integer idTest, List<Respuesta> respuestas) {
		super();
		this.idPregunta = idPregunta;
		this.pregunta = pregunta;
		this.idTest = idTest;
		this.respuestas = respuestas;
	}
	public List<Respuesta> getRespuestas() {
		return respuestas;
	}
	public void setRespuestas(List<Respuesta> respuestas) {
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
	public Integer getIdTest() {
		return idTest;
	}
	public void setIdTest(Integer idTest) {
		this.idTest = idTest;
	} 
	
	
	
	
}
