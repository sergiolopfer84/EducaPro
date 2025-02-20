package es.prw.dtos;

import es.prw.models.Test;

public class TestDTO {
    private Integer idTest;
    private String nombreTest;
    private boolean activa;
    private Integer idMateria;
    private String nombreMateria;

    public TestDTO(Test test) {
        this.idTest = test.getIdTest();
        this.nombreTest = test.getNombreTest();
        this.activa = test.isActiva();
        this.idMateria = test.getMateria().getIdMateria();
        this.nombreMateria = test.getMateria().getNombreMateria();
    }
    public TestDTO() {
    }

    public void setIdTest(Integer idTest) {
		this.idTest = idTest;
	}

	public void setNombreTest(String nombreTest) {
		this.nombreTest = nombreTest;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}

	public void setIdMateria(Integer idMateria) {
		this.idMateria = idMateria;
	}

	public void setNombreMateria(String nombreMateria) {
		this.nombreMateria = nombreMateria;
	}

	public Integer getIdTest() {
        return idTest;
    }

    public String getNombreTest() {
        return nombreTest;
    }

    public boolean isActiva() {
        return activa;
    }

    public Integer getIdMateria() {
        return idMateria;
    }

    public String getNombreMateria() {
        return nombreMateria;
    }
}
