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
                    SELECT m.nombre_materia, COUNT(t.id_test) AS total_tests,
                           SUM(CASE WHEN p.nota_obtenida >= 5 THEN 1 ELSE 0 END) AS tests_aprobados
                    FROM test t
                    JOIN materia m ON t.id_materia = m.id_materia
                    LEFT JOIN puntuacion p ON t.id_test = p.id_test AND p.id_usuario = ?
                    GROUP BY m.nombre_materia
                    """;
            
            ResultSet rs = connection.executeSelect(sql, idUsuario);
            
            
            while (rs.next()) {
                String materia = rs.getString("nombre_materia");  // <--- Corrección aquí
                int totalTests = rs.getInt("total_tests");
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
                    ORDER BY p.fecha DESC
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
