package es.prw.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Repository;

import es.prw.connection.MySqlConnection;
import es.prw.models.Puntuacion;

@Repository
public class PuntuacionDao {

    private MySqlConnection objMySqlConnection;

    public PuntuacionDao() {
        objMySqlConnection = new MySqlConnection(false);
    }

    public Optional<Puntuacion> savePuntuacion(Puntuacion puntuacion, HttpSession session) throws SQLException {
        String sqlInsert = "INSERT INTO puntuacion (id_usuario, id_test, nota_obtenida, fecha) VALUES (?, ?, ?, ?)";

        objMySqlConnection.open();
        if (!objMySqlConnection.isError()) {
            try {
                // Insertar la nueva puntuación
                objMySqlConnection.executeInsert(sqlInsert, 
                    puntuacion.getIdUsuario(), 
                    puntuacion.getIdTest(), 
                    puntuacion.getNotaConseguida(), 
                    new Timestamp(puntuacion.getFecha().getTime())
                );

                // ✅ Actualizar las puntuaciones en la sesión después de la inserción
                List<Puntuacion> puntuaciones = getPuntuacionesByUsuario(puntuacion.getIdUsuario(), session);
                puntuaciones.add(0, puntuacion); // Insertar la nueva al inicio
                session.setAttribute("puntuaciones_usuario_" + puntuacion.getIdUsuario(), puntuaciones);

                objMySqlConnection.commit();
                return Optional.of(puntuacion);
            } finally {
                objMySqlConnection.close();
            }
        }
        return Optional.empty();
    }

    public List<Puntuacion> getPuntuacionesPorMateria(int idUsuario, int idMateria, HttpSession session) {
        String sessionKey = "puntuaciones_usuario_materia_" + idUsuario + "_" + idMateria;

        // Intentar recuperar desde la sesión para mejorar rendimiento
        List<Puntuacion> puntuaciones = (List<Puntuacion>) session.getAttribute(sessionKey);
        if (puntuaciones != null) {
            return puntuaciones;
        }

        // Si no está en sesión, consultar en BD
        puntuaciones = new ArrayList<>();
        String sql = """
            SELECT p.* FROM puntuacion p
            JOIN test t ON p.id_test = t.id_test
            WHERE p.id_usuario = ? AND t.id_materia = ?
            ORDER BY p.fecha DESC
        """;

        objMySqlConnection.open();
        if (!objMySqlConnection.isError()) {
            try {
                ResultSet result = objMySqlConnection.executeSelect(sql, idUsuario, idMateria);
                while (result != null && result.next()) {
                    Puntuacion puntuacion = new Puntuacion();
                    puntuacion.setIdPuntuacion(result.getInt("id_puntuacion"));
                    puntuacion.setIdUsuario(result.getInt("id_usuario"));
                    puntuacion.setIdTest(result.getInt("id_test"));
                    puntuacion.setNotaConseguida(result.getDouble("nota_obtenida"));
                    puntuacion.setFecha(result.getTimestamp("fecha"));
                    puntuaciones.add(puntuacion);
                }
                session.setAttribute(sessionKey, puntuaciones);
                result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                objMySqlConnection.close();
            }
        }
        return puntuaciones;
    }



    // Método para obtener la última puntuación con uso de sesión
    public List<Double> getUltimasPuntuacionesByTest(int idUsuario, int idTest, HttpSession session) {
        List<Puntuacion> puntuaciones = getPuntuacionesByUsuario(idUsuario, session);

        // Filtrar las puntuaciones del test específico
        List<Double> notas = puntuaciones.stream()
                .filter(p -> p.getIdTest() == idTest)
                .map(Puntuacion::getNotaConseguida)
                .limit(2) // Solo las dos más recientes
                .toList();

        return notas;
    }



    // Método para obtener todas las puntuaciones de un usuario con cacheo en sesión
    public List<Puntuacion> getPuntuacionesByUsuario(int idUsuario, HttpSession session) {
        String sessionKey = "puntuaciones_usuario_" + idUsuario;

        // Intentar recuperar desde la sesión
        List<Puntuacion> puntuaciones = (List<Puntuacion>) session.getAttribute(sessionKey);
        if (puntuaciones != null) {
            return puntuaciones;
        }

        // Si no están en sesión, consultar en la BD
        puntuaciones = new ArrayList<>();
        String sql = "SELECT * FROM puntuacion WHERE id_usuario = ? ORDER BY fecha DESC";
        objMySqlConnection.open();
        if (!objMySqlConnection.isError()) {
            try {
                ResultSet result = objMySqlConnection.executeSelect(sql, idUsuario);
                while (result != null && result.next()) {
                    Puntuacion puntuacion = new Puntuacion();
                    puntuacion.setIdPuntuacion(result.getInt("id_puntuacion"));
                    puntuacion.setIdUsuario(result.getInt("id_usuario"));
                    puntuacion.setIdTest(result.getInt("id_test"));
                    puntuacion.setNotaConseguida(result.getDouble("nota_obtenida"));
                    puntuacion.setFecha(result.getTimestamp("fecha"));
                    puntuaciones.add(puntuacion);
                }

                // ✅ Guardar en sesión para evitar consultas repetidas
                session.setAttribute(sessionKey, puntuaciones);

                result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                objMySqlConnection.close();
            }
        }
        return puntuaciones;
    }

}
