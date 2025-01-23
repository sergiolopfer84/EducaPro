package es.prw.models;

public class Test {
	private Integer idTest; 
	private String test;
	private Integer idMateria;
	public Test() {
		super();
		
	}
	public Test(Integer idTest, String test, Integer idMateria) {
		super();
		this.idTest = idTest;
		this.test = test;
		this.idMateria = idMateria;
	}
	public Integer getIdTest() {
		return idTest;
	}
	public void setIdTest(Integer idTest) {
		this.idTest = idTest;
	}
	public String getTest() {
		return test;
	}
	public void setTest(String test) {
		this.test = test;
	}
	public Integer getIdMateria() {
		return idMateria;
	}
	public void setIdMateria(Integer idMateria) {
		this.idMateria = idMateria;
	}
	
	
	
	
}
