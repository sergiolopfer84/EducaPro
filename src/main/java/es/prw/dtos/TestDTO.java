package es.prw.dtos;

public class TestDTO {
    private Integer idTest;
    private String nombreTest;
    private Integer idMateria; // 🔹 Solo ID de materia
    private boolean activa;

    // Getters y setters
    public Integer getIdTest() { return idTest; }
    public void setIdTest(Integer idTest) { this.idTest = idTest; }

    public String getNombreTest() { return nombreTest; }
    public void setNombreTest(String nombreTest) { this.nombreTest = nombreTest; }

    public Integer getIdMateria() { return idMateria; }
    public void setIdMateria(Integer idMateria) { this.idMateria = idMateria; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
