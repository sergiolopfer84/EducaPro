package es.prw.daos;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import es.prw.connection.MySqlConnection;
import es.prw.models.Puntuacion;

@Repository
public class PuntuacionDao {

    private MySqlConnection objMySqlConnection;

    public PuntuacionDao() {
        objMySqlConnection = new MySqlConnection(false);
    }

    // Método para guardar una puntuación en la base de datos
    public void savePuntuacion(Puntuacion puntuacion) throws SQLException {
        String sql = "INSERT INTO puntuacion (id_usuario, id_test, nota_obtenida, fecha) VALUES (?, ?, ?, ?)";
        System.out.println(" Entramos en save sql: "+ sql);
        objMySqlConnection.open();
        if (!objMySqlConnection.isError()) {
            try {
                // Ejecutar la inserción y obtener el ResultSet con la clave generada
            	
                ResultSet rs = objMySqlConnection.executeInsert(sql, 
                    puntuacion.getIdUsuario(), 
                    puntuacion.getIdTest(), 
                    puntuacion.getNotaConseguida(), 
                    new Timestamp(puntuacion.getFecha().getTime())
                );

                if (rs != null && rs.next()) {
                    int idGenerado = rs.getInt(1); // Extraer el ID generado
                    puntuacion.setIdPuntuacion(idGenerado);
                    objMySqlConnection.commit(); // Confirmar la transacción solo si fue exitosa
                } else {
                    throw new SQLException("No se generó un ID para la puntuación.");
                }
            } catch (SQLException e) {
                objMySqlConnection.rollback();
                throw new SQLException("Error al guardar la puntuación: " + e.getMessage());
            } finally {
                objMySqlConnection.close();
            }
        } else {
            throw new SQLException("Error al abrir la conexión: " + objMySqlConnection.msgError());
        }
    }


    // Método para obtener puntuaciones de un usuario
    public List<Puntuacion> getPuntuacionesByUsuario(int idUsuario) {
    	System.out.println(idUsuario + "  idusuario");
        List<Puntuacion> puntuaciones = new ArrayList<>();
        String sql = "SELECT * FROM puntuaciones WHERE id_usuario = ?";

        objMySqlConnection.open();
        if (!objMySqlConnection.isError()) {
            ResultSet result = objMySqlConnection.executeSelect(sql, idUsuario);
            try {
                while (result != null && result.next()) {
                    Puntuacion puntuacion = new Puntuacion();
                    puntuacion.setIdPuntuacion(result.getInt("id_puntuacion"));
                    puntuacion.setIdUsuario(result.getInt("id_usuario"));
                    puntuacion.setIdTest(result.getInt("id_test"));
                    puntuacion.setNotaConseguida(result.getDouble("nota_conseguida"));
                    puntuacion.setFecha(result.getTimestamp("fecha"));
                    puntuaciones.add(puntuacion);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                objMySqlConnection.close();
            }
        }
        return puntuaciones;
    }

    // Método para obtener la última puntuación de un usuario en un test específico
 // Método para obtener la última puntuación de un usuario en un test específico
    public Optional<Puntuacion> getUltimaPuntuacion(int idUsuario, int idTest) {
    	
        String sql = "SELECT * FROM puntuacion WHERE id_usuario = ? AND id_test = ? ORDER BY fecha DESC LIMIT 1";
        
        System.out.println(" sql "+ sql);
        Puntuacion puntuacion = null;

        objMySqlConnection.open();
        if (!objMySqlConnection.isError()) {
            ResultSet result = objMySqlConnection.executeSelect(sql, idUsuario, idTest);
            try {
                if (result != null && result.next()) {
                    puntuacion = new Puntuacion();
                    puntuacion.setIdPuntuacion(result.getInt("id_puntuacion"));
                    puntuacion.setIdUsuario(result.getInt("id_usuario"));
                    puntuacion.setIdTest(result.getInt("id_test"));
                    puntuacion.setNotaConseguida(result.getDouble("nota_obtenida"));
                    puntuacion.setFecha(result.getTimestamp("fecha"));

                    System.out.println("Última puntuación encontrada: " + puntuacion.getNotaConseguida());
                } else {
                    System.out.println("No se encontró ninguna puntuación previa para este test y usuario.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                objMySqlConnection.close();
            }
        }
        return Optional.ofNullable(puntuacion);
    }

}
