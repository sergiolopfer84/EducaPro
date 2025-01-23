package es.prw.models;

public class Pregunta {
	
	private Integer idPregunta;
	private String pregunta;
	private Integer idTest;
	public Pregunta() {
		super();
		
	}
	public Pregunta(Integer idPregunta, String pregunta, Integer idTest) {
		super();
		this.idPregunta = idPregunta;
		this.pregunta = pregunta;
		this.idTest = idTest;
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
