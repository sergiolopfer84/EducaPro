package es.prw.dtos;

import es.prw.models.Materia;

public class MateriaDTO {
    private Integer idMateria;
    private String nombreMateria;
    private boolean activa;

    public MateriaDTO(Materia materia) {
        this.idMateria = materia.getIdMateria();
        this.nombreMateria = materia.getNombreMateria();
        this.activa = materia.isActiva();
    }

    public void setIdMateria(Integer idMateria) {
		this.idMateria = idMateria;
	}

	public void setNombreMateria(String nombreMateria) {
		this.nombreMateria = nombreMateria;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}

	public Integer getIdMateria() {
        return idMateria;
    }

    public String getNombreMateria() {
        return nombreMateria;
    }

    public boolean isActiva() {
        return activa;
    }
}
