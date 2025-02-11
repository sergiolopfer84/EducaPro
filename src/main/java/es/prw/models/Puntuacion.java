package es.prw.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Puntuacion")
public class Puntuacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPuntuacion;

    @Column(nullable = false)
    private double notaConseguida;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_test", nullable = false)
    private Test test;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    public Puntuacion() {}

    public Puntuacion(Integer idPuntuacion, double notaConseguida, Usuario usuario, Test test, Date fecha) {
        this.idPuntuacion = idPuntuacion;
        this.notaConseguida = notaConseguida;
        this.usuario = usuario;
        this.test = test;
        this.fecha = fecha;
    }

    public Integer getIdPuntuacion() {
        return idPuntuacion;
    }

    public void setIdPuntuacion(Integer idPuntuacion) {
        this.idPuntuacion = idPuntuacion;
    }

    public double getNotaConseguida() {
        return notaConseguida;
    }

    public void setNotaConseguida(double notaConseguida) {
        this.notaConseguida = notaConseguida;
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
