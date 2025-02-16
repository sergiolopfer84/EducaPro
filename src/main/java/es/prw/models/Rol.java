package es.prw.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre", unique = true, nullable = false)
    private String nombre;

    // Getters y Setters
    public Long getId() {
        return idRol;
    }

    public void setId(Long id) {
        this.idRol = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}