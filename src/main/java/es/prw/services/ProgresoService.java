package es.prw.services;

import es.prw.connection.MySqlConnection;
import es.prw.dtos.MateriaProgresoDTO;
import es.prw.dtos.NotaHistorialDTO;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProgresoService {
    
    public List<MateriaProgresoDTO> obtenerProgresoMaterias(int idUsuario) {
        List<MateriaProgresoDTO> progresoMaterias = new ArrayList<>();
        MySqlConnection connection = new MySqlConnection();

        try {
            connection.open();
            String sql = """
                     SELECT  m.nombre_materia, COUNT(DISTINCT t.id_test) AS total_tests_materia, 
            			COUNT(DISTINCT CASE WHEN p.nota_obtenida >= 5 THEN t.id_test END) AS tests_aprobados
            			FROM materia m LEFT JOIN test t ON m.id_materia = t.id_materia LEFT JOIN (
            			SELECT p1.id_test, p1.nota_obtenida FROM puntuacion p1 JOIN (
            				SELECT id_test, MAX(fecha) AS ultima_fecha FROM puntuacion
            				WHERE id_usuario = ? GROUP BY id_test) p2 ON p1.id_test = p2.id_test 
            				AND p1.fecha = p2.ultima_fecha WHERE p1.id_usuario = ? ) p 
            				ON t.id_test = p.id_test GROUP BY m.nombre_materia
                    """;
            
            ResultSet rs = connection.executeSelect(sql, idUsuario, idUsuario);
            
            
            while (rs.next()) {
                String materia = rs.getString("nombre_materia");  // <--- Corrección aquí
                int totalTests = rs.getInt("total_tests_materia");
                int aprobados = rs.getInt("tests_aprobados");


                progresoMaterias.add(new MateriaProgresoDTO(materia, totalTests, aprobados));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }

        return progresoMaterias;
    }

    public Map<String, List<Double>> obtenerProgresoTests(int idUsuario) {
        Map<String, List<Double>> historialNotas = new HashMap<>();
        MySqlConnection connection = new MySqlConnection();

        try {
            connection.open();
            String sql = """
                    SELECT t.nombre_test, p.nota_obtenida
                    FROM puntuacion p
                    JOIN test t ON p.id_test = t.id_test
                    WHERE p.id_usuario = ?
                    ORDER BY p.fecha ASC
                    """;

            ResultSet rs = connection.executeSelect(sql, idUsuario);
            while (rs.next()) {
                String testNombre = rs.getString("nombre_test");
                double nota = rs.getDouble("nota_obtenida");

                historialNotas.putIfAbsent(testNombre, new ArrayList<>());
                historialNotas.get(testNombre).add(nota);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }

        return historialNotas;
    }

}
