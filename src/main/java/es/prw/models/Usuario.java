package es.prw.models;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class Usuario {
	private Integer idUsuario;
	private String nombre;
	private String pass;
	private String email;

	public Usuario() {
		super();

	}

	public Usuario(Integer idUsuario, String nombre, String pass, String email) {
		super();
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.pass = pass;
		this.email = email;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
