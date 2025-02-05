package es.prw.dtos;

public class MateriaProgresoDTO {
	  private String materia;
	    private int totalTests;
	    private int testsAprobados;
	    private double porcentajeAprobados;

	    public MateriaProgresoDTO() {
	    }

	    public MateriaProgresoDTO(String materia, int totalTests, int testsAprobados) {
	        this.materia = materia;
	        this.totalTests = totalTests;
	        this.testsAprobados = testsAprobados;
	        this.porcentajeAprobados = (totalTests > 0) ? ((double) testsAprobados / totalTests) * 100 : 0;
	    }

	    public String getMateria() {
	        return materia;
	    }

	    public void setMateria(String materia) {
	        this.materia = materia;
	    }

	    public int getTotalTests() {
	        return totalTests;
	    }

	    public void setTotalTests(int totalTests) {
	        this.totalTests = totalTests;
	    }

	    public int getTestsAprobados() {
	        return testsAprobados;
	    }

	    public void setTestsAprobados(int testsAprobados) {
	        this.testsAprobados = testsAprobados;
	    }

	    public double getPorcentajeAprobados() {
	        return porcentajeAprobados;
	    }

	    public void setPorcentajeAprobados(double porcentajeAprobados) {
	        this.porcentajeAprobados = porcentajeAprobados;
	    }
}
