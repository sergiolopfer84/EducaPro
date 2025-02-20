package es.prw.dtos;

import es.prw.models.Pregunta;

public class PreguntaDTO {
    private Integer idPregunta;
    private String textoPregunta;
    private Integer idTest;
    

    public PreguntaDTO(Pregunta pregunta) {
        this.idPregunta = pregunta.getIdPregunta();
        this.textoPregunta = pregunta.getTextoPregunta();
        if (pregunta.getTest() != null) {
            this.idTest = pregunta.getTest().getIdTest();
        }
    }


    public PreguntaDTO() {
    	
    }


	public Integer getIdPregunta() {
		return idPregunta;
	}


	public void setIdPregunta(Integer idPregunta) {
		this.idPregunta = idPregunta;
	}


	public String getTextoPregunta() {
		return textoPregunta;
	}


	public void setTextoPregunta(String textoPregunta) {
		this.textoPregunta = textoPregunta;
	}


	public Integer getIdTest() {
		return idTest;
	}


	public void setIdTest(Integer idTest) {
		this.idTest = idTest;
	}

    
}

