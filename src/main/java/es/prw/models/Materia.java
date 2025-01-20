package es.prw.models;

public class Materia {
	private Integer idMateria; 
	private String materia;
	public Materia() {
		super();
		
	}
	public Materia(Integer idMateria, String materia) {
		super();
		this.idMateria = idMateria;
		this.materia = materia;
	}
	public Integer getIdMateria() {
		return idMateria;
	}
	public void setIdMateria(Integer idMateria) {
		this.idMateria = idMateria;
	}
	public String getMateria() {
		return materia;
	}
	public void setMateria(String materia) {
		this.materia = materia;
	} 
	
	
	
	
}
