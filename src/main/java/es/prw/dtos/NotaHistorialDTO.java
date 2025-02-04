package es.prw.dtos;

import java.util.List;

public class NotaHistorialDTO {
	 private String test;
	    private List<Double> notas;

	    public NotaHistorialDTO() {
	    }

	    public NotaHistorialDTO(String test, List<Double> notas) {
	        this.test = test;
	        this.notas = notas;
	    }

	    public String getTest() {
	        return test;
	    }

	    public void setTest(String test) {
	        this.test = test;
	    }

	    public List<Double> getNotas() {
	        return notas;
	    }

	    public void setNotas(List<Double> notas) {
	        this.notas = notas;
	    }
}
