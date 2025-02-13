package es.prw.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import es.prw.models.Puntuacion;
import es.prw.models.Test;
import es.prw.models.Usuario;

@Repository
public interface PuntuacionRepository extends JpaRepository<Puntuacion, Integer> {

    @Transactional(readOnly = true)
    @Query("SELECT COUNT(p) FROM Puntuacion p WHERE p.test.materia.idMateria = :idMateria AND p.notaObtenida >= 5")
    int countAprobadosByMateria(Integer idMateria);

    @Transactional(readOnly = true)
    @Query("SELECT p.notaObtenida FROM Puntuacion p WHERE p.test = :test")
    List<Double> findNotasByTest(Test test);

    @Transactional(readOnly = true)
    List<Puntuacion> findByUsuario(Usuario usuario);

    @Transactional(readOnly = true)
    List<Puntuacion> findByUsuarioAndTest(Usuario usuario, Test test);

    @Transactional(readOnly = true)
    @Query("SELECT p FROM Puntuacion p WHERE p.usuario.idUsuario = :idUsuario ORDER BY p.fecha DESC")
    List<Puntuacion> findPuntuacionesByUsuario(Integer idUsuario);

    @Transactional(readOnly = true)
    @Query("SELECT p.test.materia.nombreMateria, p.test.nombreTest, p.notaObtenida "
         + "FROM Puntuacion p WHERE p.usuario.idUsuario = :idUsuario "
         + "ORDER BY p.test.materia.nombreMateria, p.test.nombreTest, p.fecha DESC")
    List<Object[]> obtenerHistorialNotasPorUsuario(Integer idUsuario);

    @Transactional(readOnly = true)
    @Query("SELECT p.notaObtenida FROM Puntuacion p WHERE p.usuario.idUsuario = :idUsuario AND p.test.idTest = :idTest ORDER BY p.fecha DESC")
    List<Double> findUltimasPuntuacionesByUsuarioAndTest(Integer idUsuario, Integer idTest, Pageable pageable);
}
