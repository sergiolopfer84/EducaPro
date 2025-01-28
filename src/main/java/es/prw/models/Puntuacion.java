package es.prw.models;

import java.util.Date;

public class Puntuacion {
	private Integer idPuntuacion;
	private String notaConseguida;
	private Integer idUsuario;
	private Integer idTest;
	private Date fecha;

	public Puntuacion() {
		super();

	}

	public Puntuacion(Integer idPuntuacion, String notaConseguida, Integer idUsuario, Integer idTest, Date fecha) {
		super();
		this.idPuntuacion = idPuntuacion;
		this.notaConseguida = notaConseguida;
		this.idUsuario = idUsuario;
		this.idTest = idTest;
		this.fecha = fecha;
	}

	public Integer getIdPuntuacion() {
		return idPuntuacion;
	}

	public void setIdPuntuacion(Integer idPuntuacion) {
		this.idPuntuacion = idPuntuacion;
	}

	public String getNotaConseguida() {
		return notaConseguida;
	}

	public void setNotaConseguida(String notaConseguida) {
		this.notaConseguida = notaConseguida;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getIdTest() {
		return idTest;
	}

	public void setIdTest(Integer idTest) {
		this.idTest = idTest;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

}
