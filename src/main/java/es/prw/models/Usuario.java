package es.prw.models;

public class Usuario {
	private Integer idUsuario; 
	private String nombre; 
	private String password; 	
	private String email;
	public Usuario() {
		super();
		
	}
	public Usuario(Integer idUsuario, String nombre, String password, String email) {
		super();
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.password = password;
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	
	
	
	
}
