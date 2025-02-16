package es.prw.models;

import jakarta.persistence.*;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Puntuacion")
public class Puntuacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puntuacion")
    private Integer idPuntuacion;

    @Column(name = "nota_obtenida", nullable = false)
    private Double notaObtenida;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;  // ✅ Se almacena solo el ID en la base de datos, pero en el código sigue siendo un objeto Usuario


    @ManyToOne
    @JoinColumn(name = "id_test", nullable = false)
    private Test test;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    

    public Puntuacion() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Puntuacion(Integer idPuntuacion, Double notaObtenida, Usuario usuario, Test test, Date fecha) {
		super();
		this.idPuntuacion = idPuntuacion;
		this.notaObtenida = notaObtenida;
		this.usuario = usuario;
		this.test = test;
		this.fecha = fecha;
	}
	
	

	public Double getNotaObtenida() {
		return notaObtenida;
	}

	public void setNotaObtenida(Double notaObtenida) {
		this.notaObtenida = notaObtenida;
	}

	public Integer getIdPuntuacion() {
        return idPuntuacion;
    }

    public void setIdPuntuacion(Integer idPuntuacion) {
        this.idPuntuacion = idPuntuacion;
    }

   

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
