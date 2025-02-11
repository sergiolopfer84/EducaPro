package es.prw.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import es.prw.models.Puntuacion;
import es.prw.models.Test;
import es.prw.models.Usuario;

@Repository
public interface PuntuacionRepository extends JpaRepository<Puntuacion, Integer> {

	@Query("SELECT COUNT(p) FROM Puntuacion p WHERE p.test.materia.idMateria = :idMateria AND p.notaConseguida >= 5")
	int countAprobadosByMateria(Integer idMateria);

	@Query("SELECT p.notaConseguida FROM Puntuacion p WHERE p.test = :test")
	List<Double> findNotasByTest(Test test);

	List<Puntuacion> findByUsuario(Usuario usuario);

	List<Puntuacion> findByUsuarioAndTest(Usuario usuario, Test test);

	@Query("SELECT p FROM Puntuacion p WHERE p.usuario.idUsuario = :idUsuario ORDER BY p.fecha DESC")
	List<Puntuacion> findPuntuacionesByUsuario(Integer idUsuario);

	@Query("SELECT p.test.materia.materia, p.test.test, p.notaConseguida "
			+ "FROM Puntuacion p WHERE p.usuario.idUsuario = :idUsuario "
			+ "ORDER BY p.test.materia.materia, p.test.test, p.fecha DESC")
	List<Object[]> obtenerHistorialNotasPorUsuario(Integer idUsuario);

	@Query("SELECT p.notaConseguida FROM Puntuacion p WHERE p.usuario.idUsuario = :idUsuario AND p.test.idTest = :idTest ORDER BY p.fecha DESC LIMIT 2")
	List<Double> findUltimasPuntuacionesByUsuarioAndTest(Integer idUsuario, Integer idTest);

}
