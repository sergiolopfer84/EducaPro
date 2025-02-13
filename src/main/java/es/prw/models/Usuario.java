package es.prw.models;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "Usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "pass", nullable = false)
    private String pass;
    
    @Column(name = "nombre", nullable = false)
    private String nombre; 
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "Usuario_Rol",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<Rol> roles = new HashSet<>();

    // Getters y Setters
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; } 
    public void setNombre(String nombre) { this.nombre = nombre; } 

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }
}
