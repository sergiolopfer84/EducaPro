package es.prw.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MateriaProgresoDTO {

    @NotNull(message = "El nombre de la materia no puede ser nulo.")
    private String materia;

    @Min(value = 0, message = "El total de tests no puede ser negativo.")
    private int totalTests;

    @Min(value = 0, message = "El número de tests aprobados no puede ser negativo.")
    private int testsAprobados;

    private double porcentajeAprobados;

    public MateriaProgresoDTO() {
    }

    public MateriaProgresoDTO(String materia, int totalTests, int testsAprobados) {
        this.materia = materia;
        this.totalTests = Math.max(0, totalTests); // Evita valores negativos
        this.testsAprobados = Math.max(0, testsAprobados); // Evita valores negativos
        calcularPorcentajeAprobados();
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
        this.totalTests = Math.max(0, totalTests);
        calcularPorcentajeAprobados();
    }

    public int getTestsAprobados() {
        return testsAprobados;
    }

    public void setTestsAprobados(int testsAprobados) {
        this.testsAprobados = Math.max(0, testsAprobados);
        calcularPorcentajeAprobados();
    }

    public double getPorcentajeAprobados() {
        return porcentajeAprobados;
    }

    private void calcularPorcentajeAprobados() {
        this.porcentajeAprobados = (totalTests > 0) ? ((double) testsAprobados / totalTests) * 100 : 0;
    }
}
